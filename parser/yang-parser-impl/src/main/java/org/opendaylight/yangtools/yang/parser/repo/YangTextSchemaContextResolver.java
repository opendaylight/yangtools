/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFailedFluentFuture;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFluentFuture;

import com.google.common.annotations.Beta;
import com.google.common.base.Verify;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.EffectiveModelContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.GuavaSchemaSourceCache;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource.Costs;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaListenerRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class YangTextSchemaContextResolver implements AutoCloseable, SchemaSourceProvider<YangTextSchemaSource> {
    private static final Logger LOG = LoggerFactory.getLogger(YangTextSchemaContextResolver.class);
    private static final Duration SOURCE_LIFETIME = Duration.ofSeconds(60);

    private final Collection<SourceIdentifier> requiredSources = new ConcurrentLinkedDeque<>();
    private final Multimap<SourceIdentifier, YangTextSchemaSource> texts = ArrayListMultimap.create();
    private final AtomicReference<Optional<EffectiveModelContext>> currentSchemaContext =
            new AtomicReference<>(Optional.empty());
    private final GuavaSchemaSourceCache<IRSchemaSource> cache;
    private final SchemaListenerRegistration transReg;
    private final SchemaSourceRegistry registry;
    private final SchemaRepository repository;
    private volatile Object version = new Object();
    private volatile Object contextVersion = version;

    private YangTextSchemaContextResolver(final SchemaRepository repository, final SchemaSourceRegistry registry) {
        this.repository = requireNonNull(repository);
        this.registry = requireNonNull(registry);

        final TextToIRTransformer t = TextToIRTransformer.create(repository, registry);
        transReg = registry.registerSchemaSourceListener(t);

        cache = GuavaSchemaSourceCache.createSoftCache(registry, IRSchemaSource.class, SOURCE_LIFETIME);
    }

    public static @NonNull YangTextSchemaContextResolver create(final String name) {
        final SharedSchemaRepository sharedRepo = new SharedSchemaRepository(name);
        return new YangTextSchemaContextResolver(sharedRepo, sharedRepo);
    }

    public static @NonNull YangTextSchemaContextResolver create(final String name, final YangParserFactory factory) {
        final SharedSchemaRepository sharedRepo = new SharedSchemaRepository(name, factory);
        return new YangTextSchemaContextResolver(sharedRepo, sharedRepo);
    }

    /**
     * Register a {@link YangTextSchemaSource}.
     *
     * @param source YANG text source
     * @return a YangTextSchemaSourceRegistration
     * @throws YangSyntaxErrorException When the YANG file is syntactically invalid
     * @throws IOException when the URL is not readable
     * @throws SchemaSourceException When parsing encounters general error
     */
    public @NonNull YangTextSchemaSourceRegistration registerSource(final @NonNull YangTextSchemaSource source)
            throws SchemaSourceException, IOException, YangSyntaxErrorException {
        checkArgument(source != null);

        final IRSchemaSource ast = TextToIRTransformer.transformText(source);
        LOG.trace("Resolved source {} to source {}", source, ast);

        // AST carries an accurate identifier, check if it matches the one supplied by the source. If it
        // does not, check how much it differs and emit a warning.
        final SourceIdentifier providedId = source.getIdentifier();
        final SourceIdentifier parsedId = ast.getIdentifier();
        final YangTextSchemaSource text;
        if (!parsedId.equals(providedId)) {
            if (!parsedId.getName().equals(providedId.getName())) {
                LOG.info("Provided module name {} does not match actual text {}, corrected",
                    providedId.toYangFilename(), parsedId.toYangFilename());
            } else {
                final Optional<Revision> sourceRev = providedId.getRevision();
                final Optional<Revision> astRev = parsedId.getRevision();
                if (sourceRev.isPresent()) {
                    if (!sourceRev.equals(astRev)) {
                        LOG.info("Provided module revision {} does not match actual text {}, corrected",
                            providedId.toYangFilename(), parsedId.toYangFilename());
                    }
                } else {
                    LOG.debug("Expanded module {} to {}", providedId.toYangFilename(), parsedId.toYangFilename());
                }
            }

            text = YangTextSchemaSource.delegateForByteSource(parsedId, source);
        } else {
            text = source;
        }

        synchronized (this) {
            texts.put(parsedId, text);
            LOG.debug("Populated {} with text", parsedId);

            final SchemaSourceRegistration<YangTextSchemaSource> reg = registry.registerSchemaSource(this,
                PotentialSchemaSource.create(parsedId, YangTextSchemaSource.class, Costs.IMMEDIATE.getValue()));
            requiredSources.add(parsedId);
            cache.schemaSourceEncountered(ast);
            LOG.debug("Added source {} to schema context requirements", parsedId);
            version = new Object();

            return new AbstractYangTextSchemaSourceRegistration(text) {
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
     */
    public @NonNull YangTextSchemaSourceRegistration registerSource(final @NonNull URL url)
            throws SchemaSourceException, IOException, YangSyntaxErrorException {
        checkArgument(url != null, "Supplied URL must not be null");

        final String path = url.getPath();
        final String fileName = path.substring(path.lastIndexOf('/') + 1);
        return registerSource(YangTextSchemaSource.forURL(url, guessSourceIdentifier(fileName)));
    }

    private static SourceIdentifier guessSourceIdentifier(final @NonNull String fileName) {
        try {
            return YangTextSchemaSource.identifierFromFilename(fileName);
        } catch (final IllegalArgumentException e) {
            LOG.warn("Invalid file name format in '{}'", fileName, e);
            return RevisionSourceIdentifier.create(fileName);
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
        final EffectiveModelContextFactory factory = repository.createEffectiveModelContextFactory(
            config(statementParserMode));
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

            while (true) {
                final ListenableFuture<EffectiveModelContext> f = factory.createEffectiveModelContext(sources);
                try {
                    sc = Optional.of(f.get());
                    break;
                } catch (final InterruptedException e) {
                    throw new IllegalStateException("Interrupted while assembling schema context", e);
                } catch (final ExecutionException e) {
                    LOG.info("Failed to fully assemble schema context for {}", sources, e);
                    final Throwable cause = e.getCause();
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
    public synchronized @NonNull FluentFuture<YangTextSchemaSource> getSource(
            final SourceIdentifier sourceIdentifier) {
        final Collection<YangTextSchemaSource> ret = texts.get(sourceIdentifier);

        LOG.debug("Lookup {} result {}", sourceIdentifier, ret);
        if (ret.isEmpty()) {
            return immediateFailedFluentFuture(new MissingSchemaSourceException("URL for " + sourceIdentifier
                + " not registered", sourceIdentifier));
        }

        return immediateFluentFuture(ret.iterator().next());
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
    public synchronized Collection<YangTextSchemaSource> getSourceTexts(final SourceIdentifier sourceIdentifier) {
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
        final ListenableFuture<EffectiveModelContext> future = repository
                .createEffectiveModelContextFactory(config(statementParserMode))
                .createEffectiveModelContext(ImmutableSet.copyOf(requiredSources));

        try {
            return future.get();
        } catch (final InterruptedException e) {
            throw new IllegalStateException("Interrupted while waiting for SchemaContext assembly", e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof SchemaResolutionException) {
                throw (SchemaResolutionException) cause;
            }

            throw new SchemaResolutionException("Failed to assemble SchemaContext", e);
        }
    }

    @Override
    public void close() {
        transReg.close();
    }

    private static SchemaContextFactoryConfiguration config(final StatementParserMode statementParserMode) {
        return SchemaContextFactoryConfiguration.builder().setStatementParserMode(statementParserMode).build();
    }
}
