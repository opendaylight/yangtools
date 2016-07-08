/*
 * Copyright (c) 2016 Intel Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.base.Preconditions;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.util.AbstractStringUnionCodec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class XmlStringUnionCodec extends AbstractStringUnionCodec implements XmlCodec<Object> {
    private static final Logger LOG = LoggerFactory.getLogger(XmlStringUnionCodec.class);

    private final XmlCodecFactory codecFactory;
    private final NamespaceContext namespaceContext;

    XmlStringUnionCodec(final DataSchemaNode schema, final UnionTypeDefinition typeDefinition,
                        final XmlCodecFactory xmlCodecFactory, final NamespaceContext namespaceContext) {
        super(schema, typeDefinition);
        this.codecFactory = Preconditions.checkNotNull(xmlCodecFactory);
        this.namespaceContext = Preconditions.checkNotNull(namespaceContext);
    }

    @Override
    public void serializeToWriter(XMLStreamWriter writer, Object value) throws XMLStreamException {
        writer.writeCharacters(serialize(value));
    }

    @Override
    protected Codec<String, Object> codecFor(final TypeDefinition<?> type) {
        return (Codec<String, Object>) codecFactory.codecFor(schema, type, namespaceContext);
    }
}
