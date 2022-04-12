/*
 * Copyright (c) 2016 Intel Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.concepts.AbstractIllegalArgumentCodec;
import org.opendaylight.yangtools.concepts.IllegalArgumentCodec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated(forRemoval = true, since = "8.0.3")
public abstract class AbstractStringUnionCodec extends AbstractIllegalArgumentCodec<String, Object> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractStringUnionCodec.class);

    protected final DataSchemaNode schema;
    protected final UnionTypeDefinition typeDefinition;

    protected AbstractStringUnionCodec(final DataSchemaNode schema, final UnionTypeDefinition typeDefinition) {
        this.schema = requireNonNull(schema);
        this.typeDefinition = requireNonNull(typeDefinition);
    }

    protected abstract IllegalArgumentCodec<String, Object> codecFor(TypeDefinition<?> type);

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    protected Object deserializeImpl(final String stringRepresentation) {
        Object returnValue = null;
        for (final TypeDefinition<?> type : typeDefinition.getTypes()) {
            IllegalArgumentCodec<String, Object> codec = codecFor(type);
            if (codec == null) {
                /*
                 * This is a type for which we have no codec (eg identity ref) so we'll say it's valid
                 */
                returnValue = stringRepresentation;
                continue;
            }
            try {
                // FIXME: this hunting is a bit inefficient: we probably want to use Optional or something
                return codec.deserialize(stringRepresentation);
            } catch (final IllegalArgumentException e) {
                LOG.debug("Value {} did not match representation for {}", stringRepresentation, type, e);
                // invalid - try the next union type.
            }
        }
        if (returnValue == null) {
            throw new IllegalArgumentException("Invalid value \"" + stringRepresentation + "\" for union type.");
        }
        return returnValue;
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    protected final String serializeImpl(final Object data) {
        for (final TypeDefinition<?> type : typeDefinition.getTypes()) {
            IllegalArgumentCodec<String, Object> codec = codecFor(type);
            if (codec == null) {
                LOG.debug("no codec found for {}", type);
                continue;
            }
            try {
                return codec.serialize(data);
            } catch (final IllegalArgumentException e) {
                LOG.debug("Data {} did not match for {}", data, type, e);
                // invalid - try the next union type.
            }
        }
        throw new IllegalArgumentException("Invalid data \"" + data + "\" for union type.");
    }
}
