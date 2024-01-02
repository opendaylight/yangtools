/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Verify;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.FluentFuture;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.ir.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.repo.spi.GuavaSchemaSourceCache;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource.Costs;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class YangTextSchemaContextResolver implements AutoCloseable, SchemaSourceProvider<YangTextSource> {
    private static final Logger LOG = LoggerFactory.getLogger(YangTextSchemaContextResolver.class);
    private static final Duration SOURCE_LIFETIME = Duration.ofSeconds(60);

    private final Collection<SourceIdentifier> requiredSources = new ConcurrentLinkedDeque<>();
    private final Multimap<SourceIdentifier, YangTextSource> texts = ArrayListMultimap.create();
    @GuardedBy("this")
    private final Map<QNameModule, List<ImmutableSet<String>>> registeredFeatures = new HashMap<>();
    private final AtomicReference<Optional<EffectiveModelContext>> currentSchemaContext =
            new AtomicReference<>(Optional.empty());
    private final GuavaSchemaSourceCache<YangIRSchemaSource> cache;
    private final SchemaSourceRegistry registry;
    private final SchemaRepository repository;
    private final Registration transReg;

    private volatile Object version = new Object();
    private volatile Object contextVersion = version;
    @GuardedBy("this")
    private FeatureSet supportedFeatures = null;

    private YangTextSchemaContextResolver(final SchemaRepository repository, final SchemaSourceRegistry registry) {
        this.repository = requireNonNull(repository);
        this.registry = requireNonNull(registry);

        transReg = registry.registerSchemaSourceListener(TextToIRTransformer.create(repository, registry));
        cache = GuavaSchemaSourceCache.createSoftCache(registry, YangIRSchemaSource.class, SOURCE_LIFETIME);
    }

    public static @NonNull YangTextSchemaContextResolver create(final String name) {
        final var sharedRepo = new SharedSchemaRepository(name);
        return new YangTextSchemaContextResolver(sharedRepo, sharedRepo);
    }

    public static @NonNull YangTextSchemaContextResolver create(final String name, final YangParserFactory factory) {
        final var sharedRepo = new SharedSchemaRepository(name, factory);
        return new YangTextSchemaContextResolver(sharedRepo, sharedRepo);
    }

    /**
     * Register a {@link YangTextSource}.
     *
     * @param source YANG text source
     * @return a {@link Registration}
     * @throws YangSyntaxErrorException When the YANG file is syntactically invalid
     * @throws IOException when the URL is not readable
     * @throws SchemaSourceException When parsing encounters general error
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public @NonNull Registration registerSource(final @NonNull YangTextSource source)
            throws SchemaSourceException, IOException, YangSyntaxErrorException {
        final var ast = TextToIRTransformer.transformText(source);
        LOG.trace("Resolved source {} to source {}", source, ast);

        // AST carries an accurate identifier, check if it matches the one supplied by the source. If it
        // does not, check how much it differs and emit a warning.
        final var providedId = source.sourceId();
        final var parsedId = ast.sourceId();
        final YangTextSource text;
        if (!parsedId.equals(providedId)) {
            if (!parsedId.name().equals(providedId.name())) {
                LOG.info("Provided module name {} does not match actual text {}, corrected",
                    providedId.toYangFilename(), parsedId.toYangFilename());
            } else {
                final var sourceRev = providedId.revision();
                if (sourceRev != null) {
                    if (!sourceRev.equals(parsedId.revision())) {
                        LOG.info("Provided module revision {} does not match actual text {}, corrected",
                            providedId.toYangFilename(), parsedId.toYangFilename());
                    }
                } else {
                    LOG.debug("Expanded module {} to {}", providedId.toYangFilename(), parsedId.toYangFilename());
                }
            }

            text = YangTextSource.delegateForCharSource(parsedId, source);
        } else {
            text = source;
        }

        synchronized (this) {
            texts.put(parsedId, text);
            LOG.debug("Populated {} with text", parsedId);

            final var reg = registry.registerSchemaSource(this,
                PotentialSchemaSource.create(parsedId, YangTextSource.class, Costs.IMMEDIATE.getValue()));
            requiredSources.add(parsedId);
            cache.schemaSourceEncountered(ast);
            LOG.debug("Added source {} to schema context requirements", parsedId);
            version = new Object();

            return new AbstractRegistration() {
                @Override
                protected void removeRegistration() {
                    synchronized (YangTextSchemaContextResolver.this) {
                        requiredSources.remove(parsedId);
                        LOG.trace("Removed source {} from schema context requirements", parsedId);
                        version = new Object();
                        reg.close();
                        texts.remove(parsedId, text);
                    }
                }
            };
        }
    }

    /**
     * Register a URL containing a YANG text.
     *
     * @param url YANG text source URL
     * @return a YangTextSchemaSourceRegistration for this URL
     * @throws YangSyntaxErrorException When the YANG file is syntactically invalid
     * @throws IOException when the URL is not readable
     * @throws SchemaSourceException When parsing encounters general error
     * @throws NullPointerException if {@code url} is {@code null}
     */
    public @NonNull Registration registerSource(final @NonNull URL url)
            throws SchemaSourceException, IOException, YangSyntaxErrorException {
        final String path = url.getPath();
        final String fileName = path.substring(path.lastIndexOf('/') + 1);
        return registerSource(YangTextSource.forURL(url, guessSourceIdentifier(fileName)));
    }

    /**
     * Register a {@link QNameModule} as a known module namespace with a set of supported features. Union of these
     * registrations is forwarded to {@link FeatureSet} and this is then used in {@link #getEffectiveModelContext()} and
     * related methods.
     *
     * @param module Module namespace
     * @param features Features supported for that module namespace
     * @return a {@link Registration}, use {@link Registration#close()} to revert the effects of this method
     * @throws NullPointerException if any argument is {@code null}
     */
    public @NonNull Registration registerSupportedFeatures(final QNameModule module, final Set<String> features) {
        final var checked = requireNonNull(module);
        final var copy = ImmutableSet.copyOf(features);

        synchronized (this) {
            version = new Object();
            supportedFeatures = null;
            registeredFeatures.computeIfAbsent(module, ignored -> new ArrayList<>()).add(copy);
        }
        return new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                removeFeatures(checked, copy);
            }
        };
    }

    private synchronized void removeFeatures(final QNameModule module, final ImmutableSet<String> features) {
        final var moduleFeatures = registeredFeatures.get(module);
        if (moduleFeatures != null && moduleFeatures.remove(features)) {
            if (moduleFeatures.isEmpty()) {
                registeredFeatures.remove(module);
            }
            supportedFeatures = null;
            version = new Object();
        }
    }

    private synchronized @Nullable FeatureSet getSupportedFeatures() {
        var local = supportedFeatures;
        if (local == null && !registeredFeatures.isEmpty()) {
            final var builder = FeatureSet.builder();
            for (var entry : registeredFeatures.entrySet()) {
                for (var features : entry.getValue()) {
                    builder.addModuleFeatures(entry.getKey(), features);
                }
            }
            supportedFeatures = local = builder.build();
        }
        return local;
    }

    private static SourceIdentifier guessSourceIdentifier(final @NonNull String fileName) {
        try {
            return YangTextSource.identifierFromFilename(fileName);
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid file name format in '{}'", fileName, e);
            return new SourceIdentifier(fileName);
        }
    }

    /**
     * Try to parse all currently available yang files and build new schema context.
     *
     * @return new schema context iif there is at least 1 yang file registered and
     *         new schema context was successfully built.
     */
    public Optional<? extends EffectiveModelContext> getEffectiveModelContext() {
        return getEffectiveModelContext(StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Try to parse all currently available yang files and build new schema context depending on specified parsing mode.
     *
     * @param statementParserMode mode of statement parser
     * @return new schema context iif there is at least 1 yang file registered and
     *         new schema context was successfully built.
     */
    public Optional<? extends EffectiveModelContext> getEffectiveModelContext(
            final StatementParserMode statementParserMode) {
        Optional<EffectiveModelContext> sc;
        Object ver;
        do {
            // Spin get stable context version
            Object cv;
            do {
                cv = contextVersion;
                sc = currentSchemaContext.get();
                if (version == cv) {
                    return sc;
                }
            } while (cv != contextVersion);

            // Version has been updated
            Collection<SourceIdentifier> sources;
            do {
                ver = version;
                sources = ImmutableSet.copyOf(requiredSources);
            } while (ver != version);

            final var factory = repository.createEffectiveModelContextFactory(
                config(statementParserMode, getSupportedFeatures()));

            while (true) {
                final var f = factory.createEffectiveModelContext(sources);
                try {
                    sc = Optional.of(f.get());
                    break;
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Interrupted while assembling schema context", e);
                } catch (ExecutionException e) {
                    LOG.info("Failed to fully assemble schema context for {}", sources, e);
                    final var cause = e.getCause();
                    Verify.verify(cause instanceof SchemaResolutionException);
                    sources = ((SchemaResolutionException) cause).getResolvedSources();
                }
            }

            LOG.debug("Resolved schema context for {}", sources);

            synchronized (this) {
                if (contextVersion == cv) {
                    currentSchemaContext.set(sc);
                    contextVersion = ver;
                }
            }
        } while (version == ver);

        return sc;
    }

    @Override
    public synchronized @NonNull FluentFuture<YangTextSource> getSource(final SourceIdentifier sourceIdentifier) {
        final var ret = texts.get(sourceIdentifier);

        LOG.debug("Lookup {} result {}", sourceIdentifier, ret);
        if (ret.isEmpty()) {
            return FluentFutures.immediateFailedFluentFuture(
                new MissingSchemaSourceException(sourceIdentifier, "URL for " + sourceIdentifier + " not registered"));
        }

        return FluentFutures.immediateFluentFuture(ret.iterator().next());
    }

    /**
     * Return the set of sources currently available in this resolved.
     *
     * @return An immutable point-in-time view of available sources.
     */
    public synchronized Set<SourceIdentifier> getAvailableSources() {
        return ImmutableSet.copyOf(texts.keySet());
    }

    @Beta
    public synchronized Collection<YangTextSource> getSourceTexts(final SourceIdentifier sourceIdentifier) {
        return ImmutableSet.copyOf(texts.get(sourceIdentifier));
    }

    @Beta
    public EffectiveModelContext trySchemaContext() throws SchemaResolutionException {
        return trySchemaContext(StatementParserMode.DEFAULT_MODE);
    }

    @Beta
    @SuppressWarnings("checkstyle:avoidHidingCauseException")
    public EffectiveModelContext trySchemaContext(final StatementParserMode statementParserMode)
            throws SchemaResolutionException {
        final var future = repository
                .createEffectiveModelContextFactory(config(statementParserMode, getSupportedFeatures()))
                .createEffectiveModelContext(ImmutableSet.copyOf(requiredSources));

        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while waiting for SchemaContext assembly", e);
        } catch (ExecutionException e) {
            final var cause = e.getCause();
            if (cause instanceof SchemaResolutionException resolutionException) {
                throw resolutionException;
            }
            throw new SchemaResolutionException("Failed to assemble SchemaContext", e);
        }
    }

    @Override
    public void close() {
        transReg.close();
    }

    private static @NonNull SchemaContextFactoryConfiguration config(
            final StatementParserMode statementParserMode, final @Nullable FeatureSet supportedFeatures) {
        final var builder = SchemaContextFactoryConfiguration.builder().setStatementParserMode(statementParserMode);
        if (supportedFeatures != null) {
            builder.setSupportedFeatures(supportedFeatures);
        }
        return builder.build();
    }
}
