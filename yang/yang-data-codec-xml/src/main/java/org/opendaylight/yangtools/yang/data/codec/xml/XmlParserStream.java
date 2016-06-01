/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.util.AbstractNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.AnyXmlNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafListEntryNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafListNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ListEntryNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ListNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ParserStreamUtils;
import org.opendaylight.yangtools.yang.data.util.RpcAsContainer;
import org.opendaylight.yangtools.yang.data.util.SimpleNodeDataWithSchema;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class provides functionality for parsing an XML source containing YANG-modeled data. It disallows multiple
 * instances of the same element except for leaf-list and list entries. It also expects that the YANG-modeled data in
 * the XML source are wrapped in a root element.
 */
public final class XmlParserStream implements Closeable, Flushable {

    private String rootElement = null;
    private final NormalizedNodeStreamWriter writer;
    private final XmlCodecFactory codecs;
    private final SchemaContext schema;
    private final DataSchemaNode parentNode;

    private XmlParserStream(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext,
                             final DataSchemaNode parentNode) {
        this.schema = Preconditions.checkNotNull(schemaContext);
        this.writer = Preconditions.checkNotNull(writer);
        this.codecs = XmlCodecFactory.create(schemaContext);
        this.parentNode = parentNode;
    }

    public static XmlParserStream create(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext,
            final SchemaNode parentNode ) {
        if (parentNode instanceof RpcDefinition) {
            return new XmlParserStream(writer, schemaContext, new RpcAsContainer((RpcDefinition) parentNode));
        }
        Preconditions.checkArgument(parentNode instanceof DataSchemaNode, "Instance of DataSchemaNode class awaited.");
        return new XmlParserStream(writer, schemaContext, (DataSchemaNode) parentNode);
    }

    public static XmlParserStream create(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext) {
        return new XmlParserStream(writer, schemaContext, schemaContext);
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
            final CompositeNodeDataWithSchema compositeNodeDataWithSchema = new CompositeNodeDataWithSchema(parentNode);
            reader.nextTag();
            rootElement = reader.getLocalName();
            read(reader, compositeNodeDataWithSchema);
            compositeNodeDataWithSchema.write(writer);
        }

        return this;
    }

    private String readAnyXmlValue(final XMLStreamReader in) throws XMLStreamException {
        String result = "";
        String anyXmlElementName = in.getLocalName();

        while (in.hasNext()) {
            int eventType = in.next();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                result += "<" + in.getLocalName() + ">";
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                if (in.getLocalName().equals(anyXmlElementName)) {
                    break;
                }

                result += "</" + in.getLocalName() + ">";
            } else if (eventType == XMLStreamConstants.CHARACTERS) {
                result += in.getText();
            }
        }

        return result;
    }

    private void read(final XMLStreamReader in, final AbstractNodeDataWithSchema parent) throws XMLStreamException,
            URISyntaxException, ParserConfigurationException, SAXException, IOException {
        if (in.hasNext()) {
            if (parent instanceof LeafNodeDataWithSchema || parent instanceof LeafListEntryNodeDataWithSchema) {
                setValue(parent, in.getElementText().trim());
                in.nextTag();
                return;
            } else if (parent instanceof LeafListNodeDataWithSchema || parent instanceof ListNodeDataWithSchema) {
                String parentSchemaName = parent.getSchema().getQName().getLocalName();
                String xmlElementName = in.getLocalName();
                while (xmlElementName.equals(parentSchemaName)) {
                    AbstractNodeDataWithSchema newChild = newEntryNode(parent);
                    read(in, newChild);
                    xmlElementName = in.getLocalName();
                }

                return;
            } else if (parent instanceof AnyXmlNodeDataWithSchema) {
                setValue(parent, readAnyXmlValue(in));
                in.nextTag();
                return;
            }

            switch (in.nextTag()) {
                case XMLStreamConstants.START_ELEMENT:
                    final Set<String> namesakes = new HashSet<>();
                    while (in.hasNext()) {
                        String xmlElementName = in.getLocalName();
                        String xmlElementNamespace = in.getNamespaceURI();

                        if (xmlElementName.equals(rootElement)) {
                            break;
                        }

                        DataSchemaNode parentSchema = parent.getSchema();
                        if (parentSchema instanceof YangModeledAnyXmlSchemaNode) {
                            parentSchema = ((YangModeledAnyXmlSchemaNode) parentSchema).getSchemaOfAnyXmlData();
                        }

                        String parentSchemaName = parentSchema.getQName().getLocalName();
                        if (parentSchemaName.equals(xmlElementName)
                                && in.getEventType() == XMLStreamConstants.END_ELEMENT) {
                            in.nextTag();
                            break;
                        }

                        if (namesakes.contains(xmlElementName)) {
                            int lineNumber = in.getLocation().getLineNumber();
                            int columnNumber = in.getLocation().getColumnNumber();
                            throw new IllegalStateException("Duplicate element \"" + xmlElementName + "\" in XML " +
                                    "input at: line " + lineNumber + " column " + columnNumber);
                        }
                        namesakes.add(xmlElementName);

                        Deque<DataSchemaNode> childDataSchemaNodes = ParserStreamUtils.findSchemaNodeByNameAndNamespace(
                                parentSchema, xmlElementName, new URI(xmlElementNamespace));

                        if (childDataSchemaNodes.isEmpty()) {
                            throw new IllegalStateException("Schema for node with name " + xmlElementName +
                                    " and namespace " + xmlElementNamespace + " doesn't exist.");
                        }

                        AbstractNodeDataWithSchema newChild =
                                ((CompositeNodeDataWithSchema) parent).addChild(childDataSchemaNodes);

                        read(in, newChild);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    in.nextTag();
                    break;
            }
        }
    }

    private void setValue(final AbstractNodeDataWithSchema parent, final String value) throws
            ParserConfigurationException, SAXException, IOException {
        Preconditions.checkArgument(parent instanceof SimpleNodeDataWithSchema, "Node %s is not a simple type",
                parent.getSchema().getQName());
        final SimpleNodeDataWithSchema parentSimpleNode = (SimpleNodeDataWithSchema) parent;
        Preconditions.checkArgument(parentSimpleNode.getValue() == null, "Node '%s' has already set its value to '%s'",
                parentSimpleNode.getSchema().getQName(), parentSimpleNode.getValue());

        final Object translatedValue = translateValueByType(value, parentSimpleNode.getSchema());
        parentSimpleNode.setValue(translatedValue);
    }

    private Object translateValueByType(final String value, final DataSchemaNode node) throws IOException,
            SAXException, ParserConfigurationException {
        if (node instanceof AnyXmlSchemaNode) {
            /*
             *  FIXME: Figure out some YANG extension dispatch, which will
             *  reuse JSON parsing or XML parsing - anyxml is not well-defined in
             * JSON.
             */
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse( new InputSource(new StringReader(value)));
            doc.normalize();
            DOMSource anyXmlValueSource = new DOMSource(doc);

            return anyXmlValueSource;
        }
        return codecs.codecFor(node).deserialize(value);
    }

    private AbstractNodeDataWithSchema newEntryNode(final AbstractNodeDataWithSchema parent) {
        AbstractNodeDataWithSchema newChild;
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
