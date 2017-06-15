/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.util.AbstractNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.AnyXmlNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ContainerNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafListEntryNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafListNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ListEntryNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ListNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ParserStreamUtils;
import org.opendaylight.yangtools.yang.data.util.RpcAsContainer;
import org.opendaylight.yangtools.yang.data.util.SimpleNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.YangModeledAnyXmlNodeDataWithSchema;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class provides functionality for parsing an XML source containing YANG-modeled data. It disallows multiple
 * instances of the same element except for leaf-list and list entries. It also expects that the YANG-modeled data in
 * the XML source are wrapped in a root element.
 */
@Beta
@NotThreadSafe
public final class XmlParserStream implements Closeable, Flushable {
    private final NormalizedNodeStreamWriter writer;
    private final XmlCodecFactory codecs;
    private final DataSchemaNode parentNode;

    private XmlParserStream(final NormalizedNodeStreamWriter writer, final XmlCodecFactory codecs,
            final DataSchemaNode parentNode) {
        this.writer = Preconditions.checkNotNull(writer);
        this.codecs = Preconditions.checkNotNull(codecs);
        this.parentNode = parentNode;
    }

    /**
     * Construct a new {@link XmlParserStream}.
     *
     * @param writer Output write
     * @param codecs Shared codecs
     * @param parentNode Parent root node
     * @return A new stream instance
     */
    public static XmlParserStream create(final NormalizedNodeStreamWriter writer, final XmlCodecFactory codecs,
            final SchemaNode parentNode) {
        if (parentNode instanceof RpcDefinition) {
            return new XmlParserStream(writer, codecs, new RpcAsContainer((RpcDefinition) parentNode));
        }
        Preconditions.checkArgument(parentNode instanceof DataSchemaNode, "Instance of DataSchemaNode class awaited.");
        return new XmlParserStream(writer, codecs, (DataSchemaNode) parentNode);
    }

    /**
     * Construct a new {@link XmlParserStream}.
     *
     * @deprecated Use {@link #create(NormalizedNodeStreamWriter, SchemaContext, SchemaNode)} instead.
     */
    @Deprecated
    public static XmlParserStream create(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext) {
        return create(writer, schemaContext, schemaContext);
    }

    /**
     * Utility method for use when caching {@link XmlCodecFactory} is not feasible. Users with high performance
     * requirements should use {@link #create(NormalizedNodeStreamWriter, XmlCodecFactory, SchemaNode)} instead and
     * maintain a {@link XmlCodecFactory} to match the current {@link SchemaContext}.
     */
    public static XmlParserStream create(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext,
            final SchemaNode parentNode) {
        return create(writer, XmlCodecFactory.create(schemaContext), parentNode);
    }

    /**
     * This method parses the XML source and emits node events into a NormalizedNodeStreamWriter based on the
     * YANG-modeled data contained in the XML source.
     *
     * @param reader
     *              StAX reader which is to used to walk through the XML source
     * @return
     *              instance of XmlParserStream
     * @throws XMLStreamException
     *              if a well-formedness error or an unexpected processing condition occurs while parsing the XML
     * @throws URISyntaxException
     *              if the namespace URI of an XML element contains a syntax error
     * @throws IOException
     *              if an error occurs while parsing the value of an anyxml node
     * @throws ParserConfigurationException
     *              if an error occurs while parsing the value of an anyxml node
     * @throws SAXException
     *              if an error occurs while parsing the value of an anyxml node
     */
    public XmlParserStream parse(final XMLStreamReader reader) throws XMLStreamException, URISyntaxException,
            IOException, ParserConfigurationException, SAXException {
        if (reader.hasNext()) {
            reader.nextTag();
            final AbstractNodeDataWithSchema nodeDataWithSchema;
            if (parentNode instanceof ContainerSchemaNode) {
                nodeDataWithSchema = new ContainerNodeDataWithSchema(parentNode);
            } else if (parentNode instanceof ListSchemaNode) {
                nodeDataWithSchema = new ListNodeDataWithSchema(parentNode);
            } else if (parentNode instanceof YangModeledAnyXmlSchemaNode) {
                nodeDataWithSchema = new YangModeledAnyXmlNodeDataWithSchema((YangModeledAnyXmlSchemaNode) parentNode);
            } else if (parentNode instanceof AnyXmlSchemaNode) {
                nodeDataWithSchema = new AnyXmlNodeDataWithSchema(parentNode);
            } else if (parentNode instanceof LeafSchemaNode) {
                nodeDataWithSchema = new LeafNodeDataWithSchema(parentNode);
            } else {
                throw new UnsupportedOperationException(String.format(
                        "%s cannot represent XML root element. Root element can be represented only by "
                              + "DataSchemaNodes that exist in a single instance in their XML form.",
                        parentNode.getQName()));
            }

            read(reader, nodeDataWithSchema, reader.getLocalName());
            nodeDataWithSchema.write(writer);
        }

        return this;
    }

    private static String readAnyXmlValue(final XMLStreamReader in) throws XMLStreamException {
        final StringBuilder sb = new StringBuilder();
        final String anyXmlElementName = in.getLocalName();
        sb.append('<').append(anyXmlElementName).append(" xmlns=\"").append(in.getNamespaceURI()).append("\">");

        while (in.hasNext()) {
            final int eventType = in.next();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                sb.append('<').append(in.getLocalName()).append('>');
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                sb.append("</").append(in.getLocalName()).append('>');

                if (in.getLocalName().equals(anyXmlElementName)) {
                    break;
                }

            } else if (eventType == XMLStreamConstants.CHARACTERS) {
                sb.append(in.getText());
            }
        }

        return sb.toString();
    }

    private void read(final XMLStreamReader in, final AbstractNodeDataWithSchema parent, final String rootElement)
            throws XMLStreamException, URISyntaxException, ParserConfigurationException, SAXException, IOException {
        if (!in.hasNext()) {
            return;
        }

        if (parent instanceof LeafNodeDataWithSchema || parent instanceof LeafListEntryNodeDataWithSchema) {
            setValue(parent, in.getElementText().trim(), in.getNamespaceContext());
            if (isNextEndDocument(in)) {
                return;
            }

            if (!isAtElement(in)) {
                in.nextTag();
            }
            return;
        }

        if (parent instanceof LeafListNodeDataWithSchema || parent instanceof ListNodeDataWithSchema) {
            String xmlElementName = in.getLocalName();
            while (xmlElementName.equals(parent.getSchema().getQName().getLocalName())) {
                read(in, newEntryNode(parent), rootElement);
                if (in.getEventType() == XMLStreamConstants.END_DOCUMENT) {
                    break;
                }
                xmlElementName = in.getLocalName();
            }

            return;
        }

        if (parent instanceof AnyXmlNodeDataWithSchema) {
            setValue(parent, readAnyXmlValue(in), in.getNamespaceContext());
            if (isNextEndDocument(in)) {
                return;
            }

            if (!isAtElement(in)) {
                in.nextTag();
            }

            return;
        }

        switch (in.nextTag()) {
            case XMLStreamConstants.START_ELEMENT:
                final Set<String> namesakes = new HashSet<>();
                while (in.hasNext()) {
                    final String xmlElementName = in.getLocalName();

                    DataSchemaNode parentSchema = parent.getSchema();

                    final String parentSchemaName = parentSchema.getQName().getLocalName();
                    if (parentSchemaName.equals(xmlElementName)
                            && in.getEventType() == XMLStreamConstants.END_ELEMENT) {
                        if (isNextEndDocument(in)) {
                            break;
                        }

                        if (!isAtElement(in)) {
                            in.nextTag();
                        }
                        break;
                    }

                    if (in.isEndElement() && rootElement.equals(xmlElementName)) {
                        break;
                    }

                    if (parentSchema instanceof YangModeledAnyXmlSchemaNode) {
                        parentSchema = ((YangModeledAnyXmlSchemaNode) parentSchema).getSchemaOfAnyXmlData();
                    }

                    if (!namesakes.add(xmlElementName)) {
                        final Location loc = in.getLocation();
                        throw new IllegalStateException(String.format(
                                "Duplicate element \"%s\" in XML input at: line %s column %s", xmlElementName,
                                loc.getLineNumber(), loc.getColumnNumber()));
                    }

                    final String xmlElementNamespace = in.getNamespaceURI();
                    final Deque<DataSchemaNode> childDataSchemaNodes =
                            ParserStreamUtils.findSchemaNodeByNameAndNamespace(parentSchema, xmlElementName,
                                    new URI(xmlElementNamespace));

                    Preconditions.checkState(!childDataSchemaNodes.isEmpty(),
                            "Schema for node with name %s and namespace %s doesn't exist.",
                            xmlElementName, xmlElementNamespace);

                    read(in, ((CompositeNodeDataWithSchema) parent).addChild(childDataSchemaNodes), rootElement);
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (isNextEndDocument(in)) {
                    break;
                }

                if (!isAtElement(in)) {
                    in.nextTag();
                }
                break;
            default:
                break;
        }
    }

    private static boolean isNextEndDocument(final XMLStreamReader in) throws XMLStreamException {
        return in.next() == XMLStreamConstants.END_DOCUMENT;
    }

    private static boolean isAtElement(final XMLStreamReader in) {
        return in.getEventType() == XMLStreamConstants.START_ELEMENT
                || in.getEventType() == XMLStreamConstants.END_ELEMENT;
    }

    private void setValue(final AbstractNodeDataWithSchema parent, final String value, final NamespaceContext nsContext)
            throws ParserConfigurationException, SAXException, IOException {
        Preconditions.checkArgument(parent instanceof SimpleNodeDataWithSchema, "Node %s is not a simple type",
                parent.getSchema().getQName());
        final SimpleNodeDataWithSchema parentSimpleNode = (SimpleNodeDataWithSchema) parent;
        Preconditions.checkArgument(parentSimpleNode.getValue() == null, "Node '%s' has already set its value to '%s'",
                parentSimpleNode.getSchema().getQName(), parentSimpleNode.getValue());

        parentSimpleNode.setValue(translateValueByType(value, parentSimpleNode.getSchema(), nsContext));
    }

    private Object translateValueByType(final String value, final DataSchemaNode node,
            final NamespaceContext namespaceCtx) throws IOException, SAXException, ParserConfigurationException {
        if (node instanceof AnyXmlSchemaNode) {
            /*
             *  FIXME: Figure out some YANG extension dispatch, which will
             *  reuse JSON parsing or XML parsing - anyxml is not well-defined in
             * JSON.
             */
            final Document doc = UntrustedXML.newDocumentBuilder().parse(new InputSource(new StringReader(value)));
            doc.normalize();

            return new DOMSource(doc.getDocumentElement());
        }

        Preconditions.checkArgument(node instanceof TypedSchemaNode);
        return codecs.codecFor((TypedSchemaNode) node).parseValue(namespaceCtx, value);
    }

    private static AbstractNodeDataWithSchema newEntryNode(final AbstractNodeDataWithSchema parent) {
        final AbstractNodeDataWithSchema newChild;
        if (parent instanceof ListNodeDataWithSchema) {
            newChild = new ListEntryNodeDataWithSchema(parent.getSchema());
        } else {
            newChild = new LeafListEntryNodeDataWithSchema(parent.getSchema());
        }
        ((CompositeNodeDataWithSchema) parent).addChild(newChild);
        return newChild;
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }
}
