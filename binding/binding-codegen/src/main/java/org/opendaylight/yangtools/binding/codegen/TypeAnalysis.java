/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;

record TypeAnalysis(@NonNull Set<BuilderGeneratedProperty> properties) {
    private static final Comparator<MethodSignature> METHOD_COMPARATOR = Comparator.comparing(MethodSignature::name);
    private static final int GETTER_PREFIX_LENGTH = Naming.GETTER_PREFIX.length();

    TypeAnalysis {
        requireNonNull(properties);
    }

    /**
     * Run type analysis, which results in identification of the augmentable type, as well as all methods available
     * to the type, expressed as properties.
     */
    @NonNullByDefault
    static TypeAnalysis of(final DataContainerArchetype type) {
        final var methods = new LinkedHashSet<MethodSignature>();
        methods.addAll(type.getMethodDefinitions());
        collectImplementedMethods(type, methods, type.partials());
        return new TypeAnalysis(propertiesFromMethods(methods.stream().sorted(METHOD_COMPARATOR).toList()));
    }

    /**
     * Adds to the {@code methods} set all the methods of the {@code implementedIfcs} and recursively their implemented
     * interfaces.
     *
     * @param methods set of method signatures
     * @param partials list of implemented interfaces
     */
    private static void collectImplementedMethods(
            final @NonNull DataContainerArchetype archetype, final @NonNull Set<MethodSignature> methods,
            final @NonNull List<DataContainerArchetype.Partial> partials) {
        for (var partial : partials) {
            for (var implMethod : partial.getMethodDefinitions()) {
                if (JavaFileTemplate.hasOverrideAnnotation(implMethod)) {
                    methods.add(implMethod);
                } else {
                    final var implMethodName = implMethod.name();
                    if (Naming.isGetterMethodName(implMethodName)
                        && JavaFileTemplate.getterByName(methods, implMethodName) == null) {
                        methods.add(implMethod);
                    }
                }
            }

            collectImplementedMethods(archetype, methods, partial.partials());
        }
    }

    /**
     * Creates generated property instance from the getter <code>method</code> name and return type.
     *
     * @param method method signature from which is the method name and return type obtained
     * @return generated property instance for the getter <code>method</code>
     * @throws IllegalArgumentException <ul>
     *                                    <li>if the {@code method} equals {@code null}</li>
     *                                    <li>if the name of the {@code method} equals {@code null}</li>
     *                                    <li>if the name of the {@code method} is empty</li>
     *                                    <li>if the return type of the {@code method} equals {@code null}</li>
     *                                  </ul>
     */
    private static BuilderGeneratedProperty propertyFromGetter(final MethodSignature method) {
        checkArgument(method != null);
        if (!Naming.isGetterMethodName(method.name())) {
            return null;
        }

        final var fieldName = Naming.toFirstLower(method.name().substring(GETTER_PREFIX_LENGTH));
        return new BuilderGeneratedProperty(fieldName, method);
    }

    /**
     * Creates set of generated property instances from getter <code>methods</code>.
     *
     * @param methods set of method signature instances which should be transformed to list of properties
     * @return set of generated property instances which represents the getter <code>methods</code>
     */
    @NonNullByDefault
    private static Set<BuilderGeneratedProperty> propertiesFromMethods(final List<MethodSignature> methods) {
        if (methods.isEmpty()) {
            return Set.of();
        }

        final var result = new LinkedHashSet<BuilderGeneratedProperty>();
        for (var method : methods) {
            final var createdField = propertyFromGetter(method);
            if (createdField != null) {
                result.add(createdField);
            }
        }
        return result;
    }
}
