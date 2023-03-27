/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

final class SupportedFeatureSetBuilder {
    private final Map<QNameModule, Set<Set<QName>>> moduleMap = new HashMap<>();

    synchronized void add(final QNameModule module, final Set<QName> features) {
        moduleMap.computeIfAbsent(module, ignored -> new HashSet<>()).add(ImmutableSet.copyOf(features));
    }

    synchronized void remove(final QNameModule module, final Set<QName> features) {
        final var featuresSet = moduleMap.get(module);
        if (featuresSet != null) {
            featuresSet.remove(features);
            if (featuresSet.isEmpty()) {
                moduleMap.remove(module);
            }
        }
    }

    synchronized ImmutableSupportedFeatureSet build() {
        final var builder = ImmutableMap.<QNameModule, ImmutableSet<String>>builder();
        for (var entry : moduleMap.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().stream()
                .flatMap(Set::stream)
                .map(QName::getLocalName)
                .distinct()
                .sorted()
                .collect(ImmutableSet.toImmutableSet()));
        }
        return new ImmutableSupportedFeatureSet(builder.build());
    }
}
