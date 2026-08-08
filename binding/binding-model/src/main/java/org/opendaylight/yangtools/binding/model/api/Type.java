/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * The Type interface defines the base type for all types defined in java. Each Type defined in java MUST contain name
 * and package name, except of primitive types like int, byte etc. In case of mapping of primitive type the package name
 * MUST be left as empty string.
 */
@NonNullByDefault
public sealed interface Type extends Immutable permits Archetype, ConcreteType, ParameterizedType, TypeRef {
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
}
