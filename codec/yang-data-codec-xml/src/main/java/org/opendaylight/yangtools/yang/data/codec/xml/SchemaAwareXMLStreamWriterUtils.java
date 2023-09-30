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
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

final class SchemaAwareXMLStreamWriterUtils extends XMLStreamWriterUtils {
    private final @NonNull EffectiveModelContext modelContext;
    private final @Nullable PreferredPrefixes pref;

    SchemaAwareXMLStreamWriterUtils(final EffectiveModelContext modelContext, final @Nullable PreferredPrefixes pref) {
        this.modelContext = requireNonNull(modelContext);
        this.pref = pref;
    }

    @NonNull EffectiveModelContext modelContext() {
        return modelContext;
    }

    @Override
    String encodeInstanceIdentifier(final ValueWriter writer, final YangInstanceIdentifier value)
            throws XMLStreamException {
        final var serializer = new InstanceIdentifierSerializer(DataSchemaContextTree.from(modelContext),
            writer.getNamespaceContext(), pref);
        final var str = serializer.serialize(value);
        for (var entry : serializer.emittedPrefixes()) {
            writer.writeNamespace(entry.getValue(), entry.getKey().toString());
        }
        return str;
    }
}
