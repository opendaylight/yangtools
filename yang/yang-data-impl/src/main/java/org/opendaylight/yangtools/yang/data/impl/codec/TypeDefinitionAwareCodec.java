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
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.codec.CodecFactory;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> from(
        @Nullable final DataSchemaNode schema, final TypeDefinition typeDefinition,
        @Nullable final CodecFactory codecFactory) {
        return (TypeDefinitionAwareCodec)fromType(schema, typeDefinition, codecFactory);
    }

    @Deprecated
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> from(
        final TypeDefinition typeDefinition) {
        return (TypeDefinitionAwareCodec)fromType(null, typeDefinition, null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends TypeDefinition<T>> TypeDefinitionAwareCodec<?, T> fromType(
        @Nullable final DataSchemaNode schema, final T typeDefinition, @Nullable final CodecFactory codecFactory) {
        @SuppressWarnings("rawtypes")
        final TypeDefinitionAwareCodec codec;

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
        } else if (typeDefinition instanceof IntegerTypeDefinition) {
            codec = AbstractIntegerStringCodec.from((IntegerTypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof StringTypeDefinition) {
            codec = StringStringCodec.from((StringTypeDefinition)typeDefinition);
        } else if (typeDefinition instanceof UnionTypeDefinition) {
            codec = UnionStringCodec.from(schema, (UnionTypeDefinition)typeDefinition, codecFactory);
        } else if (typeDefinition instanceof UnsignedIntegerTypeDefinition) {
            codec = AbstractIntegerStringCodec.from((UnsignedIntegerTypeDefinition) typeDefinition);
        } else if (typeDefinition instanceof IdentityrefTypeDefinition) {
            if (schema != null && codecFactory != null) {
                final Codec<String, QName> identityrefCodec = codecFactory.codecForIdentityref(schema);
                codec = IdentityrefStringCodec.from(schema, (IdentityrefTypeDefinition) typeDefinition,
                                                    identityrefCodec);
            } else {
                codec = null;
            }
        } else {
            codec = null;
        }
        return codec;
    }
}
