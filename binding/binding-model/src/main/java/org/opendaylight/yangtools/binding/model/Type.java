/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.TypeRef;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A binding type. These come it two basic forms:
 * <ol>
 *   <li>a pre-existing Java class, represented by {@link ConcreteType}</li>
 *   <li>a generated Java class, represented by {@link Archetype}</li>
 * </ol>
 */
@NonNullByDefault
@SuppressWarnings("removal")
public sealed interface Type extends Immutable permits Archetype, ReturnType, ParameterizedType, TypeRef {
    /**
     * {@return this type's {@link TypeName}}
     */
    TypeName name();

    /**
     * {@return name of the package that interface belongs to}
     */
    default String packageName() {
        return name().packageName();
    }

    /**
     * {@return the {@code simple name} of this type}
     */
    default String simpleName() {
        return name().simpleName();
    }

    /**
     * {@return the {@code canonical name} of this type}
     */
    default String canonicalName() {
        return name().canonicalName();
    }

    /**
     * {@return {@code true} if type represents a Java array type, {@code false} otherwise}
     * @since 15.1.0
     */
    default boolean isArray() {
        return name().isArray();
    }

    @Override
    int hashCode();

    @Override
    boolean equals(@Nullable Object obj);

    @Override
    String toString();
}
