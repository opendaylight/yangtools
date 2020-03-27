/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static java.util.Objects.requireNonNull;

import java.util.Base64;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.codec.IllegalYangValueException;
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UnionStringCodec extends TypeDefinitionAwareCodec<Object, UnionTypeDefinition>
        implements UnionCodec<String> {
    private static final Logger LOG = LoggerFactory.getLogger(UnionStringCodec.class);

    UnionStringCodec(final UnionTypeDefinition typeDef) {
        super(requireNonNull(typeDef), Object.class);
    }

    static TypeDefinitionAwareCodec<?, UnionTypeDefinition> from(final UnionTypeDefinition normalizedType) {
        return new UnionStringCodec(normalizedType);
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    protected Object deserializeImpl(final String stringRepresentation) {
        for (final TypeDefinition<?> type : getTypeDefinition().get().getTypes()) {
            final TypeDefinitionAwareCodec<Object, ?> typeAwareCodec = from(type);
            if (typeAwareCodec == null) {
                /*
                 * This is a type for which we have no codec (eg identity ref) so we'll say it's
                 * valid
                 */
                return stringRepresentation;
            }

            try {
                return typeAwareCodec.deserialize(stringRepresentation);
            } catch (final Exception e) {
                LOG.debug("Value {} did not matched representation for {}",stringRepresentation,type,e);
                // invalid - try the next union type.
            }
        }

        throw new IllegalYangValueException(
                RpcError.ErrorSeverity.ERROR,
                RpcError.ErrorType.PROTOCOL,
                "bad-element",
                "Invalid value '" + stringRepresentation + "' for union type.");
    }

    @Override
    protected String serializeImpl(final Object data) {
        return data instanceof byte[] ? Base64.getEncoder().encodeToString((byte[]) data) : data.toString();
    }
}
