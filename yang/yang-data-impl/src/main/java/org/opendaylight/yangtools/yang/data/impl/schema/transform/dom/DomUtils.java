/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.InstanceIdentifierForXmlCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlStreamUtils;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlUtils;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public final class DomUtils {

    private DomUtils() {
    }

    public static XmlCodecProvider defaultValueCodecProvider() {
        return XmlUtils.DEFAULT_XML_CODEC_PROVIDER;
    }

    public static Object parseXmlValue(final Element xml, final XmlCodecProvider codecProvider, final TypeDefinition<?> type) {
        final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codec = codecProvider.codecFor(type);

        String text = xml.getTextContent();
        text = text.trim();

        Object value;
        if (codec != null) {
            value = codec.deserialize(text);
        } else {
            value = text;
        }

        return value;
    }

    public static void serializeXmlValue(final Element element, final TypeDefinition<? extends TypeDefinition<?>> type, final XmlCodecProvider codecProvider, final Object value) {
        try {
            final XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(new DOMResult(element));
            XmlStreamUtils.create(codecProvider).writeValue(writer, type, value);
        } catch (final XMLStreamException e) {
            throw new IllegalStateException("XML encoding failed", e);
        }
    }

    public static LinkedListMultimap<QName, Element> mapChildElementsForSingletonNode(final Element node) {
        final List<Element> childNodesCollection = Lists.newArrayList();
        final NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i) instanceof Element) {
                childNodesCollection.add((Element) childNodes.item(i));
            }
        }

        return mapChildElements(childNodesCollection);
    }

    public static LinkedListMultimap<QName, Element> mapChildElements(final Iterable<Element> childNodesCollection) {
        final LinkedListMultimap<QName, Element> mappedChildElements = LinkedListMultimap.create();

        for (final Element element : childNodesCollection) {
            final QName childQName = XmlDocumentUtils.qNameFromElement(element);
            mappedChildElements.put(childQName, element);
        }

        return mappedChildElements;
    }


    public static Map<QName, String> toAttributes(final NamedNodeMap xmlAttributes) {
        final Map<QName, String> attributes = new HashMap<>();

        for (int i = 0; i < xmlAttributes.getLength(); i++) {
            final Node node = xmlAttributes.item(i);
            String namespace = node.getNamespaceURI();
            if (namespace == null) {
                namespace = "";
            }

            // Skip namespace definitions
            if (namespace.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                continue;
            }

            final QName qName = new QName(URI.create(namespace), node.getLocalName());
            attributes.put(qName, node.getNodeValue());
        }
        return attributes;
    }

    public static Object parseXmlValue(final Element xml, final XmlCodecProvider codecProvider, final DataSchemaNode schema, final TypeDefinition<?> type, final SchemaContext schemaCtx) {
        String text = xml.getTextContent();
        text = text.trim();
        final Object value;

        if (type instanceof LeafrefTypeDefinition) {
            final LeafrefTypeDefinition leafrefTypeDefinition = (LeafrefTypeDefinition) type;
            TypeDefinition<?> baseType = SchemaContextUtil.getBaseTypeForLeafRef(leafrefTypeDefinition, schemaCtx, schema);
            value = parseXmlValue(xml, codecProvider, schema, baseType, schemaCtx);
        } else if (type instanceof InstanceIdentifierTypeDefinition) {
            value = InstanceIdentifierForXmlCodec.deserialize(xml, schemaCtx);
        } else if (type instanceof IdentityrefTypeDefinition) {
            value = InstanceIdentifierForXmlCodec.toIdentity(text, xml, schemaCtx);
        } else {
            value = parseXmlValue(xml, codecProvider, type);
        }

        return value;
    }
}
