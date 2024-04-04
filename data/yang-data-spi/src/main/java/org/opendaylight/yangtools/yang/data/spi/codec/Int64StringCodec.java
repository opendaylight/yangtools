/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import org.opendaylight.yangtools.yang.data.api.codec.Int64Codec;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;

final class Int64StringCodec extends AbstractIntegerStringCodec<Long, Int64TypeDefinition>
        implements Int64Codec<String> {
    Int64StringCodec(final Int64TypeDefinition typeDef) {
        super(Long.class, typeDef);
    }

    @Override
    protected Long deserialize(final String stringRepresentation, final int base) {
        return Long.valueOf(stringRepresentation, base);
    }
}
