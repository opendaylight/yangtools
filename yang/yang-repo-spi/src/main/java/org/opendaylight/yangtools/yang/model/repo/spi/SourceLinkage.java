/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Submodule;
import org.opendaylight.yangtools.yang.parser.api.ImportResolutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The result of a single resolution attempt. Given a set of schema source identifiers and their corresponding
 * dependency information, the {@link #of(ImportResolutionMode, Map)} method creates a view of how consistent
 * the dependencies are. In particular, this detects whether any import/include/belongs-to statements are unsatisfied.
 */
// FIXME: improve this class to track and expose how wildcard imports were resolved.
//        That information will allow us to track "damage" to dependency resolution
//        as new models are added to a schema context.
abstract sealed class SourceLinkage permits RevisionSourceLinkage {
    private static final Logger LOG = LoggerFactory.getLogger(SourceLinkage.class);

    private final ImmutableList<SourceIdentifier> resolvedSources;
    private final ImmutableList<SourceIdentifier> unresolvedSources;
    private final ImmutableMultimap<SourceIdentifier, SourceDependency> unsatisfiedImports;

    SourceLinkage(final Map<SourceIdentifier, SourceInfo> depInfo) {
        // Submodules of the same parent module may 'include' each other, forming a cycle. Identify such "sibling"
        // includes up front so the build-up pass can leave them out of its ordering.
        final var siblingIncludes = siblingIncludes(depInfo);

        // First pass: build-up resolution over imports and non-sibling includes. A genuine cycle through those (e.g.
        // two modules importing each other) never resolves. Sibling includes are exempt and instead
        // validated by the second pass, mirroring the reactor's SourceLinkageResolver.
        final var resolved = HashMap.<SourceIdentifier, SourceInfo>newHashMap(depInfo.size());
        buildUp(new HashMap<>(depInfo), resolved, siblingIncludes);

        // Second pass: drop any resolved source whose sibling includes or 'belongs-to' are unsatisfied, cascading.
        // Hard-dependency cycles never entered 'resolved', so this can't resurrect them; sibling cycles survive as
        // their mutual targets are present.
        prune(resolved);

        resolvedSources = ImmutableList.copyOf(resolved.keySet());

        // Collect the unresolved sources and their outright-missing dependencies. Both output lists are keyed by the
        // content-derived SourceInfo.sourceId(), consistent with resolvedSources above.
        final var unresolved = ImmutableList.<SourceIdentifier>builder();
        final var unstatisfied = ImmutableMultimap.<SourceIdentifier, SourceDependency>builder();
        for (var info : depInfo.values()) {
            if (resolved.containsKey(info.sourceId())) {
                continue;
            }
            unresolved.add(info.sourceId());

            for (var dep : info.imports()) {
                if (!isKnown(depInfo.keySet(), dep)) {
                    unstatisfied.put(info.sourceId(), dep);
                }
            }
            for (var dep : info.includes()) {
                if (!isKnown(depInfo.keySet(), dep)) {
                    unstatisfied.put(info.sourceId(), dep);
                }
            }
            if (info instanceof Submodule submodule) {
                final var dep = submodule.belongsTo();
                if (!isKnown(depInfo.keySet(), dep)) {
                    unstatisfied.put(info.sourceId(), dep);
                }
            }
        }
        unresolvedSources = unresolved.build();
        unsatisfiedImports = unstatisfied.build();
    }

    /**
     * Build up {@code resolved} from {@code pending}, repeatedly promoting every source whose hard dependencies are
     * already resolved, until a full sweep promotes nothing more.
     */
    private void buildUp(final Map<SourceIdentifier, SourceInfo> pending,
            final Map<SourceIdentifier, SourceInfo> resolved, final Map<SourceIdentifier, Set<Include>> siblings) {
        boolean promoted;
        do {
            promoted = false;
            final var it = pending.values().iterator();
            while (it.hasNext()) {
                final var info = it.next();
                if (tryResolve(resolved.keySet(), info, siblings.getOrDefault(info.sourceId(), Set.of()))) {
                    LOG.debug("Resolved source {}", info.sourceId());
                    resolved.put(info.sourceId(), info);
                    it.remove();
                    promoted = true;
                }
            }
        } while (promoted);
    }

    /**
     * Prune {@code resolved}, repeatedly dropping every source which is no longer fully resolved, until a full sweep
     * drops nothing more. Dropping cascades, as a source may become unresolved once one of its dependencies is gone.
     */
    private void prune(final Map<SourceIdentifier, SourceInfo> resolved) {
        boolean dropped;
        do {
            dropped = false;
            final var it = resolved.values().iterator();
            while (it.hasNext()) {
                final var info = it.next();
                if (!isFullyResolved(resolved.keySet(), info)) {
                    LOG.debug("Dropping source {}: dependency no longer resolved", info.sourceId());
                    it.remove();
                    dropped = true;
                }
            }
        } while (dropped);
    }

    @NonNullByDefault
    static final SourceLinkage of(final ImportResolutionMode resolutionMode,
            final Map<SourceIdentifier, SourceInfo> depInfo) {
        return switch (resolutionMode) {
            case DEFAULT -> new RevisionSourceLinkage(depInfo);
        };
    }

    /**
     * {@return collection of sources which have been resolved}
     */
    final ImmutableList<SourceIdentifier> resolvedSources() {
        return resolvedSources;
    }

    /**
     * {@return core collection of sources which have not been resolved due to missing dependencies}
     */
    final ImmutableList<SourceIdentifier> unresolvedSources() {
        return unresolvedSources;
    }

    /**
     * Detailed information about which imports were missing. The key in the map is the source identifier of module
     * which was issuing an import, the values are imports which were unsatisfied.
     *
     * <p>Note that this map contains only imports which are missing from the reactor, not transitive failures.
     * Examples:
     * <ul>
     *   <li>if A imports B, B imports C, and both A and B are in the reactor, only B-&gt;C will be reported</li>
     *   <li>if A imports B and C, B imports C, and both A and B are in the reactor, A-&gt;C and B-&gt;C will be
     *       reported</li>
     * </ul>
     */
    final ImmutableMultimap<SourceIdentifier, SourceDependency> unsatisfiedImports() {
        return unsatisfiedImports;
    }

    /**
     * Build-up check: a source is resolvable once all of its imports and non-sibling includes are resolved. The
     * sibling includes in {@code siblingIncludes} and {@code belongs-to} are intentionally not required here.
     */
    private boolean tryResolve(final Collection<SourceIdentifier> resolved, final SourceInfo info,
            final Set<Include> siblingIncludes) {
        for (var dep : info.imports()) {
            if (!isKnown(resolved, dep)) {
                LOG.debug("Source {} is missing import {}", info.sourceId(), dep);
                return false;
            }
        }
        for (var dep : info.includes()) {
            if (!siblingIncludes.contains(dep) && !isKnown(resolved, dep)) {
                LOG.debug("Source {} is missing include {}", info.sourceId(), dep);
                return false;
            }
        }
        return true;
    }

    /**
     * Elimination check: a source remains resolved only while every one of its imports, includes (sibling ones
     * included) and {@code belongs-to} is satisfied by the still-resolved set.
     */
    private boolean isFullyResolved(final Collection<SourceIdentifier> resolved, final SourceInfo info) {
        for (var dep : info.imports()) {
            if (!isKnown(resolved, dep)) {
                return false;
            }
        }
        for (var dep : info.includes()) {
            if (!isKnown(resolved, dep)) {
                return false;
            }
        }
        return !(info instanceof Submodule submodule) || isKnown(resolved, submodule.belongsTo());
    }

    /**
     * {@return the includes of each submodule which point at a sibling submodule, i.e. one present in the reactor and
     *          sharing the same {@code belongs-to} parent module} The result is keyed by the content-derived
     *          {@link SourceInfo#sourceId()} of the including submodule.
     */
    private static Map<SourceIdentifier, Set<Include>> siblingIncludes(
            final Map<SourceIdentifier, SourceInfo> depInfo) {
        // Index present submodules by the name of their belongs-to parent module.
        final var byParent = ArrayListMultimap.<Unqualified, Submodule>create();
        for (var info : depInfo.values()) {
            if (info instanceof Submodule submodule) {
                byParent.put(submodule.belongsTo().name(), submodule);
            }
        }

        final var result = new HashMap<SourceIdentifier, Set<Include>>();
        for (var info : depInfo.values()) {
            if (!(info instanceof Submodule submodule)) {
                continue;
            }

            Set<Include> siblings = null;
            for (var include : submodule.includes()) {
                for (var candidate : byParent.get(submodule.belongsTo().name())) {
                    if (!candidate.sourceId().equals(submodule.sourceId())
                        && include.isSatisfiedBy(candidate.sourceId())) {
                        if (siblings == null) {
                            siblings = new HashSet<>();
                        }
                        siblings.add(include);
                        break;
                    }
                }
            }
            if (siblings != null) {
                result.put(submodule.sourceId(), siblings);
            }
        }
        return result;
    }

    abstract boolean isKnown(Collection<SourceIdentifier> haystack, SourceDependency dependency);
}
