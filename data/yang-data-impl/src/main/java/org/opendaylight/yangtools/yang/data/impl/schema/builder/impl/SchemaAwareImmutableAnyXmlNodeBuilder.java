/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;

final class SchemaAwareImmutableAnyXmlNodeBuilder extends ImmutableAnyXmlNodeBuilder {
    SchemaAwareImmutableAnyXmlNodeBuilder(final AnyxmlSchemaNode schema) {
        super.withNodeIdentifier(NodeIdentifier.create(schema.getQName()));
    }

    @Override
    public ImmutableAnyXmlNodeBuilder withValue(final DOMSource withValue) {
        return super.withValue(withValue);
    }

    @Override
    public NormalizedNodeBuilder<NodeIdentifier, DOMSource, DOMSourceAnyxmlNode> withNodeIdentifier(
            final NodeIdentifier withNodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
