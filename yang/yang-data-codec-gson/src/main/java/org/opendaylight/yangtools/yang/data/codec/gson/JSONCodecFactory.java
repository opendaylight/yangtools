/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.function.BiFunction;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.impl.codec.AbstractIntegerStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BinaryStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BitsStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BooleanStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.DecimalStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.EnumStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.StringStringCodec;
import org.opendaylight.yangtools.yang.data.util.codec.AbstractCodecFactory;
import org.opendaylight.yangtools.yang.data.util.codec.CodecCache;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnknownTypeDefinition;

/**
 * Factory for creating JSON equivalents of codecs. Each instance of this object is bound to
 * a particular {@link SchemaContext}, but can be reused by multiple {@link JSONNormalizedNodeStreamWriter}s.
 */
@Beta
public final class JSONCodecFactory extends AbstractCodecFactory<JSONCodec<?>> {
    private final JSONCodec<?> iidCodec;

    JSONCodecFactory(final SchemaContext context, final CodecCache<JSONCodec<?>> cache,
            final BiFunction<SchemaContext, JSONCodecFactory, JSONInstanceIdentifierCodec> iidCodecSupplier) {
        super(context, cache);
        iidCodec = verifyNotNull(iidCodecSupplier.apply(context, this));
    }

    @Override
    protected JSONCodec<?> binaryCodec(final BinaryTypeDefinition type) {
        return new QuotedJSONCodec<>(BinaryStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> booleanCodec(final BooleanTypeDefinition type) {
        return new BooleanJSONCodec(BooleanStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> bitsCodec(final BitsTypeDefinition type) {
        return new QuotedJSONCodec<>(BitsStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> decimalCodec(final DecimalTypeDefinition type) {
        return new NumberJSONCodec<>(DecimalStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> emptyCodec(final EmptyTypeDefinition type) {
        return EmptyJSONCodec.INSTANCE;
    }

    @Override
    protected JSONCodec<?> enumCodec(final EnumTypeDefinition type) {
        return new QuotedJSONCodec<>(EnumStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> identityRefCodec(final IdentityrefTypeDefinition type, final QNameModule module) {
        return new IdentityrefJSONCodec(getSchemaContext(), module);
    }

    @Override
    protected JSONCodec<?> instanceIdentifierCodec(final InstanceIdentifierTypeDefinition type) {
        return iidCodec;
    }

    @Override
    protected JSONCodec<?> int8Codec(final Int8TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> int16Codec(final Int16TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> int32Codec(final Int32TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> int64Codec(final Int64TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> stringCodec(final StringTypeDefinition type) {
        return new QuotedJSONCodec<>(StringStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> uint8Codec(final Uint8TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> uint16Codec(final Uint16TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> uint32Codec(final Uint32TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> uint64Codec(final Uint64TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected JSONCodec<?> unionCodec(final UnionTypeDefinition type, final List<JSONCodec<?>> codecs) {
        return UnionJSONCodec.create(type, codecs);
    }

    @Override
    protected JSONCodec<?> unknownCodec(final UnknownTypeDefinition type) {
        return NullJSONCodec.INSTANCE;
    }
}
