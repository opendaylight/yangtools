/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedBelongsTo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;
import org.opendaylight.yangtools.yang.parser.source.ResolvedSourceInfo;

/**
 * Constructs a {@link ResolvedSourceInfo} of a Source containing the linkage details about imports, includes,
 * belongsTo.
 */
final class ResolvedSourceBuilder {
    private final ImmutableMap.Builder<Unqualified, ResolvedSourceBuilder> imports = new ImmutableMap.Builder<>();
    private final ImmutableSet.Builder<ResolvedSourceBuilder> includes = new ImmutableSet.Builder<>();
    private final SourceSpecificContext sourceContext;
    private final SourceInfo sourceInfo;

    private ResolvedBelongsTo belongsTo;
    private ResolvedSourceInfo buildFinished;

    ResolvedSourceBuilder(final @NonNull SourceSpecificContext sourceContext, final @NonNull SourceInfo sourceInfo) {
        this.sourceContext = requireNonNull(sourceContext);
        this.sourceInfo = requireNonNull(sourceInfo);
    }

    SourceSpecificContext context() {
        return sourceContext;
    }

    YangVersion yangVersion() {
        return sourceInfo.yangVersion();
    }

    /**
     * Adds a {@link ResolvedSourceBuilder} of an imported module.
     *
     * @param prefix String prefix defined in the import statement
     * @param importedModule ResolvedSourceBuilder of the imported module.
     * @return this instance.
     */
    ResolvedSourceBuilder addImport(final @NonNull Unqualified prefix,
            final @NonNull ResolvedSourceBuilder importedModule) {
        ensureBuilderOpened();
        imports.put(prefix, importedModule);
        return this;
    }

    /**
     * Adds a {@link ResolvedSourceBuilder} of an included submodule.
     *
     * @param includedSubmodule ResolvedSourceBuilder of the included submodule.
     * @return this instance.
     */
    ResolvedSourceBuilder addInclude(final @NonNull ResolvedSourceBuilder includedSubmodule) {
        ensureBuilderOpened();
        includes.add(includedSubmodule);
        return this;
    }

    /**
     * Adds a {@link ResolvedSourceBuilder} of the parent module this submodule belongs to.
     *
     * @param prefix String prefix defined in the belongs-to statement
     * @param belongsToModule ResolvedSourceBuilder of the parent module.
     * @return this instance.
     */
    @NonNullByDefault
    ResolvedSourceBuilder setBelongsTo(final Unqualified prefix, final ResolvedSourceBuilder belongsToModule) {
        ensureBuilderOpened();
        belongsTo = new ResolvedBelongsTo(prefix, belongsToModule.resolveQnameModule());
        return this;
    }

    /**
     * Builds a finalized {@link ResolvedSourceInfo} using the map of already-resolved sources.
     *
     * @param allResolved all the sources which were already resolved
     * @return ResolvedSourceInfo of this source
     */
    ResolvedSourceInfo build(final @NonNull Map<SourceSpecificContext, ResolvedSourceInfo> allResolved) {
        requireNonNull(allResolved);

        if (buildFinished != null) {
            return buildFinished;
        }

        // TODO: for submodules this should be the 'belongsTo' prefix
        final var prefix = sourceInfo instanceof SourceInfo.Module module ? module.prefix() : null;

        buildFinished = new ResolvedSourceInfo(sourceInfo.sourceId(), resolveQnameModule(),
            resolveImports(allResolved), resolveIncludes(), prefix, belongsTo);
        return buildFinished;
    }

    SourceIdentifier sourceId() {
        return sourceInfo.sourceId();
    }

    private List<ResolvedImport> resolveImports(
            final Map<SourceSpecificContext, ResolvedSourceInfo> allResolved) {
        return imports.build().entrySet().stream()
            .map(prefixedImport -> {
                final var impContext = prefixedImport.getValue().context();
                // FIXME: containsKey + get -> should be get() and null check
                if (!allResolved.containsKey(impContext)) {
                    // FIXME: better exception
                    throw new IllegalStateException("Unresolved import %s of module %s".formatted(
                        prefixedImport.getValue().sourceId(), sourceId()));
                }
                return new ResolvedImport(prefixedImport.getKey(), allResolved.get(impContext));
            })
            .toList();
    }

    private List<ResolvedInclude> resolveIncludes() {
        return includes.build()
            .stream()
            .map(builder -> new ResolvedInclude(builder.source(), builder.resolveQnameModule()))
            .toList();
    }

    private QNameModule resolveQnameModule() {
        if (sourceInfo instanceof SourceInfo.Module moduleInfo) {
            return QNameModule.ofRevision(moduleInfo.namespace(), latestRevision());
        }
        // Submodule's QNameModule is composed of parents Namespace + its own Revision (or null, if absent)
        verifyNotNull(belongsTo, "Cannot resolve QNameModule of a submodule %s. Missing belongs-to",
            sourceInfo.sourceId());
        return QNameModule.ofRevision(belongsTo.parentModuleQname().namespace(), latestRevision());
    }

    private Revision latestRevision() {
        return sourceInfo.revisions().isEmpty() ? null : sourceInfo.revisions().iterator().next();
    }

    private void ensureBuilderOpened() {
        verify(buildFinished == null, "Builder for source %s was already closed",
            sourceInfo.sourceId());
    }
}