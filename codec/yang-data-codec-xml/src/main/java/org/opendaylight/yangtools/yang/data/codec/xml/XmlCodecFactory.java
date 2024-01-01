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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
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
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
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
public final class XmlCodecFactory extends AbstractCodecFactory<XmlCodec<?>> {
    private final @NonNull InstanceIdentifierXmlCodec instanceIdentifierCodec;
    private final @NonNull MountPointContext mountCtx;
    private final @Nullable PreferredPrefixes pref;

    private XmlCodecFactory(final MountPointContext mountCtx, final boolean modelPrefixes) {
        super(mountCtx.modelContext(), new SharedCodecCache<>());
        this.mountCtx = requireNonNull(mountCtx);
        pref = modelPrefixes ? new PreferredPrefixes.Shared(modelContext()) : null;
        instanceIdentifierCodec = new InstanceIdentifierXmlCodec(this, pref);
    }

    MountPointContext mountPointContext() {
        return mountCtx;
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context MountPointContext to which the factory should be bound
     * @return A codec factory instance.
     */
    public static XmlCodecFactory create(final MountPointContext context) {
        return create(context, false);
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context MountPointContext to which the factory should be bound
     * @param preferPrefixes prefer prefixes known to {@code context}
     * @return A codec factory instance.
     */
    public static XmlCodecFactory create(final MountPointContext context, final boolean preferPrefixes) {
        return new XmlCodecFactory(context, preferPrefixes);
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context SchemaContext to which the factory should be bound
     * @return A codec factory instance.
     */
    public static XmlCodecFactory create(final EffectiveModelContext context) {
        return create(context, false);
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context SchemaContext to which the factory should be bound
     * @param preferPrefixes prefer prefixes known to {@code context}
     * @return A codec factory instance.
     */
    public static XmlCodecFactory create(final EffectiveModelContext context, final boolean preferPrefixes) {
        return create(MountPointContext.of(requireNonNull(context)), preferPrefixes);
    }

    @Override
    protected XmlCodec<?> binaryCodec(final BinaryTypeDefinition type) {
        return new QuotedXmlCodec<>(BinaryStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> booleanCodec(final BooleanTypeDefinition type) {
        return new BooleanXmlCodec(BooleanStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> bitsCodec(final BitsTypeDefinition type) {
        return new QuotedXmlCodec<>(BitsStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> emptyCodec(final EmptyTypeDefinition type) {
        return EmptyXmlCodec.INSTANCE;
    }

    @Override
    protected XmlCodec<?> enumCodec(final EnumTypeDefinition type) {
        return new QuotedXmlCodec<>(EnumStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> identityRefCodec(final IdentityrefTypeDefinition type, final QNameModule module) {
        return new IdentityrefXmlCodec(modelContext(), module, pref);
    }

    @Override
    protected XmlCodec<YangInstanceIdentifier> instanceIdentifierCodec(final InstanceIdentifierTypeDefinition type) {
        return instanceIdentifierCodec;
    }

    @Override
    public XmlCodec<YangInstanceIdentifier> instanceIdentifierCodec() {
        return instanceIdentifierCodec;
    }

    @Override
    protected XmlCodec<?> int8Codec(final Int8TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> int16Codec(final Int16TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> int32Codec(final Int32TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> int64Codec(final Int64TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> decimalCodec(final DecimalTypeDefinition type) {
        return new NumberXmlCodec<>(DecimalStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> stringCodec(final StringTypeDefinition type) {
        return new QuotedXmlCodec<>(StringStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> uint8Codec(final Uint8TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> uint16Codec(final Uint16TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> uint32Codec(final Uint32TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> uint64Codec(final Uint64TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> unionCodec(final UnionTypeDefinition type, final List<XmlCodec<?>> codecs) {
        return UnionXmlCodec.create(type, codecs);
    }

    @Override
    protected XmlCodec<?> unknownCodec(final UnknownTypeDefinition type) {
        return NullXmlCodec.INSTANCE;
    }
}
