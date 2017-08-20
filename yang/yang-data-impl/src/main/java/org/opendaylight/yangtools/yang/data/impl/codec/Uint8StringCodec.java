/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec;

import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.Uint8Codec;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

final class Uint8StringCodec extends AbstractIntegerStringCodec<Short, UnsignedIntegerTypeDefinition> implements
        Uint8Codec<String> {

    Uint8StringCodec(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
        super(typeDef, extractRange(typeDef.orElse(null)), Short.class);
    }

    @Override
    public String serialize(final Short data) {
        return Objects.toString(data, "");
    }

    @Override
    Short deserialize(final String stringRepresentation, final int base) {
        return Short.valueOf(stringRepresentation, base);
    }

    @Override
    Short convertValue(final Number value) {
        return value.shortValue();
    }
}
