/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;

class BooleanStringCodec extends TypeDefinitionAwareCodec<Boolean, BooleanTypeDefinition>
        implements BooleanCodec<String> {

    protected BooleanStringCodec(final Optional<BooleanTypeDefinition> typeDef) {
        super(typeDef, Boolean.class);
    }

    @Override
    public final String serialize(final Boolean data) {
        return data == null ? "" : data.toString();
    }

    @Override
    public final Boolean deserialize(final String stringRepresentation) {
        return Boolean.valueOf(stringRepresentation);
    }

    static TypeDefinitionAwareCodec<?,BooleanTypeDefinition> from(final BooleanTypeDefinition normalizedType) {
        return new BooleanStringCodec(Optional.fromNullable(normalizedType));
    }
}