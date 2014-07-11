package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import com.google.common.base.Preconditions;

import java.net.URI;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
    private static final Logger LOG = LoggerFactory.getLogger(XmlStreamUtils.class);

    public static void writeDataDocument(final XMLStreamWriter writer, final CompositeNode data, final SchemaNode schema, final XmlCodecProvider codecProvider) throws XMLStreamException {
        //        final Boolean repairing = (Boolean) writer.getProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES);
        //        Preconditions.checkArgument(repairing == true, "XML Stream Writer has to be repairing namespaces");

        writer.writeStartDocument();
        writeData(writer, data, schema, codecProvider);
        writer.writeEndDocument();
        writer.flush();
    }

    public static void writeDataDocument(final XMLStreamWriter writer, final CompositeNode data, final XmlCodecProvider codecProvider) throws XMLStreamException {
        writeDataDocument(writer, data, null, codecProvider);
    }

    public static void write(final XMLStreamWriter writer, final InstanceIdentifier id) throws XMLStreamException {
        Preconditions.checkNotNull(writer, "Writer may not be null");
        Preconditions.checkNotNull(id, "Variable should contain instance of instance identifier and can't be null");

        final RandomPrefix prefixes = new RandomPrefix();
        final String str = XmlUtils.encodeIdentifier(prefixes, id);

        for (Entry<URI, String> e: prefixes.getPrefixes()) {
            writer.writeNamespace(e.getValue(), e.getKey().toString());
        }
        writer.writeCharacters(str);
    }

    public static void writeData(final XMLStreamWriter writer, final Node<?> data, final SchemaNode schema, final XmlCodecProvider codecProvider) throws XMLStreamException {
        final QName qname = data.getNodeType();
        final String pfx = qname.getPrefix() != null ? qname.getPrefix() : "";
        final String ns;
        if (qname.getNamespace() != null) {
            ns = qname.getNamespace().toString();
        } else {
            ns = "";
        }

        writer.writeStartElement(pfx, qname.getLocalName(), ns);
        if (data instanceof AttributesContainer && ((AttributesContainer) data).getAttributes() != null) {
            for (Entry<QName, String> attribute : ((AttributesContainer) data).getAttributes().entrySet()) {
                writer.writeAttribute(attribute.getKey().getNamespace().toString(), attribute.getKey().getLocalName(), attribute.getValue());
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
                    writer.writeCharacters(String.valueOf(value));
                }
            }
        } else {
            // CompositeNode
            for (Node<?> child : ((CompositeNode) data).getValue()) {
                DataSchemaNode childSchema = null;
                if (schema instanceof DataNodeContainer) {
                    childSchema = SchemaUtils.findFirstSchema(child.getNodeType(), ((DataNodeContainer) schema).getChildNodes()).orNull();
                    if (LOG.isDebugEnabled()) {
                        if (childSchema == null) {
                            LOG.debug("Probably the data node \"{}\" does not conform to schema", child == null ? "" : child.getNodeType().getLocalName());
                        }
                    }
                }

                writeData(writer, child, childSchema, codecProvider);
            }
        }

        writer.writeEndElement();
    }

    public static void writeValue(final XMLStreamWriter writer, final TypeDefinition<?> type, final XmlCodecProvider codecProvider, final Object nodeValue) throws XMLStreamException {
        TypeDefinition<?> baseType = XmlUtils.resolveBaseTypeFrom(type);
        if (baseType instanceof IdentityrefTypeDefinition) {
            if (nodeValue instanceof QName) {
                QName value = (QName) nodeValue;
                String prefix = "x";
                if (value.getPrefix() != null && !value.getPrefix().isEmpty()) {
                    prefix = value.getPrefix();
                }

                writer.writeNamespace(prefix, value.getNamespace().toString());
                writer.writeCharacters(prefix + ':' + value.getLocalName());
            } else {
                Object value = nodeValue;
                LOG.debug("Value of {}:{} is not instance of QName but is {}", baseType.getQName().getNamespace(),
                        baseType.getQName().getLocalName(), value != null ? value.getClass() : "null");
                if (value != null) {
                    writer.writeCharacters(String.valueOf(value));
                }
            }
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            if (nodeValue instanceof InstanceIdentifier) {
                write(writer, (InstanceIdentifier)nodeValue);
            } else {
                Object value = nodeValue;
                LOG.debug("Value of {}:{} is not instance of InstanceIdentifier but is {}", baseType.getQName()
                        .getNamespace(), //
                        baseType.getQName().getLocalName(), value != null ? value.getClass() : "null");
                if (value != null) {
                    writer.writeCharacters(String.valueOf(value));
                }
            }
        } else {
            if (nodeValue != null) {
                final TypeDefinitionAwareCodec<Object, ?> codec = codecProvider.codecFor(baseType);
                String text;
                if (codec != null) {
                    try {
                        text = codec.serialize(nodeValue);
                    } catch (ClassCastException e) {
                        LOG.error("Provided node value {} did not have type {} required by mapping. Using stream instead.", nodeValue, baseType, e);
                        text = String.valueOf(nodeValue);
                    }
                } else {
                    LOG.error("Failed to find codec for {}, falling back to using stream", baseType);
                    text = String.valueOf(nodeValue);
                }
                writer.writeCharacters(text);
            }
        }
    }
}
