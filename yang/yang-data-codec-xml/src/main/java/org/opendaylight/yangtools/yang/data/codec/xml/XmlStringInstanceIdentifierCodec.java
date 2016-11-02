/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.base.Preconditions;
import java.net.URI;
import javax.annotation.Nonnull;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.AbstractModuleStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class XmlStringInstanceIdentifierCodec  extends AbstractModuleStringInstanceIdentifierCodec
        implements XmlCodec<YangInstanceIdentifier> {

    private final DataSchemaContextTree dataContextTree;
    private final XmlCodecFactory codecFactory;
    private final SchemaContext context;
    private final NamespaceContext namespaceContext;

    XmlStringInstanceIdentifierCodec(final SchemaContext context, final XmlCodecFactory xmlCodecFactory,
                                     final NamespaceContext namespaceContext) {
        this.context = Preconditions.checkNotNull(context);
        this.dataContextTree = DataSchemaContextTree.from(context);
        this.codecFactory = Preconditions.checkNotNull(xmlCodecFactory);
        this.namespaceContext = Preconditions.checkNotNull(namespaceContext);
    }

    @Override
    protected Module moduleForPrefix(@Nonnull final String prefix) {
        final String prefixedNS = namespaceContext.getNamespaceURI(prefix);
        return context.findModuleByNamespaceAndRevision(URI.create(prefixedNS), null);
    }

    @Override
    protected String prefixForNamespace(@Nonnull final URI namespace) {
        final Module module = context.findModuleByNamespaceAndRevision(namespace, null);
        return module == null ? null : module.getName();
    }

    @Nonnull
    @Override
    protected DataSchemaContextTree getDataContextTree() {
        return dataContextTree;
    }

    @Override
    protected Object deserializeKeyValue(final DataSchemaNode schemaNode, final String value) {
        Preconditions.checkNotNull(schemaNode, "schemaNode cannot be null");
        Preconditions.checkArgument(schemaNode instanceof LeafSchemaNode, "schemaNode must be of type LeafSchemaNode");
        final XmlCodec<?> objectXmlCodec = codecFactory.codecFor(schemaNode, namespaceContext);
        return objectXmlCodec.deserialize(value);
    }

    /**
     * Serialize YangInstanceIdentifier with specified XMLStreamWriter.
     *
     * @param writer XMLStreamWriter
     * @param value YangInstanceIdentifier
     */
    @Override
    public void serializeToWriter(final XMLStreamWriter writer, final YangInstanceIdentifier value)
            throws XMLStreamException {
        writer.writeCharacters(serialize(value));
    }

}
