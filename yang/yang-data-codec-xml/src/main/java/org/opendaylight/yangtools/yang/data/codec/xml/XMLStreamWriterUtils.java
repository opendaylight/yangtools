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
import javax.xml.stream.XMLStreamException;
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
    void writeValue(final @NonNull ValueWriter writer, final @NonNull SchemaNode schemaNode,
            final @NonNull Object value, final QNameModule parent) throws XMLStreamException {
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
    private void writeValue(final @NonNull ValueWriter writer, final @NonNull TypeDefinition<?> type,
            final @NonNull Object value, final QNameModule parent) throws XMLStreamException {
        if (type instanceof IdentityrefTypeDefinition) {
            write(writer, (IdentityrefTypeDefinition) type, value, parent);
        } else if (type instanceof InstanceIdentifierTypeDefinition) {
            write(writer, (InstanceIdentifierTypeDefinition) type, value);
        } else {
            writer.writeCharacters(serialize(type, value));
        }
    }

    private static String serialize(final @NonNull TypeDefinition<?> type, final @NonNull Object value) {
        final TypeDefinitionAwareCodec<Object, ?> codec = TypeDefinitionAwareCodec.from(type);
        if (codec == null) {
            LOG.warn("Failed to find codec for {}, falling back to using stream", type);
            return value.toString();
        }

        try {
            return codec.serialize(value);
        } catch (ClassCastException e) {
            LOG.warn("Provided node value {} did not have type {} required by mapping. Using stream instead.",
                value, type, e);
            return value.toString();
        }
    }

    @VisibleForTesting
    static void write(final @NonNull ValueWriter writer, final @NonNull IdentityrefTypeDefinition type,
            final @NonNull Object value, final QNameModule parent) throws XMLStreamException {
        if (value instanceof QName) {
            final QName qname = (QName) value;

            //in case parent is present and same as element namespace write value without namespace
            if (qname.getNamespace().equals(parent.getNamespace())) {
                writer.writeCharacters(qname.getLocalName());
            } else {
                final String ns = qname.getNamespace().toString();
                final String prefix = "x";
                writer.writeNamespace(prefix, ns);
                writer.writeCharacters(prefix + ':' + qname.getLocalName());
            }
        } else {
            final QName qname = type.getQName();
            LOG.debug("Value of {}:{} is not a QName but {}", qname.getNamespace(), qname.getLocalName(),
                value.getClass());
            writer.writeToStringCharacters(value);
        }
    }

    private void write(final @NonNull ValueWriter writer, final @NonNull InstanceIdentifierTypeDefinition type,
            final @NonNull Object value) throws XMLStreamException {
        if (value instanceof YangInstanceIdentifier) {
            writeInstanceIdentifier(writer, (YangInstanceIdentifier)value);
        } else {
            final QName qname = type.getQName();
            LOG.warn("Value of {}:{} is not an InstanceIdentifier but {}", qname.getNamespace(), qname.getLocalName(),
                value.getClass());
            writer.writeToStringCharacters(value);
        }
    }

    abstract @NonNull TypeDefinition<?> getBaseTypeForLeafRef(SchemaNode schemaNode,
            @NonNull LeafrefTypeDefinition type);

    abstract void writeInstanceIdentifier(@NonNull ValueWriter writer, YangInstanceIdentifier value)
            throws XMLStreamException;
}
