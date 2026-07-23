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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.common.collect.TreeBasedTable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.RevisionUnion;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.YangVersionLinkageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Identifies and organizes the main sources and library sources used to build the SchemaContext.
 * Any library sources that aren’t referenced are skipped. The referenced sources are cross-checked for consistency
 * and linked together according to their dependencies (includes, imports, belongs-to)
 */
public final class SourceLinkageResolver {
    /**
     * The set of library sources available within a resolution attempt. Split out of the outer class for state/logic
     * isolation.
     */
    @NonNullByDefault
    private static final class LibrarySources {
        // note: rows are using their natural order
        // note: columns are ordered to encounter latest revision first
        // note: values are ordered by their encounter order -- we really should have only one item in each
        //       inner list -- as the first item naturally dominates others, but that is something we can think
        //       about later, as all logic contained in this class should reside in SourceLinkageBuilder anyway
        //       and there the laziness also implies parsing a library source and dealing with SourceIdentifier
        //       normalization et al.
        private final Table<Unqualified, RevisionUnion, List<SourceInfoRef.OfModule>> modules;
        private final Table<Unqualified, RevisionUnion, List<SourceInfoRef.OfSubmodule>> submodules;

        LibrarySources(final Set<SourceInfoRef> sources) {
            modules = indexSources(sources, SourceInfoRef.OfModule.class);
            submodules = indexSources(sources, SourceInfoRef.OfSubmodule.class);
        }

        private static <R extends SourceInfoRef> Table<Unqualified, RevisionUnion, List<R>> indexSources(
                final Set<SourceInfoRef> sources, final Class<R> refClass) {
            // filter relevant sources and group them by their SourceIdentifier, retaining encounter source order
            final var tmp = ArrayListMultimap.<SourceIdentifier, R>create();
            for (var source : sources) {
                if (refClass.isInstance(source)) {
                    tmp.put(source.ref().correctId(), refClass.cast(source));
                }
            }

            if (tmp.isEmpty()) {
                // empty sources: nothing else to do
                return ImmutableTable.of();
            }

            // decompose SourceIdentifier into Unqualified/RevisionUnion and ensure the retained list is sized
            // to precisely the number elements it contains
            final var table = TreeBasedTable.<Unqualified, RevisionUnion, List<R>>create(Comparator.naturalOrder(),
                Comparator.reverseOrder());
            for (var entry : tmp.asMap().entrySet()) {
                final var sourceId = entry.getKey();
                table.put(sourceId.name(), RevisionUnion.of(sourceId.revision()), new ArrayList<>(entry.getValue()));
            }
            return table;
        }

        /**
         * Look up and remove a module source with specified name and revision.
         *
         * @param name module name
         * @param revision module revision
         * @return matching source or {@code null} if not found
         */
        SourceInfoRef.@Nullable OfModule takeModule(final Unqualified name, final RevisionUnion revision) {
            final var matching = modules.get(name, revision);
            return matching != null ? matching.removeFirst() : null;
        }

        /**
         * Look up and remove a module source with specified name and latest available revision.
         *
         * @param name module name
         * @return matching source or {@code null} if not found
         */
        SourceInfoRef.@Nullable OfModule takeLatestModule(final Unqualified name) {
            final var matching = modules.row(name);
            if (matching.isEmpty()) {
                return null;
            }
            // modules' columns have reverse order of revision, so latest revision is encountered first
            final var it = matching.entrySet().iterator();
            final var list = it.next().getValue();
            final var ret = list.removeFirst();
            if (list.isEmpty()) {
                it.remove();
            }
            return ret;
        }

        /**
         * Look up and remove a module source with specified name and latest available revision that is later than
         * the specified revision.
         *
         * @param name module name
         * @param revision module revision
         * @return matching source or {@code null} if not found
         */
        SourceInfoRef.@Nullable OfModule takeLatestModule(final Unqualified name, final RevisionUnion revision) {
            // modules' columns have reverse order of revision -- so latest revision is encountered first. Just check
            // if it is newer that the specified one
            final var it = modules.row(name).entrySet().iterator();
            if (it.hasNext()) {
                final var entry = it.next();
                if (revision.compareTo(entry.getKey()) < 0) {
                    final var list = entry.getValue();
                    final var ret = list.removeFirst();
                    if (list.isEmpty()) {
                        it.remove();
                    }
                    return ret;
                }
            }
            return null;
        }

        /**
         * Look up and remove a submodule source with specified name and revision that {@code belongs-to} to the
         * specified parent.
         *
         * @param parentName parent module name
         * @param name submodule name
         * @param revision submodule revision
         * @return matching source or {@code null} if not found
         */
        SourceInfoRef.@Nullable OfSubmodule takeSubmodule(final Unqualified parentName, final Unqualified name,
                final RevisionUnion revision) {
            final var matching = submodules.get(name, revision);
            if (matching != null) {
                final var it = matching.iterator();
                while (it.hasNext()) {
                    final var submodule = it.next();
                    if (parentName.equals(submodule.info().belongsTo().name())) {
                        it.remove();
                        return submodule;
                    }
                }
            }
            return null;
        }

        /**
         * Look up and remove a module submodule with specified name and latest available revision that
         * {@code belongs-to} to the specified parent.
         *
         * @param parentName parent module name
         * @param name module name
         * @return matching source or {@code null} if not found
         */
        SourceInfoRef.@Nullable OfSubmodule takeLatestSubmodule(final Unqualified parentName, final Unqualified name) {
            final var row = submodules.row(name);
            if (row.isEmpty()) {
                return null;
            }

            SourceInfoRef.@Nullable OfSubmodule found = null;
            @Nullable RevisionUnion foundRevision = null;
            for (var values : row.values()) {
                for (var submodule : values) {
                    if (parentName.equals(submodule.info().belongsTo().name())) {
                        final var revision = RevisionUnion.of(submodule.ref().correctId().revision());
                        if (foundRevision == null || foundRevision.compareTo(revision) < 0) {
                            found = submodule;
                            foundRevision = revision;
                        }
                    }
                }
            }
            if (found != null) {
                final var list = verifyNotNull(row.get(foundRevision));
                verify(list.remove(found));
                if (list.isEmpty()) {
                    verify(row.remove(foundRevision, list));
                }
            }
            return found;
        }
    }

    /**
     * The result of an attempt to link an {@link Include} dependency.
     */
    private enum SubmoduleOrigin {
        /**
         * Submodule was not linked.
         */
        NONE,
        /**
         * Submodule was brought in from the library.
         */
        LIBRARY,
        /**
         * Submodule was found memoized in the module.
         */
        MODULE,
        /**
         * Submodule was found among required submodules.
         */
        REQUIRED,
    }

    /**
     * Details about the origin for a request to promote a module.
     */
    @NonNullByDefault
    private record ModulePromotion(SourceIdentifier sourceId, Import dependency) {
        ModulePromotion {
            requireNonNull(sourceId);
            requireNonNull(dependency);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SourceLinkageResolver.class);

    /**
     * The set of required module sources. We are using insertion order to ensure predictable ordering.
     */
    private final LinkedHashMap<SourceInfoRef.OfModule, ResolvedSourceBuilder.ForModule> requiredModules =
        new LinkedHashMap<>();
    /**
     * The set of required submodule sources. We are using insertion order to ensure predictable ordering.
     */
    private final LinkedHashMap<SourceInfoRef.OfSubmodule, ResolvedSourceBuilder.ForSubmodule> requiredSubmodules =
        new LinkedHashMap<>();

    // As per RFC6020, every import-by-revision has to resolve to the same module. We are using a table, as that also
    // allows us quickly find all modules with the same name -- and have them ordered with latest revision first.
    private final @NonNull Table<Unqualified, RevisionUnion, ResolvedSourceBuilder.ForModule> modulesByName =
        Tables.<Unqualified, RevisionUnion, ResolvedSourceBuilder.ForModule>newCustomTable(new HashMap<>(),
            () -> new TreeMap<>(Comparator.reverseOrder()));

    // Our implementation constraints are looser than RFC6020/RFC7895/RFC7950/RFC8525 in that each module can be
    // implemented with multiple revisions, as long as each XMLNamespace/Revision combination is introduced by exactly
    // one source.
    @NonNullByDefault
    private final HashMap<QNameModule, SourceInfoRef.@Nullable OfModule> modulesByNamespace = new HashMap<>();

    /**
     * The set of required submodule sources, indexed by the name of the module they claim to belong to.
     */
    @NonNullByDefault
    private final HashMultimap<Unqualified, ResolvedSourceBuilder.ForSubmodule> submodulesByParentName =
        HashMultimap.create();
    /**
     * Index of latest module revisions. Populated lazily during {@link #linkInexactImports()}.
     */
    private final HashMap<Unqualified, ResolvedSourceBuilder.ForModule> latestModules = new HashMap<>();

    @NonNullByDefault
    private final LibrarySources libSources;

    @NonNullByDefault
    private SourceLinkageResolver(final Set<SourceInfoRef> libSources) {
        this.libSources = new LibrarySources(libSources);
    }

    /**
     * Processes all main sources and library sources. Finds out which ones are involved and resolves dependencies
     * between them.
     *
     * @param mainSources sources used as the base for the Schema Context. All of them have to be resolved and included
     *                    in the output of the {@link SourceLinkageResolver}
     * @param libSources dependencies of the main sources, as well as other library sources. Unreferenced (unused)
     *                   sources will be omitted from the output of the {@link SourceLinkageResolver}
     * @return list of resolved sources
     * @throws ReactorException if the source files couldn't be loaded or parsed
     */
    // TODO: Here we are receiving an eagerly-instantiated set of library sources, which runs contrary to what our
    //       primary user, SourceLinkageBuilder.build(), wants to do.
    //
    //       What we really want to have is a conversation around a set of initial sources and then have a method which
    //       returns a result similar to Resolution -- with List<ResolvedSourceInfo> being the terminal result
    //       indicating everything has been resolved.
    //
    //       The most notable aspect of that is that the protocol used to find modules and submodules becomes more
    //       visible: it will no longer be an implementation detail of SourceLinkageResolver, but becomes something
    //       implemented as a component interaction, in MVC speak:
    //       - SourceLinkageResolver is the model
    //       - SourceLinkageBuilder is both the controller and the user
    //       - the ADT equivalent of Resolution returned by the replacement for this method is the view
    //
    //       The algorithm continues to invoke the replacement method, e.g. continueResolution(), until:
    //       - a solution is found such that all mainSources and their dependencies are resolved, in which case
    //         the result is returned (as noted above)
    //       - a semantic invariant violation is found, in which case a ReactorException is thrown
    //       - the method invocation fails to make forward progress, in which case a ReactorException is thrown
    //
    //       Once we go this route, though, there is another thing to consider: we are just throwing all mainSources
    //       into the reactor in an unstructured way and then let the resolution deal with the fallout. See the note
    //       below to see what we should do first.
    @NonNullByDefault
    public static List<ResolvedSourceInfo> resolveInvolvedSources(final Set<SourceInfoRef> mainSources,
            final Set<SourceInfoRef> libSources) throws ReactorException {
        if (mainSources.isEmpty()) {
            return List.of();
        }

        final var resolver = new SourceLinkageResolver(libSources);

        // FIXME: Here we are unceremoniously dumping required sources into the resolver and have it pick up the pieces,
        //        which can mean it has to deal with ambiguities that could be avoided.
        //
        //        With the above note in mind, we can increase reliability by being mindful here
        for (var source : mainSources) {
            switch (source) {
                case SourceInfoRef.OfModule module -> resolver.addRequiredModule(module);
                case SourceInfoRef.OfSubmodule submodule -> resolver.addRequiredSubmodule(submodule);
            }
        }

        return resolver.resolveInvolvedSources();
    }

    @NonNullByDefault
    private List<ResolvedSourceInfo> resolveInvolvedSources() throws ReactorException {
        // What we need to achieve consistent linkage along three axis:
        //   - imported module
        //   - included submodule
        //   - parent module
        // such that the set of requiredModules and requiredSubmodules, as populated at the entry into this method, is
        // completely resolved.
        // If any linkage is found to be unsatisfied, we need to consult libSources to find the minimal set of sources
        // that result in such linkage.
        //
        // In order this, we need to resolve the following five cases:
        //   - import-by-revision, which is an exact match
        //   - import-without-revision, which is a wildcard match
        //   - include-by-revision, which is an exact match
        //   - include-without-revision, which is a wildcard match
        //   - belongs-to, which is a wildcard match
        //
        // YANG leaves the semantics of resolving import-without-revision and include-without-revision in presence of
        // multiple candidates undefined, but we define them as resolving to the latest revision available. Notably we
        // do not consider newer versions in libSources unless they are explicitly made required.
        //
        // Furthermore YANG semantics of belongs-to statement does not provide any guidance in case of multiple
        // revisions being involved, but it specifies that the mapping must be consistent with include statement.
        //
        // This is a graph theory problem: for a given a set of vertices (i.e. required sources), and edges (derived
        // from include, import and belongs-to statements), we need to find:
        // - the set of edges that result in all sources having their dependencies satisfied, and if no solution exists,
        // - the minimal set of library sources that need to promoted to required to make that happen
        //
        // The approach we take here is an approximation of the hypothetical graph solving algorithm with the explicit
        // acknowledgement that linking in face of multiple revisions of modules and, more importantly, submodules is
        // hard, but exceedingly rare in practice.
        //
        // What we do here is to work in order of decreasing certainty and contribution to invariants:
        //   - include-by-revision
        //   - include-without-revision
        //   - import-by-revision
        //   - import-without-revision
        //   - belongs-to
        // so that their invariants are established in this order. If a step ends up introducing new invariants to a
        // previous step, we restart that step. For example, if a libSource satisfying an import-by-revision introduces
        // new include-by-revision dependencies, we restart the algorithm.
        //
        // This also means that import-without-revision and include-without-revision resolution can naturally happen
        // multiple times. For import-without-revision subsequent resolution result must have a newer revision. For
        // include-without-revision the situation is somewhat more complicated, as explained next.
        //
        // The most problematic is belongs-to, as it is inherently inaccurate, but impacts the set of required modules
        // and constrains the set of sources which can satisfy include dependencies. We deal with this by resolving
        // belongs-to when a submodule is pulled into a source -- which resolves most cases.

        while (true) {
            boolean progressed = false;

            // attempt to link exact includes, either if they have explicit revisions or while we can establish them
            // through narrowing
            do {
                if (linkExactIncludes()) {
                    progressed = true;
                }
            } while (narrowInexactIncludes());

            // Attempt to link exact imports mentioned in all required sources, but keep in mind that some submodules
            // might still not be linked at this point and we do not want to make our problem worse. Visit all required
            // sources and link them to already-required modules, collecting all module revisions that need to be
            // promoted from the library.
            final var missingModules = TreeBasedTable.<Unqualified, Revision, ModulePromotion>create(
                Comparator.naturalOrder(), Comparator.reverseOrder());
            for (var source : requiredModules.values()) {
                if (linkExactImports(missingModules, source, source)) {
                    progressed = true;
                }
            }
            for (var source : requiredSubmodules.values()) {
                final var parent = source.parent();
                if (parent != null && linkExactImports(missingModules, parent, source)) {
                    progressed = true;
                }
            }
            if (!missingModules.isEmpty()) {
                // We are missing some sources to resolve exact imports, which may or may not contribute to submodule
                // linkage. First try to promote those that do and restart.
                var restart = false;
                for (var rowEntry : missingModules.rowMap().entrySet()) {
                    final var moduleName = rowEntry.getKey();
                    if (submodulesByParentName.get(moduleName).stream()
                            .anyMatch(submodule -> submodule.parent() == null)) {
                        for (var columnEntry : rowEntry.getValue().entrySet()) {
                            promoteModule(moduleName, columnEntry.getKey(), columnEntry.getValue());
                        }
                        restart = true;
                    }
                }
                if (restart) {
                    continue;
                }

                // Try to promote modules, but be careful to not promote multiple revisions with submodules, as that may
                // be us painting ourselves into a corner.
                for (var rowEntry : missingModules.rowMap().entrySet()) {
                    final var moduleName = rowEntry.getKey();
                    for (var columnEntry : rowEntry.getValue().entrySet()) {
                        final var module = promoteModule(moduleName, columnEntry.getKey(), columnEntry.getValue());
                        // do not add another revision if the promoted module has includes so as not to create a new
                        // belongs-to ambiguity
                        if (!module.sourceInfo().includes().isEmpty()) {
                            break;
                        }
                    }
                }

                // We have expanded requiredModules, which may require include/import resolution: restart
                continue;
            }

            // ensure all required submodules have their 'belongs-to' dependency satisfied, restarting if needed
            if (linkBelongsTo()) {
                continue;
            }

            // ensure all import-without-revision are resolved, restarting if needed
            if (linkInexactImports()) {
                continue;
            }

            // pass through all sources searching for an unresolved source
            final var it = streamRequiredSources().filter(source -> !source.isResolved()).iterator();
            if (!it.hasNext()) {
                // all sources have been resolved: proceed to build result
                break;
            }

            // we have some unresolved sources: report an error if we have not made progress
            if (progressed) {
                // ... but we have made some progress: make another round
                continue;
            }

            final var first = it.next();
            final var cause = newNoProgressException(first);
            while (it.hasNext()) {
                cause.addSuppressed(newNoProgressException(it.next()));
            }
            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, first.sourceId(), cause);
        }

        return streamRequiredSources().map(ResolvedSourceBuilder::build).toList();
    }

    @NonNullByDefault
    private Stream<ResolvedSourceBuilder<?>> streamRequiredSources() {
        return Stream.concat(requiredModules.values().stream(), requiredSubmodules.values().stream());
    }

    @NonNullByDefault
    private static InferenceException newNoProgressException(final ResolvedSourceBuilder<?> builder) {
        final var sb = new StringBuilder("No linkage progress while linking ").append(builder.humanName());

        appendDependencies(sb, "imports", builder.missingImports());
        appendDependencies(sb, "includes", builder.missingIncludes());
        if (builder instanceof ResolvedSourceBuilder.ForSubmodule submodule && submodule.parent() == null) {
            sb.append(" belongs-to ").append(submodule.parentName().getLocalName());
        }

        return new InferenceException(sb.toString(), builder.sourceId().toReference());
    }

    @NonNullByDefault
    private static void appendDependencies(final StringBuilder sb, final String name,
            final Iterator<? extends SourceDependency> it) {
        if (it.hasNext()) {
            appendDependency(sb.append(' ').append(name).append(" ["), it.next());
            if (it.hasNext()) {
                do {
                    appendDependency(sb.append(", "), it.next());
                } while (it.hasNext());
            }
            sb.append(']');
        }
    }

    @NonNullByDefault
    private static void appendDependency(final StringBuilder sb, final SourceDependency dependency) {
        sb.append(dependency.name().getLocalName());
        final var revision = dependency.revision();
        if (revision != null) {
            sb.append('@').append(revision);
        }
    }

    /**
     * Link as many {@link Include} dependencies required by {@link #requiredModules} and {@link #requiredSubmodules} as
     * possible, potentially expanding {@link #requiredSubmodules} to satisfy them.
     *
     * <p>The algorithm employed here has a couple of peculiarities:
     * <ol>
     *   <li>the act of linking an {@link Include} dependency to a submodule also links that submodule's
     *       {@link BelongsTo} dependency to the implied parent module</li>
     *   <li>a submodule's dependencies are considered only after it has been linked, directly or indirectly, to its
     *       parent module</li>
     *   <li>exact dependencies are considered before inexact</li>
     * </ol>
     *
     * <p>These work together to provide as much resolution accuracy as possible in face of overlapping submodule names,
     * while also ensuring that reasonably attempt to resolve one-to-one mappings, i.e. {@code include} in a module,
     * before we look at many-to-one mappings, i.e. {@code include} in a submodule.
     *
     * @return {@code true} if forward progress was made, {@code false} otherwise
     * @throws ReactorException if a dependency cannot be resolved
     */
    private boolean linkExactIncludes() throws ReactorException {
        boolean progressed = false;

        // the both loops affect each other, so we may end up repeat the them multiple times
        while (true) {
            // link all exact includes into their parent modules first, potentially expanding requiredSubmodules
            for (var parent : requiredModules.values()) {
                final var it = parent.missingIncludes();
                while (it.hasNext()) {
                    if (linkExactInclude(parent, parent, it.next()) != SubmoduleOrigin.NONE) {
                        progressed = true;
                    }
                }
            }

            // link all exact includes into their submodules, potentially expanding them
            boolean unmodified = true;
            for (var source : List.copyOf(requiredSubmodules.values())) {
                final var parent = source.parent();
                if (parent != null) {
                    final var it = source.missingIncludes();
                    while (it.hasNext()) {
                        switch (linkExactInclude(parent, source, it.next())) {
                            case null -> throw new NullPointerException();
                            case NONE -> {
                                // no-op
                            }
                            case MODULE, REQUIRED -> progressed = true;
                            case LIBRARY -> {
                                progressed = true;
                                unmodified = false;
                            }
                        }
                    }
                }
            }

            // linking a submodule can cause the specification of another submodule to become exact, in which case we
            // want to redo the two steps again
            if (unmodified) {
                return progressed;
            }
        }
    }

    @NonNullByDefault
    private SubmoduleOrigin linkExactInclude(final ResolvedSourceBuilder.ForModule module,
            final ResolvedSourceBuilder<?> source, final Include dependency) throws ReactorException {
        // parent module tracks submodule revision requirements coming in transitively from included submodules, dealing
        // with the following case:
        //
        //   module foo {
        //     include bar { revision-date 1970-01-02; }
        //     include baz;
        //   }
        //
        //   submodule bar {
        //     belongs-to foo;
        //     revision 1970-01-01;
        //     include baz { revision-date 1970-01-01; }
        //   }
        //
        //   submodule baz {
        //     belongs-to foo;
        //     revision 1970-01-02;
        //   }
        //
        // where the we can sharpen foo's include dependency to require the revision specified in baz
        final var name = dependency.name();
        final var existing = module.lookupSubmodule(name);
        if (existing != null) {
            // the submodule is already linked, just resolve the dependency in the source
            source.resolveInclude(dependency, existing);
            return SubmoduleOrigin.MODULE;
        }

        final var revision = module.lookupRevision(name);
        if (revision == null) {
            // the submodule revision is not know yet, skip it
            return SubmoduleOrigin.NONE;
        }

        final var parentName = module.name();
        final SubmoduleOrigin result;
        var submodule = lookupSubmodule(parentName, name, revision);
        if (submodule == null) {
            // TODO: Consider better interaction with library: we know what yang-version we are looking for, hence
            //       the library should be able to contain two submodules with the same SourceIdentifier,
            //       with belongs-to to the same module, differing in only on their yang-version.
            //
            //       This is a non-issue when a submodule is properly maintained with revisions, but can occur
            //       in the wild if a conversion to YANG 1.1 is made without incrementing revision and the two
            //       sources meet in a reactor.
            //
            //       We do not have an example of this happening in the wild, so it is a largely-theoretical concern.
            //
            //       Before we invest resources into resolving this TODO, we should attempt to side-step this issue
            //       by ensuring user-specific ways to ensure submodule names do not overlap -- such as done by
            //       the ietf-yang-library model, etc.
            final var fromLibrary = libSources.takeSubmodule(module.name(), name, revision);
            if (fromLibrary == null) {
                final var sourceId = source.sourceId();
                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                    // FIXME: 16.0.0: include revision
                    new InferenceException(refOf(sourceId, dependency), "Included submodule %s was not found",
                        name.getLocalName()));
            }
            submodule = addRequiredSubmodule(fromLibrary);
            result = SubmoduleOrigin.LIBRARY;
        } else {
            result = SubmoduleOrigin.REQUIRED;
        }

        // As per https://www.rfc-editor.org/info/rfc7950/#section-12:
        //
        //      A YANG version 1.1 module MUST NOT include a YANG version 1
        //      submodule, and a YANG version 1 module MUST NOT include a YANG
        //      version 1.1 submodule.
        //
        final var moduleVersion = module.yangVersion();
        final var submoduleVersion = submodule.yangVersion();
        if (moduleVersion != submoduleVersion) {
            final var sourceId = source.sourceId();
            final var depRef = dependency.sourceRef();
            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                new YangVersionLinkageException(depRef != null ? depRef : sourceId.toReference(),
                    "Cannot include a version %s submodule %s in a version %s module %s",
                    submoduleVersion, submodule.humanName(), moduleVersion, module.humanName()));
        }

        // order of operations has implications on error reporting:
        // - source will see its include dependency resolved
        // - submodule will see its belongs-to dependency resolved
        // - module will memoize the result for reuse
        source.resolveInclude(dependency, submodule);
        submodule.resolveBelongsTo(module);
        module.resolveSubmodule(submodule);
        return result;
    }

    /**
     * Try to find one or more {@link Include} dependencies reachable from {@link #requiredModules} and try to narrow
     * them to a specific revision.
     *
     * @return {@code true} if at least one dependency has been narrowed, {@code false} otherwise
     * @throws ReactorException if a dependency cannot be narrowed to a resolvable source
     */
    private boolean narrowInexactIncludes() throws ReactorException {
        // determine which submodules have a required module referring to it inexactly
        final var modulesBySubmodule = LinkedHashMultimap.<Unqualified, ResolvedSourceBuilder.ForModule>create();
        for (var module : requiredModules.values()) {
            for (var submodule : module.inexactSubmodules()) {
                modulesBySubmodule.put(submodule, module);
            }
        }
        if (modulesBySubmodule.isEmpty()) {
            // no module has known inexact includes: there is nothing to do
            return false;
        }

        // Our original resolution implementation treated inexact includes the same way we treat inexact imports and
        // picked the latest revision available including the library. That just does not work when multiple revisions
        // of modules with submodules are present: each distinct module should be picking its revision.
        // FIXME: Yeah, but that can break really easily as module and submodule revision evolution is independent, so
        //        even if we have two revisions of a module, there might legally be only one revision (and thus source)
        //        of the submodule.
        //        So the one-to-one relationship we are looking for here may not be attainable. That would leave us in
        //        quite a pickle, as the modules could be including different revisions of other submodules, etc., which
        //        means a SourceInfoRef.Submodule results in two distinct resolutions, which in turn implies the source
        //        will enter BuildGlobalContext twice, which is problematic at least from the perspective of error
        //        reporting.
        //        Original implementation did not have this problem, but then it did not pay attention to submodule
        //        revision consistency in face of mismatched exact and inexact includes from different submodules
        //        meeting in a module...

        int narrowedIncludes = 0;

        // First pass: unambiguous 1:1 relationships
        // we copy the entries to isolate iteration from removal
        for (var entry : List.copyOf(modulesBySubmodule.asMap().entrySet())) {
            final var submoduleName = entry.getKey();
            final var modules = entry.getValue();
            switch (modules.size()) {
                case 0 -> throw new VerifyException("Empty modules for " + submoduleName.getLocalName());
                case 1 -> {
                    // submodule is required only by a single required module: see if there is a single unlinked
                    // required submodule which would satisfy the dependency
                    final var it = modules.iterator();
                    if (narrowInexactInclude(it.next(), submoduleName)) {
                        it.remove();
                        narrowedIncludes++;
                    }
                }
                default -> {
                    // submodule is required by multiple modules: process those with unique name and try to match each
                    // to a single unlinked required submodule
                    final var tmp = ArrayListMultimap.<Unqualified, ResolvedSourceBuilder.ForModule>create();
                    for (var module : modules) {
                        tmp.put(module.name(), module);
                    }
                    for (var siblings : Multimaps.asMap(tmp).values()) {
                        if (siblings.size() == 1) {
                            final var module = siblings.getFirst();
                            if (narrowInexactInclude(module, submoduleName)) {
                                verify(modules.remove(module));
                                narrowedIncludes++;
                            }
                        }
                    }
                }
            }
        }
        if (narrowedIncludes != 0) {
            LOG.debug("Narrowed {} unambiguous include requirements, {} requires remain", narrowedIncludes,
                modulesBySubmodule.size());
            return true;
        }

        // FIXME: Consider all submodules which have multiple revisions and the number of modules dependending on
        //        each of them matches the number of revisions we have for that submodule. For any such combination
        //        order both by revision and assign them in order: module with oldest revision gets the submodule
        //        with oldest revision. etc.

        LOG.trace("Remaining inexact {}", modulesBySubmodule);
        return false;
    }

    @NonNullByDefault
    private boolean narrowInexactInclude(final ResolvedSourceBuilder.ForModule module, final Unqualified submoduleName)
            throws ReactorException {
        // long-winded way of extracting an only unlinked candidate with matching a belongs-to matching the module
        final var candidates = submodulesByParentName.get(module.name()).stream()
            .filter(submodule -> submodule.parent() == null && submoduleName.equals(submodule.name()))
            .iterator();
        final ResolvedSourceBuilder.ForSubmodule candidate;
        final String origin;
        if (candidates.hasNext()) {
            candidate = candidates.next();
            if (candidates.hasNext()) {
                // there is another candidate, skip
                return false;
            }
            origin = "required";
        } else {
            // No candidates: look into library for a match
            candidate = promoteLatestSubmodule(module, submoduleName);
            origin = "library";
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Narrowing {} include {} to {} {}", module.humanName(), submoduleName.getLocalName(), origin,
                candidate.humanName());
        }
        module.narrowInexact(submoduleName, RevisionUnion.of(candidate.revision()));
        return true;
    }

    @NonNullByDefault
    private boolean linkExactImports(final TreeBasedTable<Unqualified, Revision, ModulePromotion> missingModules,
            final ResolvedSourceBuilder.ForModule parent, final ResolvedSourceBuilder<?> source)
                throws ReactorException {
        var resolvedImports = 0;

        final var it = source.missingImports();
        while (it.hasNext()) {
            final var dependency = it.next();
            final var revision = dependency.revision();
            if (revision != null) {
                final var name = dependency.name();
                final var existing = modulesByName.get(name, revision);
                if (existing == null) {
                    // just add to missing
                    missingModules.row(name).putIfAbsent(revision, new ModulePromotion(source.sourceId(), dependency));
                    continue;
                }

                // Version 1 sources must not import-by-revision Version 1.1 modules
                final var depVersion = existing.yangVersion();
                if (source.yangVersion() == YangVersion.VERSION_1 && depVersion != YangVersion.VERSION_1) {
                    final var sourceId = source.sourceId();
                    throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                        new YangVersionLinkageException(refOf(sourceId, dependency),
                            "Cannot import by revision version %s module %s", depVersion,
                            // FIXME: 16.0.0: humanName()
                            existing.name().getLocalName()));
                }
                source.resolveImport(parent, dependency, existing);
                resolvedImports++;
            }
        }

        if (resolvedImports == 0) {
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolved {} imports by revision in {}", resolvedImports, source.humanName());
        }
        return true;
    }

    /**
     * Ensure all required submodules have their {@link BelongsTo} dependencies satisfied.
     *
     * @return {@code true} if the algorithm needs to restart
     * @throws ReactorException if a dependency cannot be resolved
     */
    private boolean linkBelongsTo() throws ReactorException {
        var restart = false;

        for (var entry : submodulesByParentName.asMap().entrySet()) {
            final var submodules = entry.getValue().stream()
                .filter(submodule -> submodule.parent() == null)
                .iterator();
            if (!submodules.hasNext()) {
                // all linked
                continue;
            }

            final var parentName = entry.getKey();
            final var first = submodules.next();
            final var modules = modulesByName.row(parentName);
            // we have one or more required submodules which was not claimed by a module, let's decide what to do based
            // on required modules
            final InferenceException cause;
            switch (modules.size()) {
                case 0 -> {
                    // there are no matching required modules: unlinked submodules must have been initially specified as
                    // required
                    if (submodules.hasNext()) {
                        // if there is more than one such submodule, we would still be in a pickle as, as we would have
                        // to select multiple parents to include
                        // TODO: perhaps we could do this when the total number of matching modules in library match
                        //       the number of submodules by bringing in all and restarting, but that requires
                        //       the corresponding include narrowing to work
                        final var sb = new StringBuilder("Cannot determine parent module ")
                            .append(parentName.getLocalName()).append(" assignment among submodules ")
                            .append(first.humanName());
                        do {
                            sb.append(", ").append(submodules.next().humanName());
                        } while (submodules.hasNext());

                        final var sourceInfo = first.sourceInfo();
                        cause = new InferenceException(sb.toString(), refOf(sourceInfo, sourceInfo.belongsTo()));
                    } else {
                        // a single required submodule: treat it as an import-without-revision and bring in the latest
                        // revision from library
                        final var fromLibrary = libSources.takeLatestModule(parentName);
                        if (fromLibrary != null) {
                            addRequiredModule(fromLibrary);
                            restart = true;
                            continue;
                        }

                        final var sourceInfo = first.sourceInfo();
                        cause = new InferenceException(refOf(sourceInfo, sourceInfo.belongsTo()),
                            // FIXME: 16.0.0: "Parent module %s was not found"
                            "Module %s from belongs-to was not found", parentName.getLocalName());
                    }
                }
                case 1 -> {
                    if (!submodules.hasNext()) {
                        // Legacy behaviour: if there is only a single module and a single submodule, assume the linkage
                        // is there, but warn about it.
                        // FIXME: 16.0.0: this is not consistent with (at least) YANG 1.1: it is an error for a module
                        //                to not include one of its submodules
                        final var module = modules.values().iterator().next();
                        final var versionSpecific = switch (module.yangVersion()) {
                            case VERSION_1 -> " or some of its submodules";
                            case VERSION_1_1 -> "";
                        };
                        LOG.warn("""
                            Submodule {} has belongs-to which is not matched by an include in module {}{}: treating \
                            leniently as if such an include were present. This will be a hard error in a future major \
                            release.""", first.humanName(), module.humanName(), versionSpecific);
                        first.resolveBelongsTo(module);
                        restart = true;
                        continue;
                    }
                    cause = newUnresolvedParentException(first, submodules, modules);
                }
                default -> cause = newUnresolvedParentException(first, submodules, modules);
            }
            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, first.sourceId(), cause);
        }

        return restart;
    }

    @NonNullByDefault
    private static InferenceException newUnresolvedParentException(final ResolvedSourceBuilder.ForSubmodule first,
            final Iterator<ResolvedSourceBuilder.ForSubmodule> others,
            final Map<RevisionUnion, ResolvedSourceBuilder.ForModule> modules) {
        // there are potentially-matching modules for each of the submodule(s), figure out a nice error
        final var ret = newUnresolvedParentException(first, modules);
        while (others.hasNext()) {
            ret.addSuppressed(newUnresolvedParentException(others.next(), modules));
        }
        return ret;
    }

    @NonNullByDefault
    private static InferenceException newUnresolvedParentException(final ResolvedSourceBuilder.ForSubmodule submodule,
            final Map<RevisionUnion, ResolvedSourceBuilder.ForModule> modules) {
        final var sourceInfo = submodule.sourceInfo();
        final var sourceId = sourceInfo.sourceId();
        final var name = sourceId.name();
        final var unlinkedModules = modules.values().stream()
            .filter(module -> module.lookupSubmodule(name) == null)
            .iterator();

        final var sb = new StringBuilder("Cannot determine parent module");
        if (unlinkedModules.hasNext()) {
            // one or more required modules do not have a corresponding 'include'
            final var firstUnlinked = unlinkedModules.next();
            if (unlinkedModules.hasNext()) {
                sb.append(" from among ").append(firstUnlinked.humanName());
                do {
                    sb.append(", ").append(unlinkedModules.next().humanName());
                } while (unlinkedModules.hasNext());
            } else {
                // FIXME: this should be 16.0.0, for now we should just link the source
                sb.append(": is ").append(firstUnlinked.humanName()).append(" missing corresponding 'include ")
                    .append(name.getLocalName());
                final var revision = sourceId.revision();
                if (revision != null) {
                    sb.append("{ revision-date ").append(revision).append("; }");
                }
                sb.append("' statement?");
            }
        } else {
            sb.append(": no unlinked modules named ").append(submodule.parentName().getLocalName()).append(" remain");
        }
        return new InferenceException(sb.toString(), refOf(sourceInfo, sourceInfo.belongsTo()));
    }

    /**
     * Link inexact imports to newest revision. We really want to cap the revision to requiredModules, but as soon as
     * there is one import that needs to be satisfied from library we are in a pickle, as that can promote later
     * revisions.
     *
     * @return {@code true} if a new module was introduced
     * @throws ReactorException if a dependency cannot be resolved
     */
    private boolean linkInexactImports() throws ReactorException {
        var loadedModule = false;

        for (var source : List.copyOf(requiredModules.values())) {
            if (linkInexactImports(source, source)) {
                loadedModule = true;
            }
        }
        for (var source : requiredSubmodules.values()) {
            final var parent = source.parent();
            if (parent != null && linkInexactImports(parent, source)) {
                loadedModule = true;
            }
        }

        return loadedModule;
    }

    @NonNullByDefault
    private boolean linkInexactImports(final ResolvedSourceBuilder.ForModule parentModule,
            final ResolvedSourceBuilder<?> source) throws ReactorException {
        var loadedModule = false;

        final var it = source.missingImports();
        while (it.hasNext()) {
            final var dependency = it.next();
            if (dependency.revision() != null) {
                continue;
            }

            final var name = dependency.name();
            final var cached = latestModules.get(name);
            if (cached != null) {
                source.resolveImport(parentModule, dependency, cached);
                continue;
            }

            final var allRequired = modulesByName.row(name);
            final var required = allRequired.isEmpty() ? null : allRequired.values().iterator().next();
            final ResolvedSourceBuilder.ForModule module;
            if (required == null) {
                // no match in required modules, promote from library or fail
                final var library = libSources.takeLatestModule(name);
                if (library == null) {
                    throw newModuleNotFoundException(source.sourceId(), dependency);
                }
                module = addRequiredModule(library);
                loadedModule = true;
            } else {
                // we have a match, let's see if the library has a newer one
                final var library = libSources.takeLatestModule(name, RevisionUnion.of(required.revision()));
                if (library != null) {
                    module = addRequiredModule(library);
                    loadedModule = true;
                } else {
                    module = required;
                }
            }

            latestModules.put(name, module);
            source.resolveImport(parentModule, dependency, module);
        }

        return loadedModule;
    }

    @NonNullByDefault
    private static ReactorException newModuleNotFoundException(final SourceIdentifier sourceId,
            final Import dependency) {
        return new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
            new InferenceException(refOf(sourceId, dependency), "Imported module %s was not found",
                // FIXME: 16.0.0: formatRevision(dependency.revision())
                dependency.name().getLocalName()));
    }

    @NonNullByDefault
    private ResolvedSourceBuilder.ForModule promoteModule(final Unqualified name, final RevisionUnion revision,
            final ModulePromotion origin) throws ReactorException {
        final var source = libSources.takeModule(name, revision);
        if (source == null) {
            throw newModuleNotFoundException(origin.sourceId, origin.dependency);
        }
        return addRequiredModule(source);
    }

    @NonNullByDefault
    private ResolvedSourceBuilder.ForModule addRequiredModule(final SourceInfoRef.OfModule module)
            throws ReactorException {
        final var builder = new ResolvedSourceBuilder.ForModule(module);
        if (requiredModules.putIfAbsent(module, builder) != null) {
            throw new VerifyException("Attempted to add already-required " + module);
        }

        final var sourceInfo = module.info();
        final var sourceId = sourceInfo.sourceId();
        final var namespace = sourceInfo.moduleName().getModule();

        // TODO: The exceptions here are less than perfect. We should not be reporting a combination of
        //       ReactorException + InferenceException, but rather a dedicated exception which identifies the two
        //       SourceInfoRefs involved and have SourceLinkageBuilder map them back to ReactorSource/BuildSource and
        //       their corresponding location

        final var prevByNamespace = modulesByNamespace.putIfAbsent(namespace, module);
        if (prevByNamespace != null) {
            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                new InferenceException(sourceId.toReference(),
                    "Module namespace collision: %s%s is already defined", namespace.namespace(),
                    formatRevision(namespace.revision())));
        }

        final var prevBySourceId = modulesByName.row(sourceId.name())
            .putIfAbsent(RevisionUnion.of(sourceId.revision()), builder);
        if (prevBySourceId != null) {
            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                new InferenceException(sourceId.toReference(),
                    "Module name collision: %s%s is already defined", sourceId.name(),
                    formatRevision(sourceId.revision())));
        }
        return builder;
    }

    @NonNullByDefault
    private ResolvedSourceBuilder.ForSubmodule promoteLatestSubmodule(final ResolvedSourceBuilder.ForModule module,
            final Unqualified name) throws ReactorException {
        final var moduleName = module.name();
        final var fromLibrary = libSources.takeLatestSubmodule(moduleName, name);
        if (fromLibrary != null) {
            return addRequiredSubmodule(fromLibrary);
        }
        throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, module.sourceId(),
            new InferenceException(newSubmoduleNotFoundMessage(moduleName, name), module.includeRefOf(name)));
    }

    // the library has nothing: discern the case:
    // - of there being no trace of the submodule,
    // - of us running out of unlinked submodules
    // - there being a mismatch between belongs-to and include
    @NonNullByDefault
    private String newSubmoduleNotFoundMessage(final Unqualified moduleName, final Unqualified name) {
        final var it = requiredSubmodules.values().stream().filter(source -> name.equals(source.name())).iterator();
        final var localName = name.getLocalName();
        if (!it.hasNext()) {
            return "Included submodule " + localName + " was not found";
        }

        final var sb = new StringBuilder("Required submodule ").append(localName).append(" cannot be satisfied by ");
        while (true) {
            final var submodule = it.next();
            sb.append(submodule.humanName()).append(" (").append(submoduleMismatchMessage(moduleName, submodule))
                .append(')');
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", nor by ");
        }
    }

    @NonNullByDefault
    private static String submoduleMismatchMessage(final Unqualified moduleName,
            final ResolvedSourceBuilder.ForSubmodule submodule) {
        final var parent = submodule.parent();
        if (parent != null) {
            return "included by " + parent.humanName();
        }

        final var parentName = submodule.parentName();
        if (moduleName.equals(parentName)) {
            throw new VerifyException(submodule.name() + " should have been narrowed to match " + submodule);
        }
        return "belongs-to " + parentName.getLocalName();
    }

    @NonNullByDefault
    private ResolvedSourceBuilder.ForSubmodule addRequiredSubmodule(final SourceInfoRef.OfSubmodule submodule) {
        final var builder = new ResolvedSourceBuilder.ForSubmodule(submodule);
        if (requiredSubmodules.putIfAbsent(submodule, builder) != null) {
            throw new VerifyException("Attempted to add already-required " + submodule);
        }
        verify(submodulesByParentName.put(submodule.info().belongsTo().name(), builder));
        return builder;
    }

    private ResolvedSourceBuilder.@Nullable ForSubmodule lookupSubmodule(final @NonNull Unqualified parentName,
            final @NonNull Unqualified name, final @NonNull RevisionUnion revision) {
        for (var submodule : submodulesByParentName.get(parentName)) {
            final var sourceId = submodule.infoRef().ref().correctId();
            if (name.equals(sourceId.name()) && Objects.equals(revision.revision(), sourceId.revision())) {
                return submodule;
            }
        }
        return null;
    }

    @NonNullByDefault
    private static String formatRevision(final @Nullable Revision revision) {
        return revision == null ? "" : "@" + revision;
    }

    @NonNullByDefault
    private static StatementSourceReference refOf(final SourceIdentifier sourceId, final SourceDependency dependency) {
        final var sourceRef = dependency.sourceRef();
        return sourceRef != null ? sourceRef : sourceId.toReference();
    }

    @NonNullByDefault
    private static StatementSourceReference refOf(final SourceInfo sourceInfo, final SourceDependency dependency) {
        final var sourceRef = dependency.sourceRef();
        return sourceRef != null ? sourceRef : sourceInfo.sourceId().toReference();
    }
}
