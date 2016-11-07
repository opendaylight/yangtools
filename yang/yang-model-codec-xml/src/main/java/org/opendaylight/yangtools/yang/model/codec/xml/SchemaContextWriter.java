/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.codec.xml;

import com.google.common.annotations.Beta;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnknownTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

@Beta
public final class SchemaContextWriter {
    private static final String NAMESPACE = "https://opendaylight.org/yangtools/schemacontext/1.0.0";

    private SchemaContextWriter() {

    }

    public static void write(final XMLStreamWriter writer, final SchemaContext context) throws XMLStreamException {
        writer.writeStartElement(null, "schemaContext", NAMESPACE);

        for (TypeDefinition<?> def : context.getTypeDefinitions()) {
            writeTypedef(writer, def);
        }
        for (NotificationDefinition def : context.getNotifications()) {
            writeNotification(writer, def);
        }
        for (RpcDefinition def : context.getOperations()) {
            writeRpc(writer, def);
        }
        for (DataSchemaNode def : context.getDataDefinitions()) {
            writeData(writer, def);
        }

        writer.writeEndElement();
    }

    private static void writeData(final XMLStreamWriter writer, final DataSchemaNode def) throws XMLStreamException {
        // TODO Auto-generated method stub
    }

    private static void writeNotification(final XMLStreamWriter writer, final NotificationDefinition def)
            throws XMLStreamException {
        // TODO Auto-generated method stub
    }

    private static void writeRpc(final XMLStreamWriter writer, final RpcDefinition def) throws XMLStreamException {
        // TODO Auto-generated method stub
    }

    private static void writeTypedef(final XMLStreamWriter writer, final TypeDefinition<?> def)
            throws XMLStreamException {
        if (def instanceof BinaryTypeDefinition) {
            writeBinary(writer, (BinaryTypeDefinition) def);
        } else if (def instanceof BooleanTypeDefinition) {
            writeBoolean(writer, (BooleanTypeDefinition) def);
        } else if (def instanceof DecimalTypeDefinition) {
            writeDecimal(writer, (DecimalTypeDefinition) def);
        } else if (def instanceof EmptyTypeDefinition) {
            writeEmpty(writer, (EmptyTypeDefinition) def);
        } else if (def instanceof EnumTypeDefinition) {

        } else if (def instanceof IdentityrefTypeDefinition) {

        } else if (def instanceof IdentityTypeDefinition) {

        } else if (def instanceof InstanceIdentifierTypeDefinition) {

        } else if (def instanceof IntegerTypeDefinition) {

        } else if (def instanceof LeafrefTypeDefinition) {

        } else if (def instanceof StringTypeDefinition) {

        } else if (def instanceof UnionTypeDefinition) {

        } else if (def instanceof UnsignedIntegerTypeDefinition) {

        } else if (def instanceof UnknownTypeDefinition) {
            // Ignored on purpose
            return;
        } else {
            throw new IllegalArgumentException("Unhandled type " + def);
        }

        writer.writeEndElement();
    }

    private static void writeBinary(final XMLStreamWriter writer, final BinaryTypeDefinition def)
            throws XMLStreamException {

    }

    private static void writeBoolean(final XMLStreamWriter writer, final BooleanTypeDefinition def)
            throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    private static void writeDecimal(final XMLStreamWriter writer, final DecimalTypeDefinition def)
            throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    private static void writeEmpty(final XMLStreamWriter writer, final EmptyTypeDefinition def)
            throws XMLStreamException {
        // TODO Auto-generated method stub

    }

}

