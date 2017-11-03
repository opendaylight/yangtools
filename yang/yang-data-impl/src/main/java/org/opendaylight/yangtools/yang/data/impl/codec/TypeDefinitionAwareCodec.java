/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
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

public abstract class TypeDefinitionAwareCodec<J, T extends TypeDefinition<T>> implements DataStringCodec<J> {
    private final Class<J> inputClass;
    private final T typeDefinition;

    protected TypeDefinitionAwareCodec(final Optional<T> typeDefinition, final Class<J> outputClass) {
        this.typeDefinition = typeDefinition.orElse(null);
        this.inputClass = requireNonNull(outputClass);
    }

    @Override
    public Class<J> getInputClass() {
        return inputClass;
    }

    public Optional<T> getTypeDefinition() {
        return Optional.ofNullable(typeDefinition);
    }

    @SuppressWarnings("unchecked")
    public static TypeDefinitionAwareCodec<Object, ?> from(final TypeDefinition<?> typeDefinition) {
        return (TypeDefinitionAwareCodec<Object, ?>) fromType(typeDefinition);
    }

    public static TypeDefinitionAwareCodec<?, ?> fromType(final TypeDefinition<?> typeDefinition) {
        final TypeDefinitionAwareCodec<?, ?> codec;

        if (typeDefinition instanceof BinaryTypeDefinition) {
            codec = BinaryStringCodec.from((BinaryTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof BitsTypeDefinition) {
            codec = BitsStringCodec.from((BitsTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof BooleanTypeDefinition) {
            codec = BooleanStringCodec.from((BooleanTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof DecimalTypeDefinition) {
            codec = DecimalStringCodec.from((DecimalTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof EmptyTypeDefinition) {
            codec = EmptyStringCodec.INSTANCE;
        } else if (typeDefinition instanceof EnumTypeDefinition) {
            codec = EnumStringCodec.from((EnumTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof Int8TypeDefinition) {
            codec = AbstractIntegerStringCodec.from((Int8TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof Int16TypeDefinition) {
            codec = AbstractIntegerStringCodec.from((Int16TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof Int32TypeDefinition) {
            codec = AbstractIntegerStringCodec.from((Int32TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof Int64TypeDefinition) {
            codec = AbstractIntegerStringCodec.from((Int64TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof StringTypeDefinition) {
            codec = StringStringCodec.from((StringTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof UnionTypeDefinition) {
            codec = UnionStringCodec.from((UnionTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof Uint8TypeDefinition) {
            codec = AbstractIntegerStringCodec.from((Uint8TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof Uint16TypeDefinition) {
            codec = AbstractIntegerStringCodec.from((Uint16TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof Uint32TypeDefinition) {
            codec = AbstractIntegerStringCodec.from((Uint32TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof Uint64TypeDefinition) {
            codec = AbstractIntegerStringCodec.from((Uint64TypeDefinition) typeDefinition);
        } else {
            codec = null;
        }
        return codec;
    }
}
