/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.contract;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.binding.UnionTypeObject;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Mapping of a YANG built-in type to its base Java class. Note that {@code type leafref} does not have an associated
 * type mapping.
 *
 * @param <T> Java type
 */
public final class BuiltInType<T> {
    // Ordered by RFC7950 section defining the type
    public static final @NonNull BuiltInType<@NonNull Byte> INT8 = new BuiltInType<>("int8", Byte.class);
    public static final @NonNull BuiltInType<@NonNull Short> INT16 = new BuiltInType<>("int16", Short.class);
    public static final @NonNull BuiltInType<@NonNull Integer> INT32 = new BuiltInType<>("int32", Integer.class);
    public static final @NonNull BuiltInType<@NonNull Long> INT64 = new BuiltInType<>("int64", Long.class);
    public static final @NonNull BuiltInType<@NonNull Uint8> UINT8 = new BuiltInType<>("uint8", Uint8.class);
    public static final @NonNull BuiltInType<@NonNull Uint16> UINT16 = new BuiltInType<>("uint16", Uint16.class);
    public static final @NonNull BuiltInType<@NonNull Uint32> UINT32 = new BuiltInType<>("uint32", Uint32.class);
    public static final @NonNull BuiltInType<@NonNull Uint64> UINT64 = new BuiltInType<>("uint64", Uint64.class);
    public static final @NonNull BuiltInType<@NonNull Decimal64> DECIMAL64 =
        new BuiltInType<>("decimal64", Decimal64.class);
    public static final @NonNull BuiltInType<@NonNull String> STRING = new BuiltInType<>("string", String.class);
    public static final @NonNull BuiltInType<@NonNull Boolean> BOOLEAN = new BuiltInType<>("boolean", Boolean.class);
    public static final @NonNull BuiltInType<@NonNull EnumTypeObject> ENUMERATION =
        new BuiltInType<>("enumeration", EnumTypeObject.class);
    public static final @NonNull BuiltInType<@NonNull BitsTypeObject> BITS =
        new BuiltInType<>("bits", BitsTypeObject.class);
    public static final @NonNull BuiltInType<byte @NonNull []> BINARY = new BuiltInType<>("binary", byte[].class);
    public static final @NonNull BuiltInType<@NonNull BaseIdentity> IDENTITYREF =
        new BuiltInType<>("identityref", BaseIdentity.class);
    public static final @NonNull BuiltInType<@NonNull Empty> EMPTY = new BuiltInType<>("empty", Empty.class);
    public static final @NonNull BuiltInType<@NonNull UnionTypeObject> UNION =
        new BuiltInType<>("union", UnionTypeObject.class);
    public static final @NonNull BuiltInType<@NonNull BindingInstanceIdentifier> INSTANCE_IDENTIFIER =
        new BuiltInType<>("instance-identifier", BindingInstanceIdentifier.class);

    private final @NonNull Class<T> javaClass;
    private final String str;

    private BuiltInType(final @NonNull String str, final @NonNull Class<T> javaClass) {
        this.str = requireNonNull(str);
        this.javaClass = requireNonNull(javaClass);
    }

    public static @NonNull BuiltInType<?> of(final Object obj) {
        return switch (obj) {
            case Byte value -> INT8;
            case Short value -> INT16;
            case Integer value -> INT32;
            case Long value -> INT64;
            case Uint8 value -> UINT8;
            case Uint16 value -> UINT16;
            case Uint32 value -> UINT32;
            case Uint64 value -> UINT64;
            case Decimal64 value -> DECIMAL64;
            case String value -> STRING;
            case Boolean value -> BOOLEAN;
            case EnumTypeObject value -> ENUMERATION;
            case BitsTypeObject value -> BITS;
            case byte[] value -> BINARY;
            case BaseIdentity value -> IDENTITYREF;
            case Empty value -> EMPTY;
            case UnionTypeObject value -> UNION;
            case BindingInstanceIdentifier value -> INSTANCE_IDENTIFIER;
            default -> throw new IllegalArgumentException("Invalid value " + obj);
        };
    }

    public static @NonNull Object checkValue(final Object obj) {
        return switch (obj) {
            case @NonNull Byte value -> value;
            case @NonNull Short value -> value;
            case @NonNull Integer value -> value;
            case @NonNull Long value -> value;
            case @NonNull Uint8 value -> value;
            case @NonNull Uint16 value -> value;
            case @NonNull Uint32 value -> value;
            case @NonNull Uint64 value -> value;
            case @NonNull Decimal64 value -> value;
            case @NonNull String value -> value;
            case @NonNull Boolean value -> value;
            case @NonNull EnumTypeObject value -> value;
            case @NonNull BitsTypeObject value -> value;
            case byte @NonNull [] value -> value;
            case @NonNull BaseIdentity value -> value;
            case @NonNull Empty value -> value;
            case @NonNull UnionTypeObject value -> value;
            case @NonNull BindingInstanceIdentifier value -> value;
            default -> throw new IllegalArgumentException("Invalid value " + obj);
        };
    }

    /**
     * Return the Java class carrying values of this type.
     *
     * @return the Java class carrying values of this type
     */
    public @NonNull Class<T> javaClass() {
        return javaClass;
    }

    /**
     * Cast an value object to this built-in type. Unlike {@link Class#cast(Object)}, this method does not tolarate
     * {@code null}s.
     *
     * @param obj value object
     * @return the value object
     * @throws ClassCastException if the object is not of expected type
     * @throws NullPointerException if the object is null
     */
    public @NonNull T castValue(final Object obj) {
        return requireNonNull(javaClass.cast(obj));
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || obj instanceof BuiltInType<?> other
            && javaClass.equals(other.javaClass) && str.equals(other.str);
    }

    @Override
    public String toString() {
        return "YANG " + str;
    }
}
