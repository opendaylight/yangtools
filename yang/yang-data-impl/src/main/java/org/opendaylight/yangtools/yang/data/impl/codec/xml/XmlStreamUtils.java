package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import java.util.Map.Entry;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlStreamUtils {
    private static final Logger logger = LoggerFactory.getLogger(XmlStreamUtils.class);
    private static final XMLEventFactory EVENTS = XMLEventFactory.newFactory();

    public static void writeDataDocument(final XMLEventWriter writer, final CompositeNode data, final SchemaNode schema, final XmlCodecProvider codecProvider) throws XMLStreamException {
        writer.add(EVENTS.createStartDocument());
        writeData(writer, data, schema, codecProvider);
        writer.add(EVENTS.createEndDocument());
    }

    public static void writeDataDocument(final XMLEventWriter writer, final CompositeNode data, final XmlCodecProvider codecProvider) throws XMLStreamException {
        writeDataDocument(writer, data, null, codecProvider);
    }

    private static void writeData(final XMLEventWriter writer, final Node<?> data, final SchemaNode schema, final XmlCodecProvider codecProvider) throws XMLStreamException {
        final javax.xml.namespace.QName q = createQName(data.getNodeType());
        writer.add(EVENTS.createStartElement(q, null, null));

        if (data instanceof AttributesContainer && ((AttributesContainer) data).getAttributes() != null) {
            for (Entry<QName, String> attribute : ((AttributesContainer) data).getAttributes().entrySet()) {
                writer.add(EVENTS.createAttribute(null, attribute.getKey().getNamespace().toString(), attribute.getKey().getLocalName(),
                        attribute.getValue()));
            }
        }

        if (data instanceof SimpleNode<?>) {
            // Simple node
            if (schema instanceof LeafListSchemaNode) {
                writeValue(writer, ((LeafListSchemaNode) schema).getType(), codecProvider, data.getValue());
            } else if (schema instanceof LeafSchemaNode) {
                writeValue(writer, ((LeafSchemaNode) schema).getType(), codecProvider, data.getValue());
            } else {
                Object value = data.getValue();
                if (value != null) {
                    writer.add(EVENTS.createCharacters(String.valueOf(value)));
                }
            }
        } else {
            // CompositeNode
            for (Node<?> child : ((CompositeNode) data).getValue()) {
                DataSchemaNode childSchema = null;
                if (schema instanceof DataNodeContainer) {
                    childSchema = SchemaUtils.findFirstSchema(child.getNodeType(), ((DataNodeContainer) schema).getChildNodes()).orNull();
                    if (logger.isDebugEnabled()) {
                        if (childSchema == null) {
                            logger.debug("Probably the data node \""
                                    + ((child == null) ? "" : child.getNodeType().getLocalName())
                                    + "\" does not conform to schema");
                        }
                    }
                }

                writeData(writer, child, childSchema, codecProvider);
            }
        }

        writer.add(EVENTS.createEndElement(q, null));
    }

    public static void writeValue(final XMLEventWriter writer, final TypeDefinition<?> type, final XmlCodecProvider codecProvider, final Object nodeValue) throws XMLStreamException {
        TypeDefinition<?> baseType = XmlUtils.resolveBaseTypeFrom(type);
        if (baseType instanceof IdentityrefTypeDefinition) {
            if (nodeValue instanceof QName) {
                QName value = (QName) nodeValue;
                String prefix = "x";
                if (value.getPrefix() != null && !value.getPrefix().isEmpty()) {
                    prefix = value.getPrefix();
                }

                writer.add(EVENTS.createNamespace(prefix, value.getNamespace().toString()));
                writer.add(EVENTS.createCharacters(prefix + ':' + value.getLocalName()));
            } else {
                Object value = nodeValue;
                logger.debug("Value of {}:{} is not instance of QName but is {}", baseType.getQName().getNamespace(),
                        baseType.getQName().getLocalName(), value != null ? value.getClass() : "null");
                if (value != null) {
                    writer.add(EVENTS.createCharacters(String.valueOf(value)));
                }
            }
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            if (nodeValue instanceof InstanceIdentifier) {
                InstanceIdentifierForXmlCodec.write(writer, (InstanceIdentifier)nodeValue);
            } else {
                Object value = nodeValue;
                logger.debug("Value of {}:{} is not instance of InstanceIdentifier but is {}", baseType.getQName()
                        .getNamespace(), //
                        baseType.getQName().getLocalName(), value != null ? value.getClass() : "null");
                if (value != null) {
                    writer.add(EVENTS.createCharacters(String.valueOf(value)));
                }
            }
        } else {
            if (nodeValue != null) {
                final TypeDefinitionAwareCodec<Object, ?> codec = codecProvider.codecFor(baseType);
                if (codec != null) {
                    try {
                        final String text = codec.serialize(nodeValue);
                        writer.add(EVENTS.createCharacters(text));
                    } catch (ClassCastException e) {
                        logger.error("Provided node value {} did not have type {} required by mapping. Using stream instead.", nodeValue, baseType, e);
                        writer.add(EVENTS.createCharacters(String.valueOf(nodeValue)));
                    }
                } else {
                    logger.error("Failed to find codec for {}, falling back to using stream", baseType);
                    writer.add(EVENTS.createCharacters(String.valueOf(nodeValue)));
                }
            }
        }
    }

    private static javax.xml.namespace.QName createQName(final QName qname) {
        final String ns;
        if (qname.getNamespace() != null) {
            ns = qname.getNamespace().toString();
        } else {
            ns = "";
        }

        final String pfx = qname.getPrefix() != null ? qname.getPrefix() : "";
        return new javax.xml.namespace.QName(ns, qname.getLocalName(), pfx);
    }

}
