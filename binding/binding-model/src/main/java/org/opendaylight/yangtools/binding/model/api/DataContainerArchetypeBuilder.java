/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collections;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Common base class for {@link DataContainerArchetype.Builder} implementations.
 *
 * @param <B> concrete builder type
 * @param <S> EffectiveStatement type
 */
abstract sealed class DataContainerArchetypeBuilder<
        B extends DataContainerArchetypeBuilder<B, S>,
        S extends EffectiveStatement<?, ?>> implements DataContainerArchetype.Builder
        permits AugmentationArchetype.Builder, CaseObjectArchetype.Builder, ChildOfArchetypeBuilder,
                DataRootArchetype.Builder, GroupingArchetype.Builder, RpcInputArchetype.Builder,
                InstanceNotificationArchetype.Builder, KeyedListNotificationArchetype.Builder,
                NotificationArchetype.Builder, NotificationBodyArchetype.Builder, RpcOutputArchetype.Builder,
                YangDataArchetype.Builder {
    @NonNullByDefault
    final List<Type> implementsTypes;
    final @NonNull JavaTypeName typeName;
    final @NonNull S statement;

    private List<MethodSignature> methodDefinitions = List.of();
    private List<@NonNull Archetype> enclosedTypes = List.of();

    @NonNullByDefault
    DataContainerArchetypeBuilder(final JavaTypeName typeName, final S statement,
            final List<? extends DataContainerArchetype> implementsTypes) {
        this.typeName = requireNonNull(typeName);
        this.statement = requireNonNull(statement);
        this.implementsTypes = switch (implementsTypes.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(requireNonNull(implementsTypes.getFirst()));
            default -> List.copyOf(implementsTypes);
        };
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
    public final B addMethod(final MethodSignature method) {
        methodDefinitions = LazyCollections.lazyAdd(methodDefinitions, requireNonNull(method));
        return thisInstance();
    }

    @NonNullByDefault
    final List<MethodSignature> methodDefinitions() {
        final var size = methodDefinitions.size();
        return switch (size) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(methodDefinitions.getFirst());
            default -> List.copyOf(methodDefinitions);
        };
    }

    /**
     * {@return the class of the archetype produced by this builder}
     */
    @NonNullByDefault
    abstract Class<? extends DataContainerArchetype> archetypeClass();

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
        addNonEmpty(helper, "implements", implementsTypes);

        return helper.toString();
    }

    static final void addNonEmpty(final ToStringHelper helper, final String name, final @Nullable List<?> value) {
        if (value != null && !value.isEmpty()) {
            helper.add(name, value);
        }
    }
}
