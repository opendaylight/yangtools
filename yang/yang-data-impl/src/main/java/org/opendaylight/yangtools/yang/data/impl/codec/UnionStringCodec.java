/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

class UnionStringCodec extends TypeDefinitionAwareCodec<Object, UnionTypeDefinition> implements
        UnionCodec<String> {

    protected UnionStringCodec(final Optional<UnionTypeDefinition> typeDef) {
        super(typeDef, Object.class);
    }

    static TypeDefinitionAwareCodec<?,UnionTypeDefinition> from(final UnionTypeDefinition normalizedType) {
        return new UnionStringCodec(Optional.fromNullable(normalizedType));
    }

    @Override
    public final String serialize(final Object data) {
        return data == null ? "" : data.toString();
    }

    @Override
    public final Object deserialize(final String stringRepresentation) {
        if( getTypeDefinition().isPresent() ) {
            boolean valid = false;
            for( final TypeDefinition<?> type: getTypeDefinition().get().getTypes() ) {
                final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> typeAwareCodec = from( type );
                if( typeAwareCodec == null ) {
                    // This is a type for which we have no codec (eg identity ref) so we'll say it's valid
                    // but we'll continue in case there's another type for which we do have a codec.
                    valid = true;
                    continue;
                }

                try {
                    typeAwareCodec.deserialize( stringRepresentation );
                    valid = true;
                    break;
                }
                catch( final Exception e ) {
                    // invalid - try the next union type.
                }
            }

            if( !valid ) {
                throw new IllegalArgumentException(
                                    "Invalid value \"" + stringRepresentation + "\" for union type." );
            }
        }

        return stringRepresentation;
    }
}