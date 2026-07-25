/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

final class DefaultLegacyArchetype<S extends EffectiveStatement<?, ?>> implements LegacyArchetype<S> {
    private final @NonNull JavaTypeName name;
    private final @NonNull S statement;
    private final @NonNull List<AnnotationType> annotations;
    private final @NonNull List<Type> implementsTypes;
    private final @NonNull List<Constant> constants;
    private final @NonNull List<MethodSignature> methodSignatures;
    private final @NonNull List<Archetype> enclosedTypes;

    DefaultLegacyArchetype(final LegacyArchetypeBuilder<S> builder) {
        name = builder.typeName();
        statement = builder.statement;
        annotations = toUnmodifiableAnnotations(builder.getAnnotations());
        implementsTypes = makeUnmodifiable(builder.getImplementsTypes());
        constants = makeUnmodifiable(builder.getConstants());
        methodSignatures = toUnmodifiableMethods(builder.getMethodDefinitions());
        enclosedTypes = List.copyOf(builder.getEnclosedTypes());
    }

    @Override
    public S statement() {
        return statement;
    }

    private static <T> @NonNull List<T> makeUnmodifiable(final List<T> list) {
        return switch (list.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(list.getFirst());
            case 2 -> List.copyOf(list);
            default -> Collections.unmodifiableList(list);
        };
    }

    private static @NonNull List<AnnotationType> toUnmodifiableAnnotations(
            final List<AnnotationTypeBuilder> annotationBuilders) {
        return makeUnmodifiable(annotationBuilders.stream()
            .map(AnnotationTypeBuilder::build)
            .collect(Collectors.toUnmodifiableList()));
    }

    static List<MethodSignature> toUnmodifiableMethods(final List<MethodSignatureBuilder> methodBuilders) {
        final var methods = new ArrayList<MethodSignature>(methodBuilders.size());
        for (var methodBuilder : methodBuilders) {
            methods.add(methodBuilder.build());
        }
        return makeUnmodifiable(methods);
    }

    @Override
    public JavaTypeName name() {
        return name;
    }

    @Override
    public List<AnnotationType> getAnnotations() {
        return annotations;
    }

    @Override
    public List<Type> getImplements() {
        return implementsTypes;
    }

    @Override
    public List<Archetype> enclosedTypes() {
        return enclosedTypes;
    }

    @Override
    public List<Constant> getConstantDefinitions() {
        return constants;
    }

    @Override
    public List<MethodSignature> getMethodDefinitions() {
        return methodSignatures;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Type other && name.equals(other.name());
    }

    @Override
    public String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    @NonNullByDefault
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        helper.add("name", name);

        addToStringAttribute(helper, "annotations", annotations);
        addToStringAttribute(helper, "implements", implementsTypes);
        addToStringAttribute(helper, "enclosedTypes", enclosedTypes);
        addToStringAttribute(helper, "constants", constants);
        addToStringAttribute(helper, "methods", methodSignatures);

        return helper;
    }

    @NonNullByDefault
    protected static void addToStringAttribute(final ToStringHelper helper, final String name,
            final @Nullable Collection<?> value) {
        if (value != null && !value.isEmpty()) {
            helper.add(name, value);
        }
    }
}
