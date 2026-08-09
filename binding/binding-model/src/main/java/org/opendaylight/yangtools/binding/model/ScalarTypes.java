/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.BuiltInType;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.impl.ConcreteTypeImpl;

/**
 * YANG {@link BuiltInType}s that are so simple they are mapped directly to their Java types when not restricted. They
 * are also subject to being encapsulated in {@link ScalarTypeObjectArchetype}.
 */
// FIXME: improve documentation
// FIXME: this should be an enumeration that extends ScalarType and Decimal64Type is a separate thing (or not?)
@Beta
@NonNullByDefault
public final class ScalarTypes {
    /**
     * {@code Type} representation of {@code binary} YANG type.
     */
    public static final ConcreteType BINARY = typeForBuiltIn(BuiltInType.BINARY);
    /**
     * {@code Type} representation of {@code boolean} YANG type.
     */
    public static final ConcreteType BOOLEAN = typeForBuiltIn(BuiltInType.BOOLEAN);
    /**
     * {@code Type} representation of {@code empty} YANG type.
     */
    public static final ConcreteType EMPTY = typeForBuiltIn(BuiltInType.EMPTY);
    /**
     * {@code Type} representation of {@code instance-identifier} YANG type.
     */
    public static final ConcreteType INSTANCE_IDENTIFIER = typeForBuiltIn(BuiltInType.INSTANCE_IDENTIFIER);
    /**
     * {@code Type} representation of {@code int8} YANG type.
     */
    public static final ConcreteType INT8 = typeForBuiltIn(BuiltInType.INT8);
    /**
     * {@code Type} representation of {@code int16} YANG type.
     */
    public static final ConcreteType INT16 = typeForBuiltIn(BuiltInType.INT16);
    /**
     * {@code Type} representation of {@code int32} YANG type.
     */
    public static final ConcreteType INT32 = typeForBuiltIn(BuiltInType.INT32);
    /**
     * {@code Type} representation of {@code int64} YANG type.
     */
    public static final ConcreteType INT64 = typeForBuiltIn(BuiltInType.INT64);
    /**
     * {@code Type} representation of {@code string} YANG type.
     */
    public static final ConcreteType STRING = typeForBuiltIn(BuiltInType.STRING);
    /**
     * {@code Type} representation of {@code uint8} YANG type.
     */
    public static final ConcreteType UINT8 = typeForBuiltIn(BuiltInType.UINT8);
    /**
     * {@code Type} representation of {@code uint16} YANG type.
     */
    public static final ConcreteType UINT16 = typeForBuiltIn(BuiltInType.UINT16);
    /**
     * {@code Type} representation of {@code uint32} YANG type.
     */
    public static final ConcreteType UINT32 = typeForBuiltIn(BuiltInType.UINT32);
    /**
     * {@code Type} representation of {@code uint64} YANG type.
     */
    public static final ConcreteType UINT64 = typeForBuiltIn(BuiltInType.UINT64);

    private ScalarTypes() {
        // Hidden on purpose
    }

    /**
     * Returns an instance of {@link ConcreteType} describing a {@link BuiltInType}.
     *
     * @param type {@link BuiltInType} to describe
     * @return Description of the type
     */
    private static ConcreteType typeForBuiltIn(final BuiltInType<?> type) {
        return new ConcreteTypeImpl(type.javaClass());
    }
}
