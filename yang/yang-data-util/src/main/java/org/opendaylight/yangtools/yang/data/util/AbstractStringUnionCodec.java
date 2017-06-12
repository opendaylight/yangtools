/*
 * Copyright (c) 2016 Intel Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.util;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStringUnionCodec implements Codec<String, Object> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractStringUnionCodec.class);

    protected final DataSchemaNode schema;
    protected final UnionTypeDefinition typeDefinition;

    protected AbstractStringUnionCodec(final DataSchemaNode schema, final UnionTypeDefinition typeDefinition) {
        this.schema = Preconditions.checkNotNull(schema);
        this.typeDefinition = Preconditions.checkNotNull(typeDefinition);
    }

    protected abstract Codec<String, Object> codecFor(TypeDefinition<?> type);

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public final String serialize(final Object data) {
        for (final TypeDefinition<?> type : typeDefinition.getTypes()) {
            Codec<String, Object> codec = codecFor(type);
            if (codec == null) {
                LOG.debug("no codec found for {}", type);
                continue;
            }
            try {
                return codec.serialize(data);
            } catch (final Exception e) {
                LOG.debug("Data {} did not match for {}", data, type, e);
                // invalid - try the next union type.
            }
        }
        throw new IllegalArgumentException("Invalid data \"" + data + "\" for union type.");
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public Object deserialize(final String stringRepresentation) {
        if (stringRepresentation == null) {
            return null;
        }

        Object returnValue = null;
        for (final TypeDefinition<?> type : typeDefinition.getTypes()) {
            Codec<String, Object> codec = codecFor(type);
            if (codec == null) {
                /*
                 * This is a type for which we have no codec (eg identity ref) so we'll say it's
                 * valid
                 */
                returnValue = stringRepresentation;
                continue;
            }
            try {
                final Object deserialized = codec.deserialize(stringRepresentation);
                if (deserialized != null) {
                    return deserialized;
                }
                returnValue = stringRepresentation;
            } catch (final Exception e) {
                LOG.debug("Value {} did not matched representation for {}", stringRepresentation, type, e);
                // invalid - try the next union type.
            }
        }
        if (returnValue != null) {
            return returnValue;
        }
        throw new IllegalArgumentException("Invalid value \"" + stringRepresentation + "\" for union type.");
    }
}
