/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Common base class for {@link InterfaceArchetype.Builder} implementations.
 *
 * @param <B> concrete builder type
 * @param <S> EffectiveStatement type
 */
abstract sealed class InterfaceArchetypeBuilder<
        B extends InterfaceArchetypeBuilder<B, S>,
        S extends EffectiveStatement<?, ?>> implements InterfaceArchetype.Builder
        permits ActionArchetype.Builder, AugmentationArchetype.Builder, CaseArchetype.Builder,
                ContainerArchetype.Builder, DataRootArchetype.Builder, EntryObjectArchetype.Builder,
                GroupingArchetype.Builder, InputArchetype.Builder, InstanceNotificationArchetype.Builder,
                ItemObjectArchetype.Builder, KeyedListActionArchetype.Builder, KeyedListNotificationArchetype.Builder,
                NotificationArchetype.Builder, NotificationBodyArchetype.Builder, OutputArchetype.Builder,
                YangDataArchetype.Builder {
    final @NonNull JavaTypeName typeName;
    final @NonNull S statement;

    private @Nullable ArrayList<AttachedAnnotation.@NonNull ToType> annotations = null;
    private List<Type> implementsTypes = List.of();
    private List<MethodSignature.Builder> methodDefinitions = List.of();
    private List<Archetype> enclosedTypes = List.of();

    @NonNullByDefault
    InterfaceArchetypeBuilder(final JavaTypeName typeName, final S statement) {
        this.typeName = requireNonNull(typeName);
        this.statement = requireNonNull(statement);

        // FIXME: remove this logic and let InterfaceTemplate do the equivalent
        if (statement instanceof DocumentedNode.WithStatus withStatus) {
            final var deprecated = DeprecatedAnnotation.ofStatus(withStatus.getStatus());
            if (deprecated != null) {
                addAnnotation(deprecated);
            }
        }
    }

    @Override
    public final TypeRef typeRef() {
        return TypeRef.of(typeName);
    }

    @Override
    public final B addAnnotation(final AttachedAnnotation.ToType annotation) {
        annotations = MethodSignature.Builder.addAnnotation(annotations, requireNonNull(annotation));
        return thisInstance();
    }

    @NonNullByDefault
    final List<AttachedAnnotation.ToType> annotations() {
        final var local = annotations;
        if (local == null) {
            return List.of();
        }
        return local.size() == 1 ? Collections.singletonList(local.getFirst()) : List.copyOf(local);
    }

    @Override
    public final @NonNull B addEnclosedType(final Archetype genType) {
        if (enclosedTypes.contains(requireNonNull(genType))) {
            throw new IllegalArgumentException("This generated type already contains equal enclosing transfer object.");
        }
        enclosedTypes = LazyCollections.lazyAdd(enclosedTypes, genType);
        return thisInstance();
    }

    @NonNullByDefault
    final List<Archetype> enclosedTypes() {
        return switch (enclosedTypes.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(enclosedTypes.getFirst());
            default -> List.copyOf(enclosedTypes);
        };
    }

    @Override
    public final @NonNull B addImplementsType(final Type genType) {
        checkArgument(!implementsTypes.contains(requireNonNull(genType)),
            "This generated type already contains equal implements type.");
        implementsTypes = LazyCollections.lazyAdd(implementsTypes, genType);
        return thisInstance();
    }

    @NonNullByDefault
    final List<Type> implementsTypes() {
        return switch (implementsTypes.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(implementsTypes.getFirst());
            default -> List.copyOf(implementsTypes);
        };
    }

    @Override
    public final MethodSignature.Builder addMethod(final String name) {
        checkArgument(name != null, "Name of method cannot be null!");
        final var builder = MethodSignature.builder(name);
        methodDefinitions = LazyCollections.lazyAdd(methodDefinitions, builder);
        return builder;
    }

    @NonNullByDefault
    final List<MethodSignature> methodDefinitions() {
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

    /**
     * {@return the class of the archetype produced by this builder}
     */
    @NonNullByDefault
    abstract Class<? extends InterfaceArchetype> archetypeClass();

    /**
     * {@return {@code this} in a type-safe way}
     */
    abstract @NonNull B thisInstance();

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    public final String toString() {
        final var helper = MoreObjects.toStringHelper(archetypeClass().getSimpleName() + ".Builder")
            .add("typeName", typeName);

        addNonEmpty(helper, "enclosedTypes", enclosedTypes);
        addNonEmpty(helper, "methods", methodDefinitions);
        addNonEmpty(helper, "annotations", annotations);
        addNonEmpty(helper, "implements", implementsTypes);

        return helper.toString();
    }

    static final void addNonEmpty(final ToStringHelper helper, final String name, final @Nullable List<?> value) {
        if (value != null && !value.isEmpty()) {
            helper.add(name, value);
        }
    }
}
