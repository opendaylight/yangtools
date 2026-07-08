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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.RevisionUnion;
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

    /**
     * Comparator ordering {@link ResolvedSourceBuilder}s by their {@link ResolvedSourceBuilder#sourceId()} so that
     * {@link SourceIdentifier#name()}s are encountered in their natural order and the corresponding
     * {@link SourceIdentifier#revision()}s are encountered in reverse order, that is newest revision first.
     */
    @NonNullByDefault
    private static final Comparator<ResolvedSourceBuilder<?>> BUILDER_BY_SOURCEID = (left, right) -> {
        final var leftId = left.sourceId();
        final var rightId = right.sourceId();
        final var cmp = leftId.name().compareTo(rightId.name());
        return cmp != 0 ? cmp
            // swapped arguments to get the reversed comparison
            : Revision.compare(rightId.revision(), leftId.revision());
    };

    /**
     * The set of required module sources. We are using insertion order to ensure predictable ordering.
     */
    private final @NonNull LinkedHashMap<SourceInfoRef.OfModule, ResolvedSourceBuilder.ForModule> requiredModules =
        new LinkedHashMap<>();
    /**
     * The set of required submodule sources, indexed by the name of the module they claim to belong to. Predictable
     * iteration order is provided via {@link #requiredSubmodules()}.
     */
    @NonNullByDefault
    private final HashMultimap<Unqualified, ResolvedSourceBuilder.ForSubmodule> requiredSubmodules =
        HashMultimap.create();

    // As per RFC6020, every import-by-revision has to resolve to the same module. We are using a table, as that also
    // allows us quickly find all modules with the same name -- and have them ordered with latest revision first.
    private final @NonNull Table<Unqualified, RevisionUnion, ResolvedSourceBuilder.ForModule> modulesByName =
        Tables.<Unqualified, RevisionUnion, ResolvedSourceBuilder.ForModule>newCustomTable(new HashMap<>(),
            () -> new TreeMap<>(Comparator.reverseOrder()));

    // Our implementation constraints are looser than RFC6020/RFC7895/RFC7950/RFC8525 in that each module can be
    // implemented with multiple revisions, as long as each XMLNamespace/Revision combination is introduced by exactly
    // one source.
    private final @NonNull HashMap<QNameModule, ResolvedSourceBuilder.ForModule> modulesByNamespace = new HashMap<>();

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
    //        - submodules, which can have naming conflicts as long as the conflicting submodules end up being mapped to
    //          distinct modules by the resolution logic, as tracked in requiredSubmodules -- but that is a difficult
    //          topic, as we currently handle only the case when the modules have different names
    private final SortedSetMultimap<Unqualified, SourceIdentifier> allSourcesMapped =
        Multimaps.newSortedSetMultimap(new HashMap<>(), () -> new TreeSet<>(BY_REVISION));

    /**
     * Map of involved sources with the same name.
     */
    // FIXME: replace users of this map with lookups to modulesByName and requiredSubmodules
    private final SortedSetMultimap<Unqualified, SourceIdentifier> involvedSourcesGrouped =
        Multimaps.newSortedSetMultimap(new HashMap<>(), () -> new TreeSet<>(BY_REVISION));

    /**
     * Map of involved sources ordered according to the resolution order (LinkedHashMap keeps the insertion order).
     */
    // FIXME: replace users of this map with lookups into requiredModules and requiredSubmodules
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

    private SourceLinkageResolver() {
        // hidden on purpose
    }

    /**
     * {@return the set of required module sources in the order in which they became required}.
     */
    @NonNullByDefault
    private List<ResolvedSourceBuilder.ForModule> requiredModules() {
        return List.copyOf(requiredModules.values());
    }

    /**
     * {@return the set of required submodule sources, in order dictated by {@link #BUILDER_BY_SOURCEID}}.
     */
    @NonNullByDefault
    private List<ResolvedSourceBuilder.ForSubmodule> requiredSubmodules() {
        final var values = requiredSubmodules.values();
        return switch (values.size()) {
            case 0 -> List.of();
            case 1 -> List.of(values.iterator().next());
            default -> values.stream().sorted(BUILDER_BY_SOURCEID).toList();
        };
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

        final var resolver = new SourceLinkageResolver();
        for (var source : mainSources) {
            switch (source) {
                case SourceInfoRef.OfModule module -> resolver.addRequiredModule(module);
                case SourceInfoRef.OfSubmodule submodule -> resolver.addRequiredSubmodule(submodule);
            }
            resolver.populateLegacyMaps(source);
        }
        return resolver.resolveInvolvedSources(libSources);
    }

    @NonNullByDefault
    private List<ResolvedSourceInfo> resolveInvolvedSources(final Set<SourceInfoRef> libSources)
            throws ReactorException {
        // FIXME: the order of operations is wrong here:
        //          1) we populate 'allSources' and 'allSourcesMapped' with libSources
        //          2) we resolve belongs-to dependencies based on 'allSources', including those introduced in 1)
        //          3) we expand the set of required modules based on 2)
        //          4) we perform a single pass over required sources and resolve each recursively, populating auxiliary
        //             maps, e.g. 'involvedSourcesMap', as a side effect
        //
        // What we need to achieve consistent linkage along three axis:
        //   - imported module
        //   - included submodule
        //   - parent module
        // such that the set of requiredModules and requiredSubmodules, as populated at the entry into this method, is
        // completely resolved.
        // If any linkage is found to be unsatisfied, we need to consult libSources to find the
        // minimal set of sources that result in such linkage.
        //
        // In order this, we need to resolve the following five cases:
        //   - import-by-revision, which is an exact match
        //   - import-without-revision, which is a wildcard match
        //   - include-by-revision, which is an exact match
        //   - include-without-revision, which is a wildcard match
        //   - belongs-to, which is a wildcard match
        //
        // YANG leaves the semantics of resolving import-without-revision and include-without-revision in presence of
        // multiple candidates undefined, but we define them as resolving to the latest revision available. Notably we
        // do not consider newer versions in libSources unless they are explicitly made required.
        //
        // Furthermore YANG semantics of belongs-to statement does not provide any guidance in case of multiple
        // revisions being involved, but it specifies that the mapping must be consistent with include statement.
        //
        // The approach we take here is to work in order of decreasing certainty and contribution to invariants:
        //   - import-by-revision
        //   - include-by-revision
        //   - import-without-revision
        //   - include-without-reviesion
        //   - belongs-to
        // so that their invariants are established in this order. If a step ends up introducing new invariants to a
        // previous step, we restart that step. For example, if a libSource satisfying an include-by-revision introduces
        // new import-by-revision dependencies, we restart the algorithm.
        //
        // This also means that import-without-revision and include-without-revision resolution can naturally happen
        // multiple times. For import-without-revision subsequent resolution result must have a newer revision. For
        // include-without-revision the situation is somewhat more complicated, as explained next.
        //
        // The most problematic is belongs-to, as it is inherently inaccurate, but impacts the set of required modules
        // and constrains the set of sources which can satisfy include dependencies. Here we want to do some unspecified
        // magic to take transitive include linkage to guide which module to pick.

        for (var source : libSources) {
            populateLegacyMaps(source);
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

        // ensures every required submodule has its parent module present in required modules as well
        for (var submodule : requiredSubmodules()) {
            final var sourceInfo = submodule.sourceInfo();
            final var parentId = submoduleToParentMap.get(sourceInfo.sourceId());
            if (parentId == null) {
                throw new VerifyException(submodule + " does not have a parent");
            }
            final var parentCtx = allSources.get(parentId);
            if (parentCtx == null) {
                throw new VerifyException(submodule + " parent " + parentId + " not found");
            }
            if (!(parentCtx instanceof SourceInfoRef.OfModule parentModule)) {
                throw new VerifyException(submodule + " parent " + parentId + " resolved to non-module " + parentCtx);
            }

            // only add to requirements if it is not present
            if (!requiredModules.containsKey(parentModule)) {
                addRequiredModule(parentModule);
            }
        }

        // resolve imports and non-sibling includes for all required sources. Sibling includes are identified, but are
        // resolved later (at end of this method).
        for (var mainSource : Iterables.concat(requiredModules.values(), requiredSubmodules())) {
            final var rootId = mainSource.sourceId();

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
            final var workChain = new ArrayDeque<@Nullable SourceIdentifier>();
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
        // FIXME: just assert (or prove) there are no unresolved items and return a copy of a
        //        'List<ResolvedSourceInfo> resolvedSources' field, which is populated as soon as a
        //        ResolvedSourceBuilder is known to have been fully resolved: that is what the tail of
        //        resolveInvolvedSources() does and we want to return the result in the order in which the sources have
        //        been resolved.
        final var allResolved =
            LinkedHashMap.<SourceInfoRef, ResolvedSourceInfo>newLinkedHashMap(involvedSourcesMap.size());
        for (var involvedSource : involvedSourcesMap.values()) {
            allResolved.put(involvedSource.infoRef(), involvedSource.build());
        }
        return List.copyOf(allResolved.values());
    }

    // resolve all import-by-revision statements, expanding requiredModules with libSources as needed
    private void resolveImportByRevision(final Set<SourceInfoRef> libSources) {
        boolean retry;
        do {
            retry = false;
            for (var builder : Iterables.concat(requiredModules(), requiredSubmodules())) {
                for (var dependency : builder.missingImports()) {
                    final var revision = dependency.revision();
                    if (revision == null || resolveImportByRevision(builder, dependency, revision)) {
                        continue;
                    }

                    // FIXME: find among libSources, bring in, resolve and mark the fact we have modified modules
                }
            }
        } while (retry);
    }

    @NonNullByDefault
    private boolean resolveImportByRevision(final ResolvedSourceBuilder<?> builder, final Import dependency,
            final Revision revision) {
        final var existing = modulesByName.get(dependency.name(), revision);
        if (existing == null) {
            return false;
        }
        builder.resolveImport(dependency, existing);
        return true;
    }

    @NonNullByDefault
    private void addRequiredModule(final SourceInfoRef.OfModule module) throws ReactorException {
        final var builder = new ResolvedSourceBuilder.ForModule(module);
        final var prev = requiredModules.putIfAbsent(module, builder);
        if (prev != null) {
            throw new VerifyException("Attempted to add already-required " + module);
        }

        final var sourceInfo = module.info();
        final var sourceId = sourceInfo.sourceId();
        final var namespace = sourceInfo.moduleName().getModule();

        // TODO: The exceptions here are less than perfect. We should not be reporting a combination of
        //       ReactorException + InferenceException, but rather a dedicated exception which identifies the two
        //       SourceInfoRefs involved and have SourceLinkageBuilder map them back to ReactorSource/BuildSource and
        //       their corresponding location

        final var prevByNamespace = modulesByNamespace.putIfAbsent(namespace, builder);
        if (prevByNamespace != null) {
            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                new InferenceException(sourceId.toReference(),
                    "Module namespace collision: %s%s is already defined", namespace.namespace(),
                    formatRevision(namespace.revision())));
        }

        final var prevBySourceId = modulesByName.row(sourceId.name())
            .putIfAbsent(RevisionUnion.of(sourceId.revision()), builder);
        if (prevBySourceId != null) {
            throw new SomeModifiersUnresolvedException(ModelProcessingPhase.SOURCE_LINKAGE, sourceId,
                new InferenceException(sourceId.toReference(),
                    "Module name collision: %s%s is already defined", sourceId.name(),
                    formatRevision(sourceId.revision())));
        }
    }

    @NonNullByDefault
    private static String formatRevision(final @Nullable Revision revision) {
        return revision == null ? "" : "@" + revision;
    }

    @NonNullByDefault
    private void addRequiredSubmodule(final SourceInfoRef.OfSubmodule submodule) {
        final var siblings = requiredSubmodules.get(submodule.info().belongsTo().name());
        // TODO: yeah, not exactly scalable, but works
        for (var sibling : siblings) {
            if (submodule.equals(sibling.infoRef())) {
                throw new VerifyException("Attempted to add already-required " + submodule);
            }
        }
        verify(siblings.add(new ResolvedSourceBuilder.ForSubmodule(submodule)));
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
