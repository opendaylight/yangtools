package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for bridging JAXP Stream and YANG Data APIs. Note that the definition of this class
 * by no means final and subject to change as more functionality is centralized here.
 */
@Beta
public class XmlStreamUtils {
    private static final Logger LOG = LoggerFactory.getLogger(XmlStreamUtils.class);
    private final XmlCodecProvider codecProvider;
    private final Optional<SchemaContext> schemaContext;

    protected XmlStreamUtils(final XmlCodecProvider codecProvider) {
        this(codecProvider, null);
    }

    private XmlStreamUtils(final XmlCodecProvider codecProvider, final SchemaContext schemaContext) {
        this.codecProvider = Preconditions.checkNotNull(codecProvider);
        this.schemaContext = Optional.fromNullable(schemaContext);
    }

    /**
     * Create a new instance encapsulating a particular codec provider.
     *
     * @param codecProvider XML codec provider
     * @return A new instance
     */
    public static XmlStreamUtils create(final XmlCodecProvider codecProvider) {
        return new XmlStreamUtils(codecProvider);
    }

    public static XmlStreamUtils create(final XmlCodecProvider codecProvider, final SchemaContext schemaContext) {
        return new XmlStreamUtils(codecProvider, schemaContext);
    }

    /**
     * Check if a particular data element can be emitted as an empty element, bypassing value encoding. This
     * functionality is optional, as valid XML stream is produced even if start/end element is produced unconditionally.
     *
     * @param data Data node
     * @return True if the data node will result in empty element body.
     */
    public static boolean isEmptyElement(final Node<?> data) {
        if (data == null) {
            return true;
        }

        if (data instanceof CompositeNode) {
            return ((CompositeNode) data).getValue().isEmpty();
        }
        if (data instanceof SimpleNode) {
            return data.getValue() == null;
        }

        // Safe default
        return false;
    }

    /**
     * Write an InstanceIdentifier into the output stream. Calling corresponding {@link XMLStreamWriter#writeStartElement(String)}
     * and {@link XMLStreamWriter#writeEndElement()} is the responsibility of the caller.
     *
     * @param writer XML Stream writer
     * @param id InstanceIdentifier
     * @throws XMLStreamException
     */
    public static void write(final @Nonnull XMLStreamWriter writer, final @Nonnull YangInstanceIdentifier id) throws XMLStreamException {
        Preconditions.checkNotNull(writer, "Writer may not be null");
        Preconditions.checkNotNull(id, "Variable should contain instance of instance identifier and can't be null");

        final RandomPrefix prefixes = new RandomPrefix();
        final String str = XmlUtils.encodeIdentifier(prefixes, id);

        for (Entry<URI, String> e: prefixes.getPrefixes()) {
            final String ns = e.getKey().toString();
            final String p = e.getValue();

            writer.writeNamespace(p, ns);
        }
        writer.writeCharacters(str);
    }

    /**
     * Write a full XML document corresponding to a CompositeNode into an XML stream writer.
     *
     * @param writer XML Stream writer
     * @param data data node
     * @param schema corresponding schema node, may be null
     * @throws XMLStreamException if an encoding problem occurs
     */
    public void writeDocument(final @Nonnull XMLStreamWriter writer, final @Nonnull CompositeNode data, final @Nullable SchemaNode schema) throws XMLStreamException {
        // final Boolean repairing = (Boolean) writer.getProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES);
        // Preconditions.checkArgument(repairing == true, "XML Stream Writer has to be repairing namespaces");

        writer.writeStartDocument();
        writeElement(writer, data, schema);
        writer.writeEndDocument();
        writer.flush();
    }

    /**
     * Short-hand for {@link #writeDocument(XMLStreamWriter, CompositeNode, SchemaNode)})} with
     * null SchemaNode.
     *
     * @param writer XML Stream writer
     * @param data data node
     * @throws XMLStreamException if an encoding problem occurs
     */
    public void writeDocument(final XMLStreamWriter writer, final CompositeNode data) throws XMLStreamException {
        writeDocument(writer, data, null);
    }

    /**
     * Write an element into a XML stream writer. This includes the element start/end tags and
     * the value of the element.
     *
     * @param writer XML Stream writer
     * @param data data node
     * @param schema Schema node
     * @throws XMLStreamException if an encoding problem occurs
     */
    public void writeElement(final XMLStreamWriter writer, final @Nonnull Node<?> data, final SchemaNode schema) throws XMLStreamException {
        final QName qname = data.getNodeType();
        final String pfx = qname.getPrefix() != null ? qname.getPrefix() : "";
        final String ns = qname.getNamespace() != null ? qname.getNamespace().toString() : "";

        if (isEmptyElement(data)) {
            if (hasAttributes(data)) {
                writer.writeStartElement(pfx, qname.getLocalName(), ns);
                final RandomPrefix randomPrefix = new RandomPrefix();
                writeAttributes(writer, (AttributesContainer) data, randomPrefix);
                writer.writeEndElement();
            } else {
                writer.writeEmptyElement(pfx, qname.getLocalName(), ns);
            }
            return;
        }

        writer.writeStartElement(pfx, qname.getLocalName(), ns);
        writeValue(writer, data, schema);
        writer.writeEndElement();
    }

    /**
     * Write a value into a XML stream writer. This method assumes the start and end of element is
     * emitted by the caller.
     *
     * @param writer XML Stream writer
     * @param data data node
     * @param schema Schema node
     * @throws XMLStreamException if an encoding problem occurs
     */
    public void writeValue(final XMLStreamWriter writer, final @Nonnull Node<?> data, final SchemaNode schema) throws XMLStreamException {
        if (hasAttributes(data)) {
            RandomPrefix randomPrefix = new RandomPrefix();
            writeAttributes(writer, (AttributesContainer) data, randomPrefix);
        }

        if (data instanceof SimpleNode<?>) {
            // Simple node
            if (schema instanceof LeafListSchemaNode || schema instanceof LeafSchemaNode) {
                writeValue(writer, schema, data.getValue());
            } else {
                Object value = data.getValue();
                if (value != null) {
                    writer.writeCharacters(String.valueOf(value));
                }
            }
        } else {
            // CompositeNode
            final CompositeNode castedData = ((CompositeNode) data);
            final DataNodeContainer castedSchema;
            if (schema instanceof DataNodeContainer) {
                castedSchema = (DataNodeContainer) schema;
            } else {
                castedSchema = null;
            }
            final Collection<QName> keyLeaves;
            if (schema instanceof ListSchemaNode) {
                keyLeaves = ((ListSchemaNode) schema).getKeyDefinition();

            } else {
                keyLeaves = Collections.emptyList();

            }
            for (QName key : keyLeaves) {
                SimpleNode<?> keyLeaf = castedData.getFirstSimpleByName(key);
                if(keyLeaf != null) {
                    writeChildElement(writer,keyLeaf,castedSchema);
                }
            }

            for (Node<?> child : castedData.getValue()) {

                if(keyLeaves.contains(child.getNodeType())) {
                    // Skip key leaf which was written by previous for loop.
                    continue;
                }
                writeChildElement(writer,child,castedSchema);
            }
        }
    }

    private void writeChildElement(XMLStreamWriter writer, Node<?> child, DataNodeContainer parentSchema) throws XMLStreamException {
        DataSchemaNode childSchema = null;
        if (parentSchema != null) {
            childSchema = SchemaUtils.findFirstSchema(child.getNodeType(), parentSchema.getChildNodes()).orNull();
            if ((childSchema == null) && LOG.isDebugEnabled()) {
                LOG.debug("Probably the data node \"{}\" does not conform to schema", child == null ? "" : child.getNodeType().getLocalName());
            }
        }
        writeElement(writer, child, childSchema);
    }

    private static void writeAttributes(final XMLStreamWriter writer, final AttributesContainer data, final RandomPrefix randomPrefix) throws XMLStreamException {
        for (Entry<QName, String> attribute : data.getAttributes().entrySet()) {
            writeAttribute(writer, attribute, randomPrefix);
        }
    }

    private static boolean hasAttributes(final Node<?> data) {
        if (data instanceof AttributesContainer) {
            final Map<QName, String> c = ((AttributesContainer) data).getAttributes();
            return c != null && !c.isEmpty();
        } else {
            return false;
        }
    }

    @VisibleForTesting
    static void writeAttribute(final XMLStreamWriter writer, final Entry<QName, String> attribute, final RandomPrefix randomPrefix)
            throws XMLStreamException {
        final QName key = attribute.getKey();
        final String prefix = randomPrefix.encodePrefix(key.getNamespace());
        writer.writeAttribute("xmlns:" + prefix, key.getNamespace().toString());
        writer.writeAttribute(prefix, key.getNamespace().toString(), key.getLocalName(), attribute.getValue());
    }

    /**
     * Write a value into a XML stream writer. This method assumes the start and end of element is
     * emitted by the caller. This method handles also leafref schema nodes.
     *
     * @param writer XML Stream writer
     * @param schemaNode SchemaNode (it can be also leafref node, but SchemaContext must be present)
     * @param value data value
     * @throws XMLStreamException if an encoding problem occurs
     */
    public void writeValue(final @Nonnull XMLStreamWriter writer, final SchemaNode schemaNode, final Object value)
            throws XMLStreamException {
        if (value == null) {
            LOG.debug("Value of {}:{} is null, not encoding it", schemaNode.getQName().getNamespace(), schemaNode
                    .getQName().getLocalName());
            return;
        }

        Preconditions
                .checkArgument(
                        schemaNode instanceof LeafSchemaNode || schemaNode instanceof LeafListSchemaNode,
                        "Unable to write value for node %s, only nodes of type: leaf and leaf-list can be written at this point",
                        schemaNode.getQName());

        TypeDefinition<?> type = schemaNode instanceof LeafSchemaNode ? ((LeafSchemaNode) schemaNode).getType()
                : ((LeafListSchemaNode) schemaNode).getType();

        TypeDefinition<?> baseType = XmlUtils.resolveBaseTypeFrom(type);

        if (schemaContext.isPresent() && baseType instanceof LeafrefTypeDefinition) {
            LeafrefTypeDefinition leafrefTypeDefinition = (LeafrefTypeDefinition) baseType;
            baseType = SchemaContextUtil.getBaseTypeForLeafRef(leafrefTypeDefinition, schemaContext.get(), schemaNode);
        }

        writeValue(writer, baseType, value);
    }

    /**
     * Write a value into a XML stream writer. This method assumes the start and end of element is
     * emitted by the caller.
     *
     * @param writer XML Stream writer
     * @param type data type. In case of leaf ref this should be the type of leaf being referenced
     * @param value data value
     * @throws XMLStreamException if an encoding problem occurs
     */
    public void writeValue(final @Nonnull XMLStreamWriter writer, final TypeDefinition<?> type, final Object value) throws XMLStreamException {
        if (value == null) {
            LOG.debug("Value of {}:{} is null, not encoding it", type.getQName().getNamespace(), type.getQName().getLocalName());
            return;
        }

        TypeDefinition<?> baseType = XmlUtils.resolveBaseTypeFrom(type);

        if (baseType instanceof IdentityrefTypeDefinition) {
            write(writer, (IdentityrefTypeDefinition) baseType, value);
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            write(writer, (InstanceIdentifierTypeDefinition) baseType, value);
        } else {
            final TypeDefinitionAwareCodec<Object, ?> codec = codecProvider.codecFor(baseType);
            String text;
            if (codec != null) {
                try {
                    text = codec.serialize(value);
                } catch (ClassCastException e) {
                    LOG.error("Provided node value {} did not have type {} required by mapping. Using stream instead.", value, baseType, e);
                    text = String.valueOf(value);
                }
            } else {
                LOG.error("Failed to find codec for {}, falling back to using stream", baseType);
                text = String.valueOf(value);
            }
            writer.writeCharacters(text);
        }
    }

    @SuppressWarnings("deprecation")
    private static void write(final @Nonnull XMLStreamWriter writer, final @Nonnull IdentityrefTypeDefinition type, final @Nonnull Object value) throws XMLStreamException {
        if (value instanceof QName) {
            final QName qname = (QName) value;
            final String prefix;
            if (qname.getPrefix() != null && !qname.getPrefix().isEmpty()) {
                prefix = qname.getPrefix();
            } else {
                prefix = "x";
            }

            final String ns = qname.getNamespace().toString();
            writer.writeNamespace(prefix, ns);
            writer.writeCharacters(prefix + ':' + qname.getLocalName());
        } else {
            LOG.debug("Value of {}:{} is not a QName but {}", type.getQName().getNamespace(), type.getQName().getLocalName(), value.getClass());
            writer.writeCharacters(String.valueOf(value));
        }
    }

    private static void write(final @Nonnull XMLStreamWriter writer, final @Nonnull InstanceIdentifierTypeDefinition type, final @Nonnull Object value) throws XMLStreamException {
        if (value instanceof YangInstanceIdentifier) {
            write(writer, (YangInstanceIdentifier)value);
        } else {
            LOG.warn("Value of {}:{} is not an InstanceIdentifier but {}", type.getQName().getNamespace(), type.getQName().getLocalName(), value.getClass());
            writer.writeCharacters(String.valueOf(value));
        }
    }
}
