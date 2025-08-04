/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
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
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Submodule;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ResolvedSourceInfo;

/**
 * Identifies and organizes the main sources and library sources used to build the SchemaContext.
 * Any library sources that aren’t referenced are skipped. The referenced sources are cross-checked for consistency
 * and linked together according to their dependencies (includes, imports, belongs-to)
 */
public final class SourceLinkageResolver {

    record ResolvedSourceContext(SourceSpecificContext context, ResolvedSourceInfo resolvedSourceInfo) {
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

    private final List<SourceSpecificContext> mainSources = new LinkedList<>();
    private final List<SourceSpecificContext> libSources = new LinkedList<>();

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

    SourceLinkageResolver(
        final @NonNull Collection<SourceSpecificContext> withMainSources,
        final @NonNull Collection<SourceSpecificContext> withLibSources) {
        this.mainSources.addAll(requireNonNull(withMainSources));
        this.libSources.addAll(requireNonNull(withLibSources));
    }

    /**
     * Processes all main sources and library sources. Finds out which ones are involved and resolves dependencies
     * between them.
     * @return list of resolved sources
     */
    List<ResolvedSourceContext> resolveInvolvedSources() throws SomeModifiersUnresolvedException {
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

        final Map<SourceSpecificContext, ResolvedSourceInfo> allResolved =
            new LinkedHashMap<>(involvedSourcesMap.size());
        for (Map.Entry<SourceIdentifier, ResolvedSourceBuilder> involvedSource : involvedSourcesMap.entrySet()) {
            final ResolvedSourceInfo fullyResolved = involvedSource.getValue().build(allResolved);
            allResolved.put(involvedSource.getValue().getContext(), fullyResolved);
        }

        return allResolved
            .entrySet()
            .stream()
            .map(entry -> new ResolvedSourceContext(entry.getKey(), entry.getValue()))
            .toList();
    }

    private void mapSubmodulesToParents() throws SomeModifiersUnresolvedException {
        for (Map.Entry<SourceIdentifier, SourceInfo> source : allSources.entrySet()) {
            SourceInfo sourceInfo = source.getValue();
            if (sourceInfo instanceof SourceInfo.Submodule submoduleInfo) {
                final Unqualified parentName = submoduleInfo.belongsTo().name();
                final SourceIdentifier parentId = findSatisfied(allSourcesMapped.get(submoduleInfo.belongsTo().name()),
                    submoduleInfo.belongsTo());
                if (parentId == null) {
                    throwBelongsToNotFoundException(source.getKey(), parentName);
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

    private void mapSource(final SourceIdentifier sourceId) {
        final Unqualified name = sourceId.name();
        final Set<SourceIdentifier> matches = allSourcesMapped.get(name);
        if (matches != null) {
            matches.add(sourceId);
            return;
        }

        allSourcesMapped.put(name, newMatchSetWith(sourceId));
    }

    private void tryResolveDependencies() throws SomeModifiersUnresolvedException {
        for (final SourceSpecificContext mainSource : mainSources) {
            tryResolveDependenciesOf(mainSource.getSourceInfo().sourceId());
        }
    }

    /**
     * Ensures that every submodule in main-sources has it's parent module present among main-sources as well.
     */
    private void reuniteMainSubmodulesWithParents() {
        // use classic for loop to enable expansion of the mainSources list
        for (int i = 0; i < mainSources.size(); i++) {
            final SourceInfo sourceInfo = mainSources.get(i).getSourceInfo();

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
        final Set<SourceIdentifier> visitedSources = new HashSet<>();

        // Sources currently being resolved (active dependency path)
        final Set<SourceIdentifier> inProgress = new HashSet<>();

        // Sources that need processing
        final Deque<SourceIdentifier> workChain = new ArrayDeque<>();
        workChain.add(rootId);

        while (!workChain.isEmpty()) {
            final SourceIdentifier current = workChain.pollFirst();
            if (visitedSources.contains(current)) {
                continue;
            }

            inProgress.add(current);

            final Set<SourceDependency> dependencies = getDependenciesOf(current);
            final Map<SourceDependency, Unqualified> resolvedDependencies = new HashMap<>();
            final List<SourceIdentifier> unresolvedDependencies = new ArrayList<>();
            final List<SourceIdentifier> includedSiblings = new LinkedList<>();
            boolean allResolved = true;

            for (SourceDependency dependency : dependencies) {
                final Unqualified dependencyName = dependency.name();

                // Find the best match (by revision) among all modules
                final SourceIdentifier match = findSatisfied(findAmongAll(dependencyName), dependency);
                if (match == null) {
                    // Dependency is missing
                    if (dependency instanceof Import) {
                        throwUnresolvedException(current, "Imported module %s was not found. [at %s]",
                            dependencyName.getLocalName(), current.name().getLocalName());
                    } else {
                        throwUnresolvedException(current, "Included submodule %s was not found. [at %s]",
                            dependencyName.getLocalName(), current.name().getLocalName());
                    }
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
                final ResolvedSourceBuilder newResolved = addResolvedSource(current);

                for (Map.Entry<SourceDependency, Unqualified> resolvedDep : resolvedDependencies.entrySet()) {
                    // Find the best match for this dependency among the resolved modules
                    final SourceIdentifier satisfiedDepId = requireNonNull(findSatisfied(
                        findAmongResolved(resolvedDep.getValue()), resolvedDep.getKey()));
                    final ResolvedSourceBuilder depModule = involvedSourcesMap.get(satisfiedDepId);

                    final YangVersion currentVersion = newResolved.getYangVersion();
                    final YangVersion dependencyVersion = depModule.getYangVersion();

                    if (resolvedDep.getKey() instanceof Import importedDep) {
                        // Version 1 sources must not import-by-revision Version 1.1 modules
                        if (importedDep.revision() != null && currentVersion == YangVersion.VERSION_1) {
                            if (dependencyVersion != YangVersion.VERSION_1) {
                                throwUnresolvedException(current,
                                    "Cannot import by revision version %s module %s at %s",
                                    depModule.getYangVersion().toString(),
                                    resolvedDep.getValue().getLocalName(), current.name().getLocalName());
                            }
                        }
                        newResolved.addImport(importedDep.prefix().getLocalName(), depModule);
                    } else {
                        if (currentVersion != dependencyVersion) {
                            throwUnresolvedException(current,
                                "Cannot include a version %s submodule %s in a version %s module %s",
                                depModule.getYangVersion().toString(), resolvedDep.getValue().getLocalName(),
                                newResolved.getYangVersion().toString(), current.name().getLocalName());
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

                for (SourceIdentifier dep : unresolvedDependencies) {

                    // Check circular dependency
                    if (inProgress.contains(dep)) {
                        throwCircularDependencyException(current, current, dep);
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
        for (final Map.Entry<SourceIdentifier, SourceIdentifier> mapping : submoduleToParentMap.entrySet()) {
            final SourceIdentifier submoduleId = mapping.getKey();
            final SourceIdentifier parentId = mapping.getValue();

            final ResolvedSourceBuilder resolvedSubmodule = requireNonNull(involvedSourcesMap.get(submoduleId),
                String.format("Submodule %s was not resolved", submoduleId));
            final SourceInfo.Submodule submoduleInfo = (Submodule) resolvedSubmodule.getContext().getSourceInfo();
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
    private void tryResolveSiblings() {
        final Iterator<Map.Entry<ResolvedSourceBuilder, Set<SourceIdentifier>>> iterator =
            unresolvedSiblingsMap.entrySet().iterator();

        while (iterator.hasNext()) {
            final Map.Entry<ResolvedSourceBuilder, Set<SourceIdentifier>> entry = iterator.next();
            final ResolvedSourceBuilder resolvedSource = entry.getKey();
            final Set<SourceIdentifier> siblings = entry.getValue();

            for (final SourceIdentifier sibling : siblings) {
                final ResolvedSourceBuilder resolvedSibling = requireNonNull(involvedSourcesMap.get(sibling),
                    String.format("Included module %s of module %s was not resolved", sibling,
                        resolvedSource.getContext().getSourceInfo().sourceId()));
                resolvedSource.addInclude(resolvedSibling);
            }
            iterator.remove();
        }
    }

    /**
     * Creates a new ResolvedSource.Builder for this Source and adds it to the map of Involved-Sources and
     * Involved-Sources-Grouped. It's inclusion in these maps signifies that all the dependencies of this Source had
     * been resolved.
     * @param id of the resolved Source
     * @return ResolvedSource.Builder of the Source.
     */
    private ResolvedSourceBuilder addResolvedSource(final SourceIdentifier id) {
        if (involvedSourcesMap.containsKey(id)) {
            return involvedSourcesMap.get(id);
        }
        final ResolvedSourceBuilder newResolvedBuilder = new ResolvedSourceBuilder(allContexts.get(id));
        involvedSourcesMap.put(id, newResolvedBuilder);
        final Set<SourceIdentifier> potentials = involvedSourcesGrouped.get(id.name());
        if (potentials != null) {
            potentials.add(id);
        } else {
            involvedSourcesGrouped.put(id.name(), newMatchSetWith(id));
        }
        return newResolvedBuilder;
    }

    private void mapSiblings(final ResolvedSourceBuilder newResolved, final List<SourceIdentifier> includedSiblings) {
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

        return currentParent != null && currentParent.equals(theirParent);
    }

    private @NonNull Set<SourceIdentifier> findAmongResolved(final Unqualified name) {
        final Set<SourceIdentifier> resolvedMatchingQName = involvedSourcesGrouped.get(name);
        return Objects.requireNonNullElseGet(resolvedMatchingQName, Set::of);
    }

    private @NonNull Set<SourceIdentifier> findAmongAll(final Unqualified name) {
        final Set<SourceIdentifier> resolvedMatchingQName = allSourcesMapped.get(name);
        return Objects.requireNonNullElseGet(resolvedMatchingQName, Set::of);
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

    private void throwCircularDependencyException(final SourceIdentifier throwingSource,
        final SourceIdentifier currentId, final SourceIdentifier dependencyId) throws SomeModifiersUnresolvedException {
        this.throwUnresolvedException(throwingSource, "Found circular dependency between modules %s and %s",
            currentId.name().getLocalName(), dependencyId.name().getLocalName());
    }

    private void throwBelongsToNotFoundException(final SourceIdentifier submoduleId,
        final Unqualified parentName) throws SomeModifiersUnresolvedException {
        this.throwUnresolvedException(submoduleId, "Module %s from belongs-to was not found.",
            parentName.getLocalName());
    }

    private void throwUnresolvedException(final SourceIdentifier throwingSource, final String messageFormat,
        String... messageSources) throws SomeModifiersUnresolvedException {
        this.throwUnresolvedException(throwingSource, messageFormat, Arrays.stream(messageSources).toList());
    }

    private void throwUnresolvedException(final SourceIdentifier throwingSource, final String messageFormat,
        final List<String> args) throws SomeModifiersUnresolvedException {
        final String message = String.format(messageFormat, args.stream().map(s -> "[" + s + "]").toArray());
        throw new SomeModifiersUnresolvedException(
            ModelProcessingPhase.INIT,
            throwingSource,
            new IllegalStateException(message)
        );
    }

    public static void fillNamespaces(final ResolvedSourceInfo resolvedSource) {
        final RootStatementContext<?, ?, ?> rootStmt = (RootStatementContext<?, ?, ?>)resolvedSource.root();
        populateRootNamespaces(rootStmt, resolvedSource);
    }

    private static void populateRootNamespaces(final RootStatementContext<?, ?, ?> root,
        final ResolvedSourceInfo resolvedSource) {

        fillCommonNamespaces(root, resolvedSource);
        fillImportedNamespaces(root, resolvedSource);

        if (resolvedSource.belongsTo() != null) {
            fillSubmoduleNamespaces(root, resolvedSource);
        } else {
            fillModuleNamespaces(root, resolvedSource);
        }
    }

    private static void fillCommonNamespaces(final RootStatementContext<?, ?, ?> root,
        final ResolvedSourceInfo resolvedSource) {
        root.addToNamespace(ParserNamespaces.MODULECTX_TO_QNAME, root, resolvedSource.qnameModule());
        fillImportedNamespaces(root, resolvedSource);
        fillIncludedNamespaces(root, resolvedSource);
    }

    private static void fillIncludedNamespaces(final RootStatementContext<?, ?, ?> root,
        final ResolvedSourceInfo resolvedSource) {
        for (var anInclude : resolvedSource.includes()) {
            root.addToNs(ParserNamespaces.INCLUDED_MODULE, anInclude.includeId(), anInclude.rootContext());
            root.addToNs(ParserNamespaces.INCLUDED_SUBMODULE_NAME_TO_MODULECTX,
                anInclude.includeId().name(), anInclude.rootContext());
        }
    }

    private static void fillImportedNamespaces(final RootStatementContext<?, ?, ?> root,
        final ResolvedSourceInfo resolvedSource) {
        for (var entry : resolvedSource.imports().entrySet()) {

            final ResolvedSourceInfo imported = entry.getValue();
            final QNameModule qnameModule = imported.qnameModule();
            final SourceIdentifier sourceId = imported.sourceId();
            final var importContext = imported.root();

            verifyNotNull(importContext, "Root context of imported module %s (imported by %s) was not resolved",
                qnameModule, resolvedSource.qnameModule());

            root.addToNamespace(ParserNamespaces.IMPORTED_MODULE, sourceId, importContext);
        }
    }

    private static void fillSubmoduleNamespaces(final RootStatementContext<?, ?, ?> root,
        final ResolvedSourceInfo resolvedSource) {
        root.addToNs(ParserNamespaces.SUBMODULE, root.getRootIdentifier(),
            (StmtContext<Unqualified, SubmoduleStatement, SubmoduleEffectiveStatement>) root);

        final var belongsTo = resolvedSource.belongsTo();
        root.addToNamespace(ParserNamespaces.BELONGSTO_PREFIX_TO_QNAME_MODULE, belongsTo.prefix(),
            belongsTo.parentModuleQname());
        root.addToNs(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULECTX, belongsTo.prefix(), belongsTo.parentRoot());
    }

    private static void fillModuleNamespaces(final RootStatementContext<?, ?, ?> root,
        final ResolvedSourceInfo resolvedSource) {
        root.addToNs(ParserNamespaces.MODULE, root.getRootIdentifier(),
            (StmtContext<Unqualified, ModuleStatement, ModuleEffectiveStatement>) root);
    }
}
