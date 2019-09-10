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
import java.util.Optional;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangModelDependencyInfo;

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

    private static boolean isCompatible(final Optional<SemVer> moduleSemVer, final Optional<SemVer> importSemVer) {
        if (importSemVer.isEmpty()) {
            // Import does not care about the version
            return true;
        }
        if (moduleSemVer.isEmpty()) {
            // Modules which do not declare a semantic version are incompatible with imports which do
            return false;
        }

        final SemVer modVer = moduleSemVer.get();
        final SemVer impVer = importSemVer.get();
        return modVer.getMajor() == impVer.getMajor() && modVer.compareTo(impVer) >= 0;
    }

    @Override
    protected boolean isKnown(final Collection<SourceIdentifier> haystack, final ModuleImport mi) {
        final SemVerSourceIdentifier msi = SemVerSourceIdentifier.create(mi.getModuleName(), mi.getRevision(),
            mi.getSemanticVersion().orElse(null));

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
