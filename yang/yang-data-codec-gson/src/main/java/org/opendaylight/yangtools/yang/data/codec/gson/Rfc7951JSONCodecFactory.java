/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.function.BiFunction;
import org.opendaylight.yangtools.yang.data.impl.codec.StringStringCodec;
import org.opendaylight.yangtools.yang.data.util.codec.CodecCache;
import org.opendaylight.yangtools.yang.data.util.codec.LazyCodecCache;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;

/**
 * Factory implementation for creating JSON equivalents of codecs according to RFC 7951.
 * Each instance of this object is bound to a particular {@link SchemaContext},
 * but can be reused by multiple {@link JSONNormalizedNodeStreamWriter}s.
 */
public final class Rfc7951JSONCodecFactory extends AbstractJSONCodecFactory {
    private final BiFunction<SchemaContext, Rfc7951JSONCodecFactory,
        RFC7951JSONInstanceIdentifierCodec> iidCodecSupplier;
    private final JSONCodec<?> iidCodec;

    Rfc7951JSONCodecFactory(SchemaContext context,
                     CodecCache<JSONCodec<?>> cache,
                     BiFunction<SchemaContext, Rfc7951JSONCodecFactory,
                         RFC7951JSONInstanceIdentifierCodec> iidCodecSupplier) {
        super(context, cache);
        this.iidCodecSupplier = requireNonNull(iidCodecSupplier);
        iidCodec = verifyNotNull(iidCodecSupplier.apply(context, this));
    }

    @Override
    protected JSONCodec<?> decimalCodec(final DecimalTypeDefinition type) {
        return new QuotedJSONCodec<>(StringStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> instanceIdentifierCodec(InstanceIdentifierTypeDefinition type) {
        return iidCodec;
    }

    @Override
    protected JSONCodec<?> int64Codec(final Int64TypeDefinition type) {
        return new QuotedJSONCodec<>(StringStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> uint64Codec(final Uint64TypeDefinition type) {
        return new QuotedJSONCodec<>(StringStringCodec.from(type));
    }

    // Returns a one-off factory for the purposes of normalizing an anydata tree.
    //
    // FIXME: 4.0.0: this is really ugly, as we should be able to tell if the new context is the same as ours and
    //               whether our cache is thread-safe -- in which case we should just return this.
    //               The supplier/cache/factory layout needs to be reworked so that this call ends up being equivalent
    //               to JSONCodecFactorySupplier.getShared() in case this factory is not thread safe.
    //
    //               The above is not currently possible, as we cannot reference JSONCodecFactorySupplier from the
    //               factory due to that potentially creating a circular reference.
    @Override
    Rfc7951JSONCodecFactory rebaseTo(final SchemaContext newSchemaContext) {
        return new Rfc7951JSONCodecFactory(newSchemaContext, new LazyCodecCache<>(), iidCodecSupplier);
    }
}
