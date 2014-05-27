/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import java.util.Map;
import java.util.Map.Entry;

import org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public final class YangSourceFromDependencyInfoResolver extends YangSourceContextResolver {

    private final Map<SourceIdentifier, YangModelDependencyInfo> dependencyInfo;

    public YangSourceFromDependencyInfoResolver(final Map<SourceIdentifier, YangModelDependencyInfo> moduleDependencies) {
        dependencyInfo = ImmutableMap.copyOf(moduleDependencies);
    }

    @Override
    public Optional<YangModelDependencyInfo> getDependencyInfo(final SourceIdentifier identifier) {
        if (identifier.getRevision() != null) {
            return Optional.fromNullable(dependencyInfo.get(identifier));
        }
        YangModelDependencyInfo potential = dependencyInfo.get(identifier);
        if (potential == null) {
            for (Entry<SourceIdentifier, YangModelDependencyInfo> newPotential : dependencyInfo.entrySet()) {
                String newPotentialName = newPotential.getKey().getName();

                if (newPotentialName.equals(identifier.getName())) {
                    String newPotentialRevision = newPotential.getKey().getRevision();
                    if (potential == null || 1 == newPotentialRevision.compareTo(potential.getFormattedRevision())) {
                        potential = newPotential.getValue();
                    }
                }
            }
        }
        return Optional.fromNullable(potential);
    }

    @Override
    public YangSourceContext resolveContext() {
        for (SourceIdentifier source : dependencyInfo.keySet()) {
            resolveSource(source);
        }
        return createSourceContext();
    }
}
