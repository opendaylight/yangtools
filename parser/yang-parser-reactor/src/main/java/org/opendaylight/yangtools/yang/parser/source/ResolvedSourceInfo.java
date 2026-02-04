/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Module;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Submodule;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedBelongsTo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;

/**
 * DTO containing all the linkage information which needs to be supplied to a RootStatementContext. This info will be
 * used to construct linkage substatements like imports, includes, belongs-to etc...
 */
@NonNullByDefault
public sealed interface ResolvedSourceInfo extends Immutable permits ResolvedModuleInfo, ResolvedSubmoduleInfo {
    /**
     * {@return the {@code SourceInfo} resolved by this info}
     */
    SourceInfo sourceInfo();

    Unqualified prefix();

    QNameModule qnameModule();

    List<ResolvedImport> imports();

    List<ResolvedInclude> includes();

    static Builder<?> newBuilder(final SourceInfo sourceInfo) {
        return switch (sourceInfo) {
            case SourceInfo.Module sim -> newBuilder(sim);
            case SourceInfo.Submodule sis -> newBuilder(sis);
        };
    }

    static ModuleBuilder newBuilder(final Module sourceInfo) {
        return new ModuleBuilder(sourceInfo);
    }

    static SubmoduleBuilder newBuilder(final Submodule sourceInfo) {
        return new SubmoduleBuilder(sourceInfo);
    }

    abstract sealed class Builder<I extends SourceInfo> implements Mutable permits ModuleBuilder, SubmoduleBuilder {
        // singleton object indicating an unresolved value
        static final Object UNRESOLVED = new Object();

        private final Map<Import, Object> imports;
        private final Map<Include, Object> includes;

        final @NonNull I sourceInfo;

        Builder(final I sourceInfo) {
            this.sourceInfo = requireNonNull(sourceInfo);

            final var sourceImports = sourceInfo.imports();
            final var importsSize = sourceImports.size();
            imports = switch (importsSize) {
                case 0 -> Map.of();
                default -> {
                    final var tmp = LinkedHashMap.<Import, Object>newLinkedHashMap(importsSize);
                    sourceImports.forEach(key -> tmp.put(key, UNRESOLVED));
                    yield tmp;
                }
            };

            final var sourceIncludes = sourceInfo.includes();
            final var includesSize = sourceIncludes.size();
            includes = switch (importsSize) {
                case 0 -> Map.of();
                default -> {
                    final var tmp = LinkedHashMap.<Include, Object>newLinkedHashMap(includesSize);
                    sourceIncludes.forEach(key -> tmp.put(key, UNRESOLVED));
                    yield tmp;
                }
            };
        }

        public final Set<Import> unresolvedImports() {
            return unresolvedView(imports);
        }

        public final Set<Include> unresolvedIncludes() {
            return unresolvedView(includes);
        }

        private static <T> Set<T> unresolvedView(final Map<T, ?> map) {
            return Collections.unmodifiableSet(Maps.filterValues(map, value -> value != UNRESOLVED).keySet());
        }

        public Builder<I> resolveImport(final ResolvedImport resolved) {
            resolve(imports, resolved, resolved.source());
            return this;
        }

        public Builder<I> resolveInclude(final ResolvedInclude resolved) {
            resolve(includes, resolved, resolved.source());
            return this;
        }

        private static <S extends SourceDependency, R extends ResolvedDependency> void resolve(
                final Map<S, Object> map, final R resolved, final S source) {
            map.compute(source, (key, value) -> {
                if (value == UNRESOLVED) {
                    return resolved;
                }
                throw new IllegalStateException(value == null
                    ? "Attempted to resolve unexpected " + key
                    : "Attempted to switch " + key + " from " + value + " to " + resolved);
            });
        }

        public abstract ResolvedSourceInfo build();

        final List<ResolvedImport> buildImports() {
            final var ret = new ArrayList<ResolvedImport>(imports.size());
            for (var entry : imports.entrySet()) {
                if (entry.getValue() instanceof ResolvedImport resolved) {
                    ret.add(resolved);
                }

                // FIXME: better exception
                throw new IllegalStateException("Unresolved import " + entry.getKey());
            }
            return ret;
        }

        final List<ResolvedInclude> buildIncludes() {
            final var ret = new ArrayList<ResolvedInclude>(includes.size());
            for (var entry : includes.entrySet()) {
                if (entry.getValue() instanceof ResolvedInclude resolved) {
                    ret.add(resolved);
                }

                // FIXME: better exception
                throw new IllegalStateException("Unresolved include " + entry.getKey());
            }
            return ret;
        }
    }

    final class ModuleBuilder extends Builder<Module> {
        ModuleBuilder(final Module sourceInfo) {
            super(sourceInfo);
        }

        @Override
        public ModuleBuilder resolveImport(final ResolvedImport resolved) {
            return (ModuleBuilder) super.resolveImport(resolved);
        }

        @Override
        public ModuleBuilder resolveInclude(final ResolvedInclude resolved) {
            return (ModuleBuilder) super.resolveInclude(resolved);
        }

        @Override
        public ResolvedModuleInfo build() {
            return new ResolvedModuleInfo(sourceInfo, buildImports(), buildIncludes());
        }
    }

    final class SubmoduleBuilder extends Builder<Submodule> {
        private Object belongsTo = UNRESOLVED;

        SubmoduleBuilder(final Submodule sourceInfo) {
            super(sourceInfo);
        }

        @Override
        public SubmoduleBuilder resolveImport(final ResolvedImport resolved) {
            return (SubmoduleBuilder) super.resolveImport(resolved);
        }

        @Override
        public SubmoduleBuilder resolveInclude(final ResolvedInclude resolved) {
            return (SubmoduleBuilder) super.resolveInclude(resolved);
        }

        public @Nullable BelongsTo unresolvedBelongsTo() {
            return belongsTo == UNRESOLVED ? sourceInfo.belongsTo() : null;
        }

        public SubmoduleBuilder resolveBelongsTo(final ResolvedBelongsTo resolved) {
            final var expected = sourceInfo.belongsTo();
            if (!expected.equals(resolved.source())) {
                throw new IllegalStateException("Attempted to resolve " + expected + " with " + resolved);
            }
            // TODO: tighten this by removing the identity check?
            if (belongsTo != UNRESOLVED) {
                throw new IllegalStateException(
                    "Attempted to switch " + expected + " from " + belongsTo + " to " + resolved);
            }

            belongsTo = resolved;
            return this;
        }

        @Override
        public ResolvedSubmoduleInfo build() {
            if (!(belongsTo instanceof ResolvedBelongsTo resolvedBelongsTo)) {
                throw new IllegalStateException("Unresolved belongs-to " + sourceInfo.belongsTo());
            }
            return new ResolvedSubmoduleInfo(sourceInfo, resolvedBelongsTo, buildImports(), buildIncludes());
        }
    }
}