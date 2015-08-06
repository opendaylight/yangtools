/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.Int8Codec;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;

class Int8StringCodec extends AbstractIntegerStringCodec<Byte, IntegerTypeDefinition> implements Int8Codec<String> {

    protected Int8StringCodec(final Optional<IntegerTypeDefinition> typeDef) {
        super(typeDef, extractRange(typeDef.orNull()), Byte.class);
    }

    @Override
    public final Byte deserialize(final String stringRepresentation, final int base) {
        return Byte.valueOf(stringRepresentation, base);
    }

    @Override
    public String serialize(final Byte data) {
        return data == null ? "" : data.toString();
    }

    @Override
    protected Byte convertValue(final Number value) {
        return value.byteValue();
    }
}
