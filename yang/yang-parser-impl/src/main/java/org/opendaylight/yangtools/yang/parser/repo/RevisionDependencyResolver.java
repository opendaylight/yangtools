/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import java.util.Collection;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangModelDependencyInfo;

final class RevisionDependencyResolver extends DependencyResolver {

    protected RevisionDependencyResolver(final Map<SourceIdentifier, YangModelDependencyInfo> depInfo) {
        super(depInfo);
    }

    protected static SourceIdentifier findWildcard(final Iterable<SourceIdentifier> haystack, final String needle) {
        for (final SourceIdentifier r : haystack) {
            if (needle.equals(r.getName())) {
                return r;
            }
        }

        return null;
    }

    @Override
    protected boolean isKnown(final Collection<SourceIdentifier> haystack, final ModuleImport mi) {
        final SourceIdentifier msi = RevisionSourceIdentifier.create(mi.getModuleName(), mi.getRevision());

        // Quick lookup
        if (haystack.contains(msi)) {
            return true;
        }

        // Slow revision-less walk
        return mi.getRevision().isEmpty() && findWildcard(haystack, mi.getModuleName()) != null;
    }

    public static RevisionDependencyResolver create(final Map<SourceIdentifier, YangModelDependencyInfo> depInfo) {
        return new RevisionDependencyResolver(depInfo);
    }
}