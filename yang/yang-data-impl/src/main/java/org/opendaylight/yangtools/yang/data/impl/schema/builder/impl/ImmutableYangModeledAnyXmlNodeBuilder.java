/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.YangModeledAnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerAttrNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

public final class ImmutableYangModeledAnyXmlNodeBuilder extends
        AbstractImmutableDataContainerNodeAttrBuilder<NodeIdentifier, YangModeledAnyXmlNode> {

    private final ContainerSchemaNode contentSchema;

    private ImmutableYangModeledAnyXmlNodeBuilder(final YangModeledAnyXmlSchemaNode yangModeledAnyXMLSchemaNode) {
        requireNonNull(yangModeledAnyXMLSchemaNode, "Yang modeled any xml node must not be null.");
        super.withNodeIdentifier(NodeIdentifier.create(yangModeledAnyXMLSchemaNode.getQName()));
        this.contentSchema = yangModeledAnyXMLSchemaNode.getSchemaOfAnyXmlData();
    }

    private ImmutableYangModeledAnyXmlNodeBuilder(final YangModeledAnyXmlSchemaNode yangModeledAnyXMLSchemaNode,
            final int sizeHint) {
        super(sizeHint);
        requireNonNull(yangModeledAnyXMLSchemaNode, "Yang modeled any xml node must not be null.");
        super.withNodeIdentifier(NodeIdentifier.create(yangModeledAnyXMLSchemaNode.getQName()));
        this.contentSchema = yangModeledAnyXMLSchemaNode.getSchemaOfAnyXmlData();
    }

    public static @NonNull DataContainerNodeAttrBuilder<NodeIdentifier, YangModeledAnyXmlNode> create(
            final YangModeledAnyXmlSchemaNode yangModeledAnyXMLSchemaNode) {
        return new ImmutableYangModeledAnyXmlNodeBuilder(yangModeledAnyXMLSchemaNode);
    }

    public static @NonNull DataContainerNodeAttrBuilder<NodeIdentifier, YangModeledAnyXmlNode> create(
            final YangModeledAnyXmlSchemaNode yangModeledAnyXMLSchemaNode, final int sizeHint) {
        return new ImmutableYangModeledAnyXmlNodeBuilder(yangModeledAnyXMLSchemaNode, sizeHint);
    }

    @Override
    public YangModeledAnyXmlNode build() {
        return new ImmutableYangModeledAnyXmlNode(getNodeIdentifier(), buildValue(), getAttributes(), contentSchema);
    }

    private static final class ImmutableYangModeledAnyXmlNode extends
            AbstractImmutableDataContainerAttrNode<NodeIdentifier> implements YangModeledAnyXmlNode {

        private final @NonNull ContainerSchemaNode contentSchema;

        ImmutableYangModeledAnyXmlNode(final NodeIdentifier nodeIdentifier,
                final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> value,
                final Map<QName, String> attributes, final ContainerSchemaNode contentSchema) {
            super(value, nodeIdentifier, attributes);
            this.contentSchema = requireNonNull(contentSchema, "Schema of yang modeled anyXml content cannot be null.");
        }

        @Override
        public ContainerSchemaNode getSchemaOfAnyXmlData() {
            return contentSchema;
        }
    }
}
