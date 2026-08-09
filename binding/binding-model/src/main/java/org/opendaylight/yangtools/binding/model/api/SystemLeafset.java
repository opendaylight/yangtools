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
import java.util.Set;
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
 * {@link UnknownLeafrefType}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public final class SystemLeafset implements ParameterizedType {
    /**
     * Singleton instance for reporting {@link UnknownLeafrefType}.
     */
    public static final SystemLeafset UNKNOWN = new SystemLeafset(List.of());

    static final ConcreteTypeImpl SET = new ConcreteTypeImpl(Set.class);

    private final List<Type> typeArguments;

    private SystemLeafset(final List<ReturnType> typeArguments) {
        this.typeArguments = List.copyOf(typeArguments);
    }

    public static SystemLeafset of(final Type typeArgument) {
        return switch (typeArgument) {
            case ConcreteType concrete -> new SystemLeafset(List.of(concrete));
            case IdentityArchetype identity -> new SystemLeafset(List.of(identity));
            case TypeObjectArchetype<?> typeObject -> new SystemLeafset(List.of(typeObject));
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
        return SET;
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
