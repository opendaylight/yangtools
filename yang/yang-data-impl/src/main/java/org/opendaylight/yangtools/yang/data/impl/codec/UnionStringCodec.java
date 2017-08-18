/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.io.BaseEncoding;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UnionStringCodec extends TypeDefinitionAwareCodec<Object, UnionTypeDefinition> implements UnionCodec<String> {

    private final static Logger LOG = LoggerFactory.getLogger(UnionStringCodec.class);

    private UnionStringCodec(final Optional<UnionTypeDefinition> typeDef) {
        super(typeDef, Object.class);
    }

    static TypeDefinitionAwareCodec<?, UnionTypeDefinition> from(final UnionTypeDefinition normalizedType) {
        return new UnionStringCodec(Optional.ofNullable(normalizedType));
    }

    @Override
    public String serialize(final Object data) {
        if (data instanceof byte[]) {
            return BaseEncoding.base64().encode((byte[]) data);
        }
        return Objects.toString(data, "");
    }

    @Override
    public Object deserialize(final String stringRepresentation) {

        if (!getTypeDefinition().isPresent()) {
            return stringRepresentation;
        }

        for (final TypeDefinition<?> type : getTypeDefinition().get().getTypes()) {
            final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> typeAwareCodec = from(type);
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

        throw new IllegalArgumentException("Invalid value \"" + stringRepresentation + "\" for union type.");
    }
}
