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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Submodule;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSourceInfoException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.ResolvedSourceInfo;

/**
 * Identifies and organizes the main sources and library sources used to build the SchemaContext.
 * Any library sources that aren’t referenced are skipped. The referenced sources are cross-checked for consistency
 * and linked together according to their dependencies (includes, imports, belongs-to)
 */
public final class SourceLinkageResolver {

    @NonNullByDefault
    record ResolvedSourceContext(
            SourceSpecificContext context,
            ResolvedSourceInfo resolvedSourceInfo) {
        ResolvedSourceContext {
            requireNonNull(context);
            requireNonNull(resolvedSourceInfo);
        }
    }

    /**
     * Comparator to keep groups of modules with the same name ordered by their revision (latest first).
     */
    private static final Comparator<SourceIdentifier> BY_REVISION = Comparator.comparing(
        SourceIdentifier::revision,
        Comparator.nullsLast(Revision::compareTo).reversed()
    );

    private final List<SourceSpecificContext> mainSources = new ArrayList<>();
    private final List<SourceSpecificContext> libSources = new ArrayList<>();

    private final Map<SourceIdentifier, SourceInfo> allSources = new HashMap<>();
    private final Map<SourceIdentifier, SourceSpecificContext> allContexts = new HashMap<>();

    /**
     * Map of all sources with the same name. They are stored in a TreeSet with a Revision-Comparator which will keep
     * them ordered by Revision.
     */
    private final Map<Unqualified, Set<SourceIdentifier>> allSourcesMapped = new HashMap<>();

    /**
     * Map of involved sources with the same name.
     */
    private final Map<Unqualified, Set<SourceIdentifier>> involvedSourcesGrouped = new HashMap<>();
    /**
     * Map of involved sources ordered according to the resolution order (LinkedHashMap keeps the insertion order).
     */
    private final Map<SourceIdentifier, ResolvedSourceBuilder> involvedSourcesMap = new LinkedHashMap<>();

    private final Map<SourceIdentifier, SourceIdentifier> submoduleToParentMap = new HashMap<>();

    /**
     * Map of submodules which include other submodules of the same parent module.
     */
    private final Map<ResolvedSourceBuilder, Set<SourceIdentifier>> unresolvedSiblingsMap = new HashMap<>();

    private SourceLinkageResolver(
            final @NonNull Collection<SourceSpecificContext> withMainSources,
            final @NonNull Collection<SourceSpecificContext> withLibSources) {
        mainSources.addAll(requireNonNull(withMainSources));
        libSources.addAll(requireNonNull(withLibSources));
    }

    /**
     * Creates a SourceLinkageResolver for specified main and library sources.
     *
     * @param mainSources sources used as the base for the Schema Context. All of them have to be resolved and included
     *                    in the output of the {@link SourceLinkageResolver}
     * @param libSources dependencies of the main sources, as well as other library sources. Unreferenced (unused)
     *                   sources will be omitted from the output of the {@link SourceLinkageResolver}
     * @return {@link SourceLinkageResolver} ready to resolve the inter-source linkage.
     * @throws SourceSyntaxException if the sources fail to provide the necessary {@link SourceInfo}
     * @throws ReactorException if the source files couldn't be loaded or parsed
     */
    static @NonNull SourceLinkageResolver create(final @NonNull Collection<BuildSource<?>> mainSources,
            final @NonNull Collection<BuildSource<?>> libSources) throws ReactorException, SourceSyntaxException {
        return new SourceLinkageResolver(initializeSources(mainSources), initializeSources(libSources));
    }

    private static @NonNull Collection<SourceSpecificContext> initializeSources(
            final @NonNull Collection<BuildSource<?>> sourcesToInitialize)
                    throws ReactorException, SourceSyntaxException {
        final var contexts = new HashSet<SourceSpecificContext>();
        for (final var buildSource : sourcesToInitialize) {
            final SourceSpecificContext context;
            try {
                context = buildSource.getSourceContext();
            } catch (IOException e) {
                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.INIT, buildSource.sourceId(), e);
            }
            contexts.add(context);
        }
        return contexts;
    }

    /**
     * Processes all main sources and library sources. Finds out which ones are involved and resolves dependencies
     * between them.
     * @return list of resolved sources
     */
    @NonNull List<ResolvedSourceContext> resolveInvolvedSources() throws ReactorException {
        if (mainSources.isEmpty()) {
            return List.of();
        }

        mapSources(mainSources);
        mapSources(libSources);
        mapSubmodulesToParents();
        reuniteMainSubmodulesWithParents();

        tryResolveDependencies();
        tryResolveBelongsTo();
        tryResolveSiblings();

        final var allResolved = new LinkedHashMap<SourceSpecificContext, ResolvedSourceInfo>(involvedSourcesMap.size());
        for (final Map.Entry<SourceIdentifier, ResolvedSourceBuilder> involvedSource : involvedSourcesMap.entrySet()) {
            final ResolvedSourceInfo fullyResolved = involvedSource.getValue().build(allResolved);
            allResolved.put(involvedSource.getValue().context(), fullyResolved);
        }

        return allResolved
            .entrySet()
            .stream()
            .map(entry -> new ResolvedSourceContext(entry.getKey(), entry.getValue()))
            .toList();
    }

    private void mapSubmodulesToParents() throws SomeModifiersUnresolvedException {
        for (final Map.Entry<SourceIdentifier, SourceInfo> source : allSources.entrySet()) {
            final var sourceInfo = source.getValue();
            if (sourceInfo instanceof SourceInfo.Submodule submoduleInfo) {
                final var parentName = submoduleInfo.belongsTo().name();
                final var parentId = findSatisfied(allSourcesMapped.get(submoduleInfo.belongsTo().name()),
                    submoduleInfo.belongsTo());
                if (parentId == null) {
                    final var submoduleId = source.getKey();
                    throw new SomeModifiersUnresolvedException(ModelProcessingPhase.INIT, submoduleId,
                        new IllegalStateException("Module '%s' from belongs-to was not found [at '%s']".formatted(
                            parentName, submoduleId.name().getLocalName())));
                }
                submoduleToParentMap.put(source.getKey(), parentId);
            }
        }
    }

    private void mapSources(final Collection<SourceSpecificContext> sources) throws MissingSourceInfoException {
        for (final SourceSpecificContext source : sources) {
            final SourceInfo sourceInfo = source.getSourceInfo();
            final SourceIdentifier sourceId = sourceInfo.sourceId();

            allSources.putIfAbsent(sourceId, sourceInfo);
            allContexts.putIfAbsent(sourceId, source);
            final var allOfQname = allSourcesMapped.get(sourceId.name());
            if (allOfQname != null) {
                allOfQname.add(sourceId);
            } else {
                mapSource(sourceId);
            }
        }
    }

    private void mapSource(final SourceIdentifier sourceId) {
        final Unqualified name = sourceId.name();
        final Set<SourceIdentifier> matches = allSourcesMapped.get(name);
        if (matches != null) {
            matches.add(sourceId);
            return;
        }

        allSourcesMapped.put(name, newMatchSetWith(sourceId));
    }

    private void tryResolveDependencies() throws ReactorException {
        for (final SourceSpecificContext mainSource : mainSources) {
            tryResolveDependenciesOf(mainSource.getSourceInfo().sourceId());
        }
    }

    /**
     * Ensures that every submodule in main-sources has it's parent module present among main-sources as well.
     */
    private void reuniteMainSubmodulesWithParents() throws MissingSourceInfoException {
        // use classic for loop to enable expansion of the mainSources list
        for (int i = 0; i < mainSources.size(); i++) {
            final var sourceInfo = mainSources.get(i).getSourceInfo();

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
            final var includedSiblings = new LinkedList<SourceIdentifier>();
            boolean allResolved = true;

            for (final SourceDependency dependency : dependencies) {
                final Unqualified dependencyName = dependency.name();

                // Find the best match (by revision) among all modules
                final SourceIdentifier match = findSatisfied(findAmongAll(dependencyName), dependency);
                if (match == null) {
                    // Dependency is missing
                    if (dependency instanceof Import) {
                        throw new SomeModifiersUnresolvedException(ModelProcessingPhase.INIT, current,
                            new IllegalStateException("Imported module '%s' was not found. [at '%s']".formatted(
                                dependencyName.getLocalName(), current.name().getLocalName())));
                    }

                    // FIXME: also handling of BelongsTo?
                    throw new SomeModifiersUnresolvedException(ModelProcessingPhase.INIT, current,
                        new IllegalStateException("Included submodule '%s' was not found. [at '%s']".formatted(
                            dependencyName.getLocalName(), current.name().getLocalName())));
                }

                // if the match was already resolved, just move on
                if (involvedSourcesMap.containsKey(match)) {
                    resolvedDependencies.put(dependency, dependencyName);
                    continue;
                }

                if (isIncludedSibling(current, dependency, match)) {
                    // If this is an include of a sibling submodule, don't add it as unresolved dependency.
                    // It will be resolved later in a different way.
                    includedSiblings.add(match);
                    continue;
                }

                // Dependency exists but was not fully resolved yet - mark as unresolved
                unresolvedDependencies.add(match);
                allResolved = false;
            }

            if (allResolved) {
                final var newResolved = addResolvedSource(current);

                for (Map.Entry<SourceDependency, Unqualified> resolvedDep : resolvedDependencies.entrySet()) {
                    // Find the best match for this dependency among the resolved modules
                    final var satisfiedDepId = requireNonNull(findSatisfied(
                        findAmongResolved(resolvedDep.getValue()), resolvedDep.getKey()));
                    final ResolvedSourceBuilder depModule = involvedSourcesMap.get(satisfiedDepId);

                    final var currentVersion = newResolved.yangVersion();
                    final var dependencyVersion = depModule.yangVersion();

                    if (resolvedDep.getKey() instanceof Import importedDep) {
                        // Version 1 sources must not import-by-revision Version 1.1 modules
                        if (importedDep.revision() != null && currentVersion == YangVersion.VERSION_1) {
                            if (dependencyVersion != YangVersion.VERSION_1) {
                                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.INIT, current,
                                    new IllegalStateException(
                                        "Cannot import by revision version %s module %s [at %s]".formatted(
                                            dependencyVersion, resolvedDep.getValue().getLocalName(),
                                            current.name().getLocalName())));
                            }
                        }
                        newResolved.addImport(importedDep.prefix().getLocalName(), depModule);
                    } else {
                        if (currentVersion != dependencyVersion) {
                            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.INIT, current,
                                new IllegalStateException(
                                    "Cannot include a version %s submodule %s in a version %s module %s".formatted(
                                        dependencyVersion, resolvedDep.getValue().getLocalName(), currentVersion,
                                        current.name().getLocalName())));
                        }
                        newResolved.addInclude(depModule);
                    }
                }

                if (!includedSiblings.isEmpty()) {
                    // map all the included siblings of this submodule
                    mapSiblings(newResolved, includedSiblings);
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
                        throw new SomeModifiersUnresolvedException(ModelProcessingPhase.INIT, current,
                            new IllegalStateException("Found circular dependency between modules %s and %s".formatted(
                                current.name().getLocalName(), dep.name().getLocalName())));
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
    private void tryResolveBelongsTo() throws MissingSourceInfoException {
        for (final Map.Entry<SourceIdentifier, SourceIdentifier> mapping : submoduleToParentMap.entrySet()) {
            final SourceIdentifier submoduleId = mapping.getKey();
            final SourceIdentifier parentId = mapping.getValue();

            final ResolvedSourceBuilder resolvedSubmodule = requireNonNull(involvedSourcesMap.get(submoduleId),
                String.format("Submodule %s was not resolved", submoduleId));
            final var submoduleInfo = (Submodule) resolvedSubmodule.context().getSourceInfo();
            final ResolvedSourceBuilder resolvedParent = requireNonNull(involvedSourcesMap.get(parentId),
                String.format("Parent module %s of submodule %s was not resolved", parentId, submoduleId));

            //double-check that the parent does satisfy this belongs-to
            verify(submoduleInfo.belongsTo().isSatisfiedBy(parentId));

            resolvedSubmodule.setBelongsTo(submoduleInfo.belongsTo().prefix().getLocalName(), resolvedParent);
        }
    }

    /**
     * Includes of the same parent module can form circular dependencies. That's why we need
     * to process them differently - after all other dependencies have been resolved.
     */
    private void tryResolveSiblings() throws MissingSourceInfoException {
        final var iterator = unresolvedSiblingsMap.entrySet().iterator();

        while (iterator.hasNext()) {
            final Map.Entry<ResolvedSourceBuilder, Set<SourceIdentifier>> entry = iterator.next();
            final ResolvedSourceBuilder resolvedSource = entry.getKey();
            final Set<SourceIdentifier> siblings = entry.getValue();

            for (final SourceIdentifier sibling : siblings) {
                final ResolvedSourceBuilder resolvedSibling = requireNonNull(involvedSourcesMap.get(sibling),
                    String.format("Included module %s of module %s was not resolved", sibling,
                        resolvedSource.context().getSourceInfo().sourceId()));
                resolvedSource.addInclude(resolvedSibling);
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
        final var newResolvedBuilder = new ResolvedSourceBuilder(allContexts.get(id), allSources.get(id));
        involvedSourcesMap.put(id, newResolvedBuilder);
        final var potentials = involvedSourcesGrouped.get(id.name());
        if (potentials != null) {
            potentials.add(id);
        } else {
            involvedSourcesGrouped.put(id.name(), newMatchSetWith(id));
        }
        return newResolvedBuilder;
    }

    private void mapSiblings(final ResolvedSourceBuilder newResolved, final List<SourceIdentifier> includedSiblings) {
        final var siblings = unresolvedSiblingsMap.computeIfAbsent(newResolved, k -> new HashSet<>());
        siblings.addAll(includedSiblings);
    }

    private boolean isIncludedSibling(final SourceIdentifier current, final SourceDependency dependency,
            final SourceIdentifier dependencyId) {
        if (!(dependency instanceof Include)) {
            return false;
        }

        final var currentParent = findSatisfyingParentForSubmodule(current);
        final var theirParent = findSatisfyingParentForSubmodule(dependencyId);

        return currentParent != null && currentParent.equals(theirParent);
    }

    private @NonNull Set<SourceIdentifier> findAmongResolved(final Unqualified name) {
        final var resolvedMatchingQName = involvedSourcesGrouped.get(name);
        return Objects.requireNonNullElseGet(resolvedMatchingQName, Set::of);
    }

    private @NonNull Set<SourceIdentifier> findAmongAll(final Unqualified name) {
        final var resolvedMatchingQName = allSourcesMapped.get(name);
        return Objects.requireNonNullElseGet(resolvedMatchingQName, Set::of);
    }

    private SourceIdentifier findSatisfyingParentForSubmodule(final SourceIdentifier submoduleId) {
        final var sourceInfo = allSources.get(submoduleId);
        if (sourceInfo == null || sourceInfo instanceof SourceInfo.Module) {
            return null;
        }

        final var submoduleBelongsTo = requireNonNull((Submodule) sourceInfo).belongsTo();
        final var satisfied = findSatisfied(involvedSourcesGrouped.get(submoduleBelongsTo.name()), submoduleBelongsTo);
        if (satisfied != null) {
            return satisfied;
        }
        return findSatisfied(allSourcesMapped.get(submoduleBelongsTo.name()), submoduleBelongsTo);
    }

    private static SourceIdentifier findSatisfied(final Collection<SourceIdentifier> candidates,
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
        final var sourceInfo = allSources.get(id);
        final var dependencies = new HashSet<SourceDependency>();
        dependencies.addAll(sourceInfo.imports());
        dependencies.addAll(sourceInfo.includes());
        return dependencies;
    }
}
