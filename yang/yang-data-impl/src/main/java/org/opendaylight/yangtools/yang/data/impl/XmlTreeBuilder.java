/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.MutableCompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;

/**
 * The XML Tree Builder is builder utility class designed to facilitate of
 * loading, reading and parsing XML documents into YANG DOM represented by
 * yang-data-api. <br>
 * The method {@link #buildDataTree(InputStream)} is designed to take and parse
 * XML as {@link InputStream}. The output of the operation SHOULD be root
 * <code>CompositeNode</code> or <code>SimpleElement</code> depends by which
 * element XML begins. The XML header is omitted by XML parser.
 * 
 * @author Lukas Sedlak
 * 
 * @see CompositeNode
 * @see SimpleNode
 * @see Node
 */
public final class XmlTreeBuilder {

    private final static XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private static XMLEventReader eventReader;

    private XmlTreeBuilder() {
    }

    /**
     * The method is designed to take and parse XML as {@link InputStream}. The
     * output of the operation SHOULD be root <code>CompositeNode</code> or
     * <code>SimpleElement</code> depends on element that XML document begins.
     * The XML header is omitted by XML parser.
     * 
     * @param inputStream
     *            XML Input Stream
     * @return root <code>Node</code> element conformant to XML start element in
     *         most cases it will be CompositeNode which contains child Nodes
     * @throws XMLStreamException
     */
    public static Node<?> buildDataTree(final InputStream inputStream) throws XMLStreamException {
        eventReader = xmlInputFactory.createXMLEventReader(inputStream);

        final Stack<Node<?>> processingQueue = new Stack<>();
        Node<?> parentNode = null;
        Node<?> root = null;
        while (eventReader.hasNext()) {
            final XMLEvent event = eventReader.nextEvent();

            if (event.isStartElement()) {
                final StartElement startElement = event.asStartElement();
                if (!processingQueue.isEmpty()) {
                    parentNode = processingQueue.peek();
                }
                CompositeNode compParentNode = null;
                if (parentNode instanceof CompositeNode) {
                    compParentNode = (CompositeNode) parentNode;
                }
                Node<?> newNode = null;
                if (isCompositeNodeEvent(event)) {
                    newNode = resolveCompositeNodeFromStartElement(startElement, compParentNode);
                } else if (isSimpleNodeEvent(event)) {
                    newNode = resolveSimpleNodeFromStartElement(startElement, compParentNode);
                }

                if (newNode != null) {
                    processingQueue.push(newNode);
                    if (compParentNode != null) {
                        compParentNode.getChildren().add(newNode);
                    }
                }
            } else if (event.isEndElement()) {
                root = processingQueue.pop();
            }
        }
        return root;
    }

    /**
     * Checks if the XMLEvent is compliant to SimpleNode tag that contains only
     * characters value. If the SimpleNode is composed only by empty XML tag
     * (i.e. {@code <emptyTag />} or {@code<emptyTag></emptyTag>}) the result
     * will be also <code>true</code>.
     * 
     * @param event
     *            actual XMLEvent that is processed
     * @return <code>true</code> only and only if the XMLEvent Start Element is
     *         Simple element tag and contains character values or is empty XML
     *         tag.
     * @throws XMLStreamException
     * 
     * @see SimpleNode
     */
    private static boolean isSimpleNodeEvent(final XMLEvent event) throws XMLStreamException {
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

    /**
     * Checks if XMLEvent is equivalent to CompositeNode Event. The
     * CompositeNode Event is XML element that conforms to the XML element that
     * contains 1..N XML child elements. (i.e. {@code <compositeNode>
     * 	<simpleNode>data</simpleNode>
     * </compositeNode>})
     * 
     * @param event
     *            actual XMLEvent that is processed
     * @return <code>true</code> only if XML Element contains 1..N child
     *         elements, otherwise returns <code>false</code>
     * @throws XMLStreamException
     * 
     * @see CompositeNode
     */
    private static boolean isCompositeNodeEvent(final XMLEvent event) throws XMLStreamException {
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

    /**
     * Creates and returns <code>SimpleNode</code> instance from actually
     * processed XML Start Element.
     * 
     * @param startElement
     *            actual XML Start Element that is processed
     * @param parent
     *            Parent CompositeNode
     * @return <code>new SimpleNode</code> instance from actually processed XML
     *         Start Element
     * @throws XMLStreamException
     * 
     * @see SimpleNode
     */
    private static SimpleNode<String> resolveSimpleNodeFromStartElement(final StartElement startElement,
            CompositeNode parent) throws XMLStreamException {
        checkArgument(startElement != null, "Start Element cannot be NULL!");
        String data = null;

        if (eventReader.hasNext()) {
            final XMLEvent innerEvent = eventReader.peek();
            if (innerEvent.isCharacters()) {
                final Characters chars = innerEvent.asCharacters();
                if (!chars.isWhiteSpace()) {
                    data = innerEvent.asCharacters().getData();
                }
            } else if (innerEvent.isEndElement()) {
                data = "";
            }
        }
        return NodeFactory.createImmutableSimpleNode(resolveElementQName(startElement), parent, data);
    }

    /**
     * Creates and returns <code>MutableCompositeNode</code> instance from
     * actually processed XML Start Element.
     * 
     * @param startElement
     *            actual XML Start Element that is processed
     * @param parent
     *            Parent CompositeNode
     * @return <code>new MutableCompositeNode</code> instance from actually
     *         processed XML Start Element
     * 
     * @see CompositeNode
     * @see MutableCompositeNode
     */
    private static CompositeNode resolveCompositeNodeFromStartElement(final StartElement startElement,
            CompositeNode parent) {
        checkArgument(startElement != null, "Start Element cannot be NULL!");

        return NodeFactory.createMutableCompositeNode(resolveElementQName(startElement), parent,
                new ArrayList<Node<?>>(), ModifyAction.CREATE, null);
    }

    /**
     * Extract and retrieve XML Element QName to OpenDaylight QName.
     * 
     * @param element
     *            Start Element
     * @return QName instance composed of <code>elements</code> Namespace and
     *         Local Part.
     * 
     * @see QName
     */
    private static QName resolveElementQName(final StartElement element) {
        checkArgument(element != null, "Start Element cannot be NULL!");

        final String nsURI = element.getName().getNamespaceURI();
        final String localName = element.getName().getLocalPart();
        return new QName(URI.create(nsURI), localName);
    }
}
