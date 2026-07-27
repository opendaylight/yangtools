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
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.InterfaceArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

public abstract sealed class AbstractGeneratedTypeBuilder<
        T extends InterfaceArchetype.Builder,
        S extends EffectiveStatement<?, ?>> implements InterfaceArchetype.Builder
        permits DataRootArchetype.Builder, LegacyArchetype.Builder {
    private final @NonNull JavaTypeName typeName;
    protected final @NonNull S statement;

    private @Nullable ArrayList<AttachedAnnotation.ToType> annotations = null;
    private List<Type> implementsTypes = List.of();
    private List<Constant> constants = List.of();
    private List<MethodSignature.Builder> methodDefinitions = List.of();
    private List<Archetype> enclosedTypes = List.of();

    @NonNullByDefault
    protected AbstractGeneratedTypeBuilder(final JavaTypeName typeName, final S statement) {
        this.typeName = requireNonNull(typeName);
        this.statement = requireNonNull(statement);
    }

    @Override
    public final JavaTypeName typeName() {
        return typeName;
    }

    @Override
    public final T addAnnotation(final AttachedAnnotation.ToType annotation) {
        annotations = MethodSignature.Builder.addAnnotation(annotations, annotation);
        return thisInstance();
    }

    @NonNullByDefault
    protected final List<AttachedAnnotation.ToType> getAnnotations() {
        final var local = annotations;
        if (local == null) {
            return List.of();
        }
        return local.size() == 1 ? Collections.singletonList(local.getFirst()) : List.copyOf(local);
    }

    @NonNullByDefault
    protected final List<Type> getImplementsTypes() {
        return switch (implementsTypes.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(implementsTypes.getFirst());
            default -> List.copyOf(implementsTypes);
        };
    }

    @NonNullByDefault
    protected final List<Constant> getConstants() {
        return switch (constants.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(constants.getFirst());
            default -> List.copyOf(constants);
        };
    }

    @NonNullByDefault
    protected final List<MethodSignature> getMethodDefinitions() {
        final var size = methodDefinitions.size();
        return switch (size) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(methodDefinitions.getFirst().build());
            case 2 -> List.of(methodDefinitions.getFirst().build(), methodDefinitions.getLast().build());
            default -> {
                final var tmp = new ArrayList<MethodSignature>(size);
                for (var builder : methodDefinitions) {
                    tmp.add(builder.build());
                }
                yield List.copyOf(tmp);
            }
        };
    }

    @NonNullByDefault
    protected final List<Archetype> getEnclosedTypes() {
        return switch (enclosedTypes.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(enclosedTypes.getFirst());
            default -> List.copyOf(enclosedTypes);
        };
    }

    protected abstract @NonNull T thisInstance();

    @Override
    public final @NonNull T addEnclosedType(final Archetype genType) {
        if (enclosedTypes.contains(requireNonNull(genType))) {
            throw new IllegalArgumentException("This generated type already contains equal enclosing transfer object.");
        }
        enclosedTypes = LazyCollections.lazyAdd(enclosedTypes, genType);
        return thisInstance();
    }

    @Override
    public final @NonNull T addImplementsType(final Type genType) {
        checkArgument(!implementsTypes.contains(requireNonNull(genType)),
            "This generated type already contains equal implements type.");
        implementsTypes = LazyCollections.lazyAdd(implementsTypes, genType);
        return thisInstance();
    }

    @Override
    public final Constant addConstant(final Type type, final String name, final Object value) {
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
    public MethodSignature.Builder addMethod(final String name) {
        checkArgument(name != null, "Name of method cannot be null!");
        final var builder = MethodSignature.builder(name);
        methodDefinitions = LazyCollections.lazyAdd(methodDefinitions, builder);
        return builder;
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
}
