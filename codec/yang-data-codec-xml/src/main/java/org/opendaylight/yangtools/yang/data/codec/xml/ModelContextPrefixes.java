/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

final class ModelContextPrefixes {
    private final ConcurrentMap<XMLNamespace, Optional<String>> knownMappings = new ConcurrentHashMap<>();
    private final EffectiveModelContext modelContext;

    ModelContextPrefixes(final EffectiveModelContext modelContext) {
        this.modelContext = requireNonNull(modelContext);
    }

    @Nullable String prefixForNamespace(final XMLNamespace namespace) {
        final var existing = knownMappings.get(namespace);
        if (existing != null) {
            return existing.orElse(null);
        }

        final var modules = modelContext.findModuleStatements(namespace).iterator();
        // Note: we are not caching anything if we do not find the module
        return modules.hasNext() ? loadPrefix(namespace, modules.next().prefix().argument()) : null;
    }

    private @Nullable String loadPrefix(final XMLNamespace namespace, final String candidate) {
        // Validate that all modules which have the same prefix have also the name namespace
        Optional<String> mapping = Optional.of(candidate);
        for (var module : modelContext.getModuleStatements().values()) {
            if (candidate.equals(module.prefix().argument()) && !namespace.equals(module.namespace().argument())) {
                // Nope, we cannot use a shared prefix
                mapping = Optional.empty();
                break;
            }
        }

        final var raced = knownMappings.putIfAbsent(namespace, mapping);
        return (raced != null ? raced : mapping).orElse(null);
    }
}
