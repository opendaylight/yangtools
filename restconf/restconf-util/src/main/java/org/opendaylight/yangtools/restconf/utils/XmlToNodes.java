/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.utils;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URI;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.impl.NodeUtils;
import org.opendaylight.yangtools.yang.data.impl.XmlTreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import static com.google.common.base.Preconditions.checkArgument;

public class XmlToNodes {


    private static final Logger logger = LoggerFactory.getLogger(XmlToNodes.class.toString());
    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private static XMLEventReader eventReader;




    private boolean isSimpleNodeEvent(final XMLEvent event) throws XMLStreamException {
        checkArgument(event != null, "XML Event cannot be NULL!");
        if (event.isStartElement()) {
            if (eventReader.hasNext()) {
                final XMLEvent innerEvent;
                innerEvent = eventReader.peek();
                if (innerEvent.isCharacters()) {
                    final Characters chars = innerEvent.asCharacters();
                    if (!chars.isWhiteSpace()) {
                        return true;
                    }
                } else if (innerEvent.isEndElement()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCompositeNodeEvent(final XMLEvent event) throws XMLStreamException {
        checkArgument(event != null, "XML Event cannot be NULL!");
        if (event.isStartElement()) {
            if (eventReader.hasNext()) {
                XMLEvent innerEvent;
                innerEvent = eventReader.peek();
                if (innerEvent.isCharacters()) {
                    Characters chars = innerEvent.asCharacters();
                    if (chars.isWhiteSpace()) {
                        eventReader.nextEvent();
                        innerEvent = eventReader.peek();
                    }
                }
                if (innerEvent.isStartElement()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getValueOf(StartElement startElement) throws XMLStreamException {
        String data = null;
        if (eventReader.hasNext()) {
            final XMLEvent innerEvent = eventReader.peek();
            if (innerEvent.isCharacters()) {
                final Characters chars = innerEvent.asCharacters();
                if (!chars.isWhiteSpace()) {
                    data = innerEvent.asCharacters().getData();
                }
            } else if (innerEvent.isEndElement()) {
                if (startElement.getLocation().getCharacterOffset() == innerEvent.getLocation().getCharacterOffset()) {
                    data = null;
                } else {
                    data = "";
                }
            }
        }
        return data;
    }

    private String getLocalNameFor(StartElement startElement) {
        return startElement.getName().getLocalPart();
    }

    private URI getNamespaceFor(StartElement startElement) {
        String namespaceURI = startElement.getName().getNamespaceURI();
        return namespaceURI.isEmpty() ? null : URI.create(namespaceURI);
    }
    public static CompositeNode xmlToCompositeNode(String xml){
        try {
            eventReader = xmlInputFactory.createXMLEventReader(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        } catch (Exception e){
            logger.trace("Error creating input stream {}",e);
            throw new IllegalStateException("Can't instantiate XMLEventReader for provided xml.");
        }
        if (xml==null || xml.length()==0) return null;

        Node<?> dataTree;
        try {
            dataTree = XmlTreeBuilder.buildDataTree(new ByteArrayInputStream(xml.getBytes()));
        } catch (XMLStreamException e) {
            logger.error("Error during building data tree from XML", e);
            return null;
        }
        if (dataTree == null) {
            logger.error("data tree is null");
            return null;
        }
        if (dataTree instanceof SimpleNode) {
            logger.error("RPC XML was resolved as SimpleNode");
            return null;
        }
        return (CompositeNode) dataTree;
    }

    public static String compositeNodeToXml(CompositeNode cNode){
        if (cNode == null) return "";

        Document domTree = NodeUtils.buildShadowDomTree(cNode);
        StringWriter writer = new StringWriter();
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(domTree), new StreamResult(writer));
        } catch (TransformerException e) {
            logger.error("Error during translation of Document to OutputStream", e);
        }
        return writer.toString();
    }

}
