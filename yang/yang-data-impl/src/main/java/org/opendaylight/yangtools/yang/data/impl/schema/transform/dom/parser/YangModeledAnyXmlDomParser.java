/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.YangModeledAnyXmlNodeBaseParser;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class YangModeledAnyXmlDomParser extends YangModeledAnyXmlNodeBaseParser<Element> {

    private final NodeParserDispatcher<Element> dispatcher;

    public YangModeledAnyXmlDomParser(final NodeParserDispatcher<Element> dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected Collection<DataContainerChild<? extends PathArgument, ?>> parseAnyXml(final Element element,
            final YangModeledAnyXmlSchemaNode yangModeledAnyXmlSchemaNode) {
        final QName qName = yangModeledAnyXmlSchemaNode.getQName();
        Preconditions.checkArgument(element.getNodeName().equals(qName.getLocalName()));
        Preconditions.checkArgument(element.getNamespaceURI().equals(qName.getNamespace().toString()));

        final ContainerSchemaNode schema = yangModeledAnyXmlSchemaNode.getSchemaOfAnyXmlData();

        Builder<DataContainerChild<? extends PathArgument, ?>> value = ImmutableList.builder();
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element) {
                Element elementChild = (Element) node;
                // :FIXME get child by local name and namespace, not only by localName ?
                DataSchemaNode schemaChild = schema.getDataChildByName(elementChild.getLocalName());

                Preconditions.checkNotNull(schemaChild,
                        "Unable to find schema for child element %s of yang modeled anyXml data %s.",
                        elementChild.getLocalName(), yangModeledAnyXmlSchemaNode.getQName());
                value.add(dispatcher.dispatchChildElement(schemaChild, ImmutableList.of(elementChild)));
            }
        }

        return value.build();
    }
}
