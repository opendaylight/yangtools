/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Objects;
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;

final class BooleanStringCodec extends TypeDefinitionAwareCodec<Boolean, BooleanTypeDefinition>
        implements BooleanCodec<String> {

    BooleanStringCodec(final Optional<BooleanTypeDefinition> typeDef) {
        super(typeDef, Boolean.class);
    }

    @Override
    public String serialize(final Boolean data) {
        return Objects.toString(data, "");
    }

    @Override
    public Boolean deserialize(final String stringRepresentation) {
        if (stringRepresentation == null) {
            return null;
        }
        validate(stringRepresentation);
        return Boolean.valueOf(stringRepresentation);
    }

    private static void validate(final String string) {
        Preconditions.checkArgument("true".equalsIgnoreCase(string) || "false".equalsIgnoreCase(string),
                "Invalid value '%s' for boolean type. Allowed values are true and false", string);
    }

    static TypeDefinitionAwareCodec<?,BooleanTypeDefinition> from(final BooleanTypeDefinition normalizedType) {
        return new BooleanStringCodec(Optional.fromNullable(normalizedType));
    }
}
