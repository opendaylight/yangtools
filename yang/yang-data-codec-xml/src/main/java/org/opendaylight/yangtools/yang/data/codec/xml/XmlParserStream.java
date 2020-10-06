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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
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
import org.opendaylight.yangtools.odlext.model.api.YangModeledAnyxmlSchemaNode;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContext;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.rfc8528.data.api.YangLibraryConstants;
import org.opendaylight.yangtools.rfc8528.data.api.YangLibraryConstants.ContainerName;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchemaNode;
import org.opendaylight.yangtools.rfc8528.model.api.SchemaMountConstants;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.util.AbstractMountPointDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.AbstractNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.AnyXmlNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.AnydataNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.CompositeNodeDataWithSchema.ChildReusePolicy;
import org.opendaylight.yangtools.yang.data.util.ContainerNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafListEntryNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafListNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.LeafNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ListEntryNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.ListNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.MountPointData;
import org.opendaylight.yangtools.yang.data.util.MultipleEntryDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.OperationAsContainer;
import org.opendaylight.yangtools.yang.data.util.ParserStreamUtils;
import org.opendaylight.yangtools.yang.data.util.SimpleNodeDataWithSchema;
import org.opendaylight.yangtools.yang.data.util.YangModeledAnyXmlNodeDataWithSchema;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
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
 *
 * <p>
 * Due to backwards compatibility reasons, RFC7952 metadata emitted by this parser may include key QNames with empty URI
 * (as exposed via {@link #LEGACY_ATTRIBUTE_NAMESPACE}) as their QNameModule. These indicate an unqualified XML
 * attribute and their value can be assumed to be a String. Furthermore, this extends to qualified attributes, which
 * uses the proper namespace, but will not bind to a proper module revision -- these need to be reconciled with a
 * particular SchemaContext and are expected to either be fully decoded, or contain a String value. Handling of such
 * annotations is at the discretion of the user encountering it: preferred way of handling is to either filter or
 * normalize them to proper QNames/values when encountered. This caveat will be removed in a future version.
 */
@Beta
public final class XmlParserStream implements Closeable, Flushable {
    /**
     * {@link QNameModule} for use with legacy XML attributes.
     * @deprecated The use on this namespace is discouraged and users are strongly encouraged to proper RFC7952 metadata
     *             annotations.
     */
    @Deprecated
    public static final QNameModule LEGACY_ATTRIBUTE_NAMESPACE = QNameModule.create(URI.create("")).intern();

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

    // Cache of nsUri Strings to QNameModules, as resolved in context
    private final Map<String, Optional<QNameModule>> resolvedNamespaces = new HashMap<>();
    // Cache of nsUri Strings to QNameModules, as inferred from document
    private final Map<String, QNameModule> rawNamespaces = new HashMap<>();
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
     * maintain a {@link XmlCodecFactory} to match the current {@link EffectiveModelContext}.
     */
    public static XmlParserStream create(final NormalizedNodeStreamWriter writer,
            final EffectiveModelContext schemaContext, final SchemaNode parentNode) {
        return create(writer, schemaContext, parentNode, true);
    }

    /**
     * Utility method for use when caching {@link XmlCodecFactory} is not feasible. Users with high performance
     * requirements should use {@link #create(NormalizedNodeStreamWriter, XmlCodecFactory, SchemaNode)} instead and
     * maintain a {@link XmlCodecFactory} to match the current {@link EffectiveModelContext}.
     */
    public static XmlParserStream create(final NormalizedNodeStreamWriter writer,
            final EffectiveModelContext schemaContext, final SchemaNode parentNode, final boolean strictParsing) {
        return create(writer, XmlCodecFactory.create(schemaContext), parentNode, strictParsing);
    }

    /**
     * Utility method for use when caching {@link XmlCodecFactory} is not feasible. Users with high performance
     * requirements should use {@link #create(NormalizedNodeStreamWriter, XmlCodecFactory, SchemaNode)} instead and
     * maintain a {@link XmlCodecFactory} to match the current {@link MountPointContext}.
     */
    public static XmlParserStream create(final NormalizedNodeStreamWriter writer, final MountPointContext mountCtx,
            final SchemaNode parentNode) {
        return create(writer, mountCtx, parentNode, true);
    }

    /**
     * Utility method for use when caching {@link XmlCodecFactory} is not feasible. Users with high performance
     * requirements should use {@link #create(NormalizedNodeStreamWriter, XmlCodecFactory, SchemaNode)} instead and
     * maintain a {@link XmlCodecFactory} to match the current {@link MountPointContext}.
     */
    public static XmlParserStream create(final NormalizedNodeStreamWriter writer, final MountPointContext mountCtx,
            final SchemaNode parentNode, final boolean strictParsing) {
        return create(writer, XmlCodecFactory.create(mountCtx), parentNode, strictParsing);
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
            if (parentNode instanceof ContainerLike) {
                nodeDataWithSchema = new ContainerNodeDataWithSchema((ContainerLike) parentNode);
            } else if (parentNode instanceof ListSchemaNode) {
                nodeDataWithSchema = new ListNodeDataWithSchema((ListSchemaNode) parentNode);
            } else if (parentNode instanceof YangModeledAnyxmlSchemaNode) {
                nodeDataWithSchema = new YangModeledAnyXmlNodeDataWithSchema((YangModeledAnyxmlSchemaNode) parentNode);
            } else if (parentNode instanceof AnyxmlSchemaNode) {
                nodeDataWithSchema = new AnyXmlNodeDataWithSchema((AnyxmlSchemaNode) parentNode);
            } else if (parentNode instanceof LeafSchemaNode) {
                nodeDataWithSchema = new LeafNodeDataWithSchema((LeafSchemaNode) parentNode);
            } else if (parentNode instanceof LeafListSchemaNode) {
                nodeDataWithSchema = new LeafListNodeDataWithSchema((LeafListSchemaNode) parentNode);
            } else if (parentNode instanceof AnydataSchemaNode) {
                nodeDataWithSchema = new AnydataNodeDataWithSchema((AnydataSchemaNode) parentNode);
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

    private ImmutableMap<QName, Object> getElementAttributes(final XMLStreamReader in) {
        checkState(in.isStartElement(), "Attributes can be extracted only from START_ELEMENT.");
        final Map<QName, Object> attributes = new LinkedHashMap<>();

        for (int attrIndex = 0; attrIndex < in.getAttributeCount(); attrIndex++) {
            final String attributeNS = in.getAttributeNamespace(attrIndex);

            // Skip namespace definitions
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attributeNS)) {
                continue;
            }

            final String localName = in.getAttributeLocalName(attrIndex);
            final String attrValue = in.getAttributeValue(attrIndex);
            if (Strings.isNullOrEmpty(attributeNS)) {
                StreamWriterFacade.warnLegacyAttribute(localName);
                attributes.put(QName.create(LEGACY_ATTRIBUTE_NAMESPACE, localName), attrValue);
                continue;
            }

            // Cross-relate attribute namespace to the module
            final Optional<QNameModule> optModule = resolveXmlNamespace(attributeNS);
            if (optModule.isPresent()) {
                final QName qname = QName.create(optModule.get(), localName);
                final Optional<AnnotationSchemaNode> optAnnotation = AnnotationSchemaNode.find(
                    codecs.getEffectiveModelContext(), qname);
                if (optAnnotation.isPresent()) {
                    final AnnotationSchemaNode schema = optAnnotation.get();
                    final Object value = codecs.codecFor(schema).parseValue(in.getNamespaceContext(), attrValue);
                    attributes.put(schema.getQName(), value);
                    continue;
                }

                LOG.debug("Annotation for {} not found, using legacy QName", qname);
            }

            attributes.put(QName.create(rawXmlNamespace(attributeNS), localName), attrValue);
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
            throws XMLStreamException {
        if (!in.hasNext()) {
            return;
        }

        if (parent instanceof LeafNodeDataWithSchema || parent instanceof LeafListEntryNodeDataWithSchema) {
            parent.setAttributes(getElementAttributes(in));
            setValue((SimpleNodeDataWithSchema<?>) parent, in.getElementText().trim(), in.getNamespaceContext());
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
            setValue((AnyXmlNodeDataWithSchema) parent, readAnyXmlValue(in), in.getNamespaceContext());
            if (isNextEndDocument(in)) {
                return;
            }

            if (!isAtElement(in)) {
                in.nextTag();
            }

            return;
        }

        if (parent instanceof AnydataNodeDataWithSchema) {
            final AnydataNodeDataWithSchema anydata = (AnydataNodeDataWithSchema) parent;
            anydata.setObjectModel(DOMSourceAnydata.class);
            anydata.setAttributes(getElementAttributes(in));
            setValue(anydata, readAnyXmlValue(in), in.getNamespaceContext());
            if (isNextEndDocument(in)) {
                return;
            }

            if (!isAtElement(in)) {
                in.nextTag();
            }

            return;
        }

        if (parent instanceof YangModeledAnyxmlSchemaNode) {
            parent.setAttributes(getElementAttributes(in));
        }

        switch (in.nextTag()) {
            case XMLStreamConstants.START_ELEMENT:
                // FIXME: 7.0.0: why do we even need this tracker? either document it or remove it.
                //               it looks like it is a crude duplicate finder, which should really be handled via
                //               ChildReusePolicy.REJECT
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

                    if (parentSchema instanceof YangModeledAnyxmlSchemaNode) {
                        parentSchema = ((YangModeledAnyxmlSchemaNode) parentSchema).getSchemaOfAnyXmlData();
                    }

                    final String elementNS = in.getNamespaceURI();
                    final boolean added = namesakes.add(new SimpleImmutableEntry<>(elementNS, xmlElementName));

                    final URI nsUri;
                    try {
                        nsUri = rawXmlNamespace(elementNS).getNamespace();
                    } catch (IllegalArgumentException e) {
                        throw new XMLStreamException("Failed to convert namespace " + xmlElementName, in.getLocation(),
                            e);
                    }

                    final Deque<DataSchemaNode> childDataSchemaNodes =
                            ParserStreamUtils.findSchemaNodeByNameAndNamespace(parentSchema, xmlElementName, nsUri);
                    if (!childDataSchemaNodes.isEmpty()) {
                        final boolean elementList = isElementList(childDataSchemaNodes);
                        if (!added && !elementList) {
                            throw new XMLStreamException(String.format(
                                "Duplicate element \"%s\" in namespace \"%s\" with parent \"%s\" in XML input",
                                xmlElementName, elementNS, parent.getSchema()), in.getLocation());
                        }

                        // We have a match, proceed with it
                        read(in, ((CompositeNodeDataWithSchema<?>) parent).addChild(childDataSchemaNodes,
                            elementList ? ChildReusePolicy.REUSE : ChildReusePolicy.NOOP), rootElement);
                        continue;
                    }

                    if (parent instanceof AbstractMountPointDataWithSchema) {
                        // Parent can potentially hold a mount point, let's see if there is a label present
                        final Optional<MountPointSchemaNode> optMount;
                        if (parentSchema instanceof ContainerSchemaNode) {
                            optMount = MountPointSchemaNode.streamAll((ContainerSchemaNode) parentSchema).findFirst();
                        } else if (parentSchema instanceof ListSchemaNode) {
                            optMount = MountPointSchemaNode.streamAll((ListSchemaNode) parentSchema).findFirst();
                        } else {
                            throw new XMLStreamException("Unhandled mount-aware schema " + parentSchema);
                        }

                        if (optMount.isPresent()) {
                            final MountPointIdentifier mountId = MountPointIdentifier.of(optMount.get().getQName());
                            LOG.debug("Assuming node {} and namespace {} belongs to mount point {}", xmlElementName,
                                nsUri, mountId);

                            final Optional<MountPointContextFactory> optFactory = codecs.mountPointContext()
                                    .findMountPoint(mountId);
                            if (optFactory.isPresent()) {
                                final MountPointData mountData = ((AbstractMountPointDataWithSchema<?>) parent)
                                        .getMountPointData(mountId, optFactory.get());
                                addMountPointChild(mountData, nsUri, xmlElementName,
                                    new DOMSource(readAnyXmlValue(in).getDocumentElement()));
                                continue;
                            }

                            LOG.debug("Mount point {} not attached", mountId);
                        }
                    }

                    // We have not handled the node -- let's decide what to do about that
                    if (strictParsing) {
                        throw new XMLStreamException(String.format(
                            "Schema for node with name %s and namespace %s does not exist at %s", xmlElementName,
                            elementNS, parentSchema.getPath(), in.getLocation()));
                    }

                    LOG.debug("Skipping unknown node ns=\"{}\" localName=\"{}\" at path {}", elementNS, xmlElementName,
                        parentSchema.getPath());
                    skipUnknownNode(in);
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

    // Return true if schema represents a construct which uses multiple sibling elements to represent its content. The
    // siblings MAY be interleaved as per RFC7950.
    private static boolean isElementList(final Deque<DataSchemaNode> childDataSchemaNodes) {
        final DataSchemaNode last = childDataSchemaNodes.getLast();
        return last instanceof ListSchemaNode || last instanceof LeafListSchemaNode;
    }

    private static void addMountPointChild(final MountPointData mount, final URI namespace, final String localName,
            final DOMSource source) {
        final DOMSourceMountPointChild child = new DOMSourceMountPointChild(source);
        if (YangLibraryConstants.MODULE_NAMESPACE.equals(namespace)) {
            final Optional<ContainerName> optName = ContainerName.forLocalName(localName);
            if (optName.isPresent()) {
                mount.setContainer(optName.get(), child);
                return;
            }

            LOG.warn("Encountered unknown element {} from YANG Library namespace", localName);
        } else if (SchemaMountConstants.RFC8528_MODULE.getNamespace().equals(namespace)) {
            mount.setSchemaMounts(child);
            return;
        }

        mount.addChild(child);
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

    private void setValue(final SimpleNodeDataWithSchema<?> parent, final Object value,
            final NamespaceContext nsContext) {
        final DataSchemaNode schema = parent.getSchema();
        final Object prev = parent.getValue();
        checkArgument(prev == null, "Node '%s' has already set its value to '%s'", schema.getQName(), prev);
        parent.setValue(translateValueByType(value, schema, nsContext));
    }

    private Object translateValueByType(final Object value, final DataSchemaNode node,
            final NamespaceContext namespaceCtx) {
        if (node instanceof AnyxmlSchemaNode) {
            checkArgument(value instanceof Document);
            /*
             * FIXME: Figure out some YANG extension dispatch, which will reuse JSON parsing or XML parsing -
             *        anyxml is not well-defined in JSON.
             */
            return new DOMSource(((Document) value).getDocumentElement());
        }
        if (node instanceof AnydataSchemaNode) {
            checkArgument(value instanceof Document);
            return new DOMSourceAnydata(new DOMSource(((Document) value).getDocumentElement()));
        }

        checkArgument(node instanceof TypedDataSchemaNode);
        checkArgument(value instanceof String);
        return codecs.codecFor((TypedDataSchemaNode) node).parseValue(namespaceCtx, (String) value);
    }

    private static AbstractNodeDataWithSchema<?> newEntryNode(final AbstractNodeDataWithSchema<?> parent) {
        verify(parent instanceof MultipleEntryDataWithSchema, "Unexpected parent %s", parent);
        return ((MultipleEntryDataWithSchema<?>) parent).newChildEntry();
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

    private Optional<QNameModule> resolveXmlNamespace(final String xmlNamespace) {
        return resolvedNamespaces.computeIfAbsent(xmlNamespace, nsUri -> {
            final Iterator<? extends Module> it = codecs.getEffectiveModelContext().findModules(URI.create(nsUri))
                    .iterator();
            return it.hasNext() ? Optional.of(it.next().getQNameModule()) : Optional.empty();
        });
    }

    private QNameModule rawXmlNamespace(final String xmlNamespace) {
        return rawNamespaces.computeIfAbsent(xmlNamespace, nsUri -> QNameModule.create(URI.create(nsUri)));
    }
}
