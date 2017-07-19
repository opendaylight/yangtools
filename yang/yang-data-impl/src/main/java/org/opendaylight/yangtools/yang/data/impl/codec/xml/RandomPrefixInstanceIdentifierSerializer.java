/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import java.net.URI;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.util.AbstractStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Deprecated
final class RandomPrefixInstanceIdentifierSerializer extends AbstractStringInstanceIdentifierCodec {
    private final RandomPrefix prefixes = new RandomPrefix();
    private final DataSchemaContextTree schemaTree;

    RandomPrefixInstanceIdentifierSerializer(final SchemaContext ctx) {
        schemaTree = DataSchemaContextTree.from(ctx);
    }

    Iterable<Map.Entry<URI, String>> getPrefixes() {
        return prefixes.getPrefixes();
    }

    @Override
    protected String prefixForNamespace(@Nonnull final URI namespace) {
        return prefixes.encodePrefix(namespace);
    }

    @Override
    protected QName createQName(@Nonnull final String prefix, @Nonnull final String localName) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Nonnull
    @Override
    protected DataSchemaContextTree getDataContextTree() {
        return schemaTree;
    }
}
