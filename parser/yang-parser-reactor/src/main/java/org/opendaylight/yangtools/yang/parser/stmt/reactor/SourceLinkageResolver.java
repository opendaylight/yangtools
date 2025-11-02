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
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

/**
 * This class identifies and organizes the main sources and library sources used to build the SchemaContext.
 * Any library sources that aren’t referenced are skipped, and the remaining sources are cross-checked for consistency.
 */
public final class SourceLinkageResolver {

    private Set<SourceSpecificContext> mainSources = new HashSet<>();
    private Set<SourceSpecificContext> libSources = new HashSet<>();

    private final Map<SourceIdentifier, SourceInfo> allSources = new HashMap<>();
    private final Map<SourceIdentifier, SourceSpecificContext> allContexts = new HashMap<>();
    private final Map<Unqualified, List<SourceIdentifier>> allSourcesMapped = new HashMap<>();

    private final Map<Unqualified, List<SourceIdentifier>> resolvedSourcesGrouped = new HashMap<>();
    private final Map<SourceIdentifier, ResolvedSource.Builder> resolvedSources = new HashMap<>();

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

    List<ResolvedSource> resolveInvolvedSources() {
        if (mainSources.isEmpty()) {
            return List.of();
        }

        mapSources(mainSources);
        mapSources(libSources);

        for (SourceSpecificContext mainSource : mainSources) {
            tryResolveDependencies(mainSource.getSourceInfo().sourceId());
        }

        resolvedSources.forEach(this::tryResolveBelongsTo);

        return resolvedSources.values().stream().map(ResolvedSource.Builder::build).toList();
    }

    private void tryResolveBelongsTo(final SourceIdentifier id, final ResolvedSource.Builder resolvedSource) {
        final SourceInfo sourceInfo = allSources.get(id);
        if (sourceInfo instanceof SourceInfo.Submodule submodule) {
            resolvedSources.keySet().stream()
                .filter(submodule.belongsTo()::isSatisfiedBy)
                .findFirst()
                .ifPresentOrElse(found -> {
                    final String prefix = submodule.belongsTo().prefix().getLocalName();
                    final QNameModule parentQnameModule = ((SourceInfo.Module)(resolvedSources.get(found)
                        .sourceInfo())).resolveModuleQName();
                    resolvedSource.setBelongsTo(prefix, parentQnameModule);
                }, () -> {
                    throw new IllegalStateException(String.format(
                        "Missing belongs-to dependency %s of source %s", submodule.belongsTo().name(), id));
                });
        }
    }

    private @NonNull List<SourceIdentifier> findAmongResolved(final Unqualified name) {
        final List<SourceIdentifier> resolvedMatchingQName = resolvedSourcesGrouped.get(name);
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
    private void tryResolveDependencies(final SourceIdentifier id) {
        if (resolvedSources.containsKey(id)) {
            return;
        }

        final Deque<SourceIdentifier> dependencyChain = new ArrayDeque<>();
        dependencyChain.addFirst(id);
        while (!dependencyChain.isEmpty()) {
            final SourceIdentifier current = dependencyChain.pollFirst();
            // get all dependencies - imports and includes
            final Set<SourceDependency> dependencies = getDependenciesOf(current);
            final Map<SourceDependency, SourceIdentifier> resolvedDependencies = new HashMap<>(dependencies.size());
            final Set<SourceIdentifier> unresolvedDependencies = new HashSet<>();
            // check if it's already among resolved - we don't add it to the chain
            boolean allDependenciesResolved = true;
            for (SourceDependency dependency : dependencies) {
                final Unqualified dependencyName = dependency.name();
                // we can have multiple sources with the same name, but different revision. Here we simply
                // iterate over them search for one that satisfies this dependency.
                final List<SourceIdentifier> resolvedMatchingQName = findAmongResolved(dependencyName);
                final Optional<SourceIdentifier> foundResolvedMatch = resolvedMatchingQName.stream()
                    .filter(dependency::isSatisfiedBy).findFirst();
                if (foundResolvedMatch.isPresent()) {
                    resolvedDependencies.put(dependency, foundResolvedMatch.orElseThrow());
                } else {
                    //if it's not resolved yet, find it among all libs and add it to the dependency chain to be resolved
                    final List<SourceIdentifier> allMatchingName = findAmongAll(dependencyName);
                    final Optional<SourceIdentifier> foundMatching = allMatchingName.stream()
                        .filter(dependency::isSatisfiedBy).findFirst();
                    if (foundMatching.isPresent()) {
                        unresolvedDependencies.add(foundMatching.orElseThrow());
                        allDependenciesResolved = false;
                    } else {
                        throw new IllegalStateException(String.format("Missing dependency %s of source %s",
                            dependencyName, current));
                    }
                }
            }
            if (allDependenciesResolved) {
                final ResolvedSource.Builder resolved = addResolvedSource(current);
                resolvedDependencies.forEach((resolvedDep, resolvedId) -> {
                    final QNameModule module = ((SourceInfo.Module) allSources.get(resolvedId)).resolveModuleQName();
                    if (resolvedDep instanceof Import importedDep) {
                        final String prefix = importedDep.prefix().getLocalName();
                        resolved.addImport(prefix, module);
                    } else {
                        resolved.addInclude(module);
                    }
                });

            } else {
                // some imports were not resolved, so we'll have to add them to the dependencyChain first.
                // we need to also add this source back in the dependencyChain, since it needs to be revisited after
                // its dependencies get resolved
                dependencyChain.addFirst(current);
                // check for circular dependency
                for (SourceIdentifier unresolved : unresolvedDependencies) {
                    if (!dependencyChain.contains(unresolved)) {
                        dependencyChain.addFirst(unresolved);
                    } else {
                        throw new IllegalStateException(
                            String.format("Found circular dependency between modules %s and %s", unresolved, current));
                    }
                }
            }
        }
    }

    private ResolvedSource.Builder addResolvedSource(final SourceIdentifier id) {
        if (resolvedSources.containsKey(id)) {
            return resolvedSources.get(id);
        }
        final ResolvedSource.Builder newResolvedBuilder = ResolvedSource.builder()
            .setContext(allContexts.get(id))
            .setSourceInfo(allSources.get(id));
        resolvedSources.put(id, newResolvedBuilder);
        final List<SourceIdentifier> potentials = resolvedSourcesGrouped.get(id.name());
        if (potentials != null) {
            potentials.add(id);
        } else {
            resolvedSourcesGrouped.put(id.name(), new LinkedList<>(List.of(id)));
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
