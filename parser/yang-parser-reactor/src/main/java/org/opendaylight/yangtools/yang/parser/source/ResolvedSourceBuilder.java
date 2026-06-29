/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedBelongsTo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;

/**
 * Constructs a {@link ResolvedSourceInfo} of a Source containing the linkage details about imports, includes,
 * belongsTo.
 */
abstract sealed class ResolvedSourceBuilder<R extends SourceInfoRef> implements Mutable {
    /**
     * A {@link ResolvedSourceBuilder} for a module.
     */
    @NonNullByDefault
    static final class ForModule extends ResolvedSourceBuilder<SourceInfoRef.OfModule> {
        ForModule(final SourceInfoRef.OfModule infoRef) {
            super(infoRef);
        }

        @Override
        SourceInfo.Module sourceInfo() {
            return infoRef().info();
        }

        @Override
        QNameModule definingModule() {
            return sourceInfo().moduleName().getModule();
        }

        @Override
        void resolveBelongsTo(final BelongsTo dependency, final ForModule module) {
            throw new VerifyException("Attempted to resolve belongs-to in non-submodule" + this);
        }

        @Override
        ResolvedSourceInfo buildProduct(final List<ResolvedImport> resolvedImports,
                final List<ResolvedInclude> resolveIncludes) {
            return ResolvedSourceInfo.ofModule(infoRef(), resolvedImports, resolveIncludes);
        }
    }

    /**
     * A {@link ResolvedSourceBuilder} for a submodule.
     */
    static final class ForSubmodule extends ResolvedSourceBuilder<SourceInfoRef.OfSubmodule> {
        private @Nullable ResolvedBelongsTo belongsTo;

        @NonNullByDefault
        ForSubmodule(final SourceInfoRef.OfSubmodule infoRef) {
            super(infoRef);
        }

        private @NonNull ResolvedBelongsTo belongsTo() {
            final var local = belongsTo;
            if (local == null) {
                throw new VerifyException("Unresolved belongs-to in " + this);
            }
            return local;
        }

        @Override
        SourceInfo.Submodule sourceInfo() {
            return infoRef().info();
        }

        @Override
        void resolveBelongsTo(final BelongsTo dependency, final ForModule module) {
            final var local = belongsTo;
            if (local != null) {
                throw new VerifyException("Attempted to re-resolve belongs-to from " + local + " to " + module);
            }
            final var moduleRef = module.infoRef();
            belongsTo = new ResolvedBelongsTo(dependency, moduleRef.ref(), moduleRef.info().moduleName().getModule());
        }

        @Override
        QNameModule definingModule() {
            // A submodule's QNameModule is composed of parent's namespace + its own Revision (or null if absent)
            return QNameModule.ofRevision(belongsTo().parentModuleQname().namespace(), sourceInfo().latestRevision());
        }

        @Override
        ResolvedSourceInfo buildProduct(final List<ResolvedImport> resolvedImports,
                final List<ResolvedInclude> resolveIncludes) {
            return ResolvedSourceInfo.ofSubmodule(infoRef(), belongsTo(), resolvedImports, resolveIncludes);
        }
    }

    // these retain insertion order
    private final ImmutableMap.Builder<Include, ResolvedSourceBuilder.ForSubmodule> includes =
        new ImmutableMap.Builder<>();
    private final ImmutableMap.Builder<Import, ResolvedSourceBuilder.ForModule> imports = new ImmutableMap.Builder<>();
    private final @NonNull R infoRef;

    private @Nullable ResolvedSourceInfo product;

    @NonNullByDefault
    private ResolvedSourceBuilder(final R infoRef) {
        this.infoRef = requireNonNull(infoRef);
    }

    final @NonNull R infoRef() {
        return infoRef;
    }

    final @NonNull SourceIdentifier sourceId() {
        return sourceInfo().sourceId();
    }

    abstract @NonNull SourceInfo sourceInfo();

    final @NonNull YangVersion yangVersion() {
        return sourceInfo().yangVersion();
    }

    /**
     * Adds a {@link ResolvedSourceBuilder} of an imported module.
     *
     * @param dependency the {@link Import} being satisfied
     * @param link ResolvedSourceBuilder of the imported module.
     */
    @NonNullByDefault
    final void resolveImport(final Import dependency, final ResolvedSourceBuilder<?> link) {
        if (!(link instanceof ForModule module)) {
            throw new VerifyException(
                "Attempted to resolve import " + dependency + " with non-module " + link);
        }
        ensureBuilderOpened();
        imports.put(dependency, module);
    }

    /**
     * Adds a {@link ResolvedSourceBuilder} of an included submodule.
     *
     * @param dependency the {@link Include} dependency being satisfied
     * @param link ResolvedSourceBuilder of the included submodule.
     */
    @NonNullByDefault
    final void resolveInclude(final Include dependency, final ResolvedSourceBuilder<?> link) {
        if (!(link instanceof ForSubmodule submodule)) {
            throw new VerifyException(
                "Attempted to resolve import " + dependency + " with non-submodule " + link);
        }
        ensureBuilderOpened();
        includes.put(dependency, submodule);
    }

    /**
     * Adds a {@link ResolvedSourceBuilder} of the parent module this submodule belongs to.
     *
     * @param dependency the {@link BelongsTo} being satistifed
     * @param link {@link ResolvedSourceBuilder} of the parent module.
     */
    @NonNullByDefault
    final void resolveBelongsTo(final BelongsTo dependency, final ResolvedSourceBuilder<?> link) {
        if (!(link instanceof ForModule module)) {
            throw new VerifyException(
                "Attempted to resolve belongs-to " + dependency + " with non-module " + link);
        }
        ensureBuilderOpened();
        resolveBelongsTo(dependency, module);
    }

    @NonNullByDefault
    abstract void resolveBelongsTo(BelongsTo dependency, ForModule module);

    /**
     * Builds a finalized {@link ResolvedSourceInfo} using the map of already-resolved sources.
     *
     * @param allResolved all the sources which were already resolved
     * @return ResolvedSourceInfo of this source
     */
    @NonNullByDefault
    final ResolvedSourceInfo build(final Map<SourceInfoRef, ResolvedSourceInfo> allResolved) {
        requireNonNull(allResolved);

        final var local = product;
        if (local != null) {
            return local;
        }
        final var result = buildProduct(resolveImports(allResolved), resolveIncludes());
        product = result;
        return result;
    }

    abstract @NonNull ResolvedSourceInfo buildProduct(@NonNull List<ResolvedImport> resolvedImports,
        @NonNull List<ResolvedInclude> resolveIncludes);

    private @NonNull List<ResolvedImport> resolveImports(final Map<SourceInfoRef, ResolvedSourceInfo> allResolved) {
        final var map = imports.build();
        final var result = new ArrayList<ResolvedImport>(map.size());

        for (var entry : map.entrySet()) {
            final var requirement = entry.getKey();
            final var importedModule = entry.getValue();

            final var impContext = importedModule.infoRef();
            final var resolved = allResolved.get(impContext);
            if (resolved == null) {
                // FIXME: better exception
                throw new IllegalStateException("Unresolved import %s of module %s".formatted(
                    importedModule.sourceId(), sourceId()));
            }

            final var resolvedRef = resolved.infoRef();
            if (!(resolvedRef instanceof SourceInfoRef.OfModule moduleRef)) {
                throw new VerifyException(
                    "Attempted to resolve import " + requirement + " with non-module " + resolvedRef);
            }

            result.add(new ResolvedImport(requirement, moduleRef.ref(), resolved.qnameModule()));
        }

        return result;
    }

    private @NonNull List<ResolvedInclude> resolveIncludes() {
        final var map = includes.build();
        final var result = new ArrayList<ResolvedInclude>(map.size());

        for (var entry : map.entrySet()) {
            final var builder = entry.getValue();
            result.add(new ResolvedInclude(entry.getKey(), builder.infoRef().ref(), builder.definingModule()));
        }

        return result;
    }

    abstract @NonNull QNameModule definingModule();

    private void ensureBuilderOpened() {
        final var local = product;
        if (local != null) {
            throw new VerifyException("Attempted to modify " + this + " with product " + local);
        }
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("ref", infoRef).toString();
    }
}
