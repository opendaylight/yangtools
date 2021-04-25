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
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;

final class RFC7951JSONCodecFactory extends JSONCodecFactory {
    private final RFC7951JSONInstanceIdentifierCodec iidCodec;

    RFC7951JSONCodecFactory(final EffectiveModelContext context, final CodecCache<JSONCodec<?>> cache) {
        super(context, cache);
        iidCodec = new RFC7951JSONInstanceIdentifierCodec(context, this);
    }

    @Override
    protected JSONCodec<?> instanceIdentifierCodec(final InstanceIdentifierTypeDefinition type) {
        return iidCodec;
    }

    @Override
    JSONCodecFactory rebaseTo(final EffectiveModelContext newSchemaContext, final CodecCache<JSONCodec<?>> newCache) {
        return new RFC7951JSONCodecFactory(newSchemaContext, newCache);
    }

    @Override
    JSONCodec<?> wrapDecimalCodec(final DecimalStringCodec decimalCodec) {
        return new QuotedJSONCodec<>(decimalCodec);
    }

    @Override
    JSONCodec<?> wrapIntegerCodec(final AbstractIntegerStringCodec<?, ?> integerCodec) {
        return new QuotedJSONCodec<>(integerCodec);
    }
}
