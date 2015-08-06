/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.Int32Codec;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;

class Int32StringCodec extends AbstractIntegerStringCodec<Integer, IntegerTypeDefinition> implements Int32Codec<String> {

    protected Int32StringCodec(final Optional<IntegerTypeDefinition> typeDef) {
        super(typeDef, extractRange(typeDef.orNull()), Integer.class);
    }

    @Override
    public final Integer deserialize(final String stringRepresentation, final int base) {
        return Integer.valueOf(stringRepresentation, base);
    }

    @Override
    public final String serialize(final Integer data) {
        return data == null ? "" : data.toString();
    }

    @Override
    protected Integer convertValue(final Number value) {
        return value.intValue();
    }
}
