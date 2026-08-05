/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.isGetterMethodName;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;

/**
 * A {@link BlockFragment} emitting getter methods for a {@link DataContainerArchetype}.
 */
@NonNullByDefault
record DataContainerGetters(List<GetterShape> methods, List<GetterShape> partialMethods) {
    DataContainerGetters {
        requireNonNull(methods);
        requireNonNull(partialMethods);
    }

    Stream<GetterShape> allMethods() {
        return Stream.concat(methods.stream(), partialMethods.stream());
    }

    static DataContainerGetters of(final DataContainerArchetype archetype) {
        // traverse the DataContainerArchetype's partials and collect their methods
        final var partials = new HashMap<String, MethodSignature>();
        for (var partial : archetype.partials()) {
            collectPartialMethods(partials, partial);
        }

        // FIXME: pre-size to methods once we do not filter
        final var nameToSpec = new LinkedHashMap<String, GetterShape>();
        for (var method : archetype.getMethodDefinitions()) {
            // FIXME: do not filter
            final var name = method.name();
            if (isGetterMethodName(name)) {
                verify(nameToSpec.put(name, new GetterShape(method, partials.containsKey(name))) == null);
            }
        }
        return new DataContainerGetters(List.copyOf(nameToSpec.values()), partials.values().stream()
            .filter(method -> !nameToSpec.containsKey(method.name()))
            .map(method -> new GetterShape(method, true))
            .toList());
    }

    private static void collectPartialMethods(final HashMap<String, MethodSignature> partials,
            final DataContainerArchetype.Partial archetype) {
        for (var method : archetype.getMethodDefinitions()) {
            // FIXME: do not filter
            final var name = method.name();
            if (isGetterMethodName(name)) {
                partials.putIfAbsent(name, method);
            }
        }
        for (var partial : archetype.partials()) {
            collectPartialMethods(partials, partial);
        }
    }
}
