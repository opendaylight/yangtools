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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * The state required to construct a {@link ResolvedSourceInfo} for a particular {@link SourceInfoRef}. There should be
 * exactly one instance of this class for each {@link SourceInfoRef} in a particular {@link SourceLinkageResolver}
 * instance.
 *
 * <p>This class is an implementation detail of {@link SourceLinkageResolver} and is expected to be used in the context
 * of a single thread executing {@link SourceLinkageResolver#resolveInvolvedSources(Set, Set)}.
 */
@NonNullByDefault
abstract sealed class SourceLinker<R extends SourceInfoRef, I extends ResolvedSourceInfo>
        extends ResolvedSourceInfo.Builder
        permits ResolvedSourceInfo.ModuleBuilder, ResolvedSourceInfo.SubmoduleBuilder {
    private final R infoRef;

    private DependencyLinker<Import, ModuleLinker> imports;
    private DependencyLinker<Include, SubmoduleLinker> includes;

    SourceLinker(final R infoRef) {
        this.infoRef = requireNonNull(infoRef);

        final var info = infoRef.info();
        imports = DependencyLinker.of(info.imports());
        includes = DependencyLinker.of(info.includes());
    }

    @Override
    public final R infoRef() {
        return infoRef;
    }

    @Override
    public final String humanName() {
        final var sourceId = sourceId();
        return humanName(sourceId.name(), sourceId.revision());
    }

    static final String humanName(final Unqualified name, final @Nullable Revision revision) {
        final var localName = name.getLocalName();
        return revision == null ? localName : localName + "@" + revision;
    }

    @Override
    public final Iterator<Import> missingImports() {
        return imports.missing();
    }

    @Override
    public final Iterator<Include> missingIncludes() {
        return includes.missing();
    }

    /**
     * {@return {@code true} if all dependencies specified in {@link #sourceInfo()} have been satisfied}
     */
    boolean isResolved() {
        return !missingImports().hasNext() && !missingIncludes().hasNext();
    }

    /**
     * Adds a {@link ModuleLinker} of an imported module.
     *
     * @param parentModule {@link ModuleLinker} of the parent module
     * @param dependency the {@link Import} being satisfied
     * @param target {@link ModuleLinker} of the imported module
     * @throws ReactorException if resolving the dependency to target would create a loop module import graph
     */
    final void resolveImport(final ModuleLinker parentModule, final Import dependency, final ModuleLinker target)
            throws ReactorException {
        if (target.equals(requireNonNull(parentModule)) && parentModule.equals(this)) {
            final var sourceId = sourceId();
            final var depRef = dependency.sourceRef();
            final var sourceRef = depRef != null ? depRef : sourceId.toReference();

            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                dependency.revision() != null
                    // import by revision: this is a bug in the module
                    ? new SourceException(sourceRef, "Module %s imports itself", humanName())
                    // import without revision: this happens to resolve this way
                    : new InferenceException(sourceRef, "Imported module %s resolves to itself",
                        dependency.name().getLocalName()));
        }

        // check that target module does not import parentModule
        final var path = importPathOf(new HashSet<>(), target, parentModule);
        if (path == null) {
            final var resolved = imports.resolveMissing(dependency, target);
            if (resolved != null) {
                imports = resolved;
            }
            return;
        }

        // there is an import path: make sure to print it
        final var sb = new StringBuilder("Module ").append(humanName()).append(" imports itself");
        for (var source : path.reversed()) {
            sb.append(" via ").append(source.humanName());
        }
        final var sourceId = sourceId();
        final var depRef = dependency.sourceRef();
        throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
            new InferenceException(sb.toString(), depRef != null ? depRef : sourceId.toReference()));
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
    private static @Nullable ArrayList<SourceLinker<?, ?>> importPathOf(final HashSet<SourceLinker<?, ?>> visited,
            final SourceLinker<?, ?> source, final ModuleLinker module) {
        // only process a source if we have not visited it yet
        if (visited.add(source)) {
            final var impIt = source.imports.present();
            while (impIt.hasNext()) {
                final var target = impIt.next();
                if (target.equals(module)) {
                    final var ret = new ArrayList<SourceLinker<?, ?>>();
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
    final void resolveInclude(final Include dependency, final SubmoduleLinker target) {
        // FIXME: YANG 1 submodules should enforce no circular includes
        final var resolved = includes.resolveMissing(dependency, requireNonNull(target));
        if (resolved != null) {
            includes = resolved;
        }
    }

    @Override
    public final I build() {
        return doBuild(
            imports.buildResolved((requirement, target) -> {
                final var source = target.infoRef();
                return new ResolvedImport(requirement, source.ref(), source.info().moduleName().getModule());
            }),
            includes.buildResolved((requirement, target) -> new ResolvedInclude(requirement, target.infoRef().ref())));
    }

    abstract I doBuild(List<ResolvedImport> imports, List<ResolvedInclude> includes);

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("ref", infoRef()).toString();
    }
}
