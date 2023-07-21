/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import java.util.List;
import java.util.Map.Entry;
import javax.xml.namespace.NamespaceContext;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;

final class InstanceIdentifierSerializer extends AbstractInstanceIdentifierCodec {
    private final NamespacePrefixes prefixes;

    InstanceIdentifierSerializer(final DataSchemaContextTree dataContextTree,  final NamespaceContext nsContext,
            final @Nullable PreferredPrefixes pref) {
        super(dataContextTree);
        prefixes = new NamespacePrefixes(nsContext, pref);
    }

    List<Entry<XMLNamespace, String>> emittedPrefixes() {
        return prefixes.emittedPrefixes();
    }

    @Override
    protected QNameModule moduleForPrefix(final String prefix) {
        // This is deserialize() path, we do not support that in this class
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected String prefixForNamespace(final XMLNamespace namespace) {
        return prefixes.encodePrefix(namespace);
    }
}
