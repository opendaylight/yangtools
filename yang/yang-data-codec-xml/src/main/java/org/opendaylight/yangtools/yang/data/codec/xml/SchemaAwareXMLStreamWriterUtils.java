/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

final class SchemaAwareXMLStreamWriterUtils extends XMLStreamWriterUtils {
    private final SchemaContext schemaContext;

    SchemaAwareXMLStreamWriterUtils(final SchemaContext schemaContext) {
        this.schemaContext = requireNonNull(schemaContext);
    }

    @Override
    TypeDefinition<?> getBaseTypeForLeafRef(final SchemaNode schemaNode, final LeafrefTypeDefinition type) {
        final TypeDefinition<?> ret = SchemaContextUtil.getBaseTypeForLeafRef(type, schemaContext, schemaNode);
        return verifyNotNull(ret, "Unable to find base type for leafref node '%s'.", schemaNode.getPath());
    }

    @Override
    void writeInstanceIdentifier(final XMLStreamWriter writer, final YangInstanceIdentifier value)
            throws XMLStreamException {
        RandomPrefixInstanceIdentifierSerializer iiCodec = new RandomPrefixInstanceIdentifierSerializer(schemaContext,
            writer.getNamespaceContext());
        String serializedValue = iiCodec.serialize(value);

        for (Entry<URI, String> e : iiCodec.getPrefixes()) {
            writer.writeNamespace(e.getValue(), e.getKey().toString());
        }

        writer.writeCharacters(serializedValue);
    }
}
