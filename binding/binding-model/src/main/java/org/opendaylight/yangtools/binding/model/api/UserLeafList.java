/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.IdentityArchetype;
import org.opendaylight.yangtools.binding.model.ReturnType;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.UnknownLeafrefType;
import org.opendaylight.yangtools.binding.model.impl.ConcreteTypeImpl;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;

/**
 * {@link ParameterizedType} compatibility with {@link ReturnType} being either a {@link ConcreteType} or
 * {@link UnknownLeafrefType} combined with a user-ordered {@code leaf-list}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
@SuppressWarnings("removal")
public final class UserLeafList implements ReturnTypeCompat {
    /**
     * Singleton instance for reporting {@link UnknownLeafrefType}.
     */
    public static final UserLeafList UNKNOWN = new UserLeafList(List.of());

    static final ConcreteTypeImpl LIST = new ConcreteTypeImpl(List.class);

    private final List<Type> typeArguments;

    private UserLeafList(final List<ReturnType> typeArguments) {
        this.typeArguments = List.copyOf(typeArguments);
    }

    public static UserLeafList of(final Type typeArgument) {
        return switch (typeArgument) {
            case ConcreteType concrete -> new UserLeafList(List.of(concrete));
            case IdentityArchetype identity -> new UserLeafList(List.of(identity));
            case TypeObjectArchetype<?> typeObject -> new UserLeafList(List.of(typeObject));
            case UnknownLeafrefType unknown -> UNKNOWN;
            default -> throw new IllegalArgumentException(typeArgument + " is not an allowed type");
        };
    }

    @Override
    public List<Type> getActualTypeArguments() {
        return typeArguments;
    }

    @Override
    public ConcreteType getRawType() {
        return LIST;
    }

    @Override
    public int hashCode() {
        return TypeMethods.hashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return TypeMethods.equals(this, obj);
    }

    @Override
    public String toString() {
        return TypeMethods.toString(this);
    }
}
