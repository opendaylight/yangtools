/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import java.util.Map.Entry;
import javax.xml.namespace.NamespaceContext;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.LeafrefResolver;

final class InstanceIdentifierSerializer extends AbstractInstanceIdentifierCodec {
    private final RandomPrefix prefixes;

    InstanceIdentifierSerializer(final DataSchemaContextTree dataContextTree, final NamespaceContext nsContext) {
        super(dataContextTree);
        prefixes = new RandomPrefix(nsContext);
    }

    Iterable<Entry<XMLNamespace, String>> getPrefixes() {
        return prefixes.getPrefixes();
    }

    @Override
    protected ModuleEffectiveStatement moduleForPrefix(final String prefix) {
        // This is deserialize() path, we do not support that in this class
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected String prefixForNamespace(final XMLNamespace namespace) {
        return prefixes.encodePrefix(namespace);
    }

    @Override
    protected Object deserializeKeyValue(final DataSchemaNode schemaNode, final LeafrefResolver resolver,
            final String value) {
        // This is deserialize() path, we do not support that in this class
        throw new UnsupportedOperationException("Not implemented");
    }
}
