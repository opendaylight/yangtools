/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import java.net.URI;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for bridging JAXP Stream and YANG Data APIs. Note that the definition of this class
 * by no means final and subject to change as more functionality is centralized here.
 */
abstract class XMLStreamWriterUtils {
    private static final Logger LOG = LoggerFactory.getLogger(XMLStreamWriterUtils.class);

    @VisibleForTesting
    static void writeAttribute(final XMLStreamWriter writer, final Entry<QName, String> attribute,
                               final RandomPrefix randomPrefix) throws XMLStreamException {
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
     * @param parent module QName owning the leaf definition
     * @throws XMLStreamException if an encoding problem occurs
     */
    void writeValue(final @NonNull XMLStreamWriter writer, final @NonNull SchemaNode schemaNode,
            final Object value, final QNameModule parent) throws XMLStreamException {
        if (value == null) {
            LOG.debug("Value of {}:{} is null, not encoding it", schemaNode.getQName().getNamespace(),
                    schemaNode.getQName().getLocalName());
            return;
        }

        checkArgument(schemaNode instanceof TypedDataSchemaNode,
            "Unable to write value for node %s, only nodes of type: leaf and leaf-list can be written at this point",
            schemaNode.getQName());

        TypeDefinition<?> type = ((TypedDataSchemaNode) schemaNode).getType();
        if (type instanceof LeafrefTypeDefinition) {
            type = getBaseTypeForLeafRef(schemaNode, (LeafrefTypeDefinition) type);
        }

        writeValue(writer, type, value, parent);
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
    private void writeValue(final @NonNull XMLStreamWriter writer, final @NonNull TypeDefinition<?> type,
            final @NonNull Object value, final QNameModule parent) throws XMLStreamException {
        if (type instanceof IdentityrefTypeDefinition) {
            write(writer, (IdentityrefTypeDefinition) type, value, parent);
        } else if (type instanceof InstanceIdentifierTypeDefinition) {
            write(writer, (InstanceIdentifierTypeDefinition) type, value);
        } else {
            write(writer, type, value);
        }
    }

    private static void write(final @NonNull XMLStreamWriter writer, final @NonNull TypeDefinition<?> type,
            final @NonNull Object value) throws XMLStreamException {
        final TypeDefinitionAwareCodec<Object, ?> codec = TypeDefinitionAwareCodec.from(type);
        if (codec == null) {
            LOG.warn("Failed to find codec for {}, falling back to using stream", type);
            writeObjectCharacters(writer, value);
            return;
        }

        final String text;
        try {
            text = codec.serialize(value);
        } catch (ClassCastException e) {
            LOG.warn("Provided node value {} did not have type {} required by mapping. Using stream instead.", value,
                type, e);
            writeObjectCharacters(writer, value);
            return;
        }
        writeCharacters(writer, text);
    }

    private void write(final @NonNull XMLStreamWriter writer, final @NonNull InstanceIdentifierTypeDefinition type,
            final @NonNull Object value) throws XMLStreamException {
        if (value instanceof YangInstanceIdentifier) {
            writeInstanceIdentifier(writer, (YangInstanceIdentifier)value);
        } else {
            LOG.warn("Value of {}:{} is not an InstanceIdentifier but {}", type.getQName().getNamespace(),
                    type.getQName().getLocalName(), value.getClass());
            writeObjectCharacters(writer, value);
        }
    }

    @VisibleForTesting
    static void write(final @NonNull XMLStreamWriter writer, final @NonNull IdentityrefTypeDefinition type,
            final @NonNull Object value, final QNameModule parent) throws XMLStreamException {
        if (value instanceof QName) {
            final QName qname = (QName) value;
            final URI ns = qname.getNamespace();

            //in case parent is present and same as element namespace write value without namespace
            if (ns.equals(parent.getNamespace())) {
                writer.writeCharacters(qname.getLocalName());
            } else {
                final String prefix = "x";
                writer.writeNamespace(prefix, ns.toString());
                writer.writeCharacters(prefix + ':' + qname.getLocalName());
            }

        } else {
            LOG.debug("Value of {}:{} is not a QName but {}", type.getQName().getNamespace(),
                    type.getQName().getLocalName(), value.getClass());
            writeObjectCharacters(writer, value);
        }
    }

    static void writeCharacters(final @NonNull XMLStreamWriter writer, final String text)
            throws XMLStreamException {
        if (!text.isEmpty()) {
            writer.writeCharacters(text);
        }
    }

    static void writeObjectCharacters(final @NonNull XMLStreamWriter writer, final @NonNull Object obj)
            throws XMLStreamException {
        writeCharacters(writer, obj.toString());
    }

    abstract @NonNull TypeDefinition<?> getBaseTypeForLeafRef(SchemaNode schemaNode, LeafrefTypeDefinition type);

    abstract void writeInstanceIdentifier(XMLStreamWriter writer, YangInstanceIdentifier value)
            throws XMLStreamException;
}
