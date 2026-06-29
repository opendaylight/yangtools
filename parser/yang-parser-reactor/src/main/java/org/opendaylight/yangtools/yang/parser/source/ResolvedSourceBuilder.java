/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
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
abstract sealed class ResolvedSourceBuilder<R extends SourceInfoRef> {
    /**
     * A {@link ResolvedSourceBuilder} for a module.
     */
    static final class ForModule extends ResolvedSourceBuilder<SourceInfoRef.OfModule> {
        @NonNullByDefault
        ForModule(final SourceInfoRef.OfModule infoRef) {
            super(infoRef);
        }
    }

    /**
     * A {@link ResolvedSourceBuilder} for a submodule.
     */
    static final class ForSubmodule extends ResolvedSourceBuilder<SourceInfoRef.OfSubmodule> {
        @NonNullByDefault
        ForSubmodule(final SourceInfoRef.OfSubmodule infoRef) {
            super(infoRef);
        }
    }

    /**
     * A requirement to {@code belongs-to} a source.
     */
    @NonNullByDefault
    sealed interface BelongsToRequirement permits UnresolvedBelongsTo, ResolvedDependency.ResolvedBelongsTo {
        // Marker interface
    }

    /**
     * A requirement to import a source.
     */
    @NonNullByDefault
    sealed interface ImportRequirement permits UnresolvedImport, ResolvedDependency.ResolvedImport {
        // Marker interface
    }

    /**
     * A requirement to include a source.
     */
    @NonNullByDefault
    sealed interface IncludeRequirement permits UnresolvedInclude, ResolvedDependency.ResolvedInclude {
        // Marker interface
    }

    /**
     * A {@link BelongsToRequirement} that is not resolved.
     */
    private static final class UnresolvedBelongsTo implements BelongsToRequirement {
        static final UnresolvedBelongsTo INSTANCE = new UnresolvedBelongsTo();

        private UnresolvedBelongsTo() {
            // Hidden on purpose
        }
    }

    /**
     * An {@link ImportRequirement} that is not resolved.
     */
    private static final class UnresolvedImport implements ImportRequirement {
        static final UnresolvedImport INSTANCE = new UnresolvedImport();

        private UnresolvedImport() {
            // Hidden on purpose
        }
    }

    /**
     * An {@link IncludeRequirement} that is not resolved.
     */
    private static final class UnresolvedInclude implements IncludeRequirement {
        static final UnresolvedInclude INSTANCE = new UnresolvedInclude();

        private UnresolvedInclude() {
            // Hidden on purpose
        }
    }

    // these retain insertion order
    private final LinkedHashMap<Import, ImportRequirement> imports = new LinkedHashMap<>();
    private final LinkedHashMap<Include, IncludeRequirement> includes = new LinkedHashMap<>();
    private final @NonNull R infoRef;

    private BelongsToRequirement belongsTo = UnresolvedBelongsTo.INSTANCE;
    private ResolvedSourceInfo buildFinished;

    @NonNullByDefault
    private ResolvedSourceBuilder(final R infoRef) {
        this.infoRef = requireNonNull(infoRef);
        final var info = infoRef.info();
        for (var dep : info.imports()) {
            imports.put(dep, UnresolvedImport.INSTANCE);
        }
        for (var dep : info.includes()) {
            includes.put(dep, UnresolvedInclude.INSTANCE);
        }
    }

    final @NonNull R infoRef() {
        return infoRef;
    }

    final @NonNull SourceIdentifier sourceId() {
        return sourceInfo().sourceId();
    }

    final @NonNull SourceInfo sourceInfo() {
        return infoRef.info();
    }

    final @NonNull YangVersion yangVersion() {
        return sourceInfo().yangVersion();
    }

    /**
     * Adds a {@link ResolvedSourceBuilder} of an imported module.
     *
     * @param dependency the {@link Import} being satisfied
     * @param importedModule ResolvedSourceBuilder of the imported module.
     */
    @NonNullByDefault
    final void resolveImport(final Import dependency, final ResolvedSourceBuilder<?> importedModule) {
        if (!(importedModule instanceof ForModule forModule)) {
            throw new VerifyException("bad imported source " + importedModule);
        }
        ensureBuilderOpened();
        final var ref = forModule.infoRef();
        final var info = ref.info();
        final var resolved = new ResolvedImport(dependency, ref.ref(), info.moduleName().getModule());
        imports.replace(dependency, UnresolvedImport.INSTANCE, resolved);
    }

    /**
     * Adds a {@link ResolvedSourceBuilder} of an included submodule.
     *
     * @param dependency the {@link Include} dependency being satisfied
     * @param includedSubmodule ResolvedSourceBuilder of the included submodule.
     */
    @NonNullByDefault
    final void resolveInclude(final Include dependency, final ResolvedSourceBuilder<?> includedSubmodule) {
        ensureBuilderOpened();
        includes.put(dependency, includedSubmodule);
    }

    /**
     * Adds a {@link ResolvedSourceBuilder} of the parent module this submodule belongs to.
     *
     * @param dependency the {@link BelongsTo} being satistifed
     * @param belongsToModule {@link ResolvedSourceBuilder} of the parent module.
     */
    @NonNullByDefault
    final void resolveBelongsTo(final BelongsTo dependency, final ResolvedSourceBuilder<?> belongsToModule) {
        ensureBuilderOpened();
        belongsTo = new ResolvedBelongsTo(dependency, belongsToModule.infoRef.ref(),
            belongsToModule.resolveQnameModule());
    }

    /**
     * Builds a finalized {@link ResolvedSourceInfo} using the map of already-resolved sources.
     *
     * @param allResolved all the sources which were already resolved
     * @return ResolvedSourceInfo of this source
     */
    @NonNullByDefault
    final ResolvedSourceInfo build(final Map<SourceInfoRef, ResolvedSourceInfo> allResolved) {
        requireNonNull(allResolved);

        final var finished = buildFinished;
        if (finished != null) {
            return finished;
        }

        // TODO: for submodules this should be the 'belongsTo' prefix
        final var prefix = sourceInfo() instanceof SourceInfo.Module module ? module.prefix() : null;

        final var result = new ResolvedSourceInfo(infoRef, resolveQnameModule(), resolveImports(allResolved),
            resolveIncludes(), prefix, belongsTo);
        buildFinished = result;
        return result;
    }

    private List<ResolvedImport> resolveImports(final Map<SourceInfoRef, ResolvedSourceInfo> allResolved) {
        final var map = imports.build();
        final var result = new ArrayList<ResolvedImport>(map.size());

        for (var entry : map.entrySet()) {
            final var importedModule = entry.getValue();

            final var impContext = importedModule.infoRef();
            final var resolved = allResolved.get(impContext);
            if (resolved == null) {
                // FIXME: better exception
                throw new IllegalStateException("Unresolved import %s of module %s".formatted(
                    importedModule.sourceId(), sourceId()));
            }
            result.add(new ResolvedImport(entry.getKey(), resolved.infoRef().ref(), resolved.qnameModule()));
        }

        return result;
    }

    private List<ResolvedInclude> resolveIncludes() {
        final var map = includes.build();
        final var result = new ArrayList<ResolvedInclude>(map.size());

        for (var entry : map.entrySet()) {
            final var builder = entry.getValue();
            result.add(new ResolvedInclude(entry.getKey(), builder.infoRef().ref(), builder.resolveQnameModule()));
        }

        return result;
    }

    private QNameModule resolveQnameModule() {
        final var sourceInfo = sourceInfo();
        if (sourceInfo instanceof SourceInfo.Module moduleInfo) {
            return QNameModule.ofRevision(moduleInfo.namespace(), moduleInfo.latestRevision());
        }
        // Submodule's QNameModule is composed of parents Namespace + its own Revision (or null, if absent)
        verifyNotNull(belongsTo, "Cannot resolve QNameModule of a submodule %s. Missing belongs-to",
            sourceInfo.sourceId());
        return QNameModule.ofRevision(belongsTo.parentModuleQname().namespace(), sourceInfo.latestRevision());
    }

    private void ensureBuilderOpened() {
        verify(buildFinished == null, "Builder for source %s was already closed", sourceId());
    }
}
