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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

/**
 * The state required to construct a {@link ResolvedSourceInfo} for a particular {@link SourceInfoRef}. There should be
 * exactly one instance of this class for each {@link SourceInfoRef} in a particular {@link SourceLinkageResolver}
 * instance.
 *
 * <p>This class is an implementation detail of {@link SourceLinkageResolver} and is expected to be used in the context
 * of a single thread executing {@link SourceLinkageResolver#resolveInvolvedSources(Set, Set)}.
 */
abstract sealed class SourceLinker<R extends SourceInfoRef> extends ResolvedSourceInfo.Builder
        permits ModuleLinker, SubmoduleLinker {
    private final @NonNull R infoRef;

    @NonNullByDefault
    private DependencyLinker<Import, ModuleLinker> imports;
    @NonNullByDefault
    private DependencyLinker<Include, SubmoduleLinker> includes;

    @NonNullByDefault
    SourceLinker(final R infoRef) {
        this.infoRef = requireNonNull(infoRef);

        final var info = infoRef.info();
        imports = DependencyLinker.of(info.imports());
        includes = DependencyLinker.of(info.includes());
    }

    @Override
    final R infoRef() {
        return infoRef;
    }

    /**
     * {@return {@code true} if all dependencies specified in {@link #sourceInfo()} have been satisfied}
     */
    boolean isResolved() {
        return !missingImports().hasNext() && !missingIncludes().hasNext();
    }

    /**
     * {@return the set of {@link Import}s that remain unresolved}
     */
    @NonNullByDefault
    final Iterator<Import> missingImports() {
        return imports.missing();
    }

    /**
     * {@return the set of {@link Include}s that remain unresolved}
     */
    @NonNullByDefault
    final Iterator<Include> missingIncludes() {
        return includes.missing();
    }

    /**
     * Adds a {@link ModuleLinker} of an imported module.
     *
     * @param parentModule {@link ModuleLinker} of the parent module
     * @param dependency the {@link Import} being satisfied
     * @param target {@link ModuleLinker} of the imported module
     * @throws ReactorException if resolving the dependency to target would create a loop module import graph
     */
    @NonNullByDefault
    final void resolveImport(final ModuleLinker parentModule, final Import dependency, final ModuleLinker target)
            throws ReactorException {
        // check that target module does not import parentModule
        // FIXME: 16.0.0: different exception for the case of self-import
        final var path = target.equals(requireNonNull(parentModule)) ? List.of(target)
            : importPathOf(new HashSet<>(), target, parentModule);
        if (path != null) {
            final var sourceId = sourceId();
            final var depRef = dependency.sourceRef();
            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                 new InferenceException(depRef != null ? depRef : sourceId.toReference(),
                     // FIXME: 16.0.0: humanName() and exact path
                     "Found circular dependency between modules %s and %s",
                     sourceId.name().getLocalName(), target.name().getLocalName()));
        }

        final var resolved = imports.resolveMissing(dependency, target);
        if (resolved != null) {
            imports = resolved;
        }
    }

    /**
     * Check that specified module is not imported, directly or indirectly, by specified source.
     *
     * @param visited the set of sources which have already been visited
     * @param source the source
     * @param module the module
     * @return the reverse sequence of sources through which the specified module is imported, or {@code null} when it
     *         is not imported
     */
    private static @Nullable ArrayList<@NonNull SourceLinker<?>> importPathOf(
            final @NonNull HashSet<SourceLinker<?>> visited, final @NonNull SourceLinker<?> source,
            final @NonNull ModuleLinker module) {
        // only process a source if we have not visited it yet
        if (visited.add(source)) {
            final var impIt = source.imports.present();
            while (impIt.hasNext()) {
                final var target = impIt.next();
                if (target.equals(module)) {
                    final var ret = new ArrayList<SourceLinker<?>>();
                    ret.add(source);
                    return ret;
                }
                final var path = importPathOf(visited, target, module);
                if (path != null) {
                    path.add(source);
                    return path;
                }
            }
            final var incIt = source.includes.present();
            while (incIt.hasNext()) {
                final var path = importPathOf(visited, incIt.next(), module);
                if (path != null) {
                    path.add(source);
                    return path;
                }
            }
        }
        return null;
    }

    /**
     * Adds a {@link SubmoduleLinker} of an included submodule.
     *
     * @param dependency the {@link Include} dependency being satisfied
     * @param target {@link SubmoduleLinker} of the included submodule
     */
    @NonNullByDefault
    final void resolveInclude(final Include dependency, final SubmoduleLinker target) {
        // FIXME: YANG 1 submodules should enforce no circular includes
        final var resolved = includes.resolveMissing(dependency, requireNonNull(target));
        if (resolved != null) {
            includes = resolved;
        }
    }

    @Override
    final ResolvedSourceInfo build() {
        return doBuild(
            imports.buildResolved((requirement, target) -> {
                final var source = target.infoRef();
                return new ResolvedImport(requirement, source.ref(), source.info().moduleName().getModule());
            }),
            includes.buildResolved((requirement, target) -> new ResolvedInclude(requirement, target.infoRef().ref())));
    }

    @NonNullByDefault
    abstract ResolvedSourceInfo doBuild(List<ResolvedImport> resolvedImports, List<ResolvedInclude> resolveIncludes);

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("ref", infoRef()).toString();
    }
}
