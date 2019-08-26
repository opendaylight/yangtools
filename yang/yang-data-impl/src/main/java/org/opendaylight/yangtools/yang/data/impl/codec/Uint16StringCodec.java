/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import org.opendaylight.yangtools.yang.data.api.codec.Uint16Codec;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;

final class Uint16StringCodec extends AbstractIntegerStringCodec<Integer, Uint16TypeDefinition> implements
        Uint16Codec<String> {
    Uint16StringCodec(final Uint16TypeDefinition typeDef) {
        super(typeDef, extractRange(typeDef), Integer.class);
    }

    @Override
    protected Integer deserialize(final String stringRepresentation, final int base) {
        return Integer.valueOf(stringRepresentation, base);
    }
}
