/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.base.Optional;
import java.util.Collection;
import java.util.Map;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.util.YangModelDependencyInfo;

final class SemVerDependencyResolver extends DependencyResolver {

    protected SemVerDependencyResolver(final Map<SourceIdentifier, YangModelDependencyInfo> depInfo) {
        super(depInfo);
    }

    protected static SourceIdentifier findCompatibleVersion(final Iterable<SourceIdentifier> haystack,
            final ModuleImport mi) {
        final String requestedModuleName = mi.getModuleName();
        for (SourceIdentifier r : haystack) {
            if (requestedModuleName.equals(r.getName())
                    && isCompatible(((SemVerSourceIdentifier) r).getSemanticVersion(), mi.getSemanticVersion())) {
                return r;
            }
        }

        return null;
    }

    private static boolean isCompatible(final SemVer moduleSemVer, final SemVer importSemVer) {
        return moduleSemVer.getMajor() == importSemVer.getMajor() && moduleSemVer.compareTo(importSemVer) >= 0;
    }

    @Override
    protected boolean isKnown(final Collection<SourceIdentifier> haystack, final ModuleImport mi) {
        final String rev = mi.getRevision() != null ? QName.formattedRevision(mi.getRevision()) : null;
        final SemVerSourceIdentifier msi = SemVerSourceIdentifier.create(mi.getModuleName(), Optional.fromNullable(rev),
            mi.getSemanticVersion());

        // Quick lookup
        if (haystack.contains(msi)) {
            return true;
        }

        // Slow revision-less walk
        return findCompatibleVersion(haystack, mi) != null;
    }

    public static SemVerDependencyResolver create(final Map<SourceIdentifier, YangModelDependencyInfo> depInfo) {
        return new SemVerDependencyResolver(depInfo);
    }
}
