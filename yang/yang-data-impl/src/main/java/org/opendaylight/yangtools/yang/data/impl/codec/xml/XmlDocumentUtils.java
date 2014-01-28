/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.activation.UnsupportedDataTypeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
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
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class XmlDocumentUtils {

    private static final XmlCodecProvider DEFAULT_XML_VALUE_CODEC_PROVIDER = new XmlCodecProvider() {

        @Override
        public TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codecFor(TypeDefinition<?> baseType) {
            return TypeDefinitionAwareCodec.from(baseType);
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(XmlDocumentUtils.class);

    public static Document toDocument(CompositeNode data, DataNodeContainer schema, XmlCodecProvider codecProvider)
            throws UnsupportedDataTypeException {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(schema);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder bob = dbf.newDocumentBuilder();
            doc = bob.newDocument();
        } catch (ParserConfigurationException e) {
            return null;
        }

        if (schema instanceof ContainerSchemaNode || schema instanceof ListSchemaNode) {
            doc.appendChild(createXmlRootElement(doc, data, (SchemaNode) schema, codecProvider));
            return doc;
        } else {
            throw new UnsupportedDataTypeException(
                    "Schema can be ContainerSchemaNode or ListSchemaNode. Other types are not supported yet.");
        }
    }

    private static Element createXmlRootElement(Document doc, Node<?> data, SchemaNode schema,
            XmlCodecProvider codecProvider) throws UnsupportedDataTypeException {
        Element itemEl = createElementFor(doc, data);
        if (data instanceof SimpleNode<?>) {
            if (schema instanceof LeafListSchemaNode) {
                writeValueByType(itemEl, (SimpleNode<?>) data, ((LeafListSchemaNode) schema).getType(),
                        (DataSchemaNode) schema, codecProvider);
            } else if (schema instanceof LeafSchemaNode) {
                writeValueByType(itemEl, (SimpleNode<?>) data, ((LeafSchemaNode) schema).getType(),
                        (DataSchemaNode) schema, codecProvider);
            } else {
                Object value = data.getValue();
                if (value != null) {
                    itemEl.setTextContent(String.valueOf(value));
                }
            }
        } else { // CompositeNode
            for (Node<?> child : ((CompositeNode) data).getChildren()) {
                DataSchemaNode childSchema = null;
                if (schema != null) {
                    childSchema = findFirstSchemaForNode(child, ((DataNodeContainer) schema).getChildNodes());
                    if (logger.isDebugEnabled()) {
                        if (childSchema == null) {
                            logger.debug("Probably the data node \""
                                    + ((child == null) ? "" : child.getNodeType().getLocalName())
                                    + "\" is not conform to schema");
                        }
                    }
                }
                itemEl.appendChild(createXmlRootElement(doc, child, childSchema, codecProvider));
            }
        }
        return itemEl;
    }

    private static Element createElementFor(Document doc, Node<?> data) {
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

    public static void writeValueByType(Element element, SimpleNode<?> node, TypeDefinition<?> type,
            DataSchemaNode schema, XmlCodecProvider codecProvider) {
        TypeDefinition<?> baseType = resolveBaseTypeFrom(type);

        if (baseType instanceof IdentityrefTypeDefinition) {
            if (node.getValue() instanceof QName) {
                QName value = (QName) node.getValue();
                String prefix = "x";
                if (value.getPrefix() != null && !value.getPrefix().isEmpty()) {
                    prefix = value.getPrefix();
                }
                element.setAttribute("xmlns:" + prefix, value.getNamespace().toString());
                element.setTextContent(prefix + ":" + value.getLocalName());
            } else {
                Object value = node.getValue();
                logger.debug("Value of {}:{} is not instance of QName but is {}", baseType.getQName().getNamespace(),
                        baseType.getQName().getLocalName(), value != null ? value.getClass() : "null");
                if (value != null) {
                    element.setTextContent(String.valueOf(value));
                }
            }
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            if (node.getValue() instanceof InstanceIdentifier) {
                // Map< key = namespace, value = prefix>
                Map<String, String> prefixes = new HashMap<>();
                InstanceIdentifier instanceIdentifier = (InstanceIdentifier) node.getValue();
                StringBuilder textContent = new StringBuilder();
                for (PathArgument pathArgument : instanceIdentifier.getPath()) {
                    textContent.append("/");
                    writeIdentifierWithNamespacePrefix(element, textContent, pathArgument.getNodeType(), prefixes);
                    if (pathArgument instanceof NodeIdentifierWithPredicates) {
                        Map<QName, Object> predicates = ((NodeIdentifierWithPredicates) pathArgument).getKeyValues();

                        for (QName keyValue : predicates.keySet()) {
                            String predicateValue = String.valueOf(predicates.get(keyValue));
                            textContent.append("[");
                            writeIdentifierWithNamespacePrefix(element, textContent, keyValue, prefixes);
                            textContent.append("='");
                            textContent.append(predicateValue);
                            textContent.append("'");
                            textContent.append("]");
                        }
                    } else if (pathArgument instanceof NodeWithValue) {
                        textContent.append("[.='");
                        textContent.append(((NodeWithValue)pathArgument).getValue());
                        textContent.append("'");
                        textContent.append("]");
                    }
                }
                element.setTextContent(textContent.toString());

            } else {
                Object value = node.getValue();
                logger.debug("Value of {}:{} is not instance of InstanceIdentifier but is {}", baseType.getQName()
                        .getNamespace(), //
                        baseType.getQName().getLocalName(), value != null ? value.getClass() : "null");
                if (value != null) {
                    element.setTextContent(String.valueOf(value));
                }
            }
        } else {
            if (node.getValue() != null) {
                final TypeDefinitionAwareCodec<Object, ?> codec = codecProvider.codecFor(baseType);
                if (codec != null) {
                    try {
                        final String text = codec.serialize(node.getValue());
                        element.setTextContent(text);
                    } catch (ClassCastException e) {
                        logger.error("Provided node did not have type required by mapping. Using stream instead.", e);
                        element.setTextContent(String.valueOf(node.getValue()));
                    }
                } else {
                    logger.error("Failed to find codec for {}, falling back to using stream", baseType);
                    element.setTextContent(String.valueOf(node.getValue()));
                }
            }
        }
    }

    private static void writeIdentifierWithNamespacePrefix(Element element, StringBuilder textContent, QName qName,
            Map<String, String> prefixes) {
        String namespace = qName.getNamespace().toString();
        String prefix = prefixes.get(namespace);
        if (prefix == null) {
            prefix = qName.getPrefix();
            if (prefix == null || prefix.isEmpty() || prefixes.containsValue(prefix)) {
                prefix = generateNewPrefix(prefixes.values());
            }
        }

        element.setAttribute("xmlns:" + prefix, namespace.toString());
        textContent.append(prefix);
        prefixes.put(namespace, prefix);

        textContent.append(":");
        textContent.append(qName.getLocalName());
    }

    private static String generateNewPrefix(Collection<String> prefixes) {
        StringBuilder result = null;
        Random random = new Random();
        do {
            result = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                int randomNumber = 0x61 + (Math.abs(random.nextInt()) % 26);
                result.append(Character.toChars(randomNumber));
            }
        } while (prefixes.contains(result.toString()));

        return result.toString();
    }

    public final static TypeDefinition<?> resolveBaseTypeFrom(TypeDefinition<?> type) {
        TypeDefinition<?> superType = type;
        while (superType.getBaseType() != null) {
            superType = superType.getBaseType();
        }
        return superType;
    }

    private static final DataSchemaNode findFirstSchemaForNode(Node<?> node, Set<DataSchemaNode> dataSchemaNode) {
        if (dataSchemaNode != null && node != null) {
            for (DataSchemaNode dsn : dataSchemaNode) {
                if (node.getNodeType().getLocalName().equals(dsn.getQName().getLocalName())) {
                    return dsn;
                } else if (dsn instanceof ChoiceNode) {
                    for (ChoiceCaseNode choiceCase : ((ChoiceNode) dsn).getCases()) {
                        DataSchemaNode foundDsn = findFirstSchemaForNode(node, choiceCase.getChildNodes());
                        if (foundDsn != null) {
                            return foundDsn;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Node<?> toDomNode(Element xmlElement, Optional<DataSchemaNode> schema,
            Optional<XmlCodecProvider> codecProvider) {
        if (schema.isPresent()) {
            return toNodeWithSchema(xmlElement, schema.get(), codecProvider.or(DEFAULT_XML_VALUE_CODEC_PROVIDER));
        }
        return toDomNode(xmlElement);
    }

    public static CompositeNode fromElement(Element xmlElement) {
        CompositeNodeBuilder<ImmutableCompositeNode> node = ImmutableCompositeNode.builder();
        node.setQName(qNameFromElement(xmlElement));

        return node.toInstance();
    }

    private static QName qNameFromElement(Element xmlElement) {
        String namespace = xmlElement.getNamespaceURI();
        String localName = xmlElement.getLocalName();
        return QName.create(namespace != null ? URI.create(namespace) : null, null, localName);
    }

    private static Node<?> toNodeWithSchema(Element xmlElement, DataSchemaNode schema, XmlCodecProvider codecProvider) {
        checkQName(xmlElement, schema.getQName());
        if (schema instanceof DataNodeContainer) {
            return toCompositeNodeWithSchema(xmlElement, schema.getQName(), (DataNodeContainer) schema, codecProvider);
        } else if (schema instanceof LeafSchemaNode) {
            return toSimpleNodeWithType(xmlElement, (LeafSchemaNode) schema, codecProvider);
        } else if (schema instanceof LeafListSchemaNode) {
            return toSimpleNodeWithType(xmlElement, (LeafListSchemaNode) schema, codecProvider);

        }
        return null;
    }

    private static Node<?> toSimpleNodeWithType(Element xmlElement, LeafSchemaNode schema,
            XmlCodecProvider codecProvider) {
        TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codec = codecProvider.codecFor(schema.getType());
        String text = xmlElement.getTextContent();
        Object value;
        if (codec != null) {
            value = codec.deserialize(text);

        } else {
            value = xmlElement.getTextContent();
        }
        return new SimpleNodeTOImpl<Object>(schema.getQName(), null, value);
    }

    private static Node<?> toSimpleNodeWithType(Element xmlElement, LeafListSchemaNode schema,
            XmlCodecProvider codecProvider) {
        TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codec = codecProvider.codecFor(schema.getType());
        String text = xmlElement.getTextContent();
        Object value;
        if (codec != null) {
            value = codec.deserialize(text);

        } else {
            value = xmlElement.getTextContent();
        }
        return new SimpleNodeTOImpl<Object>(schema.getQName(), null, value);
    }

    private static Node<?> toCompositeNodeWithSchema(Element xmlElement, QName qName, DataNodeContainer schema,
            XmlCodecProvider codecProvider) {
        List<Node<?>> values = toDomNodes(xmlElement, Optional.fromNullable(schema.getChildNodes()));
        return ImmutableCompositeNode.create(qName, values);
    }

    private static void checkQName(Element xmlElement, QName qName) {
        checkState(Objects.equal(xmlElement.getNamespaceURI(), qName.getNamespace().toString()));
        checkState(qName.getLocalName().equals(xmlElement.getLocalName()));
    }

    private static final Optional<DataSchemaNode> findFirstSchema(QName qname, Set<DataSchemaNode> dataSchemaNode) {
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

    public static Node<?> toDomNode(Document doc) {
        return toDomNode(doc.getDocumentElement());
    }

    private static Node<?> toDomNode(Element element) {
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

    public static List<Node<?>> toDomNodes(final Element element, final Optional<Set<DataSchemaNode>> context) {
        return forEachChild(element.getChildNodes(), new Function<Element, Optional<Node<?>>>() {

            @Override
            public Optional<Node<?>> apply(Element input) {
                if (context.isPresent()) {
                    QName partialQName = qNameFromElement(input);
                    Optional<DataSchemaNode> schemaNode = findFirstSchema(partialQName, context.get());
                    if (schemaNode.isPresent()) {
                        return Optional.<Node<?>> fromNullable(//
                                toNodeWithSchema(input, schemaNode.get(), DEFAULT_XML_VALUE_CODEC_PROVIDER));
                    }
                }
                return Optional.<Node<?>> fromNullable(toDomNode(input));
            }

        });

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
            final Optional<Set<NotificationDefinition>> notifications) {
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
                                Optional.<Set<DataSchemaNode>> fromNullable(dataNodes));
                        return ImmutableCompositeNode.create(notificationDef.get().getQName(), domNodes);
                    }
                }
            }
        }
        return null;
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

    private static final <T> List<T> forEachChild(NodeList nodes, Function<Element, Optional<T>> forBody) {
        ImmutableList.Builder<T> ret = ImmutableList.<T> builder();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node child = nodes.item(i);
            if (child instanceof Element) {
                Optional<T> result = forBody.apply((Element) child);
                if (result.isPresent()) {
                    ret.add(result.get());
                }
            }
        }
        return ret.build();
    }

    public static final XmlCodecProvider defaultValueCodecProvider() {
        return DEFAULT_XML_VALUE_CODEC_PROVIDER;
    }

}
