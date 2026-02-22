/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Decimal64;

/**
 * This interface represents a Java type which is expected to be available via  {@code binding-spec} dependencies. This
 * includes all classes therein, but also {@code yang-common}'s {@code Decimal64}.
 *
 * <p>These types support binding and unbinding additional {@link Restrictions} via their {@link RestrictedType}
 * specialization.
 */
@NonNullByDefault
public sealed interface ConcreteType extends Type permits Decimal64Type, DefaultConcreteType, RestrictedType {
    /**
     * {@return this type's equivalent with specified {@link Restrictions}}
     * @param newRestrictions the restrictions to apply
     */
    RestrictedType withRestrictions(Restrictions newRestrictions);

    /**
     * {@return a {@link ConcreteType} for specified type {@link Class}}
     * @param typeClass the type class
     */
    static ConcreteType ofClass(final Class<?> typeClass) {
        if (typeClass.equals(Decimal64.class)) {
            throw new IllegalArgumentException("Cannot instantiate on Decimal64, use Decimal64Type instead");
        }
        return new DefaultConcreteType(JavaTypeName.create(typeClass));
    }
}
