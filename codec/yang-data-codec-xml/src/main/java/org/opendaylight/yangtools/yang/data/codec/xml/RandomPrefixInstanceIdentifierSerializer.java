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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.util.AbstractModuleStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;

final class RandomPrefixInstanceIdentifierSerializer extends AbstractModuleStringInstanceIdentifierCodec {
    private final @NonNull DataSchemaContextTree schemaTree;
    private final RandomPrefix prefixes;

    RandomPrefixInstanceIdentifierSerializer(final @NonNull EffectiveModelContext schemaContext,
            final NamespaceContext nsContext) {
        schemaTree = DataSchemaContextTree.from(schemaContext);
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
    protected Module moduleForPrefix(final String prefix) {
        final var prefixedNS = prefixes.context().getNamespaceURI(prefix);
        final var modules = schemaTree.getEffectiveModelContext().findModules(XMLNamespace.of(prefixedNS)).iterator();
        return modules.hasNext() ? modules.next() : null;
    }

    @Override
    protected DataSchemaContextTree getDataContextTree() {
        return schemaTree;
    }
}
