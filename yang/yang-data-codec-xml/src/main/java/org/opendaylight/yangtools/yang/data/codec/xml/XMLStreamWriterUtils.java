/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.annotations.VisibleForTesting;
import javax.xml.stream.XMLStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
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
     * Encode a value into a String in the context of a XML stream writer. This method assumes the start and end of
     * element is emitted by the caller.
     *
     * @param writer XML Stream writer
     * @param schemaNode Schema node that describes the value
     * @param type Schema type definition
     * @param value data value
     * @param parent module QName owning the leaf definition
     * @return String characters to be written
     * @throws XMLStreamException if an encoding problem occurs
     */
    String encodeValue(final @NonNull ValueWriter writer,final @NonNull SchemaNode schemaNode,
            final TypeDefinition<?> type, final @NonNull Object value, final QNameModule parent)
                    throws XMLStreamException {
        return type instanceof LeafrefTypeDefinition
                ? encodeValue(writer, getBaseTypeForLeafRef(schemaNode, (LeafrefTypeDefinition) type), value, parent)
                        : encodeValue(writer, type, value, parent);
    }

    /**
     * Write a value into a XML stream writer. This method assumes the start and end of element is
     * emitted by the caller.
     *
     * @param writer XML Stream writer
     * @param type data type. In case of leaf ref this should be the type of leaf being referenced
     * @param value data value
     * @param parent optional parameter of a module QName owning the leaf definition
     * @return String characters to be written
     * @throws XMLStreamException if an encoding problem occurs
     */
    private String encodeValue(final @NonNull ValueWriter writer, final @NonNull TypeDefinition<?> type,
            final @NonNull Object value, final QNameModule parent) throws XMLStreamException {
        if (type instanceof IdentityrefTypeDefinition) {
            return encode(writer, (IdentityrefTypeDefinition) type, value, parent);
        } else if (type instanceof InstanceIdentifierTypeDefinition) {
            return encode(writer, (InstanceIdentifierTypeDefinition) type, value);
        } else {
            return serialize(type, value);
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
    static String encode(final @NonNull ValueWriter writer, final @NonNull IdentityrefTypeDefinition type,
            final @NonNull Object value, final QNameModule parent) throws XMLStreamException {
        if (value instanceof QName) {
            final QName qname = (QName) value;

            //in case parent is present and same as element namespace write value without namespace
            if (qname.getNamespace().equals(parent.getNamespace())) {
                return qname.getLocalName();
            }

            final String ns = qname.getNamespace().toString();
            final String prefix = "x";
            writer.writeNamespace(prefix, ns);
            return prefix + ':' + qname.getLocalName();
        }

        final QName qname = type.getQName();
        LOG.debug("Value of {}:{} is not a QName but {}", qname.getNamespace(), qname.getLocalName(), value.getClass());
        return value.toString();
    }

    private String encode(final @NonNull ValueWriter writer, final @NonNull InstanceIdentifierTypeDefinition type,
            final @NonNull Object value) throws XMLStreamException {
        if (value instanceof YangInstanceIdentifier) {
            return encodeInstanceIdentifier(writer, (YangInstanceIdentifier)value);
        }

        final QName qname = type.getQName();
        LOG.warn("Value of {}:{} is not an InstanceIdentifier but {}", qname.getNamespace(), qname.getLocalName(),
            value.getClass());
        return value.toString();
    }

    abstract @NonNull TypeDefinition<?> getBaseTypeForLeafRef(SchemaNode schemaNode,
            @NonNull LeafrefTypeDefinition type);

    abstract String encodeInstanceIdentifier(@NonNull ValueWriter writer, YangInstanceIdentifier value)
            throws XMLStreamException;
}
