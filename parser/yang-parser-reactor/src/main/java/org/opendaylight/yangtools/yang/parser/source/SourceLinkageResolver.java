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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
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

    private final List<SourceInfoRef> mainSources = new ArrayList<>();
    private final List<SourceInfoRef> libSources = new ArrayList<>();

    private final Map<SourceIdentifier, SourceInfoRef> allSources = new HashMap<>();

    /**
     * Map of all sources with the same name. They are stored in a TreeSet with a Revision-Comparator which will keep
     * them ordered by Revision.
     */
    private final SortedSetMultimap<Unqualified, SourceIdentifier> allSourcesMapped =
        Multimaps.newSortedSetMultimap(new HashMap<>(), () -> new TreeSet<>(BY_REVISION));

    /**
     * Map of involved sources with the same name.
     */
    private final SortedSetMultimap<Unqualified, SourceIdentifier> involvedSourcesGrouped =
        Multimaps.newSortedSetMultimap(new HashMap<>(), () -> new TreeSet<>(BY_REVISION));

    /**
     * Map of involved sources ordered according to the resolution order (LinkedHashMap keeps the insertion order).
     */
    private final Map<SourceIdentifier, ResolvedSourceBuilder<?>> involvedSourcesMap = new LinkedHashMap<>();

    private final Map<SourceIdentifier, SourceIdentifier> submoduleToParentMap = new HashMap<>();

    /**
     * Map of submodules which include other submodules of the same parent module.
     */
    // FIXME: would a HashTable work better?
    private final Map<ResolvedSourceBuilder<?>, Map<Include, SourceIdentifier>> unresolvedSiblingsMap = new HashMap<>();

    @NonNullByDefault
    private SourceLinkageResolver(final Set<SourceInfoRef> withMainSources, final Set<SourceInfoRef> withLibSources) {
        mainSources.addAll(requireNonNull(withMainSources));
        libSources.addAll(requireNonNull(withLibSources));
    }

    /**
     * Processes all main sources and library sources. Finds out which ones are involved and resolves dependencies
     * between them.
     * @param mainSources sources used as the base for the Schema Context. All of them have to be resolved and included
     *                    in the output of the {@link SourceLinkageResolver}
     * @param libSources dependencies of the main sources, as well as other library sources. Unreferenced (unused)
     *                   sources will be omitted from the output of the {@link SourceLinkageResolver}
     * @return list of resolved sources
     * @throws SourceSyntaxException if the sources fail to provide the necessary {@link SourceInfo}
     * @throws ReactorException if the source files couldn't be loaded or parsed
     */
    @NonNullByDefault
    public static List<ResolvedSourceInfo> resolveInvolvedSources(final Set<SourceInfoRef> mainSources,
            final Set<SourceInfoRef> libSources) throws ReactorException, SourceSyntaxException {
        return mainSources.isEmpty() ? List.of()
            : new SourceLinkageResolver(mainSources, libSources).resolveInvolvedSources();
    }

    @NonNullByDefault
    private List<ResolvedSourceInfo> resolveInvolvedSources() throws ReactorException {
        mapSources(mainSources);
        mapSources(libSources);

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

        // process all currently-required sources
        for (var mainSource : mainSources) {
            tryResolveDependenciesOf(mainSource);
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
        // FIXME: this intermediate map should not be needed
        final var allResolved =
            LinkedHashMap.<SourceInfoRef, ResolvedSourceInfo>newLinkedHashMap(involvedSourcesMap.size());
        for (var involvedSource : involvedSourcesMap.values()) {
            allResolved.put(involvedSource.infoRef(), involvedSource.build());
        }
        return List.copyOf(allResolved.values());
    }

    private void mapSources(final Collection<SourceInfoRef> sources) {
        for (var source : sources) {
            final var sourceId = source.info().sourceId();

            // FIXME: verify no duplicates
            allSources.putIfAbsent(sourceId, source);

            allSourcesMapped.put(sourceId.name(), sourceId);
        }
    }

    /**
     * Resolves Imports and non-sibling includes. Includes of siblings are only identified here and will be resolved
     * separately.
     */
    private void tryResolveDependenciesOf(final SourceInfoRef root) throws SomeModifiersUnresolvedException {
        final var rootId = root.info().sourceId();
        if (involvedSourcesMap.containsKey(rootId)) {
            return;
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
