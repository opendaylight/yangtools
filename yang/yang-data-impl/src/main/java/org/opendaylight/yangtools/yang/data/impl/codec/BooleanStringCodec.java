/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public final class BooleanStringCodec extends TypeDefinitionAwareCodec<Boolean, BooleanTypeDefinition>
        implements BooleanCodec<String> {

    private BooleanStringCodec(final Optional<BooleanTypeDefinition> typeDef) {
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
        checkArgument("true".equalsIgnoreCase(stringRepresentation) || "false".equalsIgnoreCase(stringRepresentation),
            "Invalid value '%s' for boolean type. Allowed values are true and false", stringRepresentation);
        return Boolean.valueOf(stringRepresentation);
    }

    public static BooleanStringCodec from(final BooleanTypeDefinition normalizedType) {
        return new BooleanStringCodec(Optional.of(normalizedType));
    }
}
