/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Submodule;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
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
    private final ImmutableMultimap<SourceIdentifier, SourceDependency> unsatisfiedImports;

    protected DependencyResolver(final Map<SourceIdentifier, SourceInfo> depInfo) {
        final var resolved = Sets.<SourceIdentifier>newHashSetWithExpectedSize(depInfo.size());
        final var pending = new HashMap<>(depInfo);

        boolean progress;
        do {
            progress = false;

            final var it = pending.values().iterator();
            while (it.hasNext()) {
                final var dep = it.next();
                if (tryResolve(resolved, dep)) {
                    final var sourceId = dep.sourceId();
                    LOG.debug("Resolved source {}", sourceId);
                    resolved.add(sourceId);
                    it.remove();
                    progress = true;
                }
            }
        } while (progress);

        resolvedSources = ImmutableList.copyOf(resolved);
        unresolvedSources = ImmutableList.copyOf(pending.keySet());

        final var unstatisfied = ImmutableMultimap.<SourceIdentifier, SourceDependency>builder();
        for (var info : pending.values()) {
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
        unsatisfiedImports = unstatisfied.build();
    }

    /**
     * Collection of sources which have been resolved.
     */
    final ImmutableList<SourceIdentifier> resolvedSources() {
        return resolvedSources;
    }

    /**
     * Collection of sources which have not been resolved due to missing dependencies.
     */
    final ImmutableList<SourceIdentifier> unresolvedSources() {
        return unresolvedSources;
    }

    /**
     * Detailed information about which imports were missing. The key in the map is the source identifier of module
     * which was issuing an import, the values are imports which were unsatisfied.
     *
     * <p>
     * Note that this map contains only imports which are missing from the reactor, not transitive failures. Examples:
     * <ul>
     *   <li>if A imports B, B imports C, and both A and B are in the reactor, only B->C will be reported</li>
     *   <li>if A imports B and C, B imports C, and both A and B are in the reactor, A->C and B->C will be reported</li>
     * </ul>
     */
    final ImmutableMultimap<SourceIdentifier, SourceDependency> unsatisfiedImports() {
        return unsatisfiedImports;
    }

    private boolean tryResolve(final Collection<SourceIdentifier> resolved, final SourceInfo info) {
        for (var dep : info.imports()) {
            if (!isKnown(resolved, dep)) {
                LOG.debug("Source {} is missing import {}", info.sourceId(), dep);
                return false;
            }
        }
        for (var dep : info.includes()) {
            if (!isKnown(resolved, dep)) {
                LOG.debug("Source {} is missing include {}", info.sourceId(), dep);
                return false;
            }
        }
        if (info instanceof Submodule submodule) {
            final var dep = submodule.belongsTo();
            if (!isKnown(resolved, dep)) {
                LOG.debug("Source {} is missing belongs-to {}", info.sourceId(), dep);
                return false;
            }
        }
        return true;
    }

    abstract boolean isKnown(Collection<SourceIdentifier> haystack, SourceDependency dependency);

    abstract YangParserConfiguration parserConfig();
}
