/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SortedSetMultimap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceException;
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
     * The result of a single attempt to resolve dependencies of a {@link SourceInfoRef}.
     */
    private sealed interface Resolution {
        // marker interface
    }

    /**
     * A {@link Resolution} indicating a {@link SourceInfoRef} has been completely resolved and internal resolver state
     * updated to reflect that fact.
     */
    @NonNullByDefault
    private static final class CompleteResolution implements Resolution {
        static final CompleteResolution INSTANCE = new CompleteResolution();

        private CompleteResolution() {
            // hidden on purpose
        }
    }

    /**
     * A {@link Resolution} indicating that an attempt to resolve a {@link SourceInfoRef} has encountered dependencies
     * which it cannot resolve.
     *
     * @param unresolved dependencies that could not be resolved, guaranteed to contain distinct objects
     */
    @NonNullByDefault
    private record IncompleteResolution(List<SourceIdentifier> unresolved) implements Resolution {
        IncompleteResolution {
            requireNonNull(unresolved);
        }
    }

    /**
     * Comparator to keep groups of modules with the same name ordered by their revision (latest first).
     */
    private static final Comparator<SourceIdentifier> BY_REVISION = Comparator.comparing(
        SourceIdentifier::revision,
        Comparator.nullsLast(Revision::compareTo).reversed()
    );

    // FIXME: this field should be replaced by four fields:
    //        - LinkedHashSet<ResolvedSourceBuilder.ForModule> requiredModules
    //        - LinkedHashSet<ResolvedSourceBuilder.ForSubmodule> requiredSubmodules
    //        which track the fact a source is required while keeping modules separate from submodules
    //
    //        - HashMap<SourceIdentifier, ResolvedSourceBuilder.ForModule> modulesBySourceId
    //        - HashMap<QNameModule, ResolvedSourceBuilder.ForModule> modulesByNamespace
    //        which are derived from requiredModules by YANG semantics and our implementation constraints:
    //        - as per RFC6020 every import-by-revision has to resolve to the same module, and
    //        - as per our implementation constraint, XMLNamespace can be mapped multiple types via RevisionUnion (i.e.
    //          @Nullable Revision) as long such combination is introduced by a single module
    private final ArrayList<@NonNull SourceInfoRef> mainSources;
    // FIXME: eliminate this field: it is eagerly instantiated and should nicely decompose to requiredModules and
    //        requiredSubmodules noted above, but the SourceIdentifier as a key is making things hazy: what exactly
    //        is the SourceIdentifier? how does it work with conflicting submodules (and which lack a revision
    //        statement?
    private final Map<SourceIdentifier, SourceInfoRef> allSources = new HashMap<>();

    /**
     * Map of all sources with the same name. They are stored in a TreeSet with a Revision-Comparator which will keep
     * them ordered by Revision.
     */
    // FIXME: link directly to ResolvedSourceBuilder: this map exists to ensure we can find all mainSources items by
    //        their name when search for them
    // FIXME: overall it seems we want to differentiate the set for
    //        - modules, which cannot have conflicting SourceIdentifiers as implied by modulesBySourceId
    //        - submodules, which can have naming conflicts as long as the conflicting submodules belong to
    //          differently-named modules
    private final SortedSetMultimap<Unqualified, SourceIdentifier> allSourcesMapped =
        Multimaps.newSortedSetMultimap(new HashMap<>(), () -> new TreeSet<>(BY_REVISION));

    /**
     * Map of involved sources with the same name.
     */
    // FIXME: this seems to be similar to allSourcesMapped: how is it different in lieu of the associated FIXME?
    private final SortedSetMultimap<Unqualified, SourceIdentifier> involvedSourcesGrouped =
        Multimaps.newSortedSetMultimap(new HashMap<>(), () -> new TreeSet<>(BY_REVISION));

    /**
     * Map of involved sources ordered according to the resolution order (LinkedHashMap keeps the insertion order).
     */
    // FIXME: this field should be replaced with 'List<ResolvedSourceInfo> resolvedSources', which is populated as soon
    //        as a ResolvedSourceBuilder is known to have been fully resolved: that is what the tail of
    //        resolveInvolvedSources() does
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
    private SourceLinkageResolver(final Set<SourceInfoRef> mainSources) {
        this.mainSources = new ArrayList<>(mainSources.stream().map(Objects::requireNonNull).toList());
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

        return new SourceLinkageResolver(mainSources).resolveInvolvedSources(libSources);
    }

    private @NonNull List<@NonNull ResolvedSourceInfo> resolveInvolvedSources(
            final @NonNull Set<@NonNull SourceInfoRef> libSources) throws ReactorException {
        // FIXME: this is eager population of 'allSources' and 'allSourcesMapped':
        //        - processing of mainSources should happen in the constructor
        //        - libSources should be processed only once needed -- and this method should be the one to know when
        //          and how exactly that happens
        for (var source : mainSources) {
            addRequiredSource(source);
        }
        for (var source : libSources) {
            addRequiredSource(source);
        }

        // map all sources to their respective parents
        for (var source : allSources.values()) {
            if (source instanceof SourceInfoRef.OfSubmodule submodule) {
                final var submoduleInfo = submodule.info();
                final var belongsTo = submoduleInfo.belongsTo();
                final var parentName = belongsTo.name();
                final var parentId = findSatisfied(allSourcesMapped, parentName, belongsTo);
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

        // Ensures that every submodule in main-sources has it's parent module present among main-sources as well.
        // We iterate by offset to enable expansion of the mainSources list
        for (int i = 0; i < mainSources.size(); i++) {
            if (mainSources.get(i).info() instanceof SourceInfo.Submodule sourceInfo) {
                final var parentId = submoduleToParentMap.get(sourceInfo.sourceId());
                final var parentCtx = allSources.get(parentId);

                // Only add if not already in the list
                if (!mainSources.contains(parentCtx)) {
                    mainSources.add(parentCtx);
                }
            }
        }

        // resolve imports and non-sibling includes for all required sources. Sibling includes are identified, but are
        // resolved later (at end of this method).
        for (var mainSource : mainSources) {
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
            final var workChain = new ArrayDeque<SourceIdentifier>();
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

                final Resolution resolution;
                try {
                    resolution = resolveDependencies(currentSource);
                } catch (StatementSourceException e) {
                    throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, current, e);
                }

                switch (resolution) {
                    case CompleteResolution unused -> {
                        // Current was fully processed
                        inProgress.remove(current);
                        visitedSources.add(current);
                    }
                    case IncompleteResolution(var unresolvedDependencies) -> {
                        // Need to process unresolved dependencies first.
                        // Requeue current so it gets another chance after it's dependencies get resolved.
                        workChain.addFirst(current);

                        for (var dep : unresolvedDependencies) {
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
                    }
                }
            }
        }

        // iterate of submodules and link each of them to its resolved parent
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

            resolvedSubmodule.resolveBelongsTo(belongsTo, resolvedParent);
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
                resolvedSource.resolveInclude(includeEntry.getKey(), resolvedSibling);
            }
            siblingEntries.remove();
        }

        // we are all done: build the result
        // FIXME: just assert (or prove) there are no unresolved items and return a copy of resourcedSources (see above)
        final var allResolved =
            LinkedHashMap.<SourceInfoRef, ResolvedSourceInfo>newLinkedHashMap(involvedSourcesMap.size());
        for (var involvedSource : involvedSourcesMap.values()) {
            allResolved.put(involvedSource.infoRef(), involvedSource.build());
        }
        return List.copyOf(allResolved.values());
    }

    @NonNullByDefault
    private void addRequiredSource(final SourceInfoRef source) {
        switch (source) {
            case SourceInfoRef.OfModule module -> addRequiredModule(module);
            case SourceInfoRef.OfSubmodule submodule -> addRequiredSubmodule(submodule);
        }
    }

    @NonNullByDefault
    private void addRequiredModule(final SourceInfoRef.OfModule module) {
        // FIXME: populate
        //        - requiredModules
        //        - modulesByNamespace
        //        - modulesBySourceId
        populateLegacyMaps(module);
    }

    @NonNullByDefault
    private void addRequiredSubmodule(final SourceInfoRef.OfSubmodule submodule) {
        // FIXME: populate requiredSubmodules
        populateLegacyMaps(submodule);
    }

    // FIXME: remove this method once we do not need the two maps
    @NonNullByDefault
    private void populateLegacyMaps(final SourceInfoRef source) {
        final var sourceId = source.info().sourceId();
        allSources.putIfAbsent(sourceId, source);
        allSourcesMapped.put(sourceId.name(), sourceId);
    }

    /**
     * Resolve a source's dependencies.
     *
     * @param source the source to resolve
     * @return a dependency {@link Resolution}
     * @throws StatementSourceException if a resolution error occurs
     */
    @NonNullByDefault
    private Resolution resolveDependencies(final SourceInfoRef source) {
        final var currentInfo = source.info();
        final var sourceId = currentInfo.sourceId();
        final var dependencies = new LinkedHashSet<SourceDependency>();
        dependencies.addAll(currentInfo.imports());
        dependencies.addAll(currentInfo.includes());

        // try to resolve dependencies
        final var resolved = new HashMap<SourceDependency, Unqualified>();
        final var unresolved = new LinkedHashSet<SourceIdentifier>();
        final var includedSiblings = new LinkedHashMap<Include, SourceIdentifier>();

        for (var dependency : dependencies) {
            final var dependencyName = dependency.name();

            // Find the best match (by revision) among all modules
            final var match = findSatisfied(allSourcesMapped, dependencyName, dependency);
            if (match == null) {
                // Dependency is missing
                if (dependency instanceof Import) {
                    throw new InferenceException(refOf(sourceId, dependency.sourceRef()),
                        "Imported module %s was not found", dependencyName.getLocalName());
                }
                // FIXME: also handling of BelongsTo?
                throw new InferenceException(refOf(sourceId, dependency.sourceRef()),
                        "Included submodule %s was not found", dependencyName.getLocalName());
            }

            // if the match was already resolved, just move on
            if (involvedSourcesMap.containsKey(match)) {
                resolved.put(dependency, dependencyName);
                continue;
            }

            final var includedSibling = asIncludedSibling(sourceId, dependency, match);
            if (includedSibling != null) {
                // If this is an include of a sibling submodule, don't add it as unresolved dependency.
                // It will be resolved later in a different way.
                includedSiblings.put(includedSibling, match);
                continue;
            }

            // Dependency exists but was not fully resolved yet - mark as unresolved
            unresolved.add(match);
        }

        if (!unresolved.isEmpty()) {
            // We have unresolved dependencies: report them to caller
            return new IncompleteResolution(List.copyOf(unresolved));
        }

        // We have completely resolved this source: proceed to link it into internal state

        // FIXME: improve the population logic here: the nested lookup and population of 'involvedSourcesGrouped'
        //        is quite counter-productive. Furthermore, if we could operate directly on current being SourceInfoRef,
        //        or better yet, on the builder itself, we should be able to reduce some of the trouble.
        final var newResolved = involvedSourcesMap.computeIfAbsent(sourceId, key -> {
            final var builder = switch (source) {
                case SourceInfoRef.OfModule infoRef -> new ResolvedSourceBuilder.ForModule(infoRef);
                case SourceInfoRef.OfSubmodule infoRef -> new ResolvedSourceBuilder.ForSubmodule(infoRef);
            };
            involvedSourcesGrouped.put(key.name(), key);
            return builder;
        });

        for (var resolvedDep : resolved.entrySet()) {
            final var dep = resolvedDep.getKey();
            // Find the best match for this dependency among the resolved modules
            final var satisfiedDepId = findSatisfied(involvedSourcesGrouped, resolvedDep.getValue(), dep);
            if (satisfiedDepId == null) {
                throw new VerifyException("Failed to find " + resolvedDep.getValue() + " as " + dep);
            }

            final var depModule = involvedSourcesMap.get(satisfiedDepId);

            final var currentVersion = newResolved.yangVersion();
            final var dependencyVersion = depModule.yangVersion();

            switch (dep) {
                case Import importDep -> {
                    // Version 1 sources must not import-by-revision Version 1.1 modules
                    if (importDep.revision() != null && currentVersion == YangVersion.VERSION_1
                        && dependencyVersion != YangVersion.VERSION_1) {
                        throw new YangVersionLinkageException(refOf(sourceId, importDep.sourceRef()),
                            "Cannot import by revision version %s module %s", dependencyVersion,
                            resolvedDep.getValue().getLocalName());
                    }
                    newResolved.resolveImport(importDep, depModule);
                }
                case Include includeDep -> {
                    if (currentVersion != dependencyVersion) {
                        throw new YangVersionLinkageException(refOf(sourceId, dep.sourceRef()),
                            "Cannot include a version %s submodule %s in a version %s module %s",
                            dependencyVersion, resolvedDep.getValue().getLocalName(), currentVersion,
                            sourceId.name().getLocalName());
                    }
                    newResolved.resolveInclude(includeDep, depModule);
                }
                case BelongsTo belongsToDep -> {
                    // FIXME: verify() this never happens or document
                }
            }
        }

        if (!includedSiblings.isEmpty()) {
            // map all the included siblings of this submodule
            unresolvedSiblingsMap.computeIfAbsent(newResolved, k -> new HashMap<>()).putAll(includedSiblings);
        }

        return CompleteResolution.INSTANCE;
    }

    private Include asIncludedSibling(final SourceIdentifier current, final SourceDependency dependency,
            final SourceIdentifier dependencyId) {
        if (!(dependency instanceof Include sibling)) {
            return null;
        }

        final var currentParent = findSatisfyingParentForSubmodule(current);
        // FIXME: not needed if currentParent == null?
        final var theirParent = findSatisfyingParentForSubmodule(dependencyId);

        return currentParent != null && currentParent.equals(theirParent) ? sibling : null;
    }

    private @Nullable SourceIdentifier findSatisfyingParentForSubmodule(final SourceIdentifier submoduleId) {
        final var infoRef = allSources.get(requireNonNull(submoduleId));
        if (!(infoRef instanceof SourceInfoRef.OfSubmodule submoduleRef)) {
            return null;
        }

        final var submoduleBelongsTo = submoduleRef.info().belongsTo();
        final var satisfied = findSatisfied(involvedSourcesGrouped, submoduleBelongsTo.name(), submoduleBelongsTo);
        if (satisfied != null) {
            return satisfied;
        }
        return findSatisfied(allSourcesMapped, submoduleBelongsTo.name(), submoduleBelongsTo);
    }

    private static @Nullable SourceIdentifier findSatisfied(final SortedSetMultimap<Unqualified, SourceIdentifier> map,
            final Unqualified key, final SourceDependency dependency) {
        for (var candidate : map.get(key)) {
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
