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
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedSourceInfo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedSourceInfo.ResolvedBelongsTo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedSourceInfo.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedSourceInfo.ResolvedInclude;

/**
 * Constructs a {@link ResolvedSourceInfo} of a Source containing the linkage details about imports, includes,
 * belongsTo.
 */
final class ResolvedSourceBuilder {
    private final SourceInfo sourceInfo;
    private final SourceSpecificContext context;
    private final ImmutableMap.Builder<String, ResolvedSourceBuilder> imports = new ImmutableMap.Builder<>();
    private final ImmutableSet.Builder<ResolvedSourceBuilder> includes = new ImmutableSet.Builder<>();

    private ResolvedBelongsTo belongsTo;
    private ResolvedSourceInfo buildFinished;

    ResolvedSourceBuilder(final @NonNull SourceSpecificContext sourceContext) {
        this.context = requireNonNull(sourceContext);
        this.sourceInfo = sourceContext.getSourceInfo();
    }

    SourceSpecificContext context() {
        return context;
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
    ResolvedSourceBuilder addImport(final @NonNull String prefix,
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
    ResolvedSourceBuilder setBelongsTo(final @NonNull String prefix,
            final @NonNull ResolvedSourceBuilder belongsToModule) {
        ensureBuilderOpened();
        requireNonNull(belongsToModule);
        this.belongsTo = new ResolvedBelongsTo(requireNonNull(prefix), belongsToModule.sourceId(),
            belongsToModule.resolveQnameModule(), belongsToModule.context.getRoot());
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

        final var prefix = sourceInfo instanceof SourceInfo.Module module ? module.prefix().getLocalName() : null;

        buildFinished = new ResolvedSourceInfo(sourceInfo.sourceId(), resolveQnameModule(),
            context.getRoot(), resolveImports(allResolved), resolveIncludes(), prefix, belongsTo);
        return buildFinished;
    }

    SourceIdentifier sourceId() {
        return sourceInfo.sourceId();
    }

    private List<ResolvedImport> resolveImports(
        final Map<SourceSpecificContext, ResolvedSourceInfo> allResolved) {
        return imports.build().entrySet().stream()
            .map(prefixedImport -> {
                final SourceSpecificContext impContext = prefixedImport.getValue().context();
                if (!allResolved.containsKey(impContext)) {
                    throw new IllegalStateException(String.format("Unresolved import %s of module %s",
                        prefixedImport.getValue().sourceId(), sourceId()));
                }
                return ResolvedImport.of(prefixedImport.getKey(), allResolved.get(impContext));
            })
            .toList();
    }

    private List<ResolvedInclude> resolveIncludes() {
        return includes.build()
            .stream()
            .map(builder -> new ResolvedInclude(builder.sourceId(), builder.resolveQnameModule(),
                builder.context.getRoot()))
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