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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Submodule;
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
     * Comparator to keep groups of modules with the same name ordered by their revision (latest first).
     */
    private static final Comparator<SourceIdentifier> BY_REVISION = Comparator.comparing(
        SourceIdentifier::revision,
        Comparator.nullsLast(Revision::compareTo).reversed()
    );

    private final List<ReactorSource> mainSources = new ArrayList<>();
    private final List<ReactorSource> libSources = new ArrayList<>();

    private final Map<SourceIdentifier, ReactorSource> allSources = new HashMap<>();

    /**
     * Map of all sources with the same name. They are stored in a TreeSet with a Revision-Comparator which will keep
     * them ordered by Revision.
     */
    // TODO: would a SetMultimap work better?
    private final Map<Unqualified, Set<SourceIdentifier>> allSourcesMapped = new HashMap<>();

    /**
     * Map of involved sources with the same name.
     */
    // TODO: would a SetMultimap work better?
    private final Map<Unqualified, Set<SourceIdentifier>> involvedSourcesGrouped = new HashMap<>();
    /**
     * Map of involved sources ordered according to the resolution order (LinkedHashMap keeps the insertion order).
     */
    private final Map<SourceIdentifier, ResolvedSourceBuilder> involvedSourcesMap = new LinkedHashMap<>();

    private final Map<SourceIdentifier, SourceIdentifier> submoduleToParentMap = new HashMap<>();

    /**
     * Map of submodules which include other submodules of the same parent module.
     */
    // FIXME: would a HashTable work better?
    private final Map<ResolvedSourceBuilder, Map<Include, SourceIdentifier>> unresolvedSiblingsMap = new HashMap<>();

    @NonNullByDefault
    private SourceLinkageResolver(final Collection<ReactorSource> withMainSources,
            final Collection<ReactorSource> withLibSources) {
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
    public static Map<ReactorSource, ResolvedSourceInfo> resolveInvolvedSources(
            final Collection<ReactorSource> mainSources, final Collection<ReactorSource> libSources)
                throws ReactorException, SourceSyntaxException {
        return mainSources.isEmpty() ? Map.of()
            : new SourceLinkageResolver(mainSources, libSources).resolveInvolvedSources();
    }

    @NonNullByDefault
    private Map<ReactorSource, ResolvedSourceInfo> resolveInvolvedSources() throws ReactorException {
        mapSources(mainSources);
        mapSources(libSources);
        mapSubmodulesToParents();
        reuniteMainSubmodulesWithParents();

        tryResolveDependencies();
        tryResolveBelongsTo();
        tryResolveSiblings();

        final var allResolved = new LinkedHashMap<ReactorSource, ResolvedSourceInfo>(involvedSourcesMap.size());
        for (var involvedSource : involvedSourcesMap.entrySet()) {
            final var fullyResolved = involvedSource.getValue().build(allResolved);
            allResolved.put(involvedSource.getValue().reactorSource(), fullyResolved);
        }

        return allResolved;
    }

    private void mapSubmodulesToParents() throws SomeModifiersUnresolvedException {
        for (var source : allSources.values()) {
            if (source.sourceInfo() instanceof SourceInfo.Submodule submoduleInfo) {
                final var belongsTo = submoduleInfo.belongsTo();
                final var parentName = belongsTo.name();
                final var parentId = findSatisfied(allSourcesMapped.get(parentName), belongsTo);
                final var submoduleId = source.sourceId();
                if (parentId != null) {
                    submoduleToParentMap.put(submoduleId, parentId);
                    continue;
                }

                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, submoduleId,
                    new InferenceException(refOf(submoduleId, belongsTo.sourceRef()),
                        "Module %s from belongs-to was not found", parentName.getLocalName()));
            }
        }
    }

    private void mapSources(final Collection<ReactorSource> sources) {
        for (var source : sources) {
            final var sourceId = source.sourceId();

            // FIXME: verify no duplicates
            allSources.putIfAbsent(sourceId, source);
            final var allOfQname = allSourcesMapped.get(sourceId.name());
            if (allOfQname != null) {
                allOfQname.add(sourceId);
            } else {
                mapSource(sourceId);
            }
        }
    }

    private void mapSource(final SourceIdentifier sourceId) {
        final var name = sourceId.name();
        final var matches = allSourcesMapped.get(name);
        if (matches != null) {
            matches.add(sourceId);
            return;
        }

        allSourcesMapped.put(name, newMatchSetWith(sourceId));
    }

    private void tryResolveDependencies() throws ReactorException {
        for (var mainSource : mainSources) {
            tryResolveDependenciesOf(mainSource.sourceId());
        }
    }

    /**
     * Ensures that every submodule in main-sources has it's parent module present among main-sources as well.
     */
    private void reuniteMainSubmodulesWithParents() {
        // use classic for loop to enable expansion of the mainSources list
        for (int i = 0; i < mainSources.size(); i++) {
            final var sourceInfo = mainSources.get(i).sourceInfo();

            if (sourceInfo instanceof Submodule) {
                final var parentId = submoduleToParentMap.get(sourceInfo.sourceId());
                final var parentCtx = allSources.get(parentId);

                // Only add if not already in the list
                if (!mainSources.contains(parentCtx)) {
                    mainSources.add(parentCtx);
                }
            }
        }
    }

    /**
     * Resolves Imports and non-sibling includes. Includes of siblings are only identified here and will be resolved
     * separately.
     */
    private void tryResolveDependenciesOf(final SourceIdentifier rootId) throws SomeModifiersUnresolvedException {
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

        while (!workChain.isEmpty()) {
            final SourceIdentifier current = workChain.pollFirst();
            if (visitedSources.contains(current)) {
                continue;
            }

            inProgress.add(current);

            final var dependencies = getDependenciesOf(current);
            final var resolvedDependencies = new HashMap<SourceDependency, Unqualified>();
            final var unresolvedDependencies = new ArrayList<SourceIdentifier>();
            final var includedSiblings = new LinkedHashMap<Include, SourceIdentifier>();
            boolean allResolved = true;

            for (var dependency : dependencies) {
                final var dependencyName = dependency.name();

                // Find the best match (by revision) among all modules
                final var match = findSatisfied(findAmongAll(dependencyName), dependency);
                if (match == null) {
                    // Dependency is missing
                    if (dependency instanceof Import) {
                        throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, current,
                            new InferenceException(refOf(current, dependency.sourceRef()),
                                "Imported module %s was not found", dependencyName.getLocalName()));
                    }

                    // FIXME: also handling of BelongsTo?
                    throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, current,
                        new InferenceException(refOf(current, dependency.sourceRef()),
                            "Included submodule %s was not found", dependencyName.getLocalName()));
                }

                // if the match was already resolved, just move on
                if (involvedSourcesMap.containsKey(match)) {
                    resolvedDependencies.put(dependency, dependencyName);
                    continue;
                }

                final var includedSibling = asIncludedSibling(current, dependency, match);
                if (includedSibling != null) {
                    // If this is an include of a sibling submodule, don't add it as unresolved dependency.
                    // It will be resolved later in a different way.
                    includedSiblings.put(includedSibling, match);
                    continue;
                }

                // Dependency exists but was not fully resolved yet - mark as unresolved
                unresolvedDependencies.add(match);
                allResolved = false;
            }

            if (allResolved) {
                final var newResolved = addResolvedSource(current);

                for (var resolvedDep : resolvedDependencies.entrySet()) {
                    final var dep = resolvedDep.getKey();
                    // Find the best match for this dependency among the resolved modules
                    final var satisfiedDepId = requireNonNull(findSatisfied(
                        findAmongResolved(resolvedDep.getValue()), dep));
                    final var depModule = involvedSourcesMap.get(satisfiedDepId);

                    final var currentVersion = newResolved.yangVersion();
                    final var dependencyVersion = depModule.yangVersion();

                    switch (dep) {
                        case Import importDep -> {
                            // Version 1 sources must not import-by-revision Version 1.1 modules
                            if (importDep.revision() != null && currentVersion == YangVersion.VERSION_1) {
                                if (dependencyVersion != YangVersion.VERSION_1) {
                                    throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE,
                                        current,
                                        new YangVersionLinkageException(refOf(current, importDep.sourceRef()),
                                            "Cannot import by revision version %s module %s", dependencyVersion,
                                                resolvedDep.getValue().getLocalName()));
                                }
                            }
                            newResolved.resolveImport(importDep, depModule);
                        }
                        case Include includeDep -> {
                            if (currentVersion != dependencyVersion) {
                                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, current,
                                    new YangVersionLinkageException(refOf(current, dep.sourceRef()),
                                        "Cannot include a version %s submodule %s in a version %s module %s",
                                        dependencyVersion, resolvedDep.getValue().getLocalName(), currentVersion,
                                        current.name().getLocalName()));
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
                // Current was fully processed
                inProgress.remove(current);
                visitedSources.add(current);

            } else {
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

    /**
     * Iterates over submodules and links them to their resolved parents. Throws exception if the parent isn't found
     * among the resolved sources.
     */
    private void tryResolveBelongsTo() {
        for (var entry : submoduleToParentMap.entrySet()) {
            final SourceIdentifier submoduleId = entry.getKey();
            final SourceIdentifier parentId = entry.getValue();

            final var resolvedSubmodule = involvedSourcesMap.get(submoduleId);
            if (resolvedSubmodule == null) {
                throw new InferenceException(submoduleId.toReference(), "Submodule %s was not resolved", submoduleId);
            }

            // FIXME: ensure this through type safety
            final var submoduleInfo = (Submodule) resolvedSubmodule.reactorSource().sourceInfo();
            final var resolvedParent = involvedSourcesMap.get(parentId);
            if (resolvedParent == null) {
                throw new InferenceException(submoduleId.toReference(),
                    "Parent module %s of submodule %s was not resolved", parentId, submoduleId);
            }

            // FIXME: better message and/or better exception
            //double-check that the parent does satisfy this belongs-to
            final var belongsTo = submoduleInfo.belongsTo();
            verify(belongsTo.isSatisfiedBy(parentId));

            resolvedSubmodule.resolveBelongsTo(belongsTo, resolvedParent);
        }
    }

    /**
     * Includes of the same parent module can form circular dependencies. That's why we need
     * to process them differently - after all other dependencies have been resolved.
     */
    private void tryResolveSiblings() {
        final var iterator = unresolvedSiblingsMap.entrySet().iterator();

        while (iterator.hasNext()) {
            final Entry<ResolvedSourceBuilder, Map<Include, SourceIdentifier>> entry = iterator.next();
            final ResolvedSourceBuilder resolvedSource = entry.getKey();
            final Map<Include, SourceIdentifier> siblings = entry.getValue();

            for (var includeEntry : siblings.entrySet()) {
                final var sibling = includeEntry.getValue();
                final var resolvedSibling = involvedSourcesMap.get(sibling);
                if (resolvedSibling == null) {
                    final var sourceId = resolvedSource.reactorSource().sourceId();
                    throw new InferenceException(sourceId.toReference(),
                        "Included submodule %s of module %s was not resolved", sibling, sourceId);
                }
                resolvedSource.resolveInclude(includeEntry.getKey(), resolvedSibling);
            }
            iterator.remove();
        }
    }

    /**
     * Creates a new {@link ResolvedSourceBuilder} for this Source and adds it to the map of Involved-Sources and
     * Involved-Sources-Grouped. It's inclusion in these maps signifies that all the dependencies of this Source had
     * been resolved.
     * @param id of the resolved Source
     * @return ResolvedSourceBuilder of the Source.
     */
    private ResolvedSourceBuilder addResolvedSource(final SourceIdentifier id) {
        if (involvedSourcesMap.containsKey(id)) {
            return involvedSourcesMap.get(id);
        }
        final var newResolvedBuilder = new ResolvedSourceBuilder(allSources.get(id));
        involvedSourcesMap.put(id, newResolvedBuilder);
        final var potentials = involvedSourcesGrouped.get(id.name());
        if (potentials != null) {
            potentials.add(id);
        } else {
            involvedSourcesGrouped.put(id.name(), newMatchSetWith(id));
        }
        return newResolvedBuilder;
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

    private @NonNull Set<SourceIdentifier> findAmongResolved(final Unqualified name) {
        final var matchingInvolved = involvedSourcesGrouped.get(name);
        return matchingInvolved != null ? matchingInvolved : Set.of();
    }

    private @NonNull Set<SourceIdentifier> findAmongAll(final Unqualified name) {
        final var matchingMapped = allSourcesMapped.get(name);
        return matchingMapped != null ? matchingMapped : Set.of();
    }

    private @Nullable SourceInfo lookupSourceInfo(final SourceIdentifier sourceId) {
        final var reactorSource = allSources.get(sourceId);
        return reactorSource == null ? null : reactorSource.sourceInfo();
    }

    private @Nullable SourceIdentifier findSatisfyingParentForSubmodule(final SourceIdentifier submoduleId) {
        if (!(lookupSourceInfo(submoduleId) instanceof SourceInfo.Submodule submodule)) {
            return null;
        }

        final var submoduleBelongsTo = submodule.belongsTo();
        final var satisfied = findSatisfied(involvedSourcesGrouped.get(submoduleBelongsTo.name()), submoduleBelongsTo);
        if (satisfied != null) {
            return satisfied;
        }
        return findSatisfied(allSourcesMapped.get(submoduleBelongsTo.name()), submoduleBelongsTo);
    }

    private static SourceIdentifier findSatisfied(final Set<SourceIdentifier> candidates,
            final SourceDependency dependency) {
        return candidates == null ? null : candidates.stream()
            .filter(dependency::isSatisfiedBy)
            .findFirst()
            .orElse(null);
    }

    private static Set<SourceIdentifier> newMatchSetWith(final SourceIdentifier id) {
        final var matchSet = new TreeSet<>(BY_REVISION);
        matchSet.add(id);
        return matchSet;
    }

    private Set<SourceDependency> getDependenciesOf(final SourceIdentifier id) {
        final var sourceInfo = lookupSourceInfo(id);
        final var dependencies = new HashSet<SourceDependency>();
        dependencies.addAll(sourceInfo.imports());
        dependencies.addAll(sourceInfo.includes());
        return dependencies;
    }

    @NonNullByDefault
    private static StatementSourceReference refOf(final SourceIdentifier sourceId,
            final @Nullable StatementSourceReference sourceRef) {
        return sourceRef != null ? sourceRef : sourceId.toReference();
    }
}
