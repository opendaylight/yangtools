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
    private final Map<SourceIdentifier, ResolvedSource> resolvedSources = new HashMap<>();

    /**
     * Contains the resolved information about a source. Such as the linkage details about imports, includes, belongsTo.
     */
    static final class ResolvedSource {
        private final SourceSpecificContext context;
        private final List<SourceIdentifier> imports;
        private final List<SourceIdentifier> includes;
        // TODO: consider a way to store this info. Map.Entry works for now.
        private Map.Entry<String, QNameModule> belongsTo;

        ResolvedSource(final SourceSpecificContext context, final List<SourceIdentifier> imports,
            final List<SourceIdentifier> includes, final Map.Entry<String, QNameModule> belongsTo) {
            this.context = requireNonNull(context);
            this.imports = imports;
            this.includes = includes;
            this.belongsTo = belongsTo;
        }

        ResolvedSource(final @NonNull SourceSpecificContext sourceContext) {
            this(sourceContext, new LinkedList<>(), new LinkedList<>(), null);
        }

        public SourceSpecificContext context() {
            return context;
        }

        public Map.Entry<String, QNameModule> getBelongsTo() {
            return belongsTo;
        }

        public void addImport(final SourceIdentifier importedSourceId) {
            imports.add(importedSourceId);
        }

        public void addInclude(final SourceIdentifier includedSourceId) {
            includes.add(includedSourceId);
        }

        public void setBelongsTo(final Map.Entry<String, QNameModule> moduleQname) {
            belongsTo = moduleQname;
        }

        public QNameModule getModuleQname() {
            // find latest revision

            // find namespace

            return QNameModule.ofRevision(moduleNs, revisionDate).intern();
        }
    }

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

    Map<SourceIdentifier, ResolvedSource> resolveInvolvedSources() {
        if (mainSources.isEmpty()) {
            return Map.of();
        }

        mapSources(mainSources);
        mapSources(libSources);

        for (SourceSpecificContext mainSource : mainSources) {
            tryResolveDependencies(mainSource.getSourceInfo().sourceId());
        }

        resolvedSources.forEach(this::tryResolveBelongsTo);
        return resolvedSources;
    }

    private void tryResolveBelongsTo(final SourceIdentifier id, final ResolvedSource resolvedSource) {
        final SourceInfo sourceInfo = allSources.get(id);
        if (sourceInfo instanceof SourceInfo.Submodule submodule) {
            resolvedSources.keySet().stream()
                .filter(submodule.belongsTo()::isSatisfiedBy)
                .findFirst()
                .ifPresentOrElse(found ->
                        resolvedSource.setBelongsTo(Map.entry(submodule.belongsTo().prefix().getPrefix(),
                            resolvedSources.get(found).getModuleQname())),
                    () -> { throw new IllegalStateException(String.format(
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
                final ResolvedSource resolved = addResolvedSource(current);
                resolvedDependencies.forEach((resolvedDep, resolvedId) -> {
                    if (resolvedDep instanceof Import) {
                        resolved.addImport(resolvedId);
                    } else {
                        resolved.addInclude(resolvedId);
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

    private ResolvedSource addResolvedSource(final SourceIdentifier id) {
        if (resolvedSources.containsKey(id)) {
            return resolvedSources.get(id);
        }
        final ResolvedSource newResolved = new ResolvedSource(allContexts.get(id));
        resolvedSources.put(id, newResolved);
        final List<SourceIdentifier> potentials = resolvedSourcesGrouped.get(id.name());
        if (potentials != null) {
            potentials.add(id);
        } else {
            resolvedSourcesGrouped.put(id.name(), new LinkedList<>(List.of(id)));
        }
        return newResolved;
    }

    private Set<SourceDependency> getDependenciesOf(final SourceIdentifier id) {
        final SourceInfo sourceInfo = allSources.get(id);
        final Set<SourceDependency> dependencies = new HashSet<>();
        dependencies.addAll(sourceInfo.imports());
        dependencies.addAll(sourceInfo.includes());
        return dependencies;
    }
}
