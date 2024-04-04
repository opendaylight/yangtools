/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import org.opendaylight.yangtools.yang.data.api.codec.Int16Codec;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;

final class Int16StringCodec extends AbstractIntegerStringCodec<Short, Int16TypeDefinition>
        implements Int16Codec<String> {
    Int16StringCodec(final Int16TypeDefinition typeDef) {
        super(Short.class, typeDef);
    }

    @Override
    protected Short deserialize(final String stringRepresentation, final int base) {
        return Short.valueOf(stringRepresentation, base);
    }
}
