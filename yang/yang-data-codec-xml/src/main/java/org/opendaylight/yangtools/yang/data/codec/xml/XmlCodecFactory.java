/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.annotations.Beta;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.impl.codec.AbstractIntegerStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BinaryStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BitsStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BooleanStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.DecimalStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.EnumStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.StringStringCodec;
import org.opendaylight.yangtools.yang.data.util.codec.AbstractCodecFactory;
import org.opendaylight.yangtools.yang.data.util.codec.SharedCodecCache;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnknownTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

@Beta
@ThreadSafe
public final class XmlCodecFactory extends AbstractCodecFactory<XmlCodec<?>> {

    private XmlCodecFactory(final SchemaContext context) {
        super(context, new SharedCodecCache<>());
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context SchemaContext to which the factory should be bound
     * @return A codec factory instance.
     */
    public static XmlCodecFactory create(final SchemaContext context) {
        return new XmlCodecFactory(context);
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
        return new IdentityrefXmlCodec(getSchemaContext(), module);
    }

    @Override
    protected XmlCodec<?> instanceIdentifierCodec(final InstanceIdentifierTypeDefinition type) {
        return new XmlStringInstanceIdentifierCodec(getSchemaContext(), this);
    }

    @Override
    protected XmlCodec<?> intCodec(final IntegerTypeDefinition type) {
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
    protected XmlCodec<?> uintCodec(final UnsignedIntegerTypeDefinition type) {
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
