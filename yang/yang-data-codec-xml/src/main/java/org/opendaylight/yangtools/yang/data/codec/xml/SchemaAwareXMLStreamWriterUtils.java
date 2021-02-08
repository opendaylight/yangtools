/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;

final class SchemaAwareXMLStreamWriterUtils extends XMLStreamWriterUtils implements EffectiveModelContextProvider {
    private final @NonNull EffectiveModelContext schemaContext;

    SchemaAwareXMLStreamWriterUtils(final EffectiveModelContext schemaContext) {
        this.schemaContext = requireNonNull(schemaContext);
    }

    @Override
    String encodeInstanceIdentifier(final ValueWriter writer, final YangInstanceIdentifier value)
            throws XMLStreamException {
        RandomPrefixInstanceIdentifierSerializer iiCodec = new RandomPrefixInstanceIdentifierSerializer(schemaContext,
            writer.getNamespaceContext());
        String serializedValue = iiCodec.serialize(value);

        for (Entry<XMLNamespace, String> e : iiCodec.getPrefixes()) {
            writer.writeNamespace(e.getValue(), e.getKey().toString());
        }

        return serializedValue;
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return schemaContext;
    }
}
