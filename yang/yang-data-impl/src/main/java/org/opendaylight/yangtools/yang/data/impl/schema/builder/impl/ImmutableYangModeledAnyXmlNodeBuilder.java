/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.base.Preconditions;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedValueAttrNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;

public class ImmutableYangModeledAnyXmlNodeBuilder extends
        AbstractImmutableNormalizedNodeBuilder<NodeIdentifier, DataContainerChild<?, ?>, YangModeledAnyXmlNode> {

    private final DataSchemaNode contentSchema;

    private ImmutableYangModeledAnyXmlNodeBuilder(final YangModeledAnyXmlSchemaNode yangModeledAnyXMLSchemaNode) {
        Preconditions.checkNotNull(yangModeledAnyXMLSchemaNode, "Yang modeled any xml node must not be null.");
        super.withNodeIdentifier(NodeIdentifier.create(yangModeledAnyXMLSchemaNode.getQName()));
        this.contentSchema = yangModeledAnyXMLSchemaNode.getSchemaOfAnyXmlData();
    }

    public static NormalizedNodeAttrBuilder<NodeIdentifier, DataContainerChild<?, ?>, YangModeledAnyXmlNode> create(
            final YangModeledAnyXmlSchemaNode yangModeledAnyXMLSchemaNode) {
        return new ImmutableYangModeledAnyXmlNodeBuilder(yangModeledAnyXMLSchemaNode);
    }

    @Override
    public YangModeledAnyXmlNode build() {
        return new ImmutableYangModeledAnyXmlNode(getNodeIdentifier(), getValue(), getAttributes(), contentSchema);
    }

    private static final class ImmutableYangModeledAnyXmlNode extends
            AbstractImmutableNormalizedValueAttrNode<NodeIdentifier, DataContainerChild<?, ?>> implements
            YangModeledAnyXmlNode {

        private final DataSchemaNode contentSchema;

        ImmutableYangModeledAnyXmlNode(final NodeIdentifier nodeIdentifier, final DataContainerChild<?, ?> value,
                final Map<QName, String> attributes, final DataSchemaNode contentSchema) {
            super(nodeIdentifier, value, attributes);
            this.contentSchema = contentSchema;
        }

        @Override
        public DataSchemaNode getSchemaOfAnyXmlData() {
            return contentSchema;
        }
    }
}
