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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
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
    // these retain insertion order
    private final ImmutableMap.Builder<Include, ResolvedSourceBuilder> includes = new ImmutableMap.Builder<>();
    private final ImmutableMap.Builder<Import, ResolvedSourceBuilder> imports = new ImmutableMap.Builder<>();
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
     * @param dependency the import dependency being satisfied
     * @param importedModule ResolvedSourceBuilder of the imported module.
     * @return this instance.
     */
    @NonNullByDefault
    ResolvedSourceBuilder resolveImport(final Import dependency, final ResolvedSourceBuilder importedModule) {
        ensureBuilderOpened();
        imports.put(dependency, importedModule);
        return this;
    }

    /**
     * Adds a {@link ResolvedSourceBuilder} of an included submodule.
     *
     * @param includedSubmodule ResolvedSourceBuilder of the included submodule.
     * @return this instance.
     */
    @NonNullByDefault
    ResolvedSourceBuilder resolveInclude(final Include dependency, final ResolvedSourceBuilder includedSubmodule) {
        ensureBuilderOpened();
        includes.put(dependency, includedSubmodule);
        return this;
    }

    /**
     * Adds a {@link ResolvedSourceBuilder} of the parent module this submodule belongs to.
     *
     * @param dependency the {@link BelongsTo}
     * @param belongsToModule {@link ResolvedSourceBuilder} of the parent module.
     * @return this instance.
     */
    @NonNullByDefault
    ResolvedSourceBuilder resolveBelongsTo(final BelongsTo dependency, final ResolvedSourceBuilder belongsToModule) {
        ensureBuilderOpened();
        belongsTo = new ResolvedBelongsTo(dependency, belongsToModule.resolveQnameModule());
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

    private List<ResolvedImport> resolveImports(final Map<SourceSpecificContext, ResolvedSourceInfo> allResolved) {
        final var map = imports.build();
        final var result = new ArrayList<ResolvedImport>(map.size());

        for (var entry : map.entrySet()) {
            final var importedModule = entry.getValue();

            final var impContext = importedModule.context();
            final var resolved = allResolved.get(impContext);
            if (resolved == null) {
                // FIXME: better exception
                throw new IllegalStateException("Unresolved import %s of module %s".formatted(
                    importedModule.sourceId(), sourceId()));
            }
            result.add(new ResolvedImport(entry.getKey(), resolved.sourceInfo().sourceId(), resolved.qnameModule()));
        }

        return result;
    }

    private List<ResolvedInclude> resolveIncludes() {
        final var map = includes.build();
        final var result = new ArrayList<ResolvedInclude>(map.size());

        for (var entry : map.entrySet()) {
            final var builder = entry.getValue();
            result.add(new ResolvedInclude(entry.getKey(), builder.sourceId(), builder.resolveQnameModule()));
        }

        return result;
    }

    private QNameModule resolveQnameModule() {
        if (sourceInfo instanceof SourceInfo.Module moduleInfo) {
            // FIXME: separate subclass with eager instantiation and interned
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