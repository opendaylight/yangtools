/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collections;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ChoiceInArchetype;
import org.opendaylight.yangtools.binding.model.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.IdentityArchetype;
import org.opendaylight.yangtools.binding.model.OperationArchetype;
import org.opendaylight.yangtools.binding.model.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.RestrictedType;

/**
 * Utility methods for {@link Type} implementations.
 */
public final class TypeMethods {
    private TypeMethods() {
        // hidden on purpose
    }

    /**
     * Return an immutable copy of specified list.
     *
     * @param list the list
     * @return a squashed copy of the list
     */
    @NonNullByDefault
    public static <T> List<T> copyList(final List<? extends T> list) {
        return switch (list.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(requireNonNull(list.getFirst()));
            default -> List.copyOf(list);
        };
    }

    /**
     * Implementation of {@link Type#hashCode()}.
     *
     * @param self the type
     * @return a hash code
     */
    @NonNullByDefault
    public static int hashCode(final Type self) {
        return self.name().hashCode();
    }

    /**
     * Implementation of {@link Type#equals(Object)}.
     *
     * @param self the type
     * @param obj the object
     * @return {@code true} if {@code obj} is considered equal to {@code self}, {@code false} otherwise
     */
    public static boolean equals(final @NonNull Type self, final @Nullable Object obj) {
        return requireNonNull(self) == obj || obj instanceof Type other && self.name().equals(other.name());
    }

    /**
     * Implementation of {@link ParameterizedType#toString()}.
     *
     * @param self the type
     * @return a String
     */
    @NonNullByDefault
    @SuppressWarnings("removal")
    public static String toString(final ParameterizedType self) {
        final var helper = MoreObjects.toStringHelper(ParameterizedType.class).add("name", self.name());
        final var arguments = self.getActualTypeArguments();
        if (!arguments.isEmpty()) {
            helper.add("arguments", arguments);
        }
        return helper.toString();
    }

    /**
     * Implementation of {@link BitsTypeObjectArchetype#toString()}.
     *
     * @param self the archetype
     * @return a String
     */
    @NonNullByDefault
    static String toString(final BitsTypeObjectArchetype self) {
        final var helper = MoreObjects.toStringHelper(BitsTypeObjectArchetype.class)
            .add("name", self.name())
            .add("type", self.typeDefinition());
        final var superType = self.superType();
        if (superType != null) {
            helper.add("extends", superType.name());
        }
        return helper.toString();
    }

    /**
     * Implementation of {@link ScalarTypeObjectArchetype#toString()}.
     *
     * @param self the archetype
     * @return a String
     */
    @NonNullByDefault
    static String toString(final ScalarTypeObjectArchetype self) {
        final var helper = MoreObjects.toStringHelper(ScalarTypeObjectArchetype.class).omitNullValues()
            .add("name", self.name())
            .add("type", self.typeDefinition());
        final var superType = self.superType();
        if (superType != null) {
            helper.add("extends", superType.name());
        }
        return helper.add("restrictions", self.restrictions()).toString();
    }

    /**
     * Implementation of {@link UnionTypeObjectArchetype#toString()}.
     *
     * @param self the archetype
     * @return a String
     */
    @NonNullByDefault
    static String toString(final UnionTypeObjectArchetype self) {
        final var helper = MoreObjects.toStringHelper(UnionTypeObjectArchetype.class)
            .add("name", self.name());
        final var superType = self.superType();
        if (superType == null) {
            helper.add("tags", self.tags());
            addNonEmpty(helper, "enclosedTypes", self.enclosedTypes());
        } else {
            helper.add("extends", superType.name());
        }
        return helper.toString();
    }

    /**
     * Implementation of {@link GetterMethod#toString()}.
     *
     * @param archetypeClass the archetype class
     * @param self the archetype
     * @return a String
     */
    @NonNullByDefault
    static String toString(final GetterMethod self) {
        final var helper = MoreObjects.toStringHelper(GetterMethod.class).omitNullValues()
            .add("name", Naming.GETTER_PREFIX + self.suffix())
            .add("returnType", self.returnType());
        final var annotations = self.annotations();
        if (!annotations.isEmpty()) {
            helper.add("annotations", annotations);
        }
        return helper.toString();
    }

    /**
     * Implementation of {@link ChoiceInArchetype#toString()}.
     *
     * @param self the archetype
     * @return a String
     */
    @NonNullByDefault
    static String toString(final ChoiceInArchetype self) {
        final var helper = MoreObjects.toStringHelper(ChoiceInArchetype.class)
            .add("name", self.name())
            .add("parentName", self.parentName());
        addNonEmpty(helper, "cases", self.cases());
        return helper.toString();
    }

    /**
     * Implementation of {@link DataContainerArchetype#toString()}.
     *
     * @param <A> the {@link DataContainerArchetype} type
     * @param archetypeClass the archetype class
     * @param self the archetype
     * @return a String
     */
    @NonNullByDefault
    static <A extends DataContainerArchetype> String toString(final Class<A> archetypeClass, final A self) {
        return toStringHelper(archetypeClass, self).toString();
    }

    /**
     * Implementation of {@link IdentityArchetype#toString()}.
     *
     * @param self the archetype
     * @return a String
     */
    @NonNullByDefault
    static String toString(final IdentityArchetype self) {
        final var helper = MoreObjects.toStringHelper(IdentityArchetype.class).add("name", self.name());
        addNonEmpty(helper, "baseIdentities", self.baseIdentities());
        return helper.toString();
    }

    /**
     * Implementation of {@link OperationArchetype.OfAction#toString()}.
     *
     * @param <A> the {@link OperationArchetype.OfAction} type
     * @param archetypeClass the archetype class
     * @param self the archetype
     * @return a String
     */
    @NonNullByDefault
    static <A extends OperationArchetype.OfAction> String toString(final Class<A> archetypeClass, final A self) {
        return toStringHelper(archetypeClass, self).add("parentName", self.parentName()).toString();
    }

    private static void addNonEmpty(final ToStringHelper helper, final @NonNull String name,
            final @NonNull List<? extends Archetype> list) {
        if (!list.isEmpty()) {
            helper.add(name, list.stream().map(Type::name).toList());
        }
    }

    /**
     * Helper for implementations of {@link DataContainerArchetype#toString()}.
     *
     * @param <A> the {@link DataContainerArchetype} type
     * @param archetypeClass the archetype class
     * @param self the archetype
     * @return a {@link ToStringHelper}
     */
    @NonNullByDefault
    static <A extends DataContainerArchetype> ToStringHelper toStringHelper(final Class<A> archetypeClass,
            final A self) {
        final var helper = MoreObjects.toStringHelper(archetypeClass).add("name", self.name());
        addNonEmpty(helper, "partials", self.partials());
        addNonEmpty(helper, "typeObjects", self.typeObjects());
        final var methods = self.getters();
        if (!methods.isEmpty()) {
            helper.add("methods", methods);
        }
        return helper;
    }

    /**
     * Helper for implementations of {@link OperationArchetype#toString()}.
     *
     * @param <A> the {@link OperationArchetype} type
     * @param archetypeClass the archetype class
     * @param self the archetype
     * @return a {@link ToStringHelper}
     */
    @NonNullByDefault
    static <A extends OperationArchetype> ToStringHelper toStringHelper(final Class<A> archetypeClass, final A self) {
        return MoreObjects.toStringHelper(archetypeClass)
            .add("name", self.name())
            .add("input", self.input().name())
            .add("output", self.output().name());
    }

    /**
     * Helper for implementations for {@link ConcreteType#toString()}.
     *
     * @param self the type
     * @return a {@link ToStringHelper}
     */
    @NonNullByDefault
    static ToStringHelper toStringHelper(final ConcreteType self) {
        return MoreObjects.toStringHelper(ConcreteType.class).add("name", self.name());
    }

    /**
     * Helper for implementations for {@link ConcreteType#toString()}.
     *
     * @param self the type
     * @return a {@link ToStringHelper}
     */
    @NonNullByDefault
    static ToStringHelper toStringHelper(final RestrictedType self) {
        return MoreObjects.toStringHelper(RestrictedType.class)
            .add("name", self.name())
            .add("restrictions", self.restrictions());
    }
}
