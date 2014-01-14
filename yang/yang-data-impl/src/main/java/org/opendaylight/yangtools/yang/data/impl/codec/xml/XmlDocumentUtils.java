package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import java.util.Set;

import javax.activation.UnsupportedDataTypeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;

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
        QName dataType = data.getNodeType();
        Element itemEl = doc.createElementNS(dataType.getNamespace().toString(), dataType.getLocalName());
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
                logger.debug("Value of {}:{} is not instance of QName but is {}",
                        baseType.getQName().getNamespace(), //
                        baseType.getQName().getLocalName(), //
                        node.getValue().getClass());
                element.setTextContent(String.valueOf(node.getValue()));
            }
        } else {
            if (node.getValue() != null) {
                try {
                    String value = codecProvider.codecFor(baseType).serialize(node.getValue());
                    element.setTextContent(value);
                } catch (ClassCastException e) {
                    element.setTextContent(String.valueOf(node.getValue()));
                    logger.error("Provided node did not have type required by mapping. Using stream instead. {}",e);
                }
            }
        }
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

    public static final XmlCodecProvider defaultValueCodecProvider() {
        return DEFAULT_XML_VALUE_CODEC_PROVIDER;
    }

}
