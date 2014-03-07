/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.model.api.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DomUtils {
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
        Object value;
        if (codecProvider != null) {
            value = codec.deserialize(text);
        } else {
            value = xml.getTextContent();
        }

        return value;
    }

    public static void serializeXmlValue(Element itemEl, TypeDefinition<? extends TypeDefinition<?>> type, XmlCodecProvider codecProvider, Object value) {
        XmlDocumentUtils.writeValueByType(itemEl, type, codecProvider, value);
    }

    public static DataSchemaNode findSchemaForChild(DataNodeContainer schema, QName qname) {
        Set<DataSchemaNode> childNodes = schema.getChildNodes();
        Optional<DataSchemaNode> childSchema = XmlDocumentUtils.findFirstSchema(qname, childNodes);
        Preconditions.checkState(childSchema.isPresent(),
                "Unknown child(ren) node(s) detected, identified by: %s, in: %s", qname, schema);
        return childSchema.get();
    }

    public static DataSchemaNode findSchemaForChild(ChoiceNode schema, QName childPartialQName) {
        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            Optional<DataSchemaNode> childSchema = XmlDocumentUtils.findFirstSchema(childPartialQName,
                    choiceCaseNode.getChildNodes());
            if (childSchema.isPresent()) {
                return childSchema.get();
            }
        }
        throw new IllegalStateException(String.format("Unknown child(ren) node(s) detected, identified by: %s, in: %s",
                childPartialQName, schema));
    }

    public static Multimap<QName, Element> mapChildElements(NodeList childNodes) {
        List<Element> childNodesCollection = Lists.newArrayList();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if(childNodes.item(i) instanceof Element) {
                childNodesCollection.add((Element) childNodes.item(i));
            }
        }

        return mapChildElements(childNodesCollection);
    }

    private static Multimap<QName, Element> mapChildElements(Collection<Element> childNodesCollection) {
        Multimap<QName, Element> mappedChildElements = LinkedHashMultimap.create();

        for (Element element : childNodesCollection) {
            QName childQName = XmlDocumentUtils.qNameFromElement(element);
            mappedChildElements.put(childQName, element);
        }

        return mappedChildElements;
    }
}
