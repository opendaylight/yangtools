/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.util.repo.AdvancedSchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils;

/**
 *
 * Context of YANG model sources
 *
 * YANG sources context represent information learned about set of model sources
 * which could be derived from dependency information only.
 *
 * Contains following information:
 * <ul>
 * <li>{@link #getValidSources()} - set of {@link SourceIdentifier} which have
 * their dependencies present and are safe to be used by full blown parser.
 * <li>{@link #getMissingSources()} - set of {@link SourceIdentifier} which have
 * been referenced by other YANG sources, but source code for them is missing.
 * <li>{@link #getMissingDependencies()} - map of {@link SourceIdentifier} and
 * their imports for which source codes was not available.
 * </ul>
 *
 * {@link YangSourceContext} may be associated with {@link SchemaSourceProvider}
 * (see {@link #getDelegate()}, which was used for retrieval of sources during
 * context computation.
 *
 * {@link YangSourceContext} may be used as schema source provider to retrieve
 * this sources.
 *
 *
 */
// FIXME: for some reason this class is Closeable even though close is never called and no resources are leaked
@Deprecated
@ThreadSafe
public class YangSourceContext implements AdvancedSchemaSourceProvider<InputStream>, Closeable,
        Delegator<AdvancedSchemaSourceProvider<InputStream>> {

    private final ImmutableSet<SourceIdentifier> validSources;

    private final ImmutableSet<SourceIdentifier> missingSources;
    private final ImmutableMultimap<SourceIdentifier, ModuleImport> missingDependencies;
    private final AdvancedSchemaSourceProvider<InputStream> sourceProvider;
    private final AtomicBoolean isClosed = new AtomicBoolean();

    /**
     * Construct YANG Source Context
     *
     * @param validSourcesSet Set of identifiers of valid sources
     * @param missingSourcesSet Set of identifiers of missing sources
     * @param missingDependenciesMap Map of identifiers of resolved sources and their missing imports.
     * @param sourceProvider Source provider which was used for context resolution or
     *          null if provider was not used.
     */
    YangSourceContext(final ImmutableSet<SourceIdentifier> validSourcesSet,
            final ImmutableSet<SourceIdentifier> missingSourcesSet,
            final ImmutableMultimap<SourceIdentifier, ModuleImport> missingDependenciesMap,
            final AdvancedSchemaSourceProvider<InputStream> sourceProvider) {
        validSources = checkNotNull(validSourcesSet, "Valid source set must not be null");
        missingSources = checkNotNull(missingSourcesSet, "Missing sources set must not be null");
        missingDependencies = checkNotNull(missingDependenciesMap, "Missing dependencies map must not be null");
        this.sourceProvider = checkNotNull(sourceProvider, "Missing sourceProvider");
    }

    /**
     * Returns set of valid source identifiers.
     *
     * Source identifier is considered valid if it's source
     * was present during resolution and sources
     * for all known dependencies was present at the time of creation
     * of {@link YangSourceContext}.
     *
     * @return Set of valid source identifiers.
     */
    public ImmutableSet<SourceIdentifier> getValidSources() {
        return validSources;
    }

    /**
     * Returns set of source identifiers, whom sources was not resolved.
     *
     * Source is considered missing if the source was not present
     * during resolution of {@link YangSourceContext}.
     *
     * @return Set of missing sources.
     */
    public ImmutableSet<SourceIdentifier> getMissingSources() {
        return missingSources;
    }

    /**
     * Returns a multimap of Source Identifier and imports which had missing
     * sources.
     *
     * Maps a source identifier to its imports, which was not resolved
     * during resolution of this context, so it is unable to fully
     * processed source identifier.
     *
     *
     * @return Multi-map of source identifier to it's unresolved dependencies.
     */
    public ImmutableMultimap<SourceIdentifier, ModuleImport> getMissingDependencies() {
        return missingDependencies;
    }

    @Override
    public Optional<InputStream> getSchemaSource(final String moduleName, final Optional<String> revision) {
        return getSchemaSource(SourceIdentifier.create(moduleName, revision));
    }

    @Override
    public Optional<InputStream> getSchemaSource(final SourceIdentifier sourceIdentifier) {
        if (validSources.contains(sourceIdentifier)) {
            return getDelegateChecked().getSchemaSource(sourceIdentifier);
        }
        return Optional.absent();
    }

    private AdvancedSchemaSourceProvider<InputStream> getDelegateChecked() {
        assertNotClosed();
        return sourceProvider;
    }

    @Override
    public AdvancedSchemaSourceProvider<InputStream> getDelegate() {
        assertNotClosed();
        return sourceProvider;
    }

    private void assertNotClosed() {
        if (isClosed.get()) {
            throw new IllegalStateException("Instance already closed");
        }
    }

    @Override
    public void close() {
        isClosed.set(true);
    }

    /**
     * Creates YANG Source context from supplied capabilities and schema source
     * provider.
     *
     * @param capabilities
     *            Set of QName representing module capabilities,
     *            {@link QName#getLocalName()} represents
     *            source name and {@link QName#getRevision()} represents
     *            revision of source.
     *
     * @param schemaSourceProvider
     *            - {@link SchemaSourceProvider} which should be used to resolve
     *            sources.
     * @return YANG source context which describes resolution of capabilities
     *         and their dependencies
     *         against supplied schema source provider.
     */
    public static YangSourceContext createFrom(final Iterable<QName> capabilities,
            final SchemaSourceProvider<InputStream> schemaSourceProvider) {
        YangSourceContextResolver resolver = new YangSourceFromCapabilitiesResolver(capabilities, schemaSourceProvider);
        return resolver.resolveContext();
    }

    public static YangSourceContext createFrom(final Map<SourceIdentifier, YangModelDependencyInfo> moduleDependencies,
            AdvancedSchemaSourceProvider<InputStream> sourceProvider) {
        YangSourceFromDependencyInfoResolver resolver = new YangSourceFromDependencyInfoResolver(
                moduleDependencies, sourceProvider);
        return resolver.resolveContext();
    }

    /**
     * Returns a list of valid input streams from YANG Source Context
     * using supplied schema source provider.
     *
     * @return List of input streams.
     * @deprecated Use {@link #getValidByteSources()}
     */
    @Deprecated
    public List<InputStream> getValidInputStreams() {
        return getValidInputStreamsInternal();
    }

    private List<InputStream> getValidInputStreamsInternal() {
        assertNotClosed();
        final Set<SourceIdentifier> sourcesToLoad = new HashSet<>();
        sourcesToLoad.addAll(this.getValidSources());
        for (SourceIdentifier source : this.getValidSources()) {
            if (source.getRevision() != null) {
                SourceIdentifier sourceWithoutRevision = SourceIdentifier.create(source.getName(),
                        Optional.<String> absent());
                sourcesToLoad.remove(sourceWithoutRevision);
            }
        }

        ImmutableList.Builder<InputStream> ret = ImmutableList.<InputStream>builder();
        for (SourceIdentifier sourceIdentifier : sourcesToLoad) {
            Optional<InputStream> source = sourceProvider.getSchemaSource(sourceIdentifier);
            ret.add(source.get());
        }
        return ret.build();
    }



    public Collection<ByteSource> getValidByteSources() throws IOException {
        List<InputStream> yangModelStreams = getValidInputStreamsInternal();
        return BuilderUtils.streamsToByteSources(yangModelStreams);
    }

    @Deprecated
    public static List<InputStream> getValidInputStreams(final YangSourceContext context) {
        return context.getValidInputStreams();
    }

}
