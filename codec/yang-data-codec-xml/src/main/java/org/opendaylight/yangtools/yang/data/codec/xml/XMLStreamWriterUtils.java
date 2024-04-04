/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.stream.XMLStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.spi.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for bridging JAXP Stream and YANG Data APIs. Note that the definition of this class
 * by no means final and subject to change as more functionality is centralized here.
 */
abstract class XMLStreamWriterUtils {
    private static final Logger LOG = LoggerFactory.getLogger(XMLStreamWriterUtils.class);
    /**
     * Warn-once set of identityref nodes which were found to be normalized to a different object than a QName.
     */
    private static final Set<QName> IDENTITYREF_WARNED = ConcurrentHashMap.newKeySet();

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
    String encodeValue(final @NonNull ValueWriter writer, final @NonNull TypeDefinition<?> type,
            final @NonNull Object value, final QNameModule parent) throws XMLStreamException {
        if (type instanceof IdentityrefTypeDefinition identityref) {
            return encode(writer, identityref, value, parent);
        } else if (type instanceof InstanceIdentifierTypeDefinition instanceIdentifier) {
            return encode(writer, instanceIdentifier, value);
        } else if (value instanceof QName qname && isIdentityrefUnion(type)) {
            // Ugly special-case form unions with identityrefs
            return encode(writer, qname, parent);
        } else if (value instanceof YangInstanceIdentifier instanceIdentifier && isInstanceIdentifierUnion(type)) {
            return encodeInstanceIdentifier(writer, instanceIdentifier);
        } else {
            return serialize(type, value);
        }
    }

    private static boolean isIdentityrefUnion(final TypeDefinition<?> type) {
        if (type instanceof UnionTypeDefinition union) {
            for (var subtype : union.getTypes()) {
                if (subtype instanceof IdentityrefTypeDefinition || isIdentityrefUnion(subtype)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInstanceIdentifierUnion(final TypeDefinition<?> type) {
        if (type instanceof UnionTypeDefinition union) {
            for (var subtype : union.getTypes()) {
                if (subtype instanceof InstanceIdentifierTypeDefinition || isInstanceIdentifierUnion(subtype)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String serialize(final @NonNull TypeDefinition<?> type, final @NonNull Object value) {
        final var codec = TypeDefinitionAwareCodec.from(type);
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
    static String encode(final @NonNull ValueWriter writer, final @NonNull QName qname, final QNameModule parent)
            throws XMLStreamException {
        //in case parent is present and same as element namespace write value without namespace
        if (qname.getNamespace().equals(parent.namespace())) {
            return qname.getLocalName();
        }

        final var ns = qname.getNamespace().toString();
        final var prefix = "x";
        writer.writeNamespace(prefix, ns);
        return prefix + ':' + qname.getLocalName();
    }

    private static String encode(final @NonNull ValueWriter writer, final @NonNull IdentityrefTypeDefinition type,
            final @NonNull Object value, final QNameModule parent) throws XMLStreamException {
        if (value instanceof QName qname) {
            return encode(writer, qname, parent);
        }

        final var qname = type.getQName();
        if (IDENTITYREF_WARNED.add(qname)) {
            LOG.warn("Value of {}:{} is not a QName but {}. Please the source of this data", qname.getNamespace(),
                qname.getLocalName(), value.getClass(), new Throwable());
        }
        return value.toString();
    }

    private String encode(final @NonNull ValueWriter writer, final @NonNull InstanceIdentifierTypeDefinition type,
            final @NonNull Object value) throws XMLStreamException {
        if (value instanceof YangInstanceIdentifier instanceIdentifier) {
            return encodeInstanceIdentifier(writer, instanceIdentifier);
        }

        final var qname = type.getQName();
        LOG.warn("Value of {}:{} is not an InstanceIdentifier but {}", qname.getNamespace(), qname.getLocalName(),
            value.getClass());
        return value.toString();
    }

    abstract String encodeInstanceIdentifier(@NonNull ValueWriter writer, YangInstanceIdentifier value)
            throws XMLStreamException;
}
