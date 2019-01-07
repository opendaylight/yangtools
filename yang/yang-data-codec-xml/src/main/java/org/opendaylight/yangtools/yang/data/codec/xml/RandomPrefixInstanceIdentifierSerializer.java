/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import java.net.URI;
import java.util.Map.Entry;
import javax.xml.namespace.NamespaceContext;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.util.AbstractStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class RandomPrefixInstanceIdentifierSerializer extends AbstractStringInstanceIdentifierCodec {
    private final @NonNull DataSchemaContextTree schemaTree;
    private final RandomPrefix prefixes;

    RandomPrefixInstanceIdentifierSerializer(final @NonNull SchemaContext schemaContext,
        final NamespaceContext nsContext) {
        schemaTree = DataSchemaContextTree.from(schemaContext);
        prefixes = new RandomPrefix(nsContext);
    }

    Iterable<Entry<URI, String>> getPrefixes() {
        return prefixes.getPrefixes();
    }

    @Override
    protected String prefixForNamespace(final URI namespace) {
        return prefixes.encodePrefix(namespace);
    }

    @Override
    protected QName createQName(final String prefix, final String localName) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected DataSchemaContextTree getDataContextTree() {
        return schemaTree;
    }
}
