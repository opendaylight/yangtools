/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static com.google.common.base.Preconditions.checkArgument;
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.impl.DefaultReactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangModelDependencyInfo;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SharedSchemaContextFactory implements SchemaContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SharedSchemaContextFactory.class);

    private final Cache<Collection<SourceIdentifier>, SchemaContext> revisionCache = CacheBuilder.newBuilder()
            .weakValues().build();
    private final Cache<Collection<SourceIdentifier>, SchemaContext> semVerCache = CacheBuilder.newBuilder()
            .weakValues().build();
    private final @NonNull SchemaRepository repository;
    private final @NonNull SchemaContextFactoryConfiguration config;

    SharedSchemaContextFactory(final @NonNull SchemaRepository repository,
        final @NonNull SchemaContextFactoryConfiguration config) {
        this.repository = requireNonNull(repository);
        this.config = requireNonNull(config);
    }

    @Override
    @Deprecated
    public @NonNull ListenableFuture<SchemaContext> createSchemaContext(
            final Collection<SourceIdentifier> requiredSources,
            final StatementParserMode statementParserMode, final Set<QName> supportedFeatures) {
        return createSchemaContext(requiredSources,
                statementParserMode == StatementParserMode.SEMVER_MODE ? semVerCache : revisionCache,
                new AssembleSources(SchemaContextFactoryConfiguration.builder()
                        .setFilter(config.getSchemaSourceFilter()).setStatementParserMode(statementParserMode)
                        .setSupportedFeatures(supportedFeatures).build()));
    }

    @Override
    public @NonNull ListenableFuture<SchemaContext> createSchemaContext(
            final @NonNull Collection<SourceIdentifier> requiredSources) {
        return createSchemaContext(requiredSources,
                config.getStatementParserMode() == StatementParserMode.SEMVER_MODE ? semVerCache : revisionCache,
                new AssembleSources(config));
    }

    private @NonNull ListenableFuture<SchemaContext> createSchemaContext(
            final Collection<SourceIdentifier> requiredSources,
            final Cache<Collection<SourceIdentifier>, SchemaContext> cache,
            final AsyncFunction<List<ASTSchemaSource>, SchemaContext> assembleSources) {
        // Make sources unique
        final List<SourceIdentifier> uniqueSourceIdentifiers = deDuplicateSources(requiredSources);

        final SchemaContext existing = cache.getIfPresent(uniqueSourceIdentifiers);
        if (existing != null) {
            LOG.debug("Returning cached context {}", existing);
            return immediateFluentFuture(existing);
        }

        // Request all sources be loaded
        ListenableFuture<List<ASTSchemaSource>> sf = Futures.allAsList(Collections2.transform(uniqueSourceIdentifiers,
            this::requestSource));

        // Detect mismatch between requested Source IDs and IDs that are extracted from parsed source
        // Also remove duplicates if present
        // We are relying on preserved order of uniqueSourceIdentifiers as well as sf
        sf = Futures.transform(sf, new SourceIdMismatchDetector(uniqueSourceIdentifiers),
            MoreExecutors.directExecutor());

        // Assemble sources into a schema context
        final ListenableFuture<SchemaContext> cf = Futures.transformAsync(sf, assembleSources,
            MoreExecutors.directExecutor());

        // Populate cache when successful
        Futures.addCallback(cf, new FutureCallback<SchemaContext>() {
            @Override
            public void onSuccess(final SchemaContext result) {
                cache.put(uniqueSourceIdentifiers, result);
            }

            @Override
            public void onFailure(final Throwable cause) {
                LOG.debug("Failed to assemble sources", cause);
            }
        }, MoreExecutors.directExecutor());

        return cf;
    }

    private ListenableFuture<ASTSchemaSource> requestSource(final @NonNull SourceIdentifier identifier) {
        return repository.getSchemaSource(identifier, ASTSchemaSource.class);
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

    private static final class SourceIdMismatchDetector implements Function<List<ASTSchemaSource>,
            List<ASTSchemaSource>> {
        private final List<SourceIdentifier> sourceIdentifiers;

        SourceIdMismatchDetector(final List<SourceIdentifier> sourceIdentifiers) {
            this.sourceIdentifiers = requireNonNull(sourceIdentifiers);
        }

        @Override
        public List<ASTSchemaSource> apply(final List<ASTSchemaSource> input) {
            final Map<SourceIdentifier, ASTSchemaSource> filtered = new LinkedHashMap<>();

            for (int i = 0; i < input.size(); i++) {

                final SourceIdentifier expectedSId = sourceIdentifiers.get(i);
                final ASTSchemaSource astSchemaSource = input.get(i);
                final SourceIdentifier realSId = astSchemaSource.getIdentifier();

                if (!expectedSId.equals(realSId)) {
                    LOG.warn("Source identifier mismatch for module \"{}\", requested as {} but actually is {}. "
                        + "Using actual id", expectedSId.getName(), expectedSId, realSId);
                }

                if (filtered.containsKey(realSId)) {
                    LOG.warn("Duplicate source for module {} detected in reactor", realSId);
                }

                filtered.put(realSId, astSchemaSource);

            }
            return ImmutableList.copyOf(filtered.values());
        }
    }

    private static final class AssembleSources implements AsyncFunction<List<ASTSchemaSource>, SchemaContext> {
        private final @NonNull SchemaContextFactoryConfiguration config;
        private final @NonNull Function<ASTSchemaSource, SourceIdentifier> getIdentifier;

        private AssembleSources(final @NonNull SchemaContextFactoryConfiguration config) {
            this.config = config;
            switch (config.getStatementParserMode()) {
                case SEMVER_MODE:
                    this.getIdentifier = ASTSchemaSource::getSemVerIdentifier;
                    break;
                default:
                    this.getIdentifier = ASTSchemaSource::getIdentifier;
            }
        }

        @Override
        public FluentFuture<SchemaContext> apply(final List<ASTSchemaSource> sources)
                throws SchemaResolutionException, ReactorException {
            final Map<SourceIdentifier, ASTSchemaSource> srcs = Maps.uniqueIndex(sources, getIdentifier);
            final Map<SourceIdentifier, YangModelDependencyInfo> deps =
                    Maps.transformValues(srcs, ASTSchemaSource::getDependencyInformation);

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

            final BuildAction reactor = DefaultReactors.defaultReactor().newBuild(statementParserMode);
            config.getSupportedFeatures().ifPresent(reactor::setSupportedFeatures);
            config.getModulesDeviatedByModules().ifPresent(reactor::setModulesWithSupportedDeviations);

            for (final Entry<SourceIdentifier, ASTSchemaSource> e : srcs.entrySet()) {
                final ASTSchemaSource ast = e.getValue();
                final ParserRuleContext parserRuleCtx = ast.getAST();
                checkArgument(parserRuleCtx instanceof StatementContext, "Unsupported context class %s for source %s",
                    parserRuleCtx.getClass(), e.getKey());

                reactor.addSource(YangStatementStreamSource.create(e.getKey(), (StatementContext) parserRuleCtx,
                    ast.getSymbolicName().orElse(null)));
            }

            final SchemaContext schemaContext;
            try {
                schemaContext = reactor.buildEffective();
            } catch (final ReactorException ex) {
                throw new SchemaResolutionException("Failed to resolve required models", ex.getSourceIdentifier(), ex);
            }

            return immediateFluentFuture(schemaContext);
        }
    }
}
