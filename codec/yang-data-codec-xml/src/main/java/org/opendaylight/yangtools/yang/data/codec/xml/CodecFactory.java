/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.impl.codec.AbstractIntegerStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BinaryStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BitsStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BooleanStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.DecimalStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.EnumStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.StringStringCodec;
import org.opendaylight.yangtools.yang.data.util.codec.AbstractCodecFactory;
import org.opendaylight.yangtools.yang.data.util.codec.SharedCodecCache;
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
 * A thread-safe factory for instantiating {@link XmlCodec}s.
 */
abstract sealed class CodecFactory extends AbstractCodecFactory<XmlCodec<?>> permits XmlCodecFactory {
    private final MountPointContext mountCtx;

    CodecFactory(final MountPointContext mountCtx) {
        super(mountCtx.getEffectiveModelContext(), new SharedCodecCache<>());
        this.mountCtx = requireNonNull(mountCtx);
    }

    final MountPointContext mountPointContext() {
        return mountCtx;
    }

    @Override
    protected final QuotedXmlCodec<?> binaryCodec(final BinaryTypeDefinition type) {
        return new QuotedXmlCodec<>(BinaryStringCodec.from(type));
    }

    @Override
    protected final BooleanXmlCodec booleanCodec(final BooleanTypeDefinition type) {
        return new BooleanXmlCodec(BooleanStringCodec.from(type));
    }

    @Override
    protected final QuotedXmlCodec<?> bitsCodec(final BitsTypeDefinition type) {
        return new QuotedXmlCodec<>(BitsStringCodec.from(type));
    }

    @Override
    protected final EmptyXmlCodec emptyCodec(final EmptyTypeDefinition type) {
        return EmptyXmlCodec.INSTANCE;
    }

    @Override
    protected final QuotedXmlCodec<?> enumCodec(final EnumTypeDefinition type) {
        return new QuotedXmlCodec<>(EnumStringCodec.from(type));
    }

    @Override
    protected final IdentityrefXmlCodec identityRefCodec(final IdentityrefTypeDefinition type,
            final QNameModule module) {
        return new IdentityrefXmlCodec(getEffectiveModelContext(), module);
    }

    @Override
    protected final XmlStringInstanceIdentifierCodec instanceIdentifierCodec(
            final InstanceIdentifierTypeDefinition type) {
        return new XmlStringInstanceIdentifierCodec(getEffectiveModelContext(), this);
    }

    @Override
    protected final NumberXmlCodec<?> int8Codec(final Int8TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final NumberXmlCodec<?> int16Codec(final Int16TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final NumberXmlCodec<?> int32Codec(final Int32TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final NumberXmlCodec<?> int64Codec(final Int64TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final NumberXmlCodec<?> decimalCodec(final DecimalTypeDefinition type) {
        return new NumberXmlCodec<>(DecimalStringCodec.from(type));
    }

    @Override
    protected final QuotedXmlCodec<?> stringCodec(final StringTypeDefinition type) {
        // FIXME: YANGTOOLS-1523: use QuotedXmlCodec
        return new StringXmlCodec(StringStringCodec.from(type));
    }

    @Override
    protected final NumberXmlCodec<?> uint8Codec(final Uint8TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final NumberXmlCodec<?> uint16Codec(final Uint16TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final NumberXmlCodec<?> uint32Codec(final Uint32TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final NumberXmlCodec<?> uint64Codec(final Uint64TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final UnionXmlCodec<?> unionCodec(final UnionTypeDefinition type, final List<XmlCodec<?>> codecs) {
        return UnionXmlCodec.create(type, codecs);
    }

    @Override
    protected final NullXmlCodec unknownCodec(final UnknownTypeDefinition type) {
        return NullXmlCodec.INSTANCE;
    }
}
