/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

class StringStringCodec extends TypeDefinitionAwareCodec<String, StringTypeDefinition> implements
        StringCodec<String> {

    protected StringStringCodec(final StringTypeDefinition typeDef) {
        super(Optional.of(typeDef), String.class);
        typeDef.getLengthConstraints();
    }

    static TypeDefinitionAwareCodec<?, StringTypeDefinition> from(final StringTypeDefinition normalizedType) {
        if (normalizedType.getPatternConstraints().isEmpty()) {
            return new StringStringCodec(normalizedType);
        }

        return new StringPatternCheckingCodec(normalizedType);
    }

    @Override
    public final String deserialize(final String stringRepresentation) {
        if (stringRepresentation == null) {
            // FIXME: These seems buggy, but someone may be using this behaviour
            return "";
        }
        validate(stringRepresentation);
        return stringRepresentation;
    }

    @Override
    public final String serialize(final String data) {
        return data == null ? "" : data;
    }

    protected void validate(final String s) {

    }
}