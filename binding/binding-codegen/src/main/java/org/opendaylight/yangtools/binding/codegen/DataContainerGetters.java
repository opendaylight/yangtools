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
import static org.opendaylight.yangtools.binding.contract.Naming.GETTER_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.toFirstLower;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * A {@link BlockFragment} emitting getter methods for a {@link DataContainerArchetype}.
 */
@NonNullByDefault
record DataContainerGetters(List<Spec> methods, List<Spec> partialMethods) {
    record Spec(MethodSignature method, boolean hasOverride) implements Comparable<Spec> {
        private static final int GETTER_PREFIX_LENGTH = GETTER_PREFIX.length();

        Spec {
            requireNonNull(method);
        }

        String name() {
            return method.name();
        }

        Type type() {
            return method.returnType();
        }

        boolean isBinary() {
            // FIXME: compare to BaseYangTypes.BINARY_TYPE
            return type().isArray();
        }

        String suffix() {
            return name().substring(GETTER_PREFIX_LENGTH);
        }

        String fieldName() {
            return "_" + propertyName();
        }

        String propertyName() {
            return toFirstLower(suffix() );
        }

        @Override
        public int compareTo(final Spec other) {
            return method.name().compareTo(other.method.name());
        }
    }

    DataContainerGetters {
        requireNonNull(methods);
        requireNonNull(partialMethods);
    }

    Stream<Spec> allMethods() {
        return Stream.concat(methods.stream(), partialMethods.stream());
    }

    static DataContainerGetters of(final DataContainerArchetype archetype) {
        // traverse the DataContainerArchetype's partials and collect their methods
        final var partials = new HashMap<String, MethodSignature>();
        for (var partial : archetype.partials()) {
            collectPartialMethods(partials, partial);
        }

        final var archMethods = archetype.getMethodDefinitions();
        final var nameToSpec = LinkedHashMap.<String, Spec>newLinkedHashMap(archMethods.size());
        for (var method : archMethods) {
            final var name = method.name();
            verify(nameToSpec.put(name, new Spec(method, partials.containsKey(name))) == null);
        }
        return new DataContainerGetters(List.copyOf(nameToSpec.values()), partials.values().stream()
            .filter(method -> !nameToSpec.containsKey(method.name()))
            .map(method -> new Spec(method, true))
            .toList());
    }

    private static void collectPartialMethods(final HashMap<String, MethodSignature> partials,
            final DataContainerArchetype.Partial archetype) {
        for (var method : archetype.getMethodDefinitions()) {
            partials.putIfAbsent(method.name(), method);
        }
        for (var partial : archetype.partials()) {
            collectPartialMethods(partials, partial);
        }
    }
}
