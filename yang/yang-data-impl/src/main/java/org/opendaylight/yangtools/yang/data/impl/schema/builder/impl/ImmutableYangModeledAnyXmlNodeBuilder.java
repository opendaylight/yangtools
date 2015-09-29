/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerAttrNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class ImmutableYangModeledAnyXmlNodeBuilder extends
        AbstractImmutableDataContainerNodeAttrBuilder<NodeIdentifier, YangModeledAnyXmlNode> {
    private DataSchemaNode schema;

    private ImmutableYangModeledAnyXmlNodeBuilder(final DataSchemaNode schema) {
        this.schema = schema;
    }

    public static DataContainerNodeAttrBuilder<NodeIdentifier, YangModeledAnyXmlNode> create(final DataSchemaNode schema) {
        return new ImmutableYangModeledAnyXmlNodeBuilder(schema);
    }

    @Override
    public YangModeledAnyXmlNode build() {
        return new ImmutableYangModeledXmlNode(getNodeIdentifier(), buildValue(), getAttributes(), getContentSchema());
    }

    public DataSchemaNode getContentSchema() {
        return schema;
    }

    private static final class ImmutableYangModeledXmlNode extends
            AbstractImmutableDataContainerAttrNode<NodeIdentifier> implements YangModeledAnyXmlNode {
        private final DataSchemaNode schema;

        ImmutableYangModeledXmlNode(final NodeIdentifier nodeIdentifier,
                final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> children,
                final Map<QName, String> attributes, final DataSchemaNode schema) {
            super(children, nodeIdentifier, attributes);
            this.schema = schema;
        }

        @Override
        public DataSchemaNode getContentSchema() {
            return schema;
        }
    }
}
