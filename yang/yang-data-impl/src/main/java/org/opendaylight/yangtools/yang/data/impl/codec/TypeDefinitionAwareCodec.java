/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DerivedType;

public abstract class TypeDefinitionAwareCodec<J, T extends TypeDefinition<T>> implements DataStringCodec<J> {
    private final Optional<T> typeDefinition;
    private final Class<J> inputClass;

    @Override
    public Class<J> getInputClass() {
        return inputClass;
    }

    protected TypeDefinitionAwareCodec(final Optional<T> typeDefinition, final Class<J> outputClass) {
        Preconditions.checkArgument(outputClass != null, "Output class must be specified.");
        this.typeDefinition = typeDefinition;
        this.inputClass = outputClass;
    }

    public Optional<T> getTypeDefinition() {
        return typeDefinition;
    }

    @Deprecated
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> from(final TypeDefinition typeDefinition) {
        return (TypeDefinitionAwareCodec)fromType(typeDefinition);
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T extends TypeDefinition<T>> TypeDefinitionAwareCodec<?, T> fromType(final T typeDefinition) {
        return fromType(typeDefinition, null, null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> from(
        final TypeDefinition typeDefinition, final SchemaContext context, final QNameModule parentModule) {
        return (TypeDefinitionAwareCodec)fromType(typeDefinition, context, parentModule);
    }

    @SuppressWarnings("unchecked")
    public static <T extends TypeDefinition<T>> TypeDefinitionAwareCodec<?, T> fromType(
        final T typeDefinition, final SchemaContext context, final QNameModule parentModule) {
        // FIXME: this is not necessary with yang.model.util.type
        final T normalizedType = (T) DerivedType.from(typeDefinition);
        @SuppressWarnings("rawtypes")
        final TypeDefinitionAwareCodec codec;

        if (normalizedType instanceof BinaryTypeDefinition) {
            codec = BinaryStringCodec.from((BinaryTypeDefinition)normalizedType);
        } else if (normalizedType instanceof BitsTypeDefinition) {
            codec = BitsStringCodec.from((BitsTypeDefinition)normalizedType);
        } else if (normalizedType instanceof BooleanTypeDefinition) {
            codec = BooleanStringCodec.from((BooleanTypeDefinition)normalizedType);
        } else if (normalizedType instanceof DecimalTypeDefinition) {
            codec = DecimalStringCodec.from((DecimalTypeDefinition)normalizedType);
        } else if (normalizedType instanceof EmptyTypeDefinition) {
            codec = EmptyStringCodec.INSTANCE;
        } else if (normalizedType instanceof EnumTypeDefinition) {
            codec = EnumStringCodec.from((EnumTypeDefinition)normalizedType);
        } else if (normalizedType instanceof IntegerTypeDefinition) {
            codec = AbstractIntegerStringCodec.from((IntegerTypeDefinition) normalizedType);
        } else if (normalizedType instanceof StringTypeDefinition) {
            codec = StringStringCodec.from((StringTypeDefinition)normalizedType);
        } else if (normalizedType instanceof UnionTypeDefinition) {
            codec = UnionStringCodec.from((UnionTypeDefinition)normalizedType, context, parentModule);
        } else if (normalizedType instanceof UnsignedIntegerTypeDefinition) {
            codec = AbstractIntegerStringCodec.from((UnsignedIntegerTypeDefinition) normalizedType);
        } else if (normalizedType instanceof IdentityrefTypeDefinition) {
            codec = IdentityrefStringCodec.from((IdentityrefTypeDefinition) normalizedType, context, parentModule);
        } else {
            codec = null;
        }
        return codec;
    }
}
