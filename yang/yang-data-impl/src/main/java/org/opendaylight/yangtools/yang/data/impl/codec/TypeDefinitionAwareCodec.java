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

    // FIXME: do we want an Optional or a throws instead of @Nullable here?
    public static @Nullable TypeDefinitionAwareCodec<?, ?> fromType(final TypeDefinition<?> typeDefinition) {
        if (typeDefinition instanceof BinaryTypeDefinition) {
            return BinaryStringCodec.from((BinaryTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof BitsTypeDefinition) {
            return BitsStringCodec.from((BitsTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof BooleanTypeDefinition) {
            return BooleanStringCodec.from((BooleanTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof DecimalTypeDefinition) {
            return  DecimalStringCodec.from((DecimalTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof EmptyTypeDefinition) {
            return EmptyStringCodec.INSTANCE;
        } else if (typeDefinition instanceof EnumTypeDefinition) {
            return EnumStringCodec.from((EnumTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof Int8TypeDefinition) {
            return AbstractIntegerStringCodec.from((Int8TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof Int16TypeDefinition) {
            return AbstractIntegerStringCodec.from((Int16TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof Int32TypeDefinition) {
            return AbstractIntegerStringCodec.from((Int32TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof Int64TypeDefinition) {
            return AbstractIntegerStringCodec.from((Int64TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof StringTypeDefinition) {
            return StringStringCodec.from((StringTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof UnionTypeDefinition) {
            return UnionStringCodec.from((UnionTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof Uint8TypeDefinition) {
            return AbstractIntegerStringCodec.from((Uint8TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof Uint16TypeDefinition) {
            return AbstractIntegerStringCodec.from((Uint16TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof Uint32TypeDefinition) {
            return AbstractIntegerStringCodec.from((Uint32TypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof Uint64TypeDefinition) {
            return AbstractIntegerStringCodec.from((Uint64TypeDefinition) typeDefinition);
        } else {
            return null;
        }
    }
}
