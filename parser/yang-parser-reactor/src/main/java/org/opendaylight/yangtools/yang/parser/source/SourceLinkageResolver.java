/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static com.google.common.base.Verify.verify;

import com.google.common.base.VerifyException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.common.collect.TreeBasedTable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
    private final class LibrarySources {
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

        // exact match
        SourceInfoRef.@Nullable OfModule findModule(final Unqualified name, final Revision revision) {
            final var matching = modules.get(name, revision);
            return matching != null ? matching.getFirst() : null;
        }

        // wildcard: match latest revision. i.e. the first item
        SourceInfoRef.@Nullable OfModule findLatestModule(final Unqualified name) {
            final var matching = modules.row(name);
            return matching.isEmpty() ? null : matching.entrySet().iterator().next().getValue().getFirst();
        }

        // exact match
        SourceInfoRef.@Nullable OfSubmodule findSubmodule(final Unqualified parentName,
                final Unqualified name, final Revision revision) {
            final var matching = submodules.get(name, revision);
            if (matching != null) {
                for (var submodule : matching) {
                    if (parentName.equals(submodule.info().belongsTo().name())) {
                        return submodule;
                    }
                }
            }
            return null;
        }

        // wildcard: match latest revision that has the same parentName
        SourceInfoRef.@Nullable OfSubmodule findLatestSubmodule(final Unqualified parentName, final Unqualified name) {
            SourceInfoRef.@Nullable OfSubmodule found = null;
            for (var values : submodules.row(name).values()) {
                for (var submodule : values) {
                    if (parentName.equals(submodule.info().belongsTo().name())
                        && (found == null || Revision.compare(
                            found.ref().correctId().revision(), submodule.ref().correctId().revision()) < 0)) {
                        found = submodule;
                    }
                }
            }
            return found;
        }

        @Deprecated(forRemoval = true)
        void populateLegacyMaps() {
            for (var sources : modules.values()) {
                sources.forEach(SourceLinkageResolver.this::populateLegacyMaps);
            }
            for (var sources : submodules.values()) {
                sources.forEach(SourceLinkageResolver.this::populateLegacyMaps);
            }
        }
    }

    /**
     * Comparator to keep groups of modules with the same name ordered by their revision (latest first).
     * Comparator ordering {@link SourceIdentifier}s so that {@link SourceIdentifier#name()}s are encountered in their
     * natural order and the corresponding {@link SourceIdentifier#revision()}s are encountered in reverse order, i.e.
     * newest revision first.
     */
    @NonNullByDefault
    private static final Comparator<SourceIdentifier> BY_REVISION = (left, right) -> {
        final var cmp = left.name().compareTo(right.name());
        return cmp != 0 ? cmp
            // swapped argument order to reverse the comparison
            : Revision.compare(right.revision(), left.revision());
    };

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
    @NonNullByDefault
    private final Table<Unqualified, RevisionUnion, SourceInfoRef.@Nullable OfModule> modulesByName =
        Tables.<Unqualified, RevisionUnion, SourceInfoRef.@Nullable OfModule>newCustomTable(new HashMap<>(),
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
    private final HashMultimap<Unqualified, SourceInfoRef.OfSubmodule> submodulesByParentName = HashMultimap.create();

    /**
     * The set of library sources available to this resolver.
     */
    @NonNullByDefault
    private final LibrarySources libSources;

    // FIXME: eliminate this field: it is eagerly instantiated and should nicely decompose to requiredModules and
    //        requiredSubmodules noted above, but the SourceIdentifier as a key is making things hazy: what exactly
    //        is the SourceIdentifier? how does it work with conflicting submodules (and which lack a revision
    //        statement?
    private final Map<SourceIdentifier, SourceInfoRef> allSources = new HashMap<>();

    /**
     * Map of involved sources with the same name.
     */
    // FIXME: replace users of this map with lookups to modulesByName and submodulesByParentName
    private final @NonNull SortedSetMultimap<Unqualified, SourceIdentifier> involvedSourcesGrouped =
        Multimaps.newSortedSetMultimap(new HashMap<>(), () -> new TreeSet<>(BY_REVISION));

    /**
     * Map of involved sources ordered according to the resolution order (LinkedHashMap keeps the insertion order).
     */
    // FIXME: replace users of this map with lookups into requiredModules and requiredSubmodules
    private final Map<SourceIdentifier, ResolvedSourceBuilder<?>> involvedSourcesMap = new LinkedHashMap<>();

    // FIXME: this is state internal to resolveInvolvedSources(): reconcile its semantics:
    //          - is this a per-invocation thing?
    //          - it this a ResolvedSourceBuilder.ForSubmodule thing?
    //        in any case, we should not be operating on SourceIdentifier
    private final Map<SourceIdentifier, SourceIdentifier> submoduleToParentMap = new HashMap<>();

    /**
     * Map of submodules which include other submodules of the same parent module.
     */
    // FIXME: move this to ResolvedSourceBuilder<?>, eliminating one level of indirection -- improving lifecycle
    //        and perhaps making this a possible intermediate in ResolvedSourceBuilder.includes resolution process
    private final Map<ResolvedSourceBuilder<?>, Map<Include, SourceIdentifier>> unresolvedSiblingsMap = new HashMap<>();

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
    @NonNullByDefault
    public static List<ResolvedSourceInfo> resolveInvolvedSources(final Set<SourceInfoRef> mainSources,
        // TODO: Here we are receiving an eagerly-instantiated set of library sources, which runs contrary to what
        //       our primary user, SourceLinkageBuilder.build(), wants to do.
        //
        //       What we really want to have is a conversation around a set of initial sources and then have a
        //       method which returns a result similar to Resolution -- with List<ResolvedSourceInfo> being
        //       the terminal result indicating everything has been resolved.
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
            final Set<SourceInfoRef> libSources) throws ReactorException {
        if (mainSources.isEmpty()) {
            return List.of();
        }

        final var resolver = new SourceLinkageResolver(libSources);
        for (var source : mainSources) {
            switch (source) {
                case SourceInfoRef.OfModule module -> resolver.addRequiredModule(module);
                case SourceInfoRef.OfSubmodule submodule -> resolver.addRequiredSubmodule(submodule);
            }
            resolver.populateLegacyMaps(source);
        }
        return resolver.resolveInvolvedSources();
    }

    @NonNullByDefault
    private List<ResolvedSourceInfo> resolveInvolvedSources() throws ReactorException {
        // FIXME: the order of operations is wrong here:
        //          1) we populate 'allSources' with libSources
        //          2) we resolve belongs-to dependencies based on 'allSources', including those introduced in 1)
        //          3) we expand the set of required modules based on 2)
        //          4) we perform a single pass over required sources and resolve each recursively, populating auxiliary
        //             maps, e.g. 'involvedSourcesMap', as a side effect
        //
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
        // The approach we take here is to work in order of decreasing certainty and contribution to invariants:
        //   - include-by-revision
        //   - include-without-reviesion
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


        linkIncludes();

        libSources.populateLegacyMaps();

        // map all sources to their respective parents
        for (var source : allSources.values()) {
            if (source instanceof SourceInfoRef.OfSubmodule submodule) {
                final var submoduleInfo = submodule.info();
                final var belongsTo = submoduleInfo.belongsTo();
                final var parentName = belongsTo.name();
                // FIXME: YANGTOOLS-1896: why are we not using findAnyParent() here?
                final var parentId = findMappedParent(parentName);
                final var submoduleId = submoduleInfo.sourceId();
                if (parentId != null) {
                    submoduleToParentMap.put(submoduleId, parentId);
                    continue;
                }

                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, submoduleId,
                    new InferenceException(refOf(submoduleId, belongsTo.sourceRef()),
                        "Module %s from belongs-to was not found", parentName.getLocalName()));
            }
        }

        // ensures every required submodule has its parent module present in required modules as well
        for (var submodule : requiredSubmodules.keySet()) {
            final var sourceInfo = submodule.info();
            final var parentId = submoduleToParentMap.get(sourceInfo.sourceId());
            if (parentId == null) {
                throw new VerifyException(submodule + " does not have a parent");
            }
            final var parentCtx = allSources.get(parentId);
            if (parentCtx == null) {
                throw new VerifyException(submodule + " parent " + parentId + " not found");
            }
            if (!(parentCtx instanceof SourceInfoRef.OfModule parentModule)) {
                throw new VerifyException(submodule + " parent " + parentId + " resolved to non-module " + parentCtx);
            }

            // only add to requirements if it is not present
            if (!requiredModules.containsKey(parentModule)) {
                addRequiredModule(parentModule);
            }
        }

        // resolve imports and non-sibling includes for all required sources. Sibling includes are identified, but are
        // resolved later (at end of this method).
        for (var mainSource : Iterables.concat(requiredModules.keySet(), requiredSubmodules.keySet())) {
            final var rootId = mainSource.info().sourceId();

            // FIXME: This requires the mainSource to complete resolution once we start and in order to achieve that
            //        we track its unresolved dependencies locally, and try to resolve them first by pushing them to
            //        workChain.
            //
            //        This means that while we are iterating over mainSource, the actual resolution order does not match
            //        that order and we can encounter a source which was already resolved by having descended into all
            //        dependency.
            //
            //        This check ensures we skip such sources: the corresponding entry we are checking here is created
            //        in resolveDependencies() just after we have determined all dependencies are resolved.
            if (involvedSourcesMap.containsKey(rootId)) {
                continue;
            }

            // Sources already fully resolved
            final var visitedSources = new HashSet<SourceIdentifier>();

            // Sources currently being resolved (active dependency path)
            final var inProgress = new HashSet<SourceIdentifier>();

            // Sources that need processing
            final var workChain = new ArrayDeque<@Nullable SourceIdentifier>();
            workChain.add(rootId);

            while (true) {
                final var current = workChain.pollFirst();
                if (current == null) {
                    break;
                }
                if (visitedSources.contains(current)) {
                    continue;
                }

                inProgress.add(current);

                // acquire the source corresponding to 'current' and establish its dependencies
                final var currentSource = allSources.get(current);
                if (currentSource == null) {
                    throw new VerifyException("Cannot find source for " + current);
                }

                // try to resolve dependencies
                final var currentInfo = currentSource.info();
                final var resolved = new HashMap<SourceDependency, Unqualified>();
                final var unresolved = new LinkedHashSet<SourceIdentifier>();
                final var includedSiblings = new LinkedHashMap<Include, SourceIdentifier>();

                for (var dependency : currentInfo.imports()) {
                    final var imported = findImportedModule(dependency);
                    if (imported == null) {
                        throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, current,
                            new InferenceException(refOf(current, dependency.sourceRef()),
                                "Imported module %s was not found", dependency.name().getLocalName()));
                    }

                    // if the match was already resolved, just move on
                    final var importedId = imported.ref().correctId();
                    if (involvedSourcesMap.containsKey(importedId)) {
                        resolved.put(dependency, dependency.name());
                        continue;
                    }

                    // Dependency exists but was not fully resolved yet - mark as unresolved
                    unresolved.add(importedId);
                }

                for (var dependency : currentInfo.includes()) {
                    final var included = findIncludedSubmodule(currentSource, dependency);
                    if (included == null) {
                        throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, current,
                            new InferenceException(refOf(current, dependency.sourceRef()),
                                "Included submodule %s was not found", dependency.name().getLocalName()));
                    }

                    // if the match was already resolved, just move on
                    final var includedId = included.ref().correctId();
                    if (involvedSourcesMap.containsKey(includedId)) {
                        resolved.put(dependency, dependency.name());
                        continue;
                    }

                    if (currentSource instanceof SourceInfoRef.OfSubmodule submodule) {
                        // If this is an include of a sibling submodule, don't add it as unresolved dependency.
                        // It will be resolved later in a different way.
                        final var parent = findAnyParent(submodule);
                        if (parent != null) {
                            final var matchParent = findAnyParent(included);
                            if (matchParent != null && parent.equals(matchParent)) {
                                includedSiblings.put(dependency, includedId);
                                continue;
                            }
                        }
                    }

                    // Dependency exists but was not fully resolved yet - mark as unresolved
                    unresolved.add(includedId);
                }

                if (!unresolved.isEmpty()) {
                    // Need to process unresolved dependencies first.
                    // Requeue current so it gets another chance after it's dependencies get resolved.
                    workChain.addFirst(current);

                    for (var dep : unresolved) {
                        // Check circular dependency
                        if (inProgress.contains(dep)) {
                            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, current,
                                new InferenceException(current.toReference(),
                                    "Found circular dependency between modules %s and %s",
                                    current.name().getLocalName(), dep.name().getLocalName()));
                        }

                        // Not processed yet, add to queue
                        if (!visitedSources.contains(dep)) {
                            workChain.addFirst(dep);
                        }
                    }
                    continue;
                }

                // We have completely resolved this source: proceed to link it into internal state

                // FIXME: improve the population logic here: the nested lookup and population of
                //        'involvedSourcesGrouped' is quite counter-productive. Furthermore, if we could operate
                //        directly on current being SourceInfoRef, or better yet, on the builder itself, we should be
                //        able to reduce some of the trouble.
                final var newResolved = involvedSourcesMap.computeIfAbsent(current, key -> {
                    final var builder = switch (currentSource) {
                        case SourceInfoRef.OfModule infoRef -> new ResolvedSourceBuilder.ForModule(infoRef);
                        case SourceInfoRef.OfSubmodule infoRef -> new ResolvedSourceBuilder.ForSubmodule(infoRef);
                    };
                    involvedSourcesGrouped.put(key.name(), key);
                    return builder;
                });

                for (var resolvedDep : resolved.entrySet()) {
                    final var dep = resolvedDep.getKey();
                    // Find the best match for this dependency among the resolved modules
                    final var satisfiedDepId = findSatisfied(resolvedDep.getValue(), dep);
                    if (satisfiedDepId == null) {
                        throw new VerifyException("Failed to find " + resolvedDep.getValue() + " as " + dep);
                    }

                    final var depModule = involvedSourcesMap.get(satisfiedDepId);
                    final var currentVersion = newResolved.yangVersion();
                    final var dependencyVersion = depModule.yangVersion();

                    switch (dep) {
                        case BelongsTo dependency -> throw new VerifyException("Unexpected " + dependency);
                        case Import dependency -> {
                            // Version 1 sources must not import-by-revision Version 1.1 modules
                            if (dependency.revision() != null && currentVersion == YangVersion.VERSION_1
                                && dependencyVersion != YangVersion.VERSION_1) {
                                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, current,
                                    new YangVersionLinkageException(refOf(current, dependency.sourceRef()),
                                        "Cannot import by revision version %s module %s", dependencyVersion,
                                        resolvedDep.getValue().getLocalName()));
                            }
                            newResolved.resolveImport(dependency, depModule.infoRef());
                        }
                        case Include dependency -> {
                            if (currentVersion != dependencyVersion) {
                                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, current,
                                    new YangVersionLinkageException(refOf(current, dependency.sourceRef()),
                                        "Cannot include a version %s submodule %s in a version %s module %s",
                                        dependencyVersion, resolvedDep.getValue().getLocalName(), currentVersion,
                                        current.name().getLocalName()));
                            }
                            newResolved.resolveInclude(dependency, depModule.infoRef());
                        }
                    }
                }

                if (!includedSiblings.isEmpty()) {
                    // map all the included siblings of this submodule
                    unresolvedSiblingsMap.computeIfAbsent(newResolved, k -> new HashMap<>()).putAll(includedSiblings);
                }

                // Current was fully processed
                inProgress.remove(current);
                visitedSources.add(current);
            }
        }

        // iterate over submodules and link each of them to its resolved parent
        for (var entry : submoduleToParentMap.entrySet()) {
            final var submoduleId = entry.getKey();
            final var parentId = entry.getValue();

            final var resolvedSubmodule = involvedSourcesMap.get(submoduleId);
            if (resolvedSubmodule == null) {
                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, submoduleId,
                    new InferenceException(submoduleId.toReference(), "Submodule %s was not resolved", submoduleId));
            }

            // FIXME: ensure this through type safety
            final var submoduleInfo = (SourceInfo.Submodule) resolvedSubmodule.sourceInfo();
            final var resolvedParent = involvedSourcesMap.get(parentId);
            if (resolvedParent == null) {
                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, submoduleId,
                    new InferenceException(submoduleId.toReference(),
                        "Parent module %s of submodule %s was not resolved", parentId, submoduleId));
            }

            // double-check that the parent does satisfy this belongs-to
            final var belongsTo = submoduleInfo.belongsTo();
            // FIXME: better message and/or better exception
            verify(belongsTo.isSatisfiedBy(parentId));

            resolvedSubmodule.resolveBelongsTo(belongsTo, resolvedParent.infoRef());
        }

        // all other dependencies have been resolved: it is now time to deal with circular includes among submodules
        final var siblingEntries = unresolvedSiblingsMap.entrySet().iterator();
        while (siblingEntries.hasNext()) {
            final var siblingEntry = siblingEntries.next();
            final var resolvedSource = siblingEntry.getKey();
            final var siblings = siblingEntry.getValue();

            for (var includeEntry : siblings.entrySet()) {
                final var sibling = includeEntry.getValue();
                final var resolvedSibling = involvedSourcesMap.get(sibling);
                if (resolvedSibling == null) {
                    final var sourceId = resolvedSource.sourceId();
                    throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                        new InferenceException(sourceId.toReference(),
                            "Included submodule %s of module %s was not resolved", sibling, sourceId));
                }
                resolvedSource.resolveInclude(includeEntry.getKey(), resolvedSibling.infoRef());
            }
            siblingEntries.remove();
        }

        // we are all done: build the result
        // FIXME: just assert (or prove) there are no unresolved items and return a copy of a
        //        'List<ResolvedSourceInfo> resolvedSources' field, which is populated as soon as a
        //        ResolvedSourceBuilder is known to have been fully resolved: that is what the tail of
        //        resolveInvolvedSources() does and we want to return the result in the order in which the sources have
        //        been resolved.
        final var allResolved =
            LinkedHashMap.<SourceInfoRef, ResolvedSourceInfo>newLinkedHashMap(involvedSourcesMap.size());
        for (var involvedSource : involvedSourcesMap.values()) {
            allResolved.put(involvedSource.infoRef(), involvedSource.build());
        }
        return List.copyOf(allResolved.values());
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
     * <p>These work together to provide as much resolution accuracy as possible in face of overlapping submodule names,
     * while also ensuring that reasonably attempt to resolve one-to-one mappings, i.e. {@code include} in a module,
     * before we look at many-to-one mappings, i.e. {@code include} in a submodule.
     *
     * @throws ReactorException if a dependency cannot be resolved
     */
    private void linkIncludes() throws ReactorException {
        // FIXME: as per RFC6-020/RFC7950, section 7.1.6:
        //
        //          Multiple revisions of the same submodule MUST NOT be included.
        //
        //        that invariant should be enforced by SourceInfo.{Module,Submodule}

        // link all exact includes into their parent modules first, potentially expanding requiredSubmodules
        for (var parent : requiredModules.values()) {
            final var parentName = parent.infoRef().ref().correctId().name();
            for (var dependency : parent.missingIncludes()) {
                final var revision = dependency.revision();
                if (revision != null) {
                    // FIXME: this method touches library, which we do not want in this case
                    final var required = findSubmodule(parentName, dependency.name(), revision);
                    if (required != null) {
                        // FIXME: check yang-version consistency https://www.rfc-editor.org/info/rfc7950/#section-12:
                        //
                        //      A YANG version 1.1 module MUST NOT include a YANG version 1
                        //      submodule, and a YANG version 1 module MUST NOT include a YANG
                        //      version 1.1 submodule.

                        parent.resolveInclude(dependency, required);
                        continue;
                    }

                    // TODO: Consider better interaction with library: we know what yang-version we are looking for, so
                    //       the library should be able to contain two submodules with the same SourceIdentifier, with
                    //       belongs-to to the same module, differing in only on their yang-version.
                    //
                    //       This is a non-issue when a submodule is properly maintained with revisions, but can occur
                    //       in the wild if a conversion to YANG 1.1 is made without incrementing revision and the two
                    //       sources meet in a reactor.
                    //
                    //       We do not have an example of this happening in the wild, so it is a largely-theoretical
                    //       concern.
                    //
                    //       Before we invest resources into resolving this TODO, we should attempt to side-step this
                    //       issue by ensuring user-specific ways to ensure submodule names do not overlap -- such as
                    //       done by the ietf-yang-library model, etc.

                    final var fromLibrary = libSources.findSubmodule(parentName, dependency.name(), revision);
                    if (fromLibrary == null) {
                        // FIXME: better exception
                        throw new VerifyException(dependency + " not found in library");
                    }

                    addRequiredSubmodule(fromLibrary);
               }
            }
        }

        // link all exact includes into their submodules.
        for (var sibling : requiredSubmodules.values()) {
            // FIXME: essentially the same as above, but with a twist in that
            //        - we can expand the set of requiredSubmodules, in which case we need to re-start
            //        - we must skip over submodules which do not have their belongsTo resolved, but at the same time
            //        - have to be mindful of which exact includes we have skipped to understand whether at all, or how
            //          we should be handling inexact requirements that follow
        }

        // FIXME: inexact includes in modules?
        // FIXME: inexact includes in submodules?
    }

    @NonNullByDefault
    private void addRequiredModule(final SourceInfoRef.OfModule module) throws ReactorException {
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
            .putIfAbsent(RevisionUnion.of(sourceId.revision()), module);
        if (prevBySourceId != null) {
            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                new InferenceException(sourceId.toReference(),
                    "Module name collision: %s%s is already defined", sourceId.name(),
                    formatRevision(sourceId.revision())));
        }
    }

    @NonNullByDefault
    private static String formatRevision(final @Nullable Revision revision) {
        return revision == null ? "" : "@" + revision;
    }

    @NonNullByDefault
    private void addRequiredSubmodule(final SourceInfoRef.OfSubmodule submodule) {
        final var builder = new ResolvedSourceBuilder.ForSubmodule(submodule);
        if (requiredSubmodules.putIfAbsent(submodule, builder) != null) {
            throw new VerifyException("Attempted to add already-required " + submodule);
        }
        verify(submodulesByParentName.put(submodule.info().belongsTo().name(), submodule));
    }

    // FIXME: remove this method once we do not need the two maps
    @Deprecated(forRemoval = true)
    @NonNullByDefault
    private void populateLegacyMaps(final SourceInfoRef source) {
        allSources.putIfAbsent(source.ref().correctId(), source);
    }

    @NonNullByDefault
    private @Nullable SourceIdentifier findAnyParent(final SourceInfoRef.OfSubmodule submodule) {
        // FIXME: explain why we are looking at both involvedSourcesGrouped and then in requiredModules/librarySources
        final var belongsToName = submodule.info().belongsTo().name();
        final var satisfied = findParent(involvedSourcesGrouped, belongsToName);
        return satisfied != null ? satisfied : findMappedParent(belongsToName);
    }

    @NonNullByDefault
    private @Nullable SourceIdentifier findMappedParent(final Unqualified parentName) {
        final var module = findLatestModule(parentName);
        return module != null ? module.ref().correctId() : null;
    }

    private static @Nullable SourceIdentifier findParent(
            final @NonNull SortedSetMultimap<Unqualified, SourceIdentifier> map,
            final @NonNull Unqualified parentName) {
        final var match = map.get(parentName);
        return match.isEmpty() ? null : match.iterator().next();
    }

    // Find the best match (by revision) among all modules
    private SourceInfoRef.@Nullable OfModule findImportedModule(final @NonNull Import dependency) {
        final var revision = dependency.revision();
        return revision != null
            // import by revision: return exact match
            ? findModule(dependency.name(), revision)
            // import without revision: return latest available
            : findLatestModule(dependency.name());
    }

    @NonNullByDefault
    private SourceInfoRef.@Nullable OfModule findModule(final Unqualified name, final Revision revision) {
        final var required = modulesByName.get(name, revision);
        return required != null ? required : libSources.findModule(name, revision);
    }

    @NonNullByDefault
    private SourceInfoRef.@Nullable OfModule findLatestModule(final Unqualified name) {
        final var matching = modulesByName.row(name).values().iterator();
        return matching.hasNext() ? matching.next() : libSources.findLatestModule(name);
    }

    // Find the best match (by revision) among all submodules
    private SourceInfoRef.@Nullable OfSubmodule findIncludedSubmodule(final @NonNull SourceInfoRef source,
            final @NonNull Include dependency) {
        final var moduleName = switch (source) {
            case SourceInfoRef.OfModule module -> module.ref().correctId().name();
            case SourceInfoRef.OfSubmodule submodule -> submodule.info().belongsTo().name();
        };
        final var name = dependency.name();
        final var revision = dependency.revision();
        return revision != null ? findSubmodule(moduleName, name, revision) : findLatestSubmodule(moduleName, name);
    }

    private SourceInfoRef.@Nullable OfSubmodule findSubmodule(final @NonNull Unqualified moduleName,
            final @NonNull Unqualified name, final @NonNull Revision revision) {
        for (var submodule : submodulesByParentName.get(moduleName)) {
            final var sourceId = submodule.ref().correctId();
            if (name.equals(sourceId.name()) && revision.equals(sourceId.revision())) {
                return submodule;
            }
        }
        return libSources.findSubmodule(moduleName, name, revision);
    }

    private SourceInfoRef.@Nullable OfSubmodule findLatestSubmodule(final @NonNull Unqualified moduleName,
            final @NonNull Unqualified name) {
        SourceInfoRef.@Nullable OfSubmodule found = null;
        for (var submodule : submodulesByParentName.get(moduleName)) {
            final var sourceId = submodule.ref().correctId();
            if (name.equals(sourceId.name())
                && (found == null || Revision.compare(found.ref().correctId().revision(), sourceId.revision()) < 0)) {
                found = submodule;
            }
        }
        return found != null ? found : libSources.findLatestSubmodule(moduleName, name);
    }

    private @Nullable SourceIdentifier findSatisfied(final @NonNull Unqualified key,
            @NonNull final SourceDependency dependency) {
        for (var candidate : involvedSourcesGrouped.get(key)) {
            if (dependency.isSatisfiedBy(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    @NonNullByDefault
    private static StatementSourceReference refOf(final SourceIdentifier sourceId,
            final @Nullable StatementSourceReference sourceRef) {
        return sourceRef != null ? sourceRef : sourceId.toReference();
    }
}
