/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
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
 *
 * FIXME: improve this class to track and expose how wildcard imports were resolved.
 *        That information will allow us to track "damage" to dependency resolution
 *        as new models are added to a schema context.
 */
final class DependencyResolver {
    private static final Logger LOG = LoggerFactory.getLogger(DependencyResolver.class);
    private final Collection<SourceIdentifier> resolvedSources;
    private final Collection<SourceIdentifier> unresolvedSources;
    private final Multimap<SourceIdentifier, ModuleImport> unsatisfiedImports;

    public DependencyResolver(final Collection<SourceIdentifier> resolvedSources,
            final Collection<SourceIdentifier> unresolvedSources, final Multimap<SourceIdentifier, ModuleImport> unsatisfiedImports) {
        this.resolvedSources = Preconditions.checkNotNull(resolvedSources);
        this.unresolvedSources = Preconditions.checkNotNull(unresolvedSources);
        this.unsatisfiedImports = Preconditions.checkNotNull(unsatisfiedImports);
    }

    private static SourceIdentifier findWildcard(final Iterable<SourceIdentifier> haystack, final String needle) {
        for (SourceIdentifier r : haystack) {
            if (r.getName().equals(needle)) {
                return r;
            }
        }

        return null;
    }

    private static boolean isKnown(final Collection<SourceIdentifier> haystack, final ModuleImport mi) {
        final String rev = mi.getRevision() != null ? QName.formattedRevision(mi.getRevision()) : null;
        final SourceIdentifier msi = SourceIdentifier.create(mi.getModuleName(), Optional.fromNullable(rev));

        // Quick lookup
        if (haystack.contains(msi)) {
            return true;
        }

        // Slow revision-less walk
        return rev == null && findWildcard(haystack, mi.getModuleName()) != null;
    }



    public static final DependencyResolver create(final Map<SourceIdentifier, YangModelDependencyInfo> depInfo) {
        final Collection<SourceIdentifier> resolved = new ArrayList<>(depInfo.size());
        final Collection<SourceIdentifier> pending = new ArrayList<>(depInfo.keySet());

        boolean progress;
        do {
            progress = false;

            final Iterator<SourceIdentifier> it = pending.iterator();
            while (it.hasNext()) {
                final SourceIdentifier id = it.next();
                final YangModelDependencyInfo dep = depInfo.get(id);

                boolean okay = true;
                for (ModuleImport mi : dep.getDependencies()) {
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

        if (!pending.isEmpty()) {
            final Multimap<SourceIdentifier, ModuleImport> imports = ArrayListMultimap.create();
            for (SourceIdentifier id : pending) {
                final YangModelDependencyInfo dep = depInfo.get(id);
                for (ModuleImport mi : dep.getDependencies()) {
                    if (!isKnown(pending, mi) && !isKnown(resolved, mi)) {
                        imports.put(id, mi);
                    }
                }
            }

            return new DependencyResolver(resolved, pending, imports);
        } else {
            return new DependencyResolver(resolved, Collections.<SourceIdentifier>emptyList(), ImmutableMultimap.<SourceIdentifier, ModuleImport>of());
        }
    }

    /**
     * Collection of sources which have been resolved.
     *
     * @return
     */
    Collection<SourceIdentifier> getResolvedSources() {
        return resolvedSources;
    }

    /**
     * Collection of sources which have not been resolved due to missing dependencies.
     *
     * @return
     */
    Collection<SourceIdentifier> getUnresolvedSources() {
        return unresolvedSources;
    }

    /**
     * Detailed information about which imports were missing. The key in the map
     * is the source identifier of module which was issuing an import, the values
     * are imports which were unsatisfied.
     *
     * Note that this map contains only imports which are missing from the reactor,
     * not transitive failures.
     *
     * Examples:
     *
     * If A imports B, B imports C, and both A and B are in the reactor, only B->C
     * will be reported.
     *
     * If A imports B and C, B imports C, and both A and B are in the reactor,
     * A->C and B->C will be reported.
     *
     * @return
     */
    Multimap<SourceIdentifier, ModuleImport> getUnsatisfiedImports() {
        return unsatisfiedImports;
    }
}
