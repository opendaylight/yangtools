/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Dependency;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangModelDependencyInfo;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangModelDependencyInfo.SubmoduleDependencyInfo;
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

    private final ImmutableList<SourceIdentifier> resolvedSources;
    private final ImmutableList<SourceIdentifier> unresolvedSources;
    private final ImmutableMultimap<SourceIdentifier, Dependency> unsatisfiedImports;

    protected DependencyResolver(final Map<SourceIdentifier, YangModelDependencyInfo> depInfo) {
        final var resolved = new ArrayList<SourceIdentifier>(depInfo.size());
        final var pending = new ArrayList<>(depInfo.keySet());
        final var submodules = new HashMap<SourceIdentifier, Dependency>();

        boolean progress;
        do {
            progress = false;

            final var it = pending.iterator();
            while (it.hasNext()) {
                final var sourceId = it.next();
                final var dep = depInfo.get(sourceId);

                // in case of submodule, remember belongs to
                if (dep instanceof SubmoduleDependencyInfo submodule) {
                    submodules.put(sourceId, submodule.getParentModule());
                }

                boolean okay = true;
                for (var dependency : dep.getDependencies()) {
                    if (!isKnown(resolved, dependency)) {
                        LOG.debug("Source {} is missing import {}", sourceId, dependency);
                        okay = false;
                        break;
                    }
                }

                if (okay) {
                    LOG.debug("Resolved source {}", sourceId);
                    resolved.add(sourceId);
                    it.remove();
                    progress = true;
                }
            }
        } while (progress);

        /// Additional check only for belongs-to statement
        for (var submodule : submodules.entrySet()) {
            final var sourceId = submodule.getKey();
            final var belongs = submodule.getValue();
            if (!isKnown(resolved, belongs)) {
                LOG.debug("Source {} is missing parent {}", sourceId, belongs);
                pending.add(sourceId);
                resolved.remove(sourceId);
            }
        }

        final var imports = ArrayListMultimap.<SourceIdentifier, Dependency>create();
        for (var sourceId : pending) {
            for (var dependency : depInfo.get(sourceId).getDependencies()) {
                if (!isKnown(pending, dependency) && !isKnown(resolved, dependency)) {
                    imports.put(sourceId, dependency);
                }
            }
        }

        resolvedSources = ImmutableList.copyOf(resolved);
        unresolvedSources = ImmutableList.copyOf(pending);
        unsatisfiedImports = ImmutableMultimap.copyOf(imports);
    }

    protected abstract boolean isKnown(Collection<SourceIdentifier> haystack, Dependency dependency);

    abstract YangParserConfiguration parserConfig();

    /**
     * Collection of sources which have been resolved.
     */
    ImmutableList<SourceIdentifier> resolvedSources() {
        return resolvedSources;
    }

    /**
     * Collection of sources which have not been resolved due to missing dependencies.
     */
    ImmutableList<SourceIdentifier> unresolvedSources() {
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
    ImmutableMultimap<SourceIdentifier, Dependency> unsatisfiedImports() {
        return unsatisfiedImports;
    }
}
