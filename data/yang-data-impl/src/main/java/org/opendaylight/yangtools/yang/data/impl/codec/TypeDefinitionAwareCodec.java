/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.LoggerFactory;

public abstract class TypeDefinitionAwareCodec<J, T extends TypeDefinition<T>> extends AbstractDataStringCodec<J> {
    private static final boolean ENABLE_UNION_CODEC =
        !Boolean.getBoolean("org.opendaylight.yangtools.yang.data.impl.codec.disable-union");

    static {
        if (!ENABLE_UNION_CODEC) {
            LoggerFactory.getLogger(TypeDefinitionAwareCodec.class).info("Support for unions is disabled");
        }
    }

    private final @NonNull Class<J> inputClass;
    private final @NonNull T typeDefinition;

    protected TypeDefinitionAwareCodec(final Class<J> outputClass, final T typeDefinition) {
        inputClass = requireNonNull(outputClass);
        this.typeDefinition = requireNonNull(typeDefinition);
    }

    @Override
    public final Class<J> getInputClass() {
        return inputClass;
    }

    protected final @NonNull T typeDefinition() {
        return typeDefinition;
    }

    @SuppressWarnings("unchecked")
    public static TypeDefinitionAwareCodec<Object, ?> from(final TypeDefinition<?> typeDefinition) {
        return (TypeDefinitionAwareCodec<Object, ?>) fromType(typeDefinition);
    }

    // FIXME: do we want an Optional or a throws instead of @Nullable here?
    public static @Nullable TypeDefinitionAwareCodec<?, ?> fromType(final TypeDefinition<?> typeDefinition) {
        return switch (typeDefinition) {
            case BinaryTypeDefinition binaryType -> BinaryStringCodec.from(binaryType);
            case BitsTypeDefinition bitsType -> BitsStringCodec.from(bitsType);
            case BooleanTypeDefinition booleanType -> BooleanStringCodec.from(booleanType);
            case DecimalTypeDefinition decimalType -> DecimalStringCodec.from(decimalType);
            case EmptyTypeDefinition emptyType -> new EmptyStringCodec(emptyType);
            case EnumTypeDefinition enumType -> EnumStringCodec.from(enumType);
            case Int8TypeDefinition int8Type -> AbstractIntegerStringCodec.from(int8Type);
            case Int16TypeDefinition int16Type -> AbstractIntegerStringCodec.from(int16Type);
            case Int32TypeDefinition int32Type -> AbstractIntegerStringCodec.from(int32Type);
            case Int64TypeDefinition int64Type -> AbstractIntegerStringCodec.from(int64Type);
            case StringTypeDefinition stringType -> StringStringCodec.from(stringType);
            case Uint8TypeDefinition uint8Type -> AbstractIntegerStringCodec.from(uint8Type);
            case Uint16TypeDefinition uint16Type -> AbstractIntegerStringCodec.from(uint16Type);
            case Uint32TypeDefinition uint32Type -> AbstractIntegerStringCodec.from(uint32Type);
            case Uint64TypeDefinition uint64Type -> AbstractIntegerStringCodec.from(uint64Type);
            case UnionTypeDefinition unionType when ENABLE_UNION_CODEC -> UnionStringCodec.from(unionType);
            default -> null;
        };
    }
}
