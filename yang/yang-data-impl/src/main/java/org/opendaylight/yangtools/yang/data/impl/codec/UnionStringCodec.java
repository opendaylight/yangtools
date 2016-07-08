/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.codec.CodecFactory;
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UnionStringCodec
        extends TypeDefinitionAwareCodec<Object, UnionTypeDefinition>
        implements UnionCodec<String> {

    private static final Logger LOG = LoggerFactory.getLogger(UnionStringCodec.class);

    private final DataSchemaNode schema;
    private final CodecFactory codecFactory;

    private UnionStringCodec(
        @Nullable final DataSchemaNode schema, final Optional<UnionTypeDefinition> typeDef,
        @Nullable final CodecFactory codecFactory) {
        super(typeDef, Object.class);
        this.schema = schema;
        this.codecFactory = codecFactory;
    }

    static TypeDefinitionAwareCodec<?, UnionTypeDefinition> from(
        @Nullable final DataSchemaNode schema, final UnionTypeDefinition normalizedType,
        @Nullable final CodecFactory codecFactory) {
        return new UnionStringCodec(schema, Optional.fromNullable(normalizedType), codecFactory);
    }

    @Override
    public String serialize(final Object data) {
        for (final TypeDefinition<?> type : getTypeDefinition().get().getTypes()) {
            final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> typeAwareCodec =
                    from(schema, type, codecFactory);
            if (typeAwareCodec == null) {
                LOG.debug("no codec found for {} {}", type, codecFactory);
                continue;
            }
            Class<?> inputClass = typeAwareCodec.getInputClass();
            if (inputClass.isInstance(data)
                || (inputClass == Void.class && data == null)) { // EmptyStringCodec
                try {
                    return typeAwareCodec.serialize(data);
                } catch (final Exception e) {
                    LOG.debug("Data {} did not match for {}", data, type, e);
                    // invalid - try the next union type.
                }
            }
        }
        throw new IllegalArgumentException("Invalid data \"" + data + "\" for union type.");
    }

    @Override
    public Object deserialize(final String stringRepresentation) {

        if (stringRepresentation == null) {
            return null;
        }
        if (!getTypeDefinition().isPresent()) {
            return stringRepresentation;
        }

        Object returnValue = null;
        for (final TypeDefinition<?> type : getTypeDefinition().get().getTypes()) {
            final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> typeAwareCodec =
                    from(schema, type, codecFactory);
            if (typeAwareCodec == null) {
                /*
                 * This is a type for which we have no codec (eg identity ref) so we'll say it's
                 * valid
                 */
                returnValue = stringRepresentation;
                continue;
            }

            try {
                final Object deserialized = typeAwareCodec.deserialize(stringRepresentation);
                if (deserialized != null) {
                    return deserialized;
                }
                returnValue = stringRepresentation;
            } catch (final Exception e) {
                LOG.debug("Value {} did not matched representation for {}",stringRepresentation,type,e);
                // invalid - try the next union type.
            }
        }
        if (returnValue != null) {
            return returnValue;
        }
        throw new IllegalArgumentException("Invalid value \"" + stringRepresentation + "\" for union type.");
    }
}
