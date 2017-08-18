/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource.Costs;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaListenerRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.repo.util.InMemorySchemaSourceCache;
import org.opendaylight.yangtools.yang.parser.util.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.TextToASTTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class YangTextSchemaContextResolver implements AutoCloseable, SchemaSourceProvider<YangTextSchemaSource> {
    private static final Logger LOG = LoggerFactory.getLogger(YangTextSchemaContextResolver.class);
    private static final long SOURCE_LIFETIME_SECONDS = 60;

    private final Collection<SourceIdentifier> requiredSources = new ConcurrentLinkedDeque<>();
    private final Multimap<SourceIdentifier, YangTextSchemaSource> texts = ArrayListMultimap.create();
    private final AtomicReference<Optional<SchemaContext>> currentSchemaContext =
            new AtomicReference<>(Optional.empty());
    private final InMemorySchemaSourceCache<ASTSchemaSource> cache;
    private final SchemaListenerRegistration transReg;
    private final SchemaSourceRegistry registry;
    private final SchemaRepository repository;
    private volatile Object version = new Object();
    private volatile Object contextVersion = version;

    private YangTextSchemaContextResolver(final SchemaRepository repository, final SchemaSourceRegistry registry) {
        this.repository = Preconditions.checkNotNull(repository);
        this.registry = Preconditions.checkNotNull(registry);

        final TextToASTTransformer t = TextToASTTransformer.create(repository, registry);
        transReg = registry.registerSchemaSourceListener(t);

        cache = InMemorySchemaSourceCache.createSoftCache(registry, ASTSchemaSource.class, SOURCE_LIFETIME_SECONDS,
            TimeUnit.SECONDS);
    }

    public static YangTextSchemaContextResolver create(final String name) {
        final SharedSchemaRepository sharedRepo = new SharedSchemaRepository(name);
        return new YangTextSchemaContextResolver(sharedRepo, sharedRepo);
    }

    /**
     * Register a {@link YangTextSchemaSource}.
     *
     * @param source YANG text source
     * @throws YangSyntaxErrorException When the YANG file is syntactically invalid
     * @throws IOException when the URL is not readable
     * @throws SchemaSourceException When parsing encounters general error
     * @return a YangTextSchemaSourceRegistration
     */
    public YangTextSchemaSourceRegistration registerSource(@Nonnull final YangTextSchemaSource source)
            throws SchemaSourceException, IOException, YangSyntaxErrorException {
        checkArgument(source != null);

        final ASTSchemaSource ast = TextToASTTransformer.transformText(source);
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
                final String sourceRev = providedId.getRevision();
                final String astRev = parsedId.getRevision();
                if (sourceRev != null && !SourceIdentifier.NOT_PRESENT_FORMATTED_REVISION.equals(sourceRev)) {
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
     * @throws YangSyntaxErrorException When the YANG file is syntactically invalid
     * @throws IOException when the URL is not readable
     * @throws SchemaSourceException When parsing encounters general error
     * @return a YangTextSchemaSourceRegistration for this URL
     */
    public YangTextSchemaSourceRegistration registerSource(@Nonnull final URL url) throws SchemaSourceException,
            IOException, YangSyntaxErrorException {
        checkArgument(url != null, "Supplied URL must not be null");

        final String path = url.getPath();
        final String fileName = path.substring(path.lastIndexOf('/') + 1);
        final SourceIdentifier guessedId = guessSourceIdentifier(fileName);
        return registerSource(new YangTextSchemaSource(guessedId) {
            @Override
            public InputStream openStream() throws IOException {
                return url.openStream();
            }

            @Override
            protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
                return toStringHelper.add("url", url);
            }
        });
    }

    private static SourceIdentifier guessSourceIdentifier(final String fileName) {
        try {
            return YangTextSchemaSource.identifierFromFilename(fileName);
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid file name format in '{}'", fileName, e);
            return RevisionSourceIdentifier.create(fileName);
        }
    }

    /**
     * Try to parse all currently available yang files and build new schema context.
     * @return new schema context iif there is at least 1 yang file registered and
     *         new schema context was successfully built.
     */
    public Optional<SchemaContext> getSchemaContext() {
        return getSchemaContext(StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Try to parse all currently available yang files and build new schema context
     * in dependence on specified parsing mode.
     *
     * @param statementParserMode mode of statement parser
     * @return new schema context iif there is at least 1 yang file registered and
     *         new schema context was successfully built.
     */
    public Optional<SchemaContext> getSchemaContext(final StatementParserMode statementParserMode) {
        final SchemaContextFactory factory = repository.createSchemaContextFactory(SchemaSourceFilter.ALWAYS_ACCEPT);
        Optional<SchemaContext> sc;
        Object v;
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
                v = version;
                sources = ImmutableSet.copyOf(requiredSources);
            } while (v != version);

            while (true) {
                final CheckedFuture<SchemaContext, SchemaResolutionException> f = factory.createSchemaContext(sources,
                    statementParserMode);
                try {
                    sc = Optional.of(f.checkedGet());
                    break;
                } catch (SchemaResolutionException e) {
                    LOG.info("Failed to fully assemble schema context for {}", sources, e);
                    sources = e.getResolvedSources();
                }
            }

            LOG.debug("Resolved schema context for {}", sources);

            synchronized (this) {
                if (contextVersion == cv) {
                    currentSchemaContext.set(sc);
                    contextVersion = v;
                }
            }
        } while (version == v);

        return sc;
    }

    @Override
    public synchronized CheckedFuture<YangTextSchemaSource, SchemaSourceException> getSource(
            final SourceIdentifier sourceIdentifier) {
        final Collection<YangTextSchemaSource> ret = texts.get(sourceIdentifier);

        LOG.debug("Lookup {} result {}", sourceIdentifier, ret);
        if (ret.isEmpty()) {
            return Futures.immediateFailedCheckedFuture(new MissingSchemaSourceException(
                "URL for " + sourceIdentifier + " not registered", sourceIdentifier));
        }

        return Futures.immediateCheckedFuture(ret.iterator().next());
    }

    /**
     * Return the set of sources currently available in this resolved.
     *
     * @return An immutable point-in-time view of available sources.
     */
    public synchronized Set<SourceIdentifier> getAvailableSources() {
        return ImmutableSet.copyOf(texts.keySet());
    }

    @Override
    public void close() {
        transReg.close();
    }
}
