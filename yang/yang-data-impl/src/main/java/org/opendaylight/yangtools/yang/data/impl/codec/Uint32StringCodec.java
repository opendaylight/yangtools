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
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.codec.Uint32Codec;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;

final class Uint32StringCodec extends AbstractIntegerStringCodec<Uint32, Uint32TypeDefinition> implements
        Uint32Codec<String> {
    Uint32StringCodec(final Optional<Uint32TypeDefinition> typeDef) {
        super(typeDef, extractRange(typeDef.orElse(null)), Uint32.class);
    }

    @Override
    Uint32 deserialize(final String stringRepresentation, final int base) {
        return Uint32.valueOf(stringRepresentation, base);
    }

    @Override
    public String serialize(final Uint32 data) {
        return Objects.toString(data, "");
    }
}
