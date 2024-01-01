/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import javax.xml.namespace.NamespaceContext;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.util.LeafrefResolver;

final class InstanceIdentifierDeserializer extends AbstractInstanceIdentifierCodec {
    private final @NonNull XmlCodecFactory codecFactory;
    private final @NonNull NamespaceContext namespaceContext;

    InstanceIdentifierDeserializer(final DataSchemaContextTree dataContextTree, final XmlCodecFactory codecFactory,
            final NamespaceContext namespaceContext) {
        super(dataContextTree);
        this.codecFactory = requireNonNull(codecFactory);
        this.namespaceContext = requireNonNull(namespaceContext);
    }

    @Override
    protected QNameModule moduleForPrefix(final String prefix) {
        final var modules = codecFactory.modelContext()
            .findModuleStatements(XMLNamespace.of(namespaceContext.getNamespaceURI(prefix)))
            .iterator();
        return modules.hasNext() ? modules.next().localQNameModule() : null;
    }

    @Override
    protected String prefixForNamespace(final XMLNamespace namespace) {
        // This is serialize() path, we do not support that in this class
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected Object deserializeKeyValue(final DataSchemaNode schemaNode, final LeafrefResolver resolver,
            final String value) {
        requireNonNull(schemaNode, "schemaNode cannot be null");
        final XmlCodec<?> objectXmlCodec;
        if (schemaNode instanceof LeafSchemaNode leafSchemaNode) {
            objectXmlCodec = codecFactory.codecFor(leafSchemaNode, resolver);
        } else if (schemaNode instanceof LeafListSchemaNode leafListSchemaNode) {
            objectXmlCodec = codecFactory.codecFor(leafListSchemaNode, resolver);
        } else {
            throw new IllegalArgumentException("schemaNode " + schemaNode
                    + " must be of type LeafSchemaNode or LeafListSchemaNode");
        }
        return objectXmlCodec.parseValue(namespaceContext, value);
    }
}
