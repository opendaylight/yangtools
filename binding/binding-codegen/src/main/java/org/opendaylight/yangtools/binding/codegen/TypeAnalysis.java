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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

record TypeAnalysis(
        @NonNull Set<BuilderGeneratedProperty> properties,
        @Nullable ParameterizedType augmentType) {
    private static final Comparator<MethodSignature> METHOD_COMPARATOR = new AlphabeticallyTypeMemberComparator<>();
    private static final int GETTER_PREFIX_LENGTH = Naming.GETTER_PREFIX.length();

    TypeAnalysis {
        requireNonNull(properties);
    }

    /**
     * Run type analysis, which results in identification of the augmentable type, as well as all methods available
     * to the type, expressed as properties.
     */
    @NonNullByDefault
    static TypeAnalysis of(final GeneratedType type) {
        final var methods = new LinkedHashSet<MethodSignature>();
        methods.addAll(type.getMethodDefinitions());
        final var augmentType = collectImplementedMethods(type, methods, type.getImplements());

        return new TypeAnalysis(propertiesFromMethods(methods.stream().sorted(METHOD_COMPARATOR).toList()),
            augmentType);
    }

    /**
     * Adds to the {@code methods} set all the methods of the {@code implementedIfcs} and recursively their implemented
     * interfaces.
     *
     * @param methods set of method signatures
     * @param implementedIfcs list of implemented interfaces
     * @return {@link ParameterizedType} of the implemented {@link Augmentation}, {@code null} if the type is not an
     *         augmentation.
     */
    private static @Nullable ParameterizedType collectImplementedMethods(final @NonNull GeneratedType type,
            final @NonNull Set<MethodSignature> methods, final @NonNull List<Type> implementedIfcs) {
        if (implementedIfcs.isEmpty()) {
            return null;
        }

        ParameterizedType augmentType = null;
        for (var implementedIfc : implementedIfcs) {
            switch (implementedIfc) {
                case ParameterizedType parameterized -> {
                    final var augmentableType = BindingTypes.extractAugmentableTarget(parameterized);
                    if (augmentableType != null) {
                        augmentType = BindingTypes.augmentation(augmentableType);
                    }
                }
                case GeneratedTransferObject<?> gto -> {
                    // no-op
                }
                case GeneratedType ifc -> {
                    for (var implMethod : ifc.getMethodDefinitions()) {
                        if (JavaFileTemplate.hasOverrideAnnotation(implMethod)) {
                            methods.add(implMethod);
                        } else {
                            final var implMethodName = implMethod.getName();
                            if (Naming.isGetterMethodName(implMethodName)
                                && JavaFileTemplate.getterByName(methods, implMethodName) == null) {
                                methods.add(implMethod);
                            }
                        }
                    }

                    final var augmentableType = collectImplementedMethods(type, methods, ifc.getImplements());
                    if (augmentableType != null && augmentType == null) {
                        augmentType = augmentableType;
                    }
                }
                default -> {
                    // no-op
                }
            }
        }
        return augmentType;
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
        checkArgument(method.getReturnType() != null);
        checkArgument(method.getName() != null);
        checkArgument(!method.getName().isEmpty());
        if (method.isDefault()) {
            return null;
        }
        if (!Naming.isGetterMethodName(method.getName())) {
            return null;
        }

        final var fieldName = Naming.toFirstLower(method.getName().substring(GETTER_PREFIX_LENGTH));
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
