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

import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.util.YangModelDependencyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        final String rev = mi.getRevision() != null ? mi.getRevision().toString() : null;
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
                    LOG.debug("Resoulved source {}", id);
                    resolved.add(id);
                    it.remove();
                    progress = true;
                }
            }
        } while (progress);


        if (pending.isEmpty()) {
            return new DependencyResolver(resolved, Collections.<SourceIdentifier>emptyList(), ImmutableMultimap.<SourceIdentifier, ModuleImport>of());
        }

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

    Multimap<SourceIdentifier, ModuleImport> getUnsatisfiedImports() {
        return unsatisfiedImports;
    }
}
