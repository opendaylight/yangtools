/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import static org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

/**
 * This class identifies and organizes the main sources and library sources used to build the SchemaContext.
 * Any library sources that aren’t referenced are skipped, and the remaining sources are cross-checked for consistency
 * and linked together according to their dependencies (includes, imports, belongs-to)
 */
public final class SourceLinkageResolver {

    private Set<SourceSpecificContext> mainSources = new HashSet<>();
    private Set<SourceSpecificContext> libSources = new HashSet<>();

    private final Map<SourceIdentifier, SourceInfo> allSources = new HashMap<>();
    private final Map<SourceIdentifier, SourceSpecificContext> allContexts = new HashMap<>();
    private final Map<Unqualified, List<SourceIdentifier>> allSourcesMapped = new HashMap<>();

    /**
     * Map of involved sources with the same name
     */
    private final Map<Unqualified, List<SourceIdentifier>> involvedSourcesGrouped = new HashMap<>();
    /**
     * Map of involved sources ordered according to the resolution order (LinkedHashMap keeps the insertion order)
     */
    private final Map<SourceIdentifier, ResolvedSource.Builder> involvedSourcesMap = new LinkedHashMap<>();

    SourceLinkageResolver(
        final @NonNull Collection<SourceSpecificContext> withMainSources,
        final @NonNull Collection<SourceSpecificContext> withLibSources) {
        this.mainSources.addAll(requireNonNull(withMainSources));
        this.libSources.addAll(requireNonNull(withLibSources));
    }

    private void mapSources(final Set<SourceSpecificContext> sources) {
        for (SourceSpecificContext source : sources) {
            final SourceInfo sourceInfo = source.getSourceInfo();
            final SourceIdentifier sourceId = sourceInfo.sourceId();

            allSources.putIfAbsent(sourceId, sourceInfo);
            allContexts.putIfAbsent(sourceId, source);
            List<SourceIdentifier> allOfQname = allSourcesMapped.get(sourceId.name());
            if (allOfQname != null) {
                allOfQname.add(sourceId);
            } else {
                allSourcesMapped.put(sourceId.name(),  new LinkedList<>(List.of(sourceId)));
            }
        }
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

        for (SourceSpecificContext mainSource : mainSources) {
            tryResolveDependencies(mainSource.getSourceInfo().sourceId());
        }

        for (Map.Entry<SourceIdentifier, ResolvedSource.Builder> involved : involvedSourcesMap.entrySet()) {
            tryResolveBelongsTo(involved.getKey(), involved.getValue());
        }

        final Map<SourceSpecificContext, ResolvedSource> allResolved = new LinkedHashMap<>(involvedSourcesMap.size());
        for (Map.Entry<SourceIdentifier, ResolvedSource.Builder> involvedSource : involvedSourcesMap.entrySet()) {
            final ResolvedSource fullyResolved = involvedSource.getValue().build(allResolved);
            allResolved.put(fullyResolved.getContext(), fullyResolved);
        }

        return allResolved.values().stream().toList();
    }

    private void tryResolveBelongsTo(final SourceIdentifier id, final ResolvedSource.Builder resolvedSource)
        throws SomeModifiersUnresolvedException {
        final SourceInfo sourceInfo = allSources.get(id);
        if (sourceInfo instanceof SourceInfo.Submodule submodule) {
            final SourceDependency.BelongsTo belongsTo = submodule.belongsTo();
            final Optional<SourceIdentifier> found = involvedSourcesMap.keySet().stream()
                .filter(belongsTo::isSatisfiedBy)
                .findFirst();

            if (found.isPresent()) {
                final String prefix = belongsTo.prefix().getLocalName();
                final ResolvedSource.Builder parentModule = involvedSourcesMap.get(found.get());
                resolvedSource.setBelongsTo(prefix, parentModule);
            } else {
                throw new SomeModifiersUnresolvedException(
                    ModelProcessingPhase.STATEMENT_DEFINITION,
                    id, new IllegalStateException(String.format("Module '%s' from belongs-to was not found.",
                    belongsTo.name()))
                );
            }
        }
    }

    private @NonNull List<SourceIdentifier> findAmongResolved(final Unqualified name) {
        final List<SourceIdentifier> resolvedMatchingQName = involvedSourcesGrouped.get(name);
        if (resolvedMatchingQName != null) {
            return resolvedMatchingQName;
        }
        return List.of();
    }

    private @NonNull List<SourceIdentifier> findAmongAll(final Unqualified name) {
        final List<SourceIdentifier> resolvedMatchingQName = allSourcesMapped.get(name);
        if (resolvedMatchingQName != null) {
            return resolvedMatchingQName;
        }
        return List.of();
    }

    /**
     * Resolving both IMPORTS and INCLUDES as SourceDependencies to avoid potential unresolved gaps - these could
     * happen if we resolved them separately.
     */
    private void tryResolveDependencies(final SourceIdentifier rootId) throws SomeModifiersUnresolvedException {
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
            boolean allResolved = true;

            for (SourceDependency dependency : dependencies) {
                final Unqualified dependencyName = dependency.name();

                // Check among resolved modules
                final List<SourceIdentifier> resolvedMatches = findAmongResolved(dependencyName);
                final Optional<SourceIdentifier> resolvedMatch =
                    resolvedMatches.stream().filter(dependency::isSatisfiedBy).findFirst();

                if (resolvedMatch.isPresent()) {
                    resolvedDependencies.put(dependency, resolvedMatch.get());
                    continue;
                }

                // Check among all modules
                final List<SourceIdentifier> allMatches = findAmongAll(dependencyName);
                final Optional<SourceIdentifier> match =
                    allMatches.stream().filter(dependency::isSatisfiedBy).findFirst();

                if (match.isEmpty()) {
                    // Dependency is missing
                    if (dependency instanceof Import) {
                        throw new SomeModifiersUnresolvedException(
                            ModelProcessingPhase.STATEMENT_DEFINITION, current,
                            new IllegalStateException(
                                String.format("Imported module [%s] was not found.",
                                    dependencyName.getLocalName())
                            ));
                    } else {
                        throw new SomeModifiersUnresolvedException(
                            ModelProcessingPhase.STATEMENT_DEFINITION, current,
                            new IllegalStateException(
                                String.format("Included submodule [%s] was not found.",
                                    dependencyName.getLocalName())
                            ));
                    }
                }

                // Dependency exists but was not fully resolved yet
                SourceIdentifier unresolved = match.get();
                unresolvedDependencies.add(unresolved);
                allResolved = false;
            }

            if (allResolved) {
                final ResolvedSource.Builder resolved = addResolvedSource(current);

                resolvedDependencies.forEach((dep, resolvedId) -> {
                    final ResolvedSource.Builder module = involvedSourcesMap.get(resolvedId);
                    if (dep instanceof Import importedDep) {
                        resolved.addImport(importedDep.prefix().getLocalName(), module);
                    } else {
                        resolved.addInclude(module, resolvedId.name());
                    }
                });

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
                        throw new SomeModifiersUnresolvedException(
                            ModelProcessingPhase.STATEMENT_DEFINITION, current,
                            new IllegalStateException(
                                String.format(
                                    "Found circular dependency between modules %s and %s",
                                    current, dep)
                            ));
                    }

                    // Not processed yet, add to queue
                    if (!visitedSources.contains(dep)) {
                        workChain.addFirst(dep);
                    }
                }
            }
        }
    }

    private ResolvedSource.Builder addResolvedSource(final SourceIdentifier id) {
        if (involvedSourcesMap.containsKey(id)) {
            return involvedSourcesMap.get(id);
        }
        final ResolvedSource.Builder newResolvedBuilder = ResolvedSource.builder(allContexts.get(id));
        involvedSourcesMap.put(id, newResolvedBuilder);
        final List<SourceIdentifier> potentials = involvedSourcesGrouped.get(id.name());
        if (potentials != null) {
            potentials.add(id);
        } else {
            involvedSourcesGrouped.put(id.name(), new LinkedList<>(List.of(id)));
        }
        return newResolvedBuilder;
    }

    private Set<SourceDependency> getDependenciesOf(final SourceIdentifier id) {
        final SourceInfo sourceInfo = allSources.get(id);
        final Set<SourceDependency> dependencies = new HashSet<>();
        dependencies.addAll(sourceInfo.imports());
        dependencies.addAll(sourceInfo.includes());
        return dependencies;
    }
}
