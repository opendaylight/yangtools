/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;

final class InstanceIdentifierXmlCodec implements XmlCodec<YangInstanceIdentifier> {
    private final @NonNull XmlCodecFactory codecFactory;
    private final DataSchemaContextTree dataContextTree;

    InstanceIdentifierXmlCodec(final XmlCodecFactory codecFactory) {
        this.codecFactory = requireNonNull(codecFactory);
        dataContextTree = DataSchemaContextTree.from(codecFactory.getEffectiveModelContext());
    }

    @Override
    public Class<YangInstanceIdentifier> getDataType() {
        return YangInstanceIdentifier.class;
    }

    @Override
    public YangInstanceIdentifier parseValue(final NamespaceContext ctx, final String str) {
        // FIXME: YANGTOOLS-1523: do not trim()
        return new InstanceIdentifierDeserializer(dataContextTree, codecFactory, ctx).deserialize(str.trim());
    }

    @Override
    public void writeValue(final XMLStreamWriter ctx, final YangInstanceIdentifier value) throws XMLStreamException {
        final var serializer = new InstanceIdentifierSerializer(dataContextTree, ctx.getNamespaceContext());

        final String str;
        try {
            str = serializer.serialize(value);
        } catch (IllegalArgumentException e) {
            throw new XMLStreamException("Failed to encode instance-identifier", e);
        }
        for (var e : serializer.getPrefixes()) {
            ctx.writeNamespace(e.getValue(), e.getKey().toString());
        }
        ctx.writeCharacters(str);
    }
}
