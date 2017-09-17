/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.util.YangModelDependencyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inter-module dependency resolved. Given a set of schema source identifiers and their
 * corresponding dependency information, the {@link #create(Map)} method creates a
 * a view of how consistent the dependencies are. In particular, this detects whether
 * any imports are unsatisfied.
 */
// FIXME: improve this class to track and expose how wildcard imports were resolved.
//        That information will allow us to track "damage" to dependency resolution
//        as new models are added to a schema context.
abstract class DependencyResolver {
    private static final Logger LOG = LoggerFactory.getLogger(DependencyResolver.class);
    private final Collection<SourceIdentifier> resolvedSources;
    private final Collection<SourceIdentifier> unresolvedSources;
    private final Multimap<SourceIdentifier, ModuleImport> unsatisfiedImports;

    protected DependencyResolver(final Map<SourceIdentifier, YangModelDependencyInfo> depInfo) {
        final Collection<SourceIdentifier> resolved = new ArrayList<>(depInfo.size());
        final Collection<SourceIdentifier> pending = new ArrayList<>(depInfo.keySet());
        final Map<SourceIdentifier, BelongsToDependency> submodules = Maps.newHashMap();

        boolean progress;
        do {
            progress = false;

            final Iterator<SourceIdentifier> it = pending.iterator();
            while (it.hasNext()) {
                final SourceIdentifier id = it.next();
                final YangModelDependencyInfo dep = depInfo.get(id);

                boolean okay = true;

                final Set<ModuleImport> dependencies = dep.getDependencies();

                // in case of submodule, remember belongs to
                if (dep instanceof YangModelDependencyInfo.SubmoduleDependencyInfo) {
                    final String parent = ((YangModelDependencyInfo.SubmoduleDependencyInfo) dep).getParentModule();
                    submodules.put(id, new BelongsToDependency(parent));
                }

                for (final ModuleImport mi : dependencies) {
                    if (!isKnown(resolved, mi)) {
                        LOG.debug("Source {} is missing import {}", id, mi);
                        okay = false;
                        break;
                    }
                }

                if (okay) {
                    LOG.debug("Resolved source {}", id);
                    resolved.add(id);
                    it.remove();
                    progress = true;
                }
            }
        } while (progress);

        /// Additional check only for belongs-to statement
        for (final Entry<SourceIdentifier, BelongsToDependency> submodule : submodules.entrySet()) {
            final BelongsToDependency belongs = submodule.getValue();
            final SourceIdentifier sourceIdentifier = submodule.getKey();
            if (!isKnown(resolved, belongs)) {
                LOG.debug("Source {} is missing parent {}", sourceIdentifier, belongs);
                pending.add(sourceIdentifier);
                resolved.remove(sourceIdentifier);
            }
        }

        final Multimap<SourceIdentifier, ModuleImport> imports = ArrayListMultimap.create();
        for (final SourceIdentifier id : pending) {
            final YangModelDependencyInfo dep = depInfo.get(id);
            for (final ModuleImport mi : dep.getDependencies()) {
                if (!isKnown(pending, mi) && !isKnown(resolved, mi)) {
                    imports.put(id, mi);
                }
            }
        }

        this.resolvedSources = ImmutableList.copyOf(resolved);
        this.unresolvedSources = ImmutableList.copyOf(pending);
        this.unsatisfiedImports = ImmutableMultimap.copyOf(imports);
    }

    protected abstract boolean isKnown(Collection<SourceIdentifier> haystack, ModuleImport mi);

    /**
     * Collection of sources which have been resolved.
     */
    Collection<SourceIdentifier> getResolvedSources() {
        return resolvedSources;
    }

    /**
     * Collection of sources which have not been resolved due to missing dependencies.
     */
    Collection<SourceIdentifier> getUnresolvedSources() {
        return unresolvedSources;
    }

    /**
     * Detailed information about which imports were missing. The key in the map
     * is the source identifier of module which was issuing an import, the values
     * are imports which were unsatisfied.
     *
     * <p>
     * Note that this map contains only imports which are missing from the reactor,
     * not transitive failures.
     *
     * <p>
     * Examples:
     * <ul><li>
     * If A imports B, B imports C, and both A and B are in the reactor, only B->C
     * will be reported.
     * </li><li>
     * If A imports B and C, B imports C, and both A and B are in the reactor,
     * A->C and B->C will be reported.
     * </li></ul>
     */
    Multimap<SourceIdentifier, ModuleImport> getUnsatisfiedImports() {
        return unsatisfiedImports;
    }

    private static class BelongsToDependency implements ModuleImport {
        private final String parent;

        BelongsToDependency(final String parent) {
            this.parent = parent;
        }

        @Override
        public String getModuleName() {
            return parent;
        }

        @Override
        public Date getRevision() {
            return null;
        }

        @Override
        public String getPrefix() {
            return null;
        }

        @Override
        public SemVer getSemanticVersion() {
            return null;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("parent", parent)
                    .toString();
        }
    }
}
