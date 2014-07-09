/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.activation.UnsupportedDataTypeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.impl.ImmutableCompositeNode;
import org.opendaylight.yangtools.yang.data.impl.SimpleNodeTOImpl;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.util.CompositeNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XmlDocumentUtils {

    private static class ElementWithSchemaContext {
        Element element;
        SchemaContext schemaContext;

        ElementWithSchemaContext(final Element element,final SchemaContext schemaContext) {
            this.schemaContext = schemaContext;
            this.element = element;
        }

        Element getElement() {
            return element;
        }

        SchemaContext getSchemaContext() {
            return schemaContext;
        }
    }

    public static final QName OPERATION_ATTRIBUTE_QNAME = QName.create(URI.create("urn:ietf:params:xml:ns:netconf:base:1.0"), null, "operation");
    private static final Logger logger = LoggerFactory.getLogger(XmlDocumentUtils.class);
    private static final XMLOutputFactory FACTORY = XMLOutputFactory.newFactory();
    private static final XmlCodecProvider DEFAULT_XML_VALUE_CODEC_PROVIDER = new XmlCodecProvider() {

        @Override
        public TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codecFor(final TypeDefinition<?> baseType) {
            return TypeDefinitionAwareCodec.from(baseType);
        }
    };

    /**
     * Converts Data DOM structure to XML Document for specified XML Codec Provider and corresponding
     * Data Node Container schema. The CompositeNode data parameter enters as root of Data DOM tree and will
     * be transformed to root in XML Document. Each element of Data DOM tree is compared against specified Data
     * Node Container Schema and transformed accordingly.
     *
     * @param data Data DOM root element
     * @param schema Data Node Container Schema
     * @param codecProvider XML Codec Provider
     * @return new instance of XML Document
     * @throws UnsupportedDataTypeException
     */
    public static Document toDocument(final CompositeNode data, final DataNodeContainer schema, final XmlCodecProvider codecProvider)
            throws UnsupportedDataTypeException {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(schema);

        if (!(schema instanceof ContainerSchemaNode || schema instanceof ListSchemaNode)) {
            throw new UnsupportedDataTypeException("Schema can be ContainerSchemaNode or ListSchemaNode. Other types are not supported yet.");
        }

        final DOMResult result = new DOMResult();
        try {
            final XMLStreamWriter writer = FACTORY.createXMLStreamWriter(result);
            XmlStreamUtils.writeDataDocument(writer, data, (SchemaNode)schema, codecProvider);
            writer.close();
            return (Document)result.getNode();
        } catch (XMLStreamException e) {
            logger.error("Failed to serialize data {}", data, e);
            return null;
        }
    }

    public static Document getDocument() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder bob = dbf.newDocumentBuilder();
            doc = bob.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return doc;
    }

    /**
     * Converts Data DOM structure to XML Document for specified XML Codec Provider. The CompositeNode
     * data parameter enters as root of Data DOM tree and will be transformed to root in XML Document. The child
     * nodes of Data Tree are transformed accordingly.
     *
     * @param data Data DOM root element
     * @param codecProvider XML Codec Provider
     * @return new instance of XML Document
     * @throws UnsupportedDataTypeException
     */
    public static Document toDocument(final CompositeNode data, final XmlCodecProvider codecProvider) {
        final DOMResult result = new DOMResult();
        try {
            final XMLStreamWriter writer = FACTORY.createXMLStreamWriter(result);
            XmlStreamUtils.writeDataDocument(writer, data, codecProvider);
            writer.close();
            return (Document)result.getNode();
        } catch (XMLStreamException e) {
            logger.error("Failed to serialize data {}", data, e);
            return null;
        }
    }

    public static Element createElementFor(final Document doc, final Node<?> data) {
        QName dataType = data.getNodeType();
        Element ret;
        if (dataType.getNamespace() != null) {
            ret = doc.createElementNS(dataType.getNamespace().toString(), dataType.getLocalName());
        } else {
            ret = doc.createElementNS(null, dataType.getLocalName());
        }
        if (data instanceof AttributesContainer && ((AttributesContainer) data).getAttributes() != null) {
            for (Entry<QName, String> attribute : ((AttributesContainer) data).getAttributes().entrySet()) {
                ret.setAttributeNS(attribute.getKey().getNamespace().toString(), attribute.getKey().getLocalName(),
                        attribute.getValue());
            }

        }
        return ret;
    }

    public static void writeValueByType(final Element element, final SimpleNode<?> node, final TypeDefinition<?> type,
            final DataSchemaNode schema, final XmlCodecProvider codecProvider) {

        Object nodeValue = node.getValue();

        writeValueByType(element, type, codecProvider, nodeValue);
    }

    public static void writeValueByType(final Element element, final TypeDefinition<?> type, final XmlCodecProvider codecProvider, final Object nodeValue) {
        TypeDefinition<?> baseType = resolveBaseTypeFrom(type);
        if (baseType instanceof IdentityrefTypeDefinition) {
            if (nodeValue instanceof QName) {
                QName value = (QName) nodeValue;
                String prefix = "x";
                if (value.getPrefix() != null && !value.getPrefix().isEmpty()) {
                    prefix = value.getPrefix();
                }
                element.setAttribute("xmlns:" + prefix, value.getNamespace().toString());
                element.setTextContent(prefix + ":" + value.getLocalName());
            } else {
                Object value = nodeValue;
                logger.debug("Value of {}:{} is not instance of QName but is {}", baseType.getQName().getNamespace(),
                        baseType.getQName().getLocalName(), value != null ? value.getClass() : "null");
                if (value != null) {
                    element.setTextContent(String.valueOf(value));
                }
            }
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            if (nodeValue instanceof InstanceIdentifier) {
                InstanceIdentifierForXmlCodec.serialize((InstanceIdentifier)nodeValue,element);
            } else {
                Object value = nodeValue;
                logger.debug("Value of {}:{} is not instance of InstanceIdentifier but is {}", baseType.getQName()
                        .getNamespace(), //
                        baseType.getQName().getLocalName(), value != null ? value.getClass() : "null");
                if (value != null) {
                    element.setTextContent(String.valueOf(value));
                }
            }
        } else {
            if (nodeValue != null) {
                final TypeDefinitionAwareCodec<Object, ?> codec = codecProvider.codecFor(baseType);
                if (codec != null) {
                    try {
                        final String text = codec.serialize(nodeValue);
                        element.setTextContent(text);
                    } catch (ClassCastException e) {
                        logger.error("Provided node value {} did not have type {} required by mapping. Using stream instead.", nodeValue, baseType, e);
                        element.setTextContent(String.valueOf(nodeValue));
                    }
                } else {
                    logger.error("Failed to find codec for {}, falling back to using stream", baseType);
                    element.setTextContent(String.valueOf(nodeValue));
                }
            }
        }
    }

    public final static TypeDefinition<?> resolveBaseTypeFrom(final TypeDefinition<?> type) {
        TypeDefinition<?> superType = type;
        while (superType.getBaseType() != null) {
            superType = superType.getBaseType();
        }
        return superType;
    }

    public static Node<?> toDomNode(final Element xmlElement, final Optional<DataSchemaNode> schema,
            final Optional<XmlCodecProvider> codecProvider) {
        if (schema.isPresent()) {
            return toNodeWithSchema(xmlElement, schema.get(), codecProvider.or(DEFAULT_XML_VALUE_CODEC_PROVIDER));
        }
        return toDomNode(xmlElement);
    }

    public static CompositeNode fromElement(final Element xmlElement) {
        CompositeNodeBuilder<ImmutableCompositeNode> node = ImmutableCompositeNode.builder();
        node.setQName(qNameFromElement(xmlElement));

        return node.toInstance();
    }

    public static QName qNameFromElement(final Element xmlElement) {
        String namespace = xmlElement.getNamespaceURI();
        String localName = xmlElement.getLocalName();
        return QName.create(namespace != null ? URI.create(namespace) : null, null, localName);
    }

    private static Node<?> toNodeWithSchema(final Element xmlElement, final DataSchemaNode schema, final XmlCodecProvider codecProvider,final SchemaContext schemaCtx) {
        checkQName(xmlElement, schema.getQName());
        if (schema instanceof DataNodeContainer) {
            return toCompositeNodeWithSchema(xmlElement, schema.getQName(), (DataNodeContainer) schema, codecProvider,schemaCtx);
        } else if (schema instanceof LeafSchemaNode) {
            return toSimpleNodeWithType(xmlElement, (LeafSchemaNode) schema, codecProvider,schemaCtx);
        } else if (schema instanceof LeafListSchemaNode) {
            return toSimpleNodeWithType(xmlElement, (LeafListSchemaNode) schema, codecProvider,schemaCtx);
        }
        return null;
    }

    private static Node<?> toNodeWithSchema(final Element xmlElement, final DataSchemaNode schema, final XmlCodecProvider codecProvider) {
        return toNodeWithSchema(xmlElement, schema, codecProvider, null);
    }

    protected static Node<?> toSimpleNodeWithType(final Element xmlElement, final LeafSchemaNode schema,
            final XmlCodecProvider codecProvider,final SchemaContext schemaCtx) {
        TypeDefinitionAwareCodec<? extends Object, ? extends TypeDefinition<?>> codec = codecProvider.codecFor(schema.getType());
        String text = xmlElement.getTextContent();
        Object value = null;
        if (codec != null) {
            value = codec.deserialize(text);
        }

        if (schema.getType() instanceof org.opendaylight.yangtools.yang.model.util.InstanceIdentifier) {
            value = InstanceIdentifierForXmlCodec.deserialize(xmlElement,schemaCtx);
        } else if(schema.getType() instanceof IdentityrefTypeDefinition){
            value = InstanceIdentifierForXmlCodec.toIdentity(xmlElement.getTextContent(), xmlElement, schemaCtx);
        }

        if (value == null) {
            value = xmlElement.getTextContent();
        }

        Optional<ModifyAction> modifyAction = getModifyOperationFromAttributes(xmlElement);
        return new SimpleNodeTOImpl<>(schema.getQName(), null, value, modifyAction.orNull());
    }

    private static Node<?> toSimpleNodeWithType(final Element xmlElement, final LeafListSchemaNode schema,
            final XmlCodecProvider codecProvider,final SchemaContext schemaCtx) {
        TypeDefinitionAwareCodec<? extends Object, ? extends TypeDefinition<?>> codec = codecProvider.codecFor(schema.getType());
        String text = xmlElement.getTextContent();
        Object value = null;
        if (codec != null) {
            value = codec.deserialize(text);
        }
        if (schema.getType() instanceof org.opendaylight.yangtools.yang.model.util.InstanceIdentifier) {
            value = InstanceIdentifierForXmlCodec.deserialize(xmlElement,schemaCtx);
        }
        if (value == null) {
            value = xmlElement.getTextContent();
        }

        Optional<ModifyAction> modifyAction = getModifyOperationFromAttributes(xmlElement);
        return new SimpleNodeTOImpl<>(schema.getQName(), null, value, modifyAction.orNull());
    }

    private static Node<?> toCompositeNodeWithSchema(final Element xmlElement, final QName qName, final DataNodeContainer schema,
            final XmlCodecProvider codecProvider,final SchemaContext schemaCtx) {
        List<Node<?>> values = toDomNodes(xmlElement, Optional.fromNullable(schema.getChildNodes()),schemaCtx);
        Optional<ModifyAction> modifyAction = getModifyOperationFromAttributes(xmlElement);
        return ImmutableCompositeNode.create(qName, values, modifyAction.orNull());
    }

    public static Optional<ModifyAction> getModifyOperationFromAttributes(final Element xmlElement) {
        Attr attributeNodeNS = xmlElement.getAttributeNodeNS(OPERATION_ATTRIBUTE_QNAME.getNamespace().toString(), OPERATION_ATTRIBUTE_QNAME.getLocalName());
        if(attributeNodeNS == null) {
			return Optional.absent();
		}

        ModifyAction action = ModifyAction.fromXmlValue(attributeNodeNS.getValue());
        Preconditions.checkArgument(action.isOnElementPermitted(), "Unexpected operation %s on %s", action, xmlElement);

        return Optional.of(action);
    }

    private static void checkQName(final Element xmlElement, final QName qName) {
        checkState(Objects.equal(xmlElement.getNamespaceURI(), qName.getNamespace().toString()));
        checkState(qName.getLocalName().equals(xmlElement.getLocalName()));
    }

    public static final Optional<DataSchemaNode> findFirstSchema(final QName qname, final Set<DataSchemaNode> dataSchemaNode) {
        if (dataSchemaNode != null && !dataSchemaNode.isEmpty() && qname != null) {
            for (DataSchemaNode dsn : dataSchemaNode) {
                if (qname.isEqualWithoutRevision(dsn.getQName())) {
                    return Optional.<DataSchemaNode> of(dsn);
                } else if (dsn instanceof ChoiceNode) {
                    for (ChoiceCaseNode choiceCase : ((ChoiceNode) dsn).getCases()) {
                        Optional<DataSchemaNode> foundDsn = findFirstSchema(qname, choiceCase.getChildNodes());
                        if (foundDsn != null && foundDsn.isPresent()) {
                            return foundDsn;
                        }
                    }
                }
            }
        }
        return Optional.absent();
    }

    public static Node<?> toDomNode(final Document doc) {
        return toDomNode(doc.getDocumentElement());
    }

    private static Node<?> toDomNode(final Element element) {
        QName qname = qNameFromElement(element);

        ImmutableList.Builder<Node<?>> values = ImmutableList.<Node<?>> builder();
        NodeList nodes = element.getChildNodes();
        boolean isSimpleObject = true;
        String value = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node child = nodes.item(i);
            if (child instanceof Element) {
                isSimpleObject = false;
                values.add(toDomNode((Element) child));
            }
            if (isSimpleObject && child instanceof org.w3c.dom.Text) {
                value = element.getTextContent();
                if (!Strings.isNullOrEmpty(value)) {
                    isSimpleObject = true;
                }
            }
        }
        if (isSimpleObject) {
            return new SimpleNodeTOImpl<>(qname, null, value);
        }
        return ImmutableCompositeNode.create(qname, values.build());
    }

    public static List<Node<?>> toDomNodes(final Element element, final Optional<Set<DataSchemaNode>> context,final SchemaContext schemaCtx) {
        return forEachChild(element.getChildNodes(),schemaCtx, new Function<ElementWithSchemaContext, Optional<Node<?>>>() {

            @Override
            public Optional<Node<?>> apply(final ElementWithSchemaContext input) {
                if (context.isPresent()) {
                    QName partialQName = qNameFromElement(input.getElement());
                    Optional<DataSchemaNode> schemaNode = findFirstSchema(partialQName, context.get());
                    if (schemaNode.isPresent()) {
                        return Optional.<Node<?>> fromNullable(//
                                toNodeWithSchema(input.getElement(), schemaNode.get(), DEFAULT_XML_VALUE_CODEC_PROVIDER,input.getSchemaContext()));
                    }
                }
                return Optional.<Node<?>> fromNullable(toDomNode(input.getElement()));
            }

        });

    }

    public static List<Node<?>> toDomNodes(final Element element, final Optional<Set<DataSchemaNode>> context) {
        return toDomNodes(element,context,null);
    }

    /**
     * Converts XML Document containing notification data from Netconf device to
     * Data DOM Nodes. <br>
     * By specification defined in <a
     * href="http://tools.ietf.org/search/rfc6020#section-7.14">RFC 6020</a>
     * there are xml elements containing notifications metadata, like eventTime
     * or root notification element which specifies namespace for which is
     * notification defined in yang model. Those elements MUST be stripped off
     * notifications body. This method returns pure notification body which
     * begins in element which is equal to notifications name defined in
     * corresponding yang model. Rest of notification metadata are obfuscated,
     * thus Data DOM contains only pure notification body.
     *
     * @param document
     *            XML Document containing notification body
     * @param notifications
     *            Notifications Definition Schema
     * @return Data DOM Nodes containing xml notification body definition or
     *         <code>null</code> if there is no NotificationDefinition with
     *         Element with equal notification QName defined in XML Document.
     */
    public static CompositeNode notificationToDomNodes(final Document document,
            final Optional<Set<NotificationDefinition>> notifications, final SchemaContext schemaCtx) {
        if (notifications.isPresent() && (document != null) && (document.getDocumentElement() != null)) {
            final NodeList originChildNodes = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < originChildNodes.getLength(); i++) {
                org.w3c.dom.Node child = originChildNodes.item(i);
                if (child instanceof Element) {
                    final Element childElement = (Element) child;
                    final QName partialQName = qNameFromElement(childElement);
                    final Optional<NotificationDefinition> notificationDef = findNotification(partialQName,
                            notifications.get());
                    if (notificationDef.isPresent()) {
                        final Set<DataSchemaNode> dataNodes = notificationDef.get().getChildNodes();
                        final List<Node<?>> domNodes = toDomNodes(childElement,
                                Optional.<Set<DataSchemaNode>> fromNullable(dataNodes),schemaCtx);
                        return ImmutableCompositeNode.create(notificationDef.get().getQName(), domNodes);
                    }
                }
            }
        }
        return null;
    }

    public static CompositeNode notificationToDomNodes(final Document document,
            final Optional<Set<NotificationDefinition>> notifications) {
        return notificationToDomNodes(document, notifications,null);
    }

    private static Optional<NotificationDefinition> findNotification(final QName notifName,
            final Set<NotificationDefinition> notifications) {
        if ((notifName != null) && (notifications != null)) {
            for (final NotificationDefinition notification : notifications) {
                if ((notification != null) && notifName.isEqualWithoutRevision(notification.getQName())) {
                    return Optional.<NotificationDefinition>fromNullable(notification);
                }
            }
        }
        return Optional.<NotificationDefinition>absent();
    }

    private static final <T> List<T> forEachChild(final NodeList nodes, final SchemaContext schemaContext, final Function<ElementWithSchemaContext, Optional<T>> forBody) {
        final int l = nodes.getLength();
        if (l == 0) {
            return ImmutableList.of();
        }

        final List<T> list = new ArrayList<>(l);
        for (int i = 0; i < l; i++) {
            org.w3c.dom.Node child = nodes.item(i);
            if (child instanceof Element) {
                Optional<T> result = forBody.apply(new ElementWithSchemaContext((Element) child,schemaContext));
                if (result.isPresent()) {
                    list.add(result.get());
                }
            }
        }
        return ImmutableList.copyOf(list);
    }

    public static final XmlCodecProvider defaultValueCodecProvider() {
        return DEFAULT_XML_VALUE_CODEC_PROVIDER;
    }
}
