/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.function.BiFunction;
import org.opendaylight.yangtools.yang.data.util.codec.CodecCache;
import org.opendaylight.yangtools.yang.data.util.codec.LazyCodecCache;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;

/**
 * Factory implementation for creating JSON equivalents of codecs according to Lhotka draft.
 * Each instance of this object is bound to a particular {@link SchemaContext},
 * but can be reused by multiple {@link JSONNormalizedNodeStreamWriter}s.
 */
@Beta
public final class LhotkaJSONCodecFactory extends AbstractJSONCodecFactory {
    private final BiFunction<SchemaContext, LhotkaJSONCodecFactory,
        LhotkaJSONInstanceIdentifierCodec> iidCodecSupplier;
    private final JSONCodec<?> iidCodec;

    LhotkaJSONCodecFactory(SchemaContext context,
                     CodecCache<JSONCodec<?>> cache,
                     BiFunction<SchemaContext, LhotkaJSONCodecFactory,
                         LhotkaJSONInstanceIdentifierCodec> iidCodecSupplier) {
        super(context, cache);
        this.iidCodecSupplier = requireNonNull(iidCodecSupplier);
        iidCodec = verifyNotNull(iidCodecSupplier.apply(context, this));
    }

    @Override
    protected JSONCodec<?> instanceIdentifierCodec(InstanceIdentifierTypeDefinition type) {
        return iidCodec;
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
    LhotkaJSONCodecFactory rebaseTo(final SchemaContext newSchemaContext) {
        return new LhotkaJSONCodecFactory(newSchemaContext, new LazyCodecCache<>(), iidCodecSupplier);
    }
}
