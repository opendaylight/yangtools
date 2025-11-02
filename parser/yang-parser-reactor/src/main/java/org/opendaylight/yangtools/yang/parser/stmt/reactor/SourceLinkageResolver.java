/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import static org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Submodule;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

/**
 * This class identifies and organizes the main sources and library sources used to build the SchemaContext.
 * Any library sources that aren’t referenced are skipped, and the remaining sources are cross-checked for consistency
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

    private List<SourceSpecificContext> mainSources = new LinkedList<>();
    private List<SourceSpecificContext> libSources = new LinkedList<>();

    private final Map<SourceIdentifier, SourceInfo> allSources = new HashMap<>();
    private final Map<SourceIdentifier, SourceSpecificContext> allContexts = new HashMap<>();

    /**
     * TreeSet with a Revision-Comparator will keep the sources with the same name ordered by Revision.
     */
    private final Map<Unqualified, Set<SourceIdentifier>> allSourcesMapped = new HashMap<>();

    /**
     * Map of involved sources with the same name.
     */
    private final Map<Unqualified, Set<SourceIdentifier>> involvedSourcesGrouped = new HashMap<>();
    /**
     * Map of involved sources ordered according to the resolution order (LinkedHashMap keeps the insertion order).
     */
    private final Map<SourceIdentifier, ResolvedSource.Builder> involvedSourcesMap = new LinkedHashMap<>();

    private final Map<SourceIdentifier, SourceIdentifier> submoduleToParentMap = new HashMap<>();

    private final Map<ResolvedSource.Builder, Set<SourceIdentifier>> unresolvedSiblingsMap = new HashMap<>();


    SourceLinkageResolver(
        final @NonNull Collection<SourceSpecificContext> withMainSources,
        final @NonNull Collection<SourceSpecificContext> withLibSources) {
        this.mainSources.addAll(requireNonNull(withMainSources));
        this.libSources.addAll(requireNonNull(withLibSources));
    }

    private void mapSubmodulesToParents() throws SomeModifiersUnresolvedException {
        for (Map.Entry<SourceIdentifier, SourceInfo> source : allSources.entrySet()) {
            SourceInfo sourceInfo = source.getValue();
            if (sourceInfo instanceof SourceInfo.Submodule submoduleInfo) {
                final Unqualified parentName = submoduleInfo.belongsTo().name();
                final SourceIdentifier parentId = findSatisfied(allSourcesMapped.get(submoduleInfo.belongsTo().name()),
                    submoduleInfo.belongsTo());
                if (parentId == null) {
                    throwUnresolvedException(source.getKey(), "Module %s from belongs-to was not found", parentName);
                }
                submoduleToParentMap.put(source.getKey(), parentId);
            }
        }
    }

    private void mapSources(final Collection<SourceSpecificContext> sources) {
        for (SourceSpecificContext source : sources) {
            final SourceInfo sourceInfo = source.getSourceInfo();
            final SourceIdentifier sourceId = sourceInfo.sourceId();

            allSources.putIfAbsent(sourceId, sourceInfo);
            allContexts.putIfAbsent(sourceId, source);
            Set<SourceIdentifier> allOfQname = allSourcesMapped.get(sourceId.name());
            if (allOfQname != null) {
                allOfQname.add(sourceId);
            } else {
                mapSource(sourceId);
            }
        }
    }

    private void mapSource(SourceIdentifier sourceId) {
        final Unqualified name = sourceId.name();
        final Set<SourceIdentifier> matches = allSourcesMapped.get(name);
        if (matches != null) {
            matches.add(sourceId);
            return;
        }

        allSourcesMapped.put(name, newMatchSetWith(sourceId));
    }

    /**
     * Processes all main sources and library sources. Finds out which ones are involved and resolves dependencies
     * between them.
     * @return list of resolved sources
     */
    List<ResolvedSource> resolveInvolvedSources() throws SomeModifiersUnresolvedException {
        if (mainSources.isEmpty()) {
            return List.of();
        }

        mapSources(mainSources);
        mapSources(libSources);
        mapSubmodulesToParents();
        reuniteMainSubmodulesWithParents();

        tryResolveAllDependencies();
        tryResolveBelongsTo();
        tryResolveSiblings();

        final Map<SourceSpecificContext, ResolvedSource> allResolved = new LinkedHashMap<>(involvedSourcesMap.size());
        for (Map.Entry<SourceIdentifier, ResolvedSource.Builder> involvedSource : involvedSourcesMap.entrySet()) {
            final ResolvedSource fullyResolved = involvedSource.getValue().build(allResolved);
            allResolved.put(fullyResolved.getContext(), fullyResolved);
        }

        return allResolved.values().stream().toList();
    }

    private void tryResolveAllDependencies() throws SomeModifiersUnresolvedException {
        for (final SourceSpecificContext mainSource : mainSources) {
            tryResolveDependenciesOf(mainSource.getSourceInfo().sourceId());
        }
    }

    /**
     * Verifies that every submodule in main-sources has it's parent module present among main-sources as well.
     */
    private void reuniteMainSubmodulesWithParents() {
        // use classic for loop to enable expansion of the mainSources list
        for (int i = 0; i < mainSources.size(); i++) {
            SourceInfo sourceInfo = mainSources.get(i).getSourceInfo();

            if (sourceInfo instanceof Submodule) {
                final SourceIdentifier parentId = submoduleToParentMap.get(sourceInfo.sourceId());
                final SourceSpecificContext parentCtx = allContexts.get(parentId);

                // Only add if not already in the list
                if (!mainSources.contains(parentCtx)) {
                    mainSources.add(parentCtx);
                }
            }
        }
    }

    private void tryResolveBelongsTo() {
        for (final Map.Entry<SourceIdentifier, SourceIdentifier> mapping : submoduleToParentMap.entrySet()) {
            final SourceIdentifier submoduleId = mapping.getKey();
            final SourceIdentifier parentId = mapping.getValue();

            final ResolvedSource.Builder resolvedSubmodule = requireNonNull(involvedSourcesMap.get(submoduleId),
                String.format("Submodule %s was not resolved", submoduleId));
            final SourceInfo.Submodule submoduleInfo = (Submodule) resolvedSubmodule.getContext().getSourceInfo();
            final ResolvedSource.Builder resolvedParent = requireNonNull(involvedSourcesMap.get(parentId),
                String.format("Parent module %s of submodule %s was not resolved", parentId, submoduleId));

            //double-check that the parent does satisfy this belongs-to
            verify(submoduleInfo.belongsTo().isSatisfiedBy(parentId));

            resolvedSubmodule.setBelongsTo(submoduleInfo.belongsTo().prefix().getLocalName(), resolvedParent);
        }
    }

    /**
     * Includes of the same parent module can form circular dependencies. That's why we need
     * to process them differently.
     */
    private void tryResolveSiblings() {
        final Iterator<Map.Entry<ResolvedSource.Builder, Set<SourceIdentifier>>> iterator =
            unresolvedSiblingsMap.entrySet().iterator();

        while (iterator.hasNext()) {
            final Map.Entry<ResolvedSource.Builder, Set<SourceIdentifier>> entry = iterator.next();
            final ResolvedSource.Builder resolvedSource = entry.getKey();
            final Set<SourceIdentifier> siblings = entry.getValue();

            for (final SourceIdentifier sibling : siblings) {
                final ResolvedSource.Builder resolvedSibling = requireNonNull(involvedSourcesMap.get(sibling),
                    String.format("Included module %s of module %s was not resolved", sibling,
                        resolvedSource.getContext().getSourceInfo().sourceId()));
                resolvedSource.addInclude(resolvedSibling);
            }
            iterator.remove();
        }
    }

    private @NonNull Set<SourceIdentifier> findAmongResolved(final Unqualified name) {
        final Set<SourceIdentifier> resolvedMatchingQName = involvedSourcesGrouped.get(name);
        if (resolvedMatchingQName != null) {
            return resolvedMatchingQName;
        }
        return Set.of();
    }

    private @NonNull Set<SourceIdentifier> findAmongAll(final Unqualified name) {
        final Set<SourceIdentifier> resolvedMatchingQName = allSourcesMapped.get(name);
        if (resolvedMatchingQName != null) {
            return resolvedMatchingQName;
        }
        return Set.of();
    }

    /**
     * Resolving both IMPORTS and INCLUDES as SourceDependencies to avoid potential unresolved gaps - these could
     * happen if we resolved them separately.
     */
    private void tryResolveDependenciesOf(final SourceIdentifier rootId) throws SomeModifiersUnresolvedException {
        if (involvedSourcesMap.containsKey(rootId)) {
            return;
        }

        // Nodes already fully resolved
        final Set<SourceIdentifier> visitedSources = new HashSet<>();

        // Nodes currently being resolved (active dependency path)
        final Set<SourceIdentifier> inProgress = new HashSet<>();

        // Modules that need resolution attempts
        final Deque<SourceIdentifier> workChain = new ArrayDeque<>();
        workChain.add(rootId);

        while (!workChain.isEmpty()) {
            final SourceIdentifier current = workChain.pollFirst();
            if (visitedSources.contains(current)) {
                continue;
            }

            inProgress.add(current);

            final Set<SourceDependency> dependencies = getDependenciesOf(current);
            final Map<SourceDependency, SourceIdentifier> resolvedDependencies = new HashMap<>();
            final List<SourceIdentifier> unresolvedDependencies = new ArrayList<>();
            final List<SourceIdentifier> includedSiblings = new LinkedList<>();
            boolean allResolved = true;

            for (SourceDependency dependency : dependencies) {
                final Unqualified dependencyName = dependency.name();

                // Check among resolved modules first
                SourceIdentifier match = findSatisfied(findAmongResolved(dependencyName), dependency);

                if (match != null) {
                    resolvedDependencies.put(dependency, match);
                    continue;
                }

                // Check among all modules
                match = findSatisfied(findAmongAll(dependencyName), dependency);
                if (match == null) {
                    // Dependency is missing
                    if (dependency instanceof Import) {
                        throwUnresolvedException(current, "Imported module %s was not found. [at %s]",
                            dependencyName, current.name());
                    } else {
                        throwUnresolvedException(current, "Included submodule %s was not found. [at %s]",
                            dependencyName, current.name());
                    }
                }

                if (isIncludedSibling(current, dependency, match)) {
                    // If this is an include of a sibling submodule, don't add it as unresolved dependency.
                    // It will be resolved later in a different way.
                    includedSiblings.add(match);
                    continue;
                }

                // Dependency exists but was not fully resolved yet
                unresolvedDependencies.add(match);
                allResolved = false;
            }

            if (allResolved) {
                final ResolvedSource.Builder newResolved = addResolvedSource(current);

                resolvedDependencies.forEach((dep, resolvedId) -> {
                    final ResolvedSource.Builder module = involvedSourcesMap.get(resolvedId);
                    if (dep instanceof Import importedDep) {
                        newResolved.addImport(importedDep.prefix().getLocalName(), module);
                    } else {
                        newResolved.addInclude(module);
                    }
                });

                if (!includedSiblings.isEmpty()) {
                    mapSiblings(newResolved, includedSiblings);
                }
                // Current was fully processed
                inProgress.remove(current);
                visitedSources.add(current);

            } else {
                // Need to process unresolved dependencies first.
                // Requeue current so it gets another chance after it's dependencies get resolved.
                workChain.addFirst(current);

                for (SourceIdentifier dep : unresolvedDependencies) {

                    // Check circular dependency
                    if (inProgress.contains(dep)) {
                        throwUnresolvedException(current, "Found circular dependency between modules %s and %s",
                            current, dep);
                    }

                    // Not processed yet, add to queue
                    if (!visitedSources.contains(dep)) {
                        workChain.addFirst(dep);
                    }
                }
            }
        }
    }

    private void mapSiblings(ResolvedSource.Builder newResolved, List<SourceIdentifier> includedSiblings) {
        final Set<SourceIdentifier> siblings = unresolvedSiblingsMap.computeIfAbsent(newResolved,
            k -> new HashSet<>());
        siblings.addAll(includedSiblings);
    }

    private boolean isIncludedSibling(final SourceIdentifier current, final SourceDependency dependency,
        final SourceIdentifier dependencyId) {
        if (!(dependency instanceof Include)) {
            return false;
        }

        final SourceIdentifier currentParent = findSatisfyingParentForSubmodule(current);
        final SourceIdentifier theirParent = findSatisfyingParentForSubmodule(dependencyId);

        if (currentParent == null || theirParent == null || !currentParent.equals(theirParent)) {
            return false;
        }

        return true;
    }

    private SourceIdentifier findSatisfyingParentForSubmodule(final SourceIdentifier submoduleId) {
        final SourceInfo sourceInfo = allSources.get(submoduleId);
        if (sourceInfo == null || sourceInfo instanceof SourceInfo.Module) {
            return null;
        }

        final SourceDependency.BelongsTo submoduleBelongsTo = requireNonNull((Submodule) sourceInfo).belongsTo();
        final SourceIdentifier satisfied = findSatisfied(involvedSourcesGrouped.get(submoduleBelongsTo.name()),
            submoduleBelongsTo);
        if (satisfied != null) {
            return satisfied;
        }

        return findSatisfied(allSourcesMapped.get(submoduleBelongsTo.name()), submoduleBelongsTo);
    }

    private static SourceIdentifier findSatisfied(final Collection<SourceIdentifier> candidates,
        final SourceDependency dependency) {

        if (candidates == null) {
            return null;
        }

        return candidates.stream()
            .filter(dependency::isSatisfiedBy)
            .findFirst()
            .orElse(null);
    }

    private ResolvedSource.Builder addResolvedSource(final SourceIdentifier id) {
        if (involvedSourcesMap.containsKey(id)) {
            return involvedSourcesMap.get(id);
        }
        final ResolvedSource.Builder newResolvedBuilder = ResolvedSource.builder(allContexts.get(id));
        involvedSourcesMap.put(id, newResolvedBuilder);
        final Set<SourceIdentifier> potentials = involvedSourcesGrouped.get(id.name());
        if (potentials != null) {
            potentials.add(id);
        } else {
            involvedSourcesGrouped.put(id.name(), newMatchSetWith(id));
        }
        return newResolvedBuilder;
    }

    private Set<SourceIdentifier> newMatchSetWith(final SourceIdentifier id) {
        final TreeSet<SourceIdentifier> matchSet = new TreeSet<>(BY_REVISION);
        matchSet.add(id);
        return matchSet;
    }

    private Set<SourceDependency> getDependenciesOf(final SourceIdentifier id) {
        final SourceInfo sourceInfo = allSources.get(id);
        final Set<SourceDependency> dependencies = new HashSet<>();
        dependencies.addAll(sourceInfo.imports());
        dependencies.addAll(sourceInfo.includes());
        return dependencies;
    }

    private void throwUnresolvedException(final SourceIdentifier throwingSource, final String messageFormat,
        SourceIdentifier... messageSources) throws SomeModifiersUnresolvedException {
        this.throwUnresolvedException(throwingSource, messageFormat, Arrays.stream(messageSources)
            .map(id -> id.name().getLocalName()).toList());
    }

    private void throwUnresolvedException(final SourceIdentifier throwingSource, final String messageFormat,
        Unqualified... messageSources) throws SomeModifiersUnresolvedException {
        this.throwUnresolvedException(throwingSource, messageFormat, Arrays.stream(messageSources)
            .map(Unqualified::getLocalName).toList());
    }

    private void throwUnresolvedException(final SourceIdentifier throwingSource, final String messageFormat,
        final List<String> args) throws SomeModifiersUnresolvedException {
        final String message = String.format(messageFormat, args.stream().map(s -> "[" + s + "]").toArray());
        throw new SomeModifiersUnresolvedException(
            ModelProcessingPhase.SOURCE_LINKAGE,
            throwingSource,
            new IllegalStateException(message)
        );
    }
}
