/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;

public class DomUtils {

    private DomUtils() {
    }

    private static final XmlCodecProvider DEFAULT_XML_VALUE_CODEC_PROVIDER = new XmlCodecProvider() {

        @Override
        public TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codecFor(TypeDefinition<?> baseType) {
            return TypeDefinitionAwareCodec.from(baseType);
        }
    };

    public static XmlCodecProvider defaultValueCodecProvider() {
        return DEFAULT_XML_VALUE_CODEC_PROVIDER;
    }

    public static Object parseXmlValue(Element xml, XmlCodecProvider codecProvider, TypeDefinition<?> type) {
        TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codec = codecProvider.codecFor(type);

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

    public static void serializeXmlValue(Element itemEl, TypeDefinition<? extends TypeDefinition<?>> type, XmlCodecProvider codecProvider, Object value) {
        XmlDocumentUtils.writeValueByType(itemEl, type, codecProvider, value);
    }

    public static LinkedListMultimap<QName, Element> mapChildElementsForSingletonNode(Element node) {
        List<Element> childNodesCollection = Lists.newArrayList();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if(childNodes.item(i) instanceof Element) {
                childNodesCollection.add((Element) childNodes.item(i));
            }
        }

        return mapChildElements(childNodesCollection);
    }

    public static LinkedListMultimap<QName, Element> mapChildElements(Collection<Element> childNodesCollection) {
        LinkedListMultimap<QName, Element> mappedChildElements = LinkedListMultimap.create();

        for (Element element : childNodesCollection) {
            QName childQName = XmlDocumentUtils.qNameFromElement(element);
            mappedChildElements.put(childQName, element);
        }

        return mappedChildElements;
    }


    public static Map<QName, String> toAttributes(NamedNodeMap xmlAttributes) {
        Map<QName, String> attributes = new HashMap<>();

        for (int i = 0; i < xmlAttributes.getLength(); i++) {
            Node node = xmlAttributes.item(i);
            String namespace = node.getNamespaceURI();
            if (namespace == null) {
                namespace = "";
            }

            // SKip namespace definitions
            if(namespace.equals(NamespaceContext.XMLNS_URI)) {
                continue;
            }

            QName qName = new QName(URI.create(namespace), node.getLocalName());
            attributes.put(qName, node.getNodeValue());
        }
        return attributes;
    }
}
