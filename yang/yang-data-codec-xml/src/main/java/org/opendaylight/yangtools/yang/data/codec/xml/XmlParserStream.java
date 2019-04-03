/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXSource;
import org.opendaylight.yangtools.odlext.model.api.YangModeledAnyXmlSchemaNode;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadata;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
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
import org.opendaylight.yangtools.yang.data.util.OperationAsContainer;
import org.opendaylight.yangtools.yang.data.util.ParserStreamUtils;
import org.opendaylight.yangtools.yang.data.util.SimpleNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.YangModeledAnyXmlNodeDataWithSchema;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class provides functionality for parsing an XML source containing YANG-modeled data. It disallows multiple
 * instances of the same element except for leaf-list and list entries. It also expects that the YANG-modeled data in
 * the XML source are wrapped in a root element. This class is NOT thread-safe.
 */
@Beta
public final class XmlParserStream implements Closeable, Flushable {
    private static final Logger LOG = LoggerFactory.getLogger(XmlParserStream.class);
    private static final String XML_STANDARD_VERSION = "1.0";
    private static final String COM_SUN_TRANSFORMER =
            "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";

    private static final TransformerFactory TRANSFORMER_FACTORY;

    static {
        TransformerFactory fa = TransformerFactory.newInstance();
        if (!fa.getFeature(StAXSource.FEATURE)) {
            LOG.warn("Platform-default TransformerFactory {} does not support StAXSource, attempting fallback to {}",
                    fa, COM_SUN_TRANSFORMER);
            fa = TransformerFactory.newInstance(COM_SUN_TRANSFORMER, null);
            if (!fa.getFeature(StAXSource.FEATURE)) {
                throw new TransformerFactoryConfigurationError("No TransformerFactory supporting StAXResult found.");
            }
        }

        TRANSFORMER_FACTORY = fa;
    }

    private final NormalizedNodeStreamWriter writer;
    private final XmlCodecFactory codecs;
    private final DataSchemaNode parentNode;
    private final boolean strictParsing;

    private XmlParserStream(final NormalizedNodeStreamWriter writer, final XmlCodecFactory codecs,
            final DataSchemaNode parentNode, final boolean strictParsing) {
        this.writer = requireNonNull(writer);
        this.codecs = requireNonNull(codecs);
        this.parentNode = parentNode;
        this.strictParsing = strictParsing;
    }

    /**
     * Construct a new {@link XmlParserStream} with strict parsing mode switched on.
     *
     * @param writer Output writer
     * @param codecs Shared codecs
     * @param parentNode Parent root node
     * @return A new stream instance
     */
    public static XmlParserStream create(final NormalizedNodeStreamWriter writer, final XmlCodecFactory codecs,
            final SchemaNode parentNode) {
        return create(writer, codecs, parentNode, true);
    }

    /**
     * Construct a new {@link XmlParserStream}.
     *
     * @param writer Output writer
     * @param codecs Shared codecs
     * @param parentNode Parent root node
     * @param strictParsing parsing mode
     *            if set to true, the parser will throw an exception if it encounters unknown child nodes
     *            (nodes, that are not defined in the provided SchemaContext) in containers and lists
     *            if set to false, the parser will skip unknown child nodes
     * @return A new stream instance
     */
    public static XmlParserStream create(final NormalizedNodeStreamWriter writer, final XmlCodecFactory codecs,
            final SchemaNode parentNode, final boolean strictParsing) {
        final DataSchemaNode parent;
        if (parentNode instanceof DataSchemaNode) {
            parent = (DataSchemaNode) parentNode;
        } else if (parentNode instanceof OperationDefinition) {
            parent = OperationAsContainer.of((OperationDefinition) parentNode);
        } else {
            throw new IllegalArgumentException("Illegal parent node " + parentNode);
        }
        return new XmlParserStream(writer, codecs, parent, strictParsing);
    }

    /**
     * Utility method for use when caching {@link XmlCodecFactory} is not feasible. Users with high performance
     * requirements should use {@link #create(NormalizedNodeStreamWriter, XmlCodecFactory, SchemaNode)} instead and
     * maintain a {@link XmlCodecFactory} to match the current {@link SchemaContext}.
     */
    public static XmlParserStream create(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext,
            final SchemaNode parentNode) {
        return create(writer, schemaContext, parentNode, true);
    }

    /**
     * Utility method for use when caching {@link XmlCodecFactory} is not feasible. Users with high performance
     * requirements should use {@link #create(NormalizedNodeStreamWriter, XmlCodecFactory, SchemaNode)} instead and
     * maintain a {@link XmlCodecFactory} to match the current {@link SchemaContext}.
     */
    public static XmlParserStream create(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext,
            final SchemaNode parentNode, final boolean strictParsing) {
        return create(writer, XmlCodecFactory.create(schemaContext), parentNode, strictParsing);
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
     * @throws SAXException
     *              if an error occurs while parsing the value of an anyxml node
     */
    public XmlParserStream parse(final XMLStreamReader reader) throws XMLStreamException, URISyntaxException,
            IOException, SAXException {
        if (reader.hasNext()) {
            reader.nextTag();
            final AbstractNodeDataWithSchema<?> nodeDataWithSchema;
            if (parentNode instanceof ContainerSchemaNode) {
                nodeDataWithSchema = new ContainerNodeDataWithSchema((ContainerSchemaNode) parentNode);
            } else if (parentNode instanceof ListSchemaNode) {
                nodeDataWithSchema = new ListNodeDataWithSchema((ListSchemaNode) parentNode);
            } else if (parentNode instanceof YangModeledAnyXmlSchemaNode) {
                nodeDataWithSchema = new YangModeledAnyXmlNodeDataWithSchema((YangModeledAnyXmlSchemaNode) parentNode);
            } else if (parentNode instanceof AnyXmlSchemaNode) {
                nodeDataWithSchema = new AnyXmlNodeDataWithSchema((AnyXmlSchemaNode) parentNode);
            } else if (parentNode instanceof LeafSchemaNode) {
                nodeDataWithSchema = new LeafNodeDataWithSchema((LeafSchemaNode) parentNode);
            } else if (parentNode instanceof LeafListSchemaNode) {
                nodeDataWithSchema = new LeafListNodeDataWithSchema((LeafListSchemaNode) parentNode);
            } else {
                throw new IllegalStateException("Unsupported schema node type " + parentNode.getClass() + ".");
            }

            read(reader, nodeDataWithSchema, reader.getLocalName());
            nodeDataWithSchema.write(writer);
        }

        return this;
    }

    /**
     * This method traverses a {@link DOMSource} and emits node events into a NormalizedNodeStreamWriter based on the
     * YANG-modeled data contained in the source.
     *
     * @param src
     *              {@link DOMSource} to be traversed
     * @return
     *              instance of XmlParserStream
     * @throws XMLStreamException
     *              if a well-formedness error or an unexpected processing condition occurs while parsing the XML
     * @throws URISyntaxException
     *              if the namespace URI of an XML element contains a syntax error
     * @throws IOException
     *              if an error occurs while parsing the value of an anyxml node
     * @throws SAXException
     *              if an error occurs while parsing the value of an anyxml node
     */
    @Beta
    public XmlParserStream traverse(final DOMSource src) throws XMLStreamException, URISyntaxException, IOException,
            SAXException {
        return parse(new DOMSourceXMLStreamReader(src));
    }

    private static ImmutableMap<QName, String> getElementAttributes(final XMLStreamReader in) {
        checkState(in.isStartElement(), "Attributes can be extracted only from START_ELEMENT.");
        final Map<QName, String> attributes = new LinkedHashMap<>();

        for (int attrIndex = 0; attrIndex < in.getAttributeCount(); attrIndex++) {
            final String attributeNS = in.getAttributeNamespace(attrIndex);

            // Skip namespace definitions
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attributeNS)) {
                continue;
            }

            final QNameModule namespace;
            if (Strings.isNullOrEmpty(attributeNS)) {
                // FIXME: bind the namespace to a module, if available
                namespace = QNameModule.create(URI.create(attributeNS));
            } else {
                namespace = NormalizedMetadata.LEGACY_ATTRIBUTE_NAMESPACE;
            }

            final QName qName = QName.create(namespace, in.getAttributeLocalName(attrIndex));
            attributes.put(qName, in.getAttributeValue(attrIndex));
        }

        return ImmutableMap.copyOf(attributes);
    }

    private static Document readAnyXmlValue(final XMLStreamReader in) throws XMLStreamException {
        // Underlying reader might return null when asked for version, however when such reader is plugged into
        // Stax -> DOM transformer, it fails with NPE due to null version. Use default xml version in such case.
        final XMLStreamReader inWrapper;
        if (in.getVersion() == null) {
            inWrapper = new StreamReaderDelegate(in) {
                @Override
                public String getVersion() {
                    final String ver = super.getVersion();
                    return ver != null ? ver : XML_STANDARD_VERSION;
                }
            };
        } else {
            inWrapper = in;
        }

        final DOMResult result = new DOMResult();
        try {
            TRANSFORMER_FACTORY.newTransformer().transform(new StAXSource(inWrapper), result);
        } catch (final TransformerException e) {
            throw new XMLStreamException("Unable to read anyxml value", e);
        }
        return (Document) result.getNode();
    }

    private void read(final XMLStreamReader in, final AbstractNodeDataWithSchema<?> parent, final String rootElement)
            throws XMLStreamException, URISyntaxException {
        if (!in.hasNext()) {
            return;
        }

        if (parent instanceof LeafNodeDataWithSchema || parent instanceof LeafListEntryNodeDataWithSchema) {
            parent.setAttributes(getElementAttributes(in));
            setValue(parent, in.getElementText().trim(), in.getNamespaceContext());
            if (isNextEndDocument(in)) {
                return;
            }

            if (!isAtElement(in)) {
                in.nextTag();
            }
            return;
        }

        if (parent instanceof ListEntryNodeDataWithSchema || parent instanceof ContainerNodeDataWithSchema) {
            parent.setAttributes(getElementAttributes(in));
        }

        if (parent instanceof LeafListNodeDataWithSchema || parent instanceof ListNodeDataWithSchema) {
            String xmlElementName = in.getLocalName();
            while (xmlElementName.equals(parent.getSchema().getQName().getLocalName())) {
                read(in, newEntryNode(parent), rootElement);
                if (in.getEventType() == XMLStreamConstants.END_DOCUMENT
                        || in.getEventType() == XMLStreamConstants.END_ELEMENT) {
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

        if (parent instanceof YangModeledAnyXmlSchemaNode) {
            parent.setAttributes(getElementAttributes(in));
        }

        switch (in.nextTag()) {
            case XMLStreamConstants.START_ELEMENT:
                // FIXME: why do we even need this tracker? either document it or remove it
                final Set<Entry<String, String>> namesakes = new HashSet<>();
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

                    final String xmlElementNamespace = in.getNamespaceURI();
                    if (!namesakes.add(new SimpleImmutableEntry<>(xmlElementNamespace, xmlElementName))) {
                        final Location loc = in.getLocation();
                        throw new IllegalStateException(String.format(
                                "Duplicate namespace \"%s\" element \"%s\" in XML input at: line %s column %s",
                                xmlElementNamespace, xmlElementName, loc.getLineNumber(), loc.getColumnNumber()));
                    }

                    final Deque<DataSchemaNode> childDataSchemaNodes =
                            ParserStreamUtils.findSchemaNodeByNameAndNamespace(parentSchema, xmlElementName,
                                    new URI(xmlElementNamespace));

                    if (childDataSchemaNodes.isEmpty()) {
                        checkState(!strictParsing, "Schema for node with name %s and namespace %s does not exist at %s",
                            xmlElementName, xmlElementNamespace, parentSchema.getPath());
                        skipUnknownNode(in);
                        continue;
                    }

                    read(in, ((CompositeNodeDataWithSchema<?>) parent).addChild(childDataSchemaNodes), rootElement);
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
        return !in.hasNext() || in.next() == XMLStreamConstants.END_DOCUMENT;
    }

    private static boolean isAtElement(final XMLStreamReader in) {
        return in.getEventType() == XMLStreamConstants.START_ELEMENT
                || in.getEventType() == XMLStreamConstants.END_ELEMENT;
    }

    private static void skipUnknownNode(final XMLStreamReader in) throws XMLStreamException {
        // in case when the unknown node and at least one of its descendant nodes have the same name
        // we cannot properly reach the end just by checking if the current node is an end element and has the same name
        // as the root unknown element. therefore we ignore the names completely and just track the level of nesting
        int levelOfNesting = 0;
        while (in.hasNext()) {
            // in case there are text characters in an element, we cannot skip them by calling nextTag()
            // therefore we skip them by calling next(), and then proceed to next element
            in.next();
            if (!isAtElement(in)) {
                in.nextTag();
            }
            if (in.isStartElement()) {
                levelOfNesting++;
            }

            if (in.isEndElement()) {
                if (levelOfNesting == 0) {
                    break;
                }

                levelOfNesting--;
            }
        }

        in.nextTag();
    }


    private void setValue(final AbstractNodeDataWithSchema<?> parent, final Object value,
            final NamespaceContext nsContext) {
        checkArgument(parent instanceof SimpleNodeDataWithSchema, "Node %s is not a simple type",
                parent.getSchema().getQName());
        final SimpleNodeDataWithSchema<?> parentSimpleNode = (SimpleNodeDataWithSchema<?>) parent;
        checkArgument(parentSimpleNode.getValue() == null, "Node '%s' has already set its value to '%s'",
                parentSimpleNode.getSchema().getQName(), parentSimpleNode.getValue());

        parentSimpleNode.setValue(translateValueByType(value, parentSimpleNode.getSchema(), nsContext));
    }

    private Object translateValueByType(final Object value, final DataSchemaNode node,
            final NamespaceContext namespaceCtx) {
        if (node instanceof AnyXmlSchemaNode) {

            checkArgument(value instanceof Document);
            /*
             *  FIXME: Figure out some YANG extension dispatch, which will
             *  reuse JSON parsing or XML parsing - anyxml is not well-defined in
             * JSON.
             */
            return new DOMSource(((Document) value).getDocumentElement());
        }

        checkArgument(node instanceof TypedDataSchemaNode);
        checkArgument(value instanceof String);
        return codecs.codecFor((TypedDataSchemaNode) node).parseValue(namespaceCtx, (String) value);
    }

    private static AbstractNodeDataWithSchema<?> newEntryNode(final AbstractNodeDataWithSchema<?> parent) {
        final AbstractNodeDataWithSchema<?> newChild;
        if (parent instanceof ListNodeDataWithSchema) {
            newChild = ListEntryNodeDataWithSchema.forSchema(((ListNodeDataWithSchema) parent).getSchema());
        } else {
            verify(parent instanceof LeafListNodeDataWithSchema, "Unexpected parent %s", parent);
            newChild = new LeafListEntryNodeDataWithSchema(((LeafListNodeDataWithSchema) parent).getSchema());
        }
        ((CompositeNodeDataWithSchema<?>) parent).addChild(newChild);
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
