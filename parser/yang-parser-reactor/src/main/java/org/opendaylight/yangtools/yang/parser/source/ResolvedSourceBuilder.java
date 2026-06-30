/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
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

    // these retain insertion order, but also allow nulls, which we use to validate resolution attempts
    private final @NonNull LinkedHashMap<@NonNull Import, ResolvedSourceBuilder.@Nullable ForModule> imports;
    private final @NonNull LinkedHashMap<@NonNull Include, ResolvedSourceBuilder.@Nullable ForSubmodule> includes;
    private final @NonNull R infoRef;

    private @Nullable ResolvedSourceInfo product;

    @NonNullByDefault
    private ResolvedSourceBuilder(final R infoRef) {
        this.infoRef = requireNonNull(infoRef);

        final var info = infoRef.info();
        imports = prepareDependencies(info.imports());
        includes = prepareDependencies(info.includes());
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
        resolveDependency(imports, dependency, module);
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
        resolveDependency(includes, dependency, submodule);
    }

    private static <K, V> void resolveDependency(final @NonNull LinkedHashMap<K, @Nullable V> map, final @NonNull K key,
            final @NonNull V value) {
        final var requirement = requireNonNull(key);
        final var resolved = requireNonNull(value);
        if (map.replace(requirement, null, resolved)) {
            // replace succeeded: this is the first resolution
            return;
        }

        // replace failed: the key is either non-existent or has already been resolved
        final var prev = map.get(requirement);
        throw prev == null ? new VerifyException("Attempted to resolve unspecified " + requirement)
            : new VerifyException("Attempted to override import " + requirement + " from " + prev + " to " + resolved);
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
     * @return ResolvedSourceInfo of this source
     */
    @NonNullByDefault
    final ResolvedSourceInfo build() {
        final var local = product;
        if (local != null) {
            return local;
        }
        final var result = buildProduct(
            resolveDependencies(imports, (requirement, builder) ->
                new ResolvedImport(requirement, builder.infoRef().ref(), builder.definingModule())),
            resolveDependencies(includes, (requirement, builder) ->
                new ResolvedInclude(requirement, builder.infoRef().ref(), builder.definingModule())));
        product = result;
        return result;
    }

    abstract @NonNull ResolvedSourceInfo buildProduct(@NonNull List<ResolvedImport> resolvedImports,
        @NonNull List<ResolvedInclude> resolveIncludes);

    abstract @NonNull QNameModule definingModule();

    private void ensureBuilderOpened() {
        final var local = product;
        if (local != null) {
            throw new VerifyException("Attemplted to modify " + this + " with product " + local);
        }
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("ref", infoRef).toString();
    }

    private static <K, V> @NonNull LinkedHashMap<@NonNull K, @Nullable V> prepareDependencies(
            final @NonNull Set<@NonNull K> keys) {
        final var ret = LinkedHashMap.<@NonNull K, @Nullable V>newLinkedHashMap(keys.size());
        for (var key : keys) {
            ret.put(requireNonNull(key), null);
        }
        return ret;
    }

    private static <K, V, R> @NonNull List<R> resolveDependencies(final LinkedHashMap<@NonNull K, @Nullable V> map,
            final @NonNull BiFunction<@NonNull K, @NonNull V, @NonNull R> function) {
        final var tmp = new ArrayList<R>(map.size());
        for (var entry : map.entrySet()) {
            final var dependency = entry.getKey();
            final var resolved = entry.getValue();
            if (resolved == null) {
                throw new VerifyException("Unresolved dependency " + dependency);
            }
            tmp.add(verifyNotNull(function.apply(dependency, resolved)));
        }
        return List.copyOf(tmp);
    }
}
