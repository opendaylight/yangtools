/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;

record TypeAnalysis(@NonNull List<BuilderField> fields) {
    private static final int GETTER_PREFIX_LENGTH = Naming.GETTER_PREFIX.length();

    TypeAnalysis {
        requireNonNull(fields);
    }

    /**
     * Run type analysis, which results in identification of the augmentable type, as well as all methods available
     * to the type, expressed as properties.
     */
    @NonNullByDefault
    static TypeAnalysis of(final DataContainerArchetype type) {
        final var methods = new LinkedHashSet<MethodSignature>();
        methods.addAll(type.getMethodDefinitions());
        collectImplementedMethods(type, methods, type.getImplements());
        return new TypeAnalysis(methodsToFields(methods));
    }

    /**
     * Adds to the {@code methods} set all the methods of the {@code implementedIfcs} and recursively their implemented
     * interfaces.
     *
     * @param methods set of method signatures
     * @param implementedIfcs list of implemented interfaces
     */
    private static void collectImplementedMethods(
            final @NonNull DataContainerArchetype archetype, final @NonNull LinkedHashSet<MethodSignature> methods,
            final @NonNull List<Type> implementedIfcs) {
        for (var implementedIfc : implementedIfcs) {
            // FIXME: narrow down?
            if (implementedIfc instanceof DataContainerArchetype ifc) {
                for (var method : ifc.getMethodDefinitions()) {
                    if (JavaFileTemplate.hasOverrideAnnotation(method)) {
                        methods.add(method);
                    } else {
                        final var methodName = method.getName();
                        if (Naming.isGetterMethodName(methodName)
                            && JavaFileTemplate.getterByName(methods, methodName) == null) {
                            methods.add(method);
                        }
                    }
                }

                collectImplementedMethods(archetype, methods, ifc.getImplements());
            }
        }
    }

    @NonNullByDefault
    private static List<BuilderField> methodsToFields(final LinkedHashSet<MethodSignature> methods) {
        final var result = new HashMap<String, BuilderField>();
        for (var method : methods) {
            if (method.isDefault()) {
                continue;
            }

            final var methodName = method.getName();
            if (!Naming.isGetterMethodName(methodName)) {
                continue;
            }

            final var fieldName = Naming.toFirstLower(methodName.substring(GETTER_PREFIX_LENGTH));
            final var prev = result.putIfAbsent(fieldName, new BuilderField(fieldName, method));
            if (prev != null) {
                throw new VerifyException("Field " + fieldName + " defined by " + method + " and " + prev.method());
            }
        }
        if (result.isEmpty()) {
            return List.of();
        }
        return result.values().stream().sorted().toList();
    }
}
