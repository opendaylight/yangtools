/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import org.opendaylight.yangtools.yang.data.impl.codec.AbstractIntegerStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.DecimalStringCodec;
import org.opendaylight.yangtools.yang.data.util.codec.CodecCache;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;

final class Lhotka02JSONCodecFactory extends JSONCodecFactory {
    private final JSONInstanceIdentifierCodec iidCodec;

    Lhotka02JSONCodecFactory(final SchemaContext context, final CodecCache<JSONCodec<?>> cache) {
        super(context, cache);
        iidCodec = new Lhotka02JSONInstanceIdentifierCodec(context, this);
    }

    @Override
    protected JSONCodec<?> instanceIdentifierCodec(final InstanceIdentifierTypeDefinition type) {
        return iidCodec;
    }

    @Override
    Lhotka02JSONCodecFactory rebaseTo(final SchemaContext newSchemaContext, final CodecCache<JSONCodec<?>> newCache) {
        return new Lhotka02JSONCodecFactory(newSchemaContext, newCache);
    }

    @Override
    JSONCodec<?> wrapDecimalCodec(final DecimalStringCodec decimalCodec) {
        return new NumberJSONCodec<>(decimalCodec);
    }

    @Override
    JSONCodec<?> wrapIntegerCodec(final AbstractIntegerStringCodec<?, ?> integerCodec) {
        return new NumberJSONCodec<>(integerCodec);
    }
}
