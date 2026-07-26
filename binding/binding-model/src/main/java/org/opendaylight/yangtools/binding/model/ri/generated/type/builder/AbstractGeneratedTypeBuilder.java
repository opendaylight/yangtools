/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.AttachedAnnotation;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

public abstract sealed class AbstractGeneratedTypeBuilder<
        T extends GeneratedTypeBuilderBase<T>,
        S extends EffectiveStatement<?, ?>> implements GeneratedTypeBuilderBase<T>
        permits LegacyArchetypeBuilder, DataRootArchetypeBuilder {
    private final @NonNull JavaTypeName typeName;
    protected final @NonNull S statement;

    private @Nullable ArrayList<@NonNull AttachedAnnotation> annotations = null;
    private @Nullable ArrayList<@NonNull MethodSignature> methodDefinitions = null;
    private List<Type> implementsTypes = List.of();
    private List<Constant> constants = List.of();
    private List<Archetype> enclosedTypes = List.of();

    @NonNullByDefault
    AbstractGeneratedTypeBuilder(final JavaTypeName typeName, final S statement) {
        this.typeName = requireNonNull(typeName);
        this.statement = requireNonNull(statement);
    }

    @Override
    public final JavaTypeName typeName() {
        return typeName;
    }

    @Override
    public final T addAnnotation(final AttachedAnnotation annotation) {
        annotations = TypeMemberBuilder.addAnnotation(annotations, annotation);
        return thisInstance();
    }

    @NonNullByDefault
    final List<AttachedAnnotation> getAnnotations() {
        return listOf(annotations);
    }

    @NonNullByDefault
    final List<Type> getImplementsTypes() {
        return switch (implementsTypes.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(implementsTypes.getFirst());
            default -> List.copyOf(implementsTypes);
        };
    }

    @NonNullByDefault
    final List<Constant> getConstants() {
        return switch (constants.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(constants.getFirst());
            case 2 -> List.of(constants.getFirst(), constants.getLast());
            default -> List.copyOf(constants);
        };
    }

    @NonNullByDefault
    final List<MethodSignature> getMethodDefinitions() {
        return listOf(methodDefinitions);
    }

    protected final List<Archetype> getEnclosedTypes() {
        return enclosedTypes;
    }

    protected abstract @NonNull T thisInstance();

    @Override
    public final T addEnclosedType(final Archetype genType) {
        if (enclosedTypes.contains(requireNonNull(genType))) {
            throw new IllegalArgumentException("This generated type already contains equal enclosing transfer object.");
        }
        enclosedTypes = LazyCollections.lazyAdd(enclosedTypes, genType);
        return thisInstance();
    }

    @Override
    public final T addImplementsType(final Type genType) {
        checkArgument(!implementsTypes.contains(requireNonNull(genType)),
            "This generated type already contains equal implements type.");
        implementsTypes = LazyCollections.lazyAdd(implementsTypes, genType);
        return thisInstance();
    }

    @Override
    public Constant addConstant(final Type type, final String name, final Object value) {
        checkArgument(type != null, "Returning Type for Constant cannot be null!");
        checkArgument(name != null, "Name of constant cannot be null!");
        checkArgument(!containsConstant(name),
            "This generated type already contains a \"%s\" constant", name);

        final var constant = new Constant(type, name, value);
        constants = LazyCollections.lazyAdd(constants, constant);
        return constant;
    }

    public boolean containsConstant(final String name) {
        checkArgument(name != null, "Parameter name can't be null");
        for (var constant : constants) {
            if (name.equals(constant.name())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final T addMethod(final MethodSignature method) {
        requireNonNull(method);
        var local = methodDefinitions;
        if (local == null) {
            methodDefinitions = local = new ArrayList<>(2);
        }
        local.add(method);
        return thisInstance();
    }

    public Type getParent() {
        return null;
    }

    @Override
    public final int hashCode() {
        return typeName.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof AbstractGeneratedTypeBuilder other && typeName.equals(other.typeName());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        helper.add("typeName", typeName);

        addToStringAttribute(helper, "constants", constants);
        addToStringAttribute(helper, "enclosedTypes", enclosedTypes);
        addToStringAttribute(helper, "methods", methodDefinitions);
        addToStringAttribute(helper, "annotations", annotations);
        addToStringAttribute(helper, "implements", implementsTypes);

        return helper;
    }

    @NonNullByDefault
    static final void addToStringAttribute(final ToStringHelper helper, final String name,
            final @Nullable Collection<?> value) {
        if (value != null && !value.isEmpty()) {
            helper.add(name, value);
        }
    }

    private static <@NonNull T> @NonNull List<T> listOf(final @Nullable ArrayList<@NonNull T> list) {
        if (list == null) {
            return List.of();
        }
        return list.size() == 1 ? Collections.singletonList(list.getFirst()) : List.copyOf(list);
    }
}
