/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFluentFuture;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.EffectiveModelContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangModelDependencyInfo;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SharedSchemaContextFactory implements EffectiveModelContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SharedSchemaContextFactory.class);

    private final Cache<Collection<SourceIdentifier>, EffectiveModelContext> revisionCache = CacheBuilder.newBuilder()
            .weakValues().build();
    private final Cache<Collection<SourceIdentifier>, EffectiveModelContext> semVerCache = CacheBuilder.newBuilder()
            .weakValues().build();
    private final @NonNull SharedSchemaRepository repository;
    private final @NonNull SchemaContextFactoryConfiguration config;

    SharedSchemaContextFactory(final @NonNull SharedSchemaRepository repository,
            final @NonNull SchemaContextFactoryConfiguration config) {
        this.repository = requireNonNull(repository);
        this.config = requireNonNull(config);
    }

    @Override
    public @NonNull ListenableFuture<EffectiveModelContext> createEffectiveModelContext(
            final @NonNull Collection<SourceIdentifier> requiredSources) {
        return createSchemaContext(requiredSources,
                config.getStatementParserMode() == StatementParserMode.SEMVER_MODE ? semVerCache : revisionCache,
                new AssembleSources(repository.factory(), config));
    }

    private @NonNull ListenableFuture<EffectiveModelContext> createSchemaContext(
            final Collection<SourceIdentifier> requiredSources,
            final Cache<Collection<SourceIdentifier>, EffectiveModelContext> cache,
            final AsyncFunction<List<IRSchemaSource>, EffectiveModelContext> assembleSources) {
        // Make sources unique
        final List<SourceIdentifier> uniqueSourceIdentifiers = deDuplicateSources(requiredSources);

        final EffectiveModelContext existing = cache.getIfPresent(uniqueSourceIdentifiers);
        if (existing != null) {
            LOG.debug("Returning cached context {}", existing);
            return immediateFluentFuture(existing);
        }

        // Request all sources be loaded
        ListenableFuture<List<IRSchemaSource>> sf = Futures.allAsList(Collections2.transform(uniqueSourceIdentifiers,
            this::requestSource));

        // Detect mismatch between requested Source IDs and IDs that are extracted from parsed source
        // Also remove duplicates if present
        // We are relying on preserved order of uniqueSourceIdentifiers as well as sf
        sf = Futures.transform(sf, new SourceIdMismatchDetector(uniqueSourceIdentifiers),
            MoreExecutors.directExecutor());

        // Assemble sources into a schema context
        final ListenableFuture<EffectiveModelContext> cf = Futures.transformAsync(sf, assembleSources,
            MoreExecutors.directExecutor());

        final SettableFuture<EffectiveModelContext> rf = SettableFuture.create();
        Futures.addCallback(cf, new FutureCallback<EffectiveModelContext>() {
            @Override
            public void onSuccess(final EffectiveModelContext result) {
                // Deduplicate concurrent loads
                final EffectiveModelContext existing;
                try {
                    existing = cache.get(uniqueSourceIdentifiers, () -> result);
                } catch (ExecutionException e) {
                    LOG.warn("Failed to recheck result with cache, will use computed value", e);
                    rf.set(result);
                    return;
                }

                rf.set(existing);
            }

            @Override
            public void onFailure(final Throwable cause) {
                LOG.debug("Failed to assemble sources", cause);
                rf.setException(cause);
            }
        }, MoreExecutors.directExecutor());

        return rf;
    }

    private ListenableFuture<IRSchemaSource> requestSource(final @NonNull SourceIdentifier identifier) {
        return repository.getSchemaSource(identifier, IRSchemaSource.class);
    }

    /**
     * Return a set of de-duplicated inputs.
     *
     * @return set (preserving ordering) from the input collection
     */
    private static List<SourceIdentifier> deDuplicateSources(final Collection<SourceIdentifier> requiredSources) {
        final Set<SourceIdentifier> uniqueSourceIdentifiers = new LinkedHashSet<>(requiredSources);
        if (uniqueSourceIdentifiers.size() == requiredSources.size()) {
            // Can potentially reuse input
            return ImmutableList.copyOf(requiredSources);
        }

        LOG.warn("Duplicate sources requested for schema context, removed duplicate sources: {}",
            Collections2.filter(uniqueSourceIdentifiers, input -> Iterables.frequency(requiredSources, input) > 1));
        return ImmutableList.copyOf(uniqueSourceIdentifiers);
    }

    @SuppressModernizer
    private static final class SourceIdMismatchDetector implements Function<List<IRSchemaSource>,
            List<IRSchemaSource>> {
        private final List<SourceIdentifier> sourceIdentifiers;

        SourceIdMismatchDetector(final List<SourceIdentifier> sourceIdentifiers) {
            this.sourceIdentifiers = requireNonNull(sourceIdentifiers);
        }

        @Override
        public List<IRSchemaSource> apply(final List<IRSchemaSource> input) {
            final Map<SourceIdentifier, IRSchemaSource> filtered = new LinkedHashMap<>();

            for (int i = 0; i < input.size(); i++) {

                final SourceIdentifier expectedSId = sourceIdentifiers.get(i);
                final IRSchemaSource irSchemaSource = input.get(i);
                final SourceIdentifier realSId = irSchemaSource.getIdentifier();

                if (!expectedSId.equals(realSId)) {
                    LOG.warn("Source identifier mismatch for module \"{}\", requested as {} but actually is {}. "
                        + "Using actual id", expectedSId.getName(), expectedSId, realSId);
                }

                if (filtered.containsKey(realSId)) {
                    LOG.warn("Duplicate source for module {} detected in reactor", realSId);
                }

                filtered.put(realSId, irSchemaSource);

            }
            return ImmutableList.copyOf(filtered.values());
        }
    }

    private static final class AssembleSources implements AsyncFunction<List<IRSchemaSource>, EffectiveModelContext> {
        private final @NonNull YangParserFactory parserFactory;
        private final @NonNull SchemaContextFactoryConfiguration config;
        private final @NonNull Function<IRSchemaSource, SourceIdentifier> getIdentifier;

        private AssembleSources(final @NonNull YangParserFactory parserFactory,
                final @NonNull SchemaContextFactoryConfiguration config) {
            this.parserFactory = parserFactory;
            this.config = config;
            switch (config.getStatementParserMode()) {
                case SEMVER_MODE:
                    this.getIdentifier = AssembleSources::getSemVerIdentifier;
                    break;
                default:
                    this.getIdentifier = IRSchemaSource::getIdentifier;
            }
        }

        @Override
        public FluentFuture<EffectiveModelContext> apply(final List<IRSchemaSource> sources)
                throws SchemaResolutionException, ReactorException {
            final Map<SourceIdentifier, IRSchemaSource> srcs = Maps.uniqueIndex(sources, getIdentifier);
            final Map<SourceIdentifier, YangModelDependencyInfo> deps =
                    Maps.transformValues(srcs, YangModelDependencyInfo::forIR);

            LOG.debug("Resolving dependency reactor {}", deps);

            final StatementParserMode statementParserMode = config.getStatementParserMode();
            final DependencyResolver res = statementParserMode == StatementParserMode.SEMVER_MODE
                    ? SemVerDependencyResolver.create(deps) : RevisionDependencyResolver.create(deps);
            if (!res.getUnresolvedSources().isEmpty()) {
                LOG.debug("Omitting models {} due to unsatisfied imports {}", res.getUnresolvedSources(),
                    res.getUnsatisfiedImports());
                throw new SchemaResolutionException("Failed to resolve required models",
                        res.getResolvedSources(), res.getUnsatisfiedImports());
            }

            final YangParser parser = parserFactory.createParser(statementParserMode);
            config.getSupportedFeatures().ifPresent(parser::setSupportedFeatures);
            config.getModulesDeviatedByModules().ifPresent(parser::setModulesWithSupportedDeviations);

            for (final Entry<SourceIdentifier, IRSchemaSource> entry : srcs.entrySet()) {
                try {
                    parser.addSource(entry.getValue());
                } catch (YangSyntaxErrorException | IOException e) {
                    throw new SchemaResolutionException("Failed to add source " + entry.getKey(), e);
                }
            }

            final EffectiveModelContext schemaContext;
            try {
                schemaContext = parser.buildEffectiveModel();
            } catch (final YangParserException e) {
                throw new SchemaResolutionException("Failed to resolve required models", e);
            }

            return immediateFluentFuture(schemaContext);
        }

        private static SemVerSourceIdentifier getSemVerIdentifier(final IRSchemaSource source) {
            final SourceIdentifier identifier = source.getIdentifier();
            final SemVer semver = YangModelDependencyInfo.findSemanticVersion(source.getRootStatement(), identifier);
            if (identifier instanceof SemVerSourceIdentifier && semver == null) {
                return (SemVerSourceIdentifier) identifier;
            }

            return SemVerSourceIdentifier.create(identifier.getName(), identifier.getRevision(), semver);
        }
    }
}
