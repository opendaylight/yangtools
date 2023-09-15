/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import javax.xml.stream.XMLStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;

final class SchemaAwareXMLStreamWriterUtils extends XMLStreamWriterUtils {
    private final @NonNull DataSchemaContextTree schemaTree;
    private final @Nullable PreferredPrefixes pref;

    SchemaAwareXMLStreamWriterUtils(final DataSchemaContextTree schemaTree, final @Nullable PreferredPrefixes pref) {
        this.schemaTree = requireNonNull(schemaTree);
        this.pref = pref;
    }

    @NonNull DataSchemaContextTree schemaTree() {
        return schemaTree;
    }

    @Override
    String encode(final ValueWriter writer, final YangInstanceIdentifier value) throws XMLStreamException {
        final var serializer = new InstanceIdentifierSerializer(schemaTree, writer.getNamespaceContext(), pref);
        final var str = serializer.serialize(value);
        for (var entry : serializer.emittedPrefixes()) {
            writer.writeNamespace(entry.getValue(), entry.getKey().toString());
        }
        return str;
    }
}
