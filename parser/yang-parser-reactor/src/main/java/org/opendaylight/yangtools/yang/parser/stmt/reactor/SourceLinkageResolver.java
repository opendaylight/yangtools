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
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Submodule;
import org.opendaylight.yangtools.yang.model.spi.stmt.ImmutableNamespaceBinding;
import org.opendaylight.yangtools.yang.parser.source.ResolvedSourceInfo;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.YangVersionLinkageException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.SourceLinkageResolver.ResolvedSourceContext;

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

    private final List<ReactorSource<?>> mainSources = new ArrayList<>();
    private final List<ReactorSource<?>> libSources = new ArrayList<>();

    private final Map<SourceIdentifier, ReactorSource<?>> allSources = new HashMap<>();

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
    private final Map<ResolvedSourceBuilder, Set<SourceIdentifier>> unresolvedSiblingsMap = new HashMap<>();

    private SourceLinkageResolver(
            final @NonNull Collection<ReactorSource<?>> withMainSources,
            // FIXME: this forces libSource materialzation -- we want to do that lazily
            final @NonNull Collection<ReactorSource<?>> withLibSources) {
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

    @NonNullByDefault
    private static Collection<ReactorSource<?>> initializeSources(final Collection<BuildSource<?>> buildSources)
            throws ReactorException, SourceSyntaxException {
        final var contexts = new HashSet<ReactorSource<?>>();
        for (final var buildSource : buildSources) {
            final ReactorSource<?> reactorSource;
            try {
                reactorSource = buildSource.ensureReactorSource();
            } catch (IOException e) {
                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.INIT, buildSource.sourceId(), e);
            }
            contexts.add(reactorSource);
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

        final var allResolved = new LinkedHashMap<ReactorSource<?>, ResolvedSourceInfo>(involvedSourcesMap.size());
        for (var involvedSource : involvedSourcesMap.entrySet()) {
            final var fullyResolved = involvedSource.getValue().build(allResolved);
            allResolved.put(involvedSource.getValue().reactorSource(), fullyResolved);
        }

        final var result = new ArrayList<ResolvedSourceContext>(allResolved.size());
        for (var entry : allResolved.entrySet()) {
            final var source = entry.getKey();
            final var resolved = entry.getValue();

            final var prefixToModule = new HashMap<Unqualified, QNameModule>();
            // all resolved imports
            for (var dep : resolved.imports()) {
                putPrefix(prefixToModule, dep.prefix(), dep.qname());
            }

            // the prefix under which the module is known as well as its claim to a namespace+revision
            final QName currentModule;
            switch (source.sourceInfo()) {
                case SourceInfo.Module info -> {
                    putPrefix(prefixToModule, info.prefix(), resolved.qnameModule());

                    // FIXME: info.moduleName() should and resolved.qnameModule() should always be the same
                    currentModule = info.moduleName().intern();
                }
                case SourceInfo.Submodule info -> {
                    // FIXME: missing @NonNull: this should be ensured through class hierarchy
                    final var parentModule = resolved.belongsTo().parentModuleQname();
                    putPrefix(prefixToModule, info.belongsTo().prefix(), parentModule);

                    // FIXME: this should live in info, just like moduleName() does
                    currentModule = info.sourceId().name().bindTo(
                        QNameModule.ofRevision(parentModule.namespace(), info.latestRevision()));
                }
            }

            result.add(new ResolvedSourceContext(new SourceSpecificContext(source.global(), source.sourceInfo(),
                new ImmutableNamespaceBinding(currentModule, Map.copyOf(prefixToModule)),
                source.toStreamSource(prefixToModule)), resolved));
        }

        return List.copyOf(result);
    }

    // FIXME: this smells of a builder for ImmutablePrefixResolver or similar
    private static void putPrefix(final HashMap<Unqualified, QNameModule> prefixToModule, final Unqualified prefix,
            final QNameModule module) {
        final var prev = prefixToModule.putIfAbsent(requireNonNull(prefix), requireNonNull(module));
        if (prev != null) {
            throw new IllegalArgumentException("Attempted to remap prefix %s from %s to %s".formatted(
                prefix.getLocalName(), prev, module));
        }
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

    private void mapSources(final Collection<ReactorSource<?>> sources) {
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
            tryResolveDependenciesOf(mainSource.sourceInfo().sourceId());
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
            final var includedSiblings = new LinkedList<SourceIdentifier>();
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

                for (var resolvedDep : resolvedDependencies.entrySet()) {
                    final var dep = resolvedDep.getKey();
                    // Find the best match for this dependency among the resolved modules
                    final var satisfiedDepId = requireNonNull(findSatisfied(
                        findAmongResolved(resolvedDep.getValue()), dep));
                    final var depModule = involvedSourcesMap.get(satisfiedDepId);

                    final var currentVersion = newResolved.yangVersion();
                    final var dependencyVersion = depModule.yangVersion();

                    if (dep instanceof Import importedDep) {
                        // Version 1 sources must not import-by-revision Version 1.1 modules
                        if (importedDep.revision() != null && currentVersion == YangVersion.VERSION_1) {
                            if (dependencyVersion != YangVersion.VERSION_1) {
                                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, current,
                                    new YangVersionLinkageException(refOf(current, importedDep.sourceRef()),
                                        "Cannot import by revision version %s module %s", dependencyVersion,
                                            resolvedDep.getValue().getLocalName()));
                            }
                        }
                        newResolved.addImport(importedDep.prefix(), depModule);
                    } else {
                        if (currentVersion != dependencyVersion) {
                            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, current,
                                new YangVersionLinkageException(refOf(current, dep.sourceRef()),
                                    "Cannot include a version %s submodule %s in a version %s module %s",
                                    dependencyVersion, resolvedDep.getValue().getLocalName(), currentVersion,
                                    current.name().getLocalName()));
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
                        throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, current,
                            new InferenceException(new SourceStatementDeclaration(current),
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
                throw new InferenceException(new SourceStatementDeclaration(submoduleId),
                    "Submodule %s was not resolved", submoduleId);
            }

            // FIXME: ensure this through type safety
            final var submoduleInfo = (Submodule) resolvedSubmodule.reactorSource().sourceInfo();
            final var resolvedParent = involvedSourcesMap.get(parentId);
            if (resolvedParent == null) {
                throw new InferenceException(new SourceStatementDeclaration(submoduleId),
                    "Parent module %s of submodule %s was not resolved", parentId, submoduleId);
            }

            // FIXME: better message and/or better exception
            //double-check that the parent does satisfy this belongs-to
            verify(submoduleInfo.belongsTo().isSatisfiedBy(parentId));

            resolvedSubmodule.setBelongsTo(submoduleInfo.belongsTo().prefix(), resolvedParent);
        }
    }

    /**
     * Includes of the same parent module can form circular dependencies. That's why we need
     * to process them differently - after all other dependencies have been resolved.
     */
    private void tryResolveSiblings() {
        final var iterator = unresolvedSiblingsMap.entrySet().iterator();

        while (iterator.hasNext()) {
            final Map.Entry<ResolvedSourceBuilder, Set<SourceIdentifier>> entry = iterator.next();
            final ResolvedSourceBuilder resolvedSource = entry.getKey();
            final Set<SourceIdentifier> siblings = entry.getValue();

            for (final SourceIdentifier sibling : siblings) {
                final var resolvedSibling = involvedSourcesMap.get(sibling);
                if (resolvedSibling == null) {
                    final var sourceId = resolvedSource.reactorSource().sourceId();
                    throw new InferenceException(new SourceStatementDeclaration(sourceId),
                        "Included submodule %s of module %s was not resolved", sibling, sourceId);
                }
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
        // FIXME: not needed if currentParent == null?
        final var theirParent = findSatisfyingParentForSubmodule(dependencyId);

        return currentParent != null && currentParent.equals(theirParent);
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
        return sourceRef != null ? sourceRef : new SourceStatementDeclaration(sourceId);
    }
}
