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

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.InstanceIdentifierForXmlCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlUtils;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DomUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DomUtils.class);
    private static final XmlCodecProvider DEFAULT_XML_VALUE_CODEC_PROVIDER = new XmlCodecProvider() {
        @Override
        public TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codecFor(final TypeDefinition<?> baseType) {
            return TypeDefinitionAwareCodec.from(baseType);
        }
    };

    private DomUtils() {
    }

    public static XmlCodecProvider defaultValueCodecProvider() {
        return DEFAULT_XML_VALUE_CODEC_PROVIDER;
    }

    public static Object parseXmlValue(final Element xml, final XmlCodecProvider codecProvider, final TypeDefinition<?> type) {
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

    public static void serializeXmlValue(final Element element, final TypeDefinition<? extends TypeDefinition<?>> type, final XmlCodecProvider codecProvider, final Object value) {
        TypeDefinition<?> baseType = XmlUtils.resolveBaseTypeFrom(type);
        if (baseType instanceof IdentityrefTypeDefinition) {
            if (value instanceof QName) {
                QName qname = (QName) value;
                String prefix = "x";
                if (qname.getPrefix() != null && !qname.getPrefix().isEmpty()) {
                    prefix = qname.getPrefix();
                }
                element.setAttribute("xmlns:" + prefix, qname.getNamespace().toString());
                element.setTextContent(prefix + ":" + qname.getLocalName());
            } else {
                LOG.debug("Value of {}:{} is not instance of QName but is {}", baseType.getQName().getNamespace(),
                        baseType.getQName().getLocalName(), value != null ? value.getClass() : "null");
                if (value != null) {
                    element.setTextContent(String.valueOf(value));
                }
            }
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            if (value instanceof InstanceIdentifier) {
                InstanceIdentifierForXmlCodec.serialize((InstanceIdentifier)value, element);
            } else {
                LOG.debug("Value of {}:{} is not instance of InstanceIdentifier but is {}", baseType.getQName()
                        .getNamespace(), //
                        baseType.getQName().getLocalName(), value != null ? value.getClass() : "null");
                if (value != null) {
                    element.setTextContent(String.valueOf(value));
                }
            }
        } else {
            if (value != null) {
                final TypeDefinitionAwareCodec<Object, ?> codec = codecProvider.codecFor(baseType);
                if (codec != null) {
                    try {
                        final String text = codec.serialize(value);
                        element.setTextContent(text);
                    } catch (ClassCastException e) {
                        LOG.error("Provided node value {} did not have type {} required by mapping. Using stream instead.", value, baseType, e);
                        element.setTextContent(String.valueOf(value));
                    }
                } else {
                    LOG.error("Failed to find codec for {}, falling back to using stream", baseType);
                    element.setTextContent(String.valueOf(value));
                }
            }
        }
    }

    public static LinkedListMultimap<QName, Element> mapChildElementsForSingletonNode(final Element node) {
        List<Element> childNodesCollection = Lists.newArrayList();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if(childNodes.item(i) instanceof Element) {
                childNodesCollection.add((Element) childNodes.item(i));
            }
        }

        return mapChildElements(childNodesCollection);
    }

    public static LinkedListMultimap<QName, Element> mapChildElements(final Iterable<Element> childNodesCollection) {
        LinkedListMultimap<QName, Element> mappedChildElements = LinkedListMultimap.create();

        for (Element element : childNodesCollection) {
            QName childQName = XmlDocumentUtils.qNameFromElement(element);
            mappedChildElements.put(childQName, element);
        }

        return mappedChildElements;
    }


    public static Map<QName, String> toAttributes(final NamedNodeMap xmlAttributes) {
        Map<QName, String> attributes = new HashMap<>();

        for (int i = 0; i < xmlAttributes.getLength(); i++) {
            Node node = xmlAttributes.item(i);
            String namespace = node.getNamespaceURI();
            if (namespace == null) {
                namespace = "";
            }

            // Skip namespace definitions
            if(namespace.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                continue;
            }

            QName qName = new QName(URI.create(namespace), node.getLocalName());
            attributes.put(qName, node.getNodeValue());
        }
        return attributes;
    }
}
