/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.net.URI;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
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
 *
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
@Beta
public final class XmlStreamUtils {
    private static final Logger LOG = LoggerFactory.getLogger(XmlStreamUtils.class);
    private final XmlCodecProvider codecProvider;
    private final Optional<SchemaContext> schemaContext;

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
        return new XmlStreamUtils(codecProvider, null);
    }

    public static XmlStreamUtils create(final XmlCodecProvider codecProvider, final SchemaContext schemaContext) {
        return new XmlStreamUtils(codecProvider, schemaContext);
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
     * emitted by the caller.
     *
     * @param writer XML Stream writer
     * @param schemaNode Schema node that describes the value
     * @param value data value
     * @param parent optional parameter of a module QName owning the leaf definition
     * @throws XMLStreamException if an encoding problem occurs
     */
    public void writeValue(@Nonnull final XMLStreamWriter writer, @Nonnull final SchemaNode schemaNode, final Object value, final Optional<QNameModule> parent) throws XMLStreamException {
        if (value == null) {
            LOG.debug("Value of {}:{} is null, not encoding it", schemaNode.getQName().getNamespace(), schemaNode.getQName().getLocalName());
            return;
        }

        Preconditions.checkArgument(schemaNode instanceof LeafSchemaNode || schemaNode instanceof LeafListSchemaNode,
                "Unable to write value for node %s, only nodes of type: leaf and leaf-list can be written at this point", schemaNode.getQName());

        TypeDefinition<?> type = schemaNode instanceof LeafSchemaNode ?
                ((LeafSchemaNode) schemaNode).getType():
                ((LeafListSchemaNode) schemaNode).getType();

        TypeDefinition<?> baseType = XmlUtils.resolveBaseTypeFrom(type);

        if (schemaContext.isPresent() && baseType instanceof LeafrefTypeDefinition) {
            LeafrefTypeDefinition leafrefTypeDefinition = (LeafrefTypeDefinition) baseType;
            baseType = SchemaContextUtil.getBaseTypeForLeafRef(leafrefTypeDefinition, schemaContext.get(), schemaNode);
            Verify.verifyNotNull(baseType, "Unable to find base type for leafref node '%s'.", schemaNode.getPath());
        }

        writeValue(writer, baseType, value, parent);
    }

    public void writeValue(@Nonnull final XMLStreamWriter writer, @Nonnull final SchemaNode schemaNode, final Object value) throws XMLStreamException {
        writeValue(writer, schemaNode, value, Optional.absent());
    }

    public void writeValue(@Nonnull final XMLStreamWriter writer, @Nonnull final SchemaNode schemaNode, final Object value, final QNameModule parent) throws XMLStreamException {
        writeValue(writer, schemaNode, value, Optional.of(parent));
    }

    /**
     * Write a value into a XML stream writer. This method assumes the start and end of element is
     * emitted by the caller.
     *
     * @param writer XML Stream writer
     * @param type data type. In case of leaf ref this should be the type of leaf being referenced
     * @param value data value
     * @param parent optional parameter of a module QName owning the leaf definition
     * @throws XMLStreamException if an encoding problem occurs
     */
    public void writeValue(@Nonnull final XMLStreamWriter writer, @Nonnull final TypeDefinition<?> type, final Object value, final Optional<QNameModule> parent) throws XMLStreamException {
        if (value == null) {
            LOG.debug("Value of {}:{} is null, not encoding it", type.getQName().getNamespace(), type.getQName().getLocalName());
            return;
        }
        TypeDefinition<?> baseType = XmlUtils.resolveBaseTypeFrom(type);

        if (baseType instanceof IdentityrefTypeDefinition) {
            if (parent.isPresent()) {
                write(writer, (IdentityrefTypeDefinition) baseType, value, parent);
            } else {
                write(writer, (IdentityrefTypeDefinition) baseType, value, Optional.absent());
            }
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            write(writer, (InstanceIdentifierTypeDefinition) baseType, value);
        } else {
            final TypeDefinitionAwareCodec<Object, ?> codec = codecProvider.codecFor(type);
            String text;
            if (codec != null) {
                try {
                    text = codec.serialize(value);
                } catch (ClassCastException e) {
                    LOG.error("Provided node value {} did not have type {} required by mapping. Using stream instead.", value, type, e);
                    text = String.valueOf(value);
                }
            } else {
                LOG.error("Failed to find codec for {}, falling back to using stream", type);
                text = String.valueOf(value);
            }
            writer.writeCharacters(text);
        }
    }

    public void writeValue(@Nonnull final XMLStreamWriter writer, @Nonnull final TypeDefinition<?> type, final Object value, final QNameModule parent) throws XMLStreamException {
        writeValue(writer, type, value, Optional.of(parent));
    }

    public void writeValue(@Nonnull final XMLStreamWriter writer, @Nonnull final TypeDefinition<?> type, final Object value) throws XMLStreamException {
        writeValue(writer, type, value, Optional.absent());
    }

    @VisibleForTesting
    static void write(@Nonnull final XMLStreamWriter writer, @Nonnull final IdentityrefTypeDefinition type, @Nonnull final Object value, final Optional<QNameModule>  parent) throws XMLStreamException {
        if (value instanceof QName) {
            final QName qname = (QName) value;
            final String prefix = "x";

            //in case parent is present and same as element namespace write value without namespace
            if (parent.isPresent() && qname.getNamespace().equals(parent.get().getNamespace())){
                writer.writeCharacters(qname.getLocalName());
            } else {
                final String ns = qname.getNamespace().toString();
                writer.writeNamespace(prefix, ns);
                writer.writeCharacters(prefix + ':' + qname.getLocalName());
            }

        } else {
            LOG.debug("Value of {}:{} is not a QName but {}", type.getQName().getNamespace(), type.getQName().getLocalName(), value.getClass());
            writer.writeCharacters(String.valueOf(value));
        }
    }

    private void write(@Nonnull final XMLStreamWriter writer, @Nonnull final InstanceIdentifierTypeDefinition type, @Nonnull final Object value) throws XMLStreamException {
        if (value instanceof YangInstanceIdentifier) {
            writeInstanceIdentifier(writer, (YangInstanceIdentifier)value);
        } else {
            LOG.warn("Value of {}:{} is not an InstanceIdentifier but {}", type.getQName().getNamespace(), type.getQName().getLocalName(), value.getClass());
            writer.writeCharacters(String.valueOf(value));
        }
    }

    public void writeInstanceIdentifier(final XMLStreamWriter writer, final YangInstanceIdentifier value) throws XMLStreamException {
        if (schemaContext.isPresent()) {
            RandomPrefixInstanceIdentifierSerializer iiCodec = new RandomPrefixInstanceIdentifierSerializer(schemaContext.get());
            String serializedValue = iiCodec.serialize(value);
            writeNamespaceDeclarations(writer,iiCodec.getPrefixes());
            writer.writeCharacters(serializedValue);
        } else {
            LOG.warn("Schema context not present in {}, serializing {} without schema.",this,value);
            writeInstanceIdentifier(writer, value);
        }
    }

    private static void writeNamespaceDeclarations(final XMLStreamWriter writer, final Iterable<Entry<URI, String>> prefixes) throws XMLStreamException {
        for (Entry<URI, String> e: prefixes) {
            final String ns = e.getKey().toString();
            final String p = e.getValue();
            writer.writeNamespace(p, ns);
        }
    }
}
