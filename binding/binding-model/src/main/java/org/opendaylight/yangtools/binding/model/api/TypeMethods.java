/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility methods for {@link Type} implementations.
 */
final class TypeMethods {
    private TypeMethods() {
        // hidden on purpose
    }

    /**
     * Implementation of {@link Type#hashCode()}.
     *
     * @param self the type
     * @return a hash code
     */
    @NonNullByDefault
    static int hashCode(final Type self) {
        return self.name().hashCode();
    }

    /**
     * Implementation of {@link Type#equals(Object)}.
     *
     * @param self the type
     * @param obj the object
     * @return {@code true} if {@code obj} is considered equal to {@code self}, {@code false} otherwise
     */
    static boolean equals(final @NonNull Type self, final @Nullable Object obj) {
        return requireNonNull(self) == obj || obj instanceof Type other && self.name().equals(other.name());
    }

    /**
     * Implementation of {@link ParameterizedType#toString()}.
     *
     * @param self the type
     * @return a String
     */
    @NonNullByDefault
    static String toString(final ParameterizedType self) {
        final var helper = MoreObjects.toStringHelper(ParameterizedType.class).add("name", self.name());
        final var arguments = self.getActualTypeArguments();
        if (!arguments.isEmpty()) {
            helper.add("arguments", arguments);
        }
        return helper.toString();
    }

    /**
     * Implementation of {@link InterfaceArchetype#toString()}.
     *
     * @param <A> the {@link InterfaceArchetype} type
     * @param archetypeClass the archetype class
     * @param self the archetype
     * @return a String
     */
    @NonNullByDefault
    static <A extends InterfaceArchetype> String toString(final Class<A> archetypeClass, final A self) {
        final var helper = MoreObjects.toStringHelper(archetypeClass).add("name", self.name());
        addNonEmpty(helper, "annotations", self.annotations());
        addNonEmpty(helper, "implements", self.getImplements());
        addNonEmpty(helper, "enclosedTypes", self.enclosedTypes());
        addNonEmpty(helper, "constants", self.getConstantDefinitions());
        addNonEmpty(helper, "methods", self.getMethodDefinitions());
        return helper.toString();
    }

    private static void addNonEmpty(final ToStringHelper helper, final @NonNull String name,
            final @NonNull List<?> list) {
        if (!list.isEmpty()) {
            helper.add(name, list);
        }
    }
}
