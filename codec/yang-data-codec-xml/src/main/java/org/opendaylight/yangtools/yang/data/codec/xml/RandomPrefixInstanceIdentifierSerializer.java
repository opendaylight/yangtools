/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import java.util.Map.Entry;
import javax.xml.namespace.NamespaceContext;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.util.AbstractStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;

final class RandomPrefixInstanceIdentifierSerializer extends AbstractStringInstanceIdentifierCodec {
    private final @NonNull DataSchemaContextTree schemaTree;
    private final RandomPrefix prefixes;

    RandomPrefixInstanceIdentifierSerializer(final DataSchemaContextTree schemaTree, final NamespaceContext nsContext) {
        this.schemaTree = requireNonNull(schemaTree);
        prefixes = new RandomPrefix(nsContext);
    }

    Iterable<Entry<XMLNamespace, String>> getPrefixes() {
        return prefixes.getPrefixes();
    }

    @Override
    protected String prefixForNamespace(final XMLNamespace namespace) {
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
