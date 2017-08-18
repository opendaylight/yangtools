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
import org.opendaylight.yangtools.yang.data.api.codec.Uint16Codec;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

final class Uint16StringCodec extends AbstractIntegerStringCodec<Integer, UnsignedIntegerTypeDefinition> implements
        Uint16Codec<String> {
    Uint16StringCodec(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
        super(typeDef, extractRange(typeDef.orElse(null)), Integer.class);
    }

    @Override
    Integer deserialize(final String stringRepresentation, final int base) {
        return Integer.valueOf(stringRepresentation, base);
    }

    @Override
    public String serialize(final Integer data) {
        return Objects.toString(data, "");
    }

    @Override
    Integer convertValue(final Number value) {
        return value.intValue();
    }
}
