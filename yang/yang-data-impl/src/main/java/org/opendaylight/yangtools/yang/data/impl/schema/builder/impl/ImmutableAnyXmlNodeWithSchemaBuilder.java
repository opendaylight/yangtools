/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.base.Optional;
import java.util.Map;
import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNodeWithSchema;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedValueAttrNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public final class ImmutableAnyXmlNodeWithSchemaBuilder extends ImmutableAnyXmlNodeBuilder {
    private DataSchemaNode schema;
    private Optional<NormalizedNode<?,?>> normalizedNode;

    private ImmutableAnyXmlNodeWithSchemaBuilder(final DataSchemaNode schema, final Optional<NormalizedNode<?,?>> normalizedNode) {
        this.schema = schema;
        this.normalizedNode = normalizedNode;
    }

    public static ImmutableAnyXmlNodeWithSchemaBuilder create(final DataSchemaNode schema, final Optional<NormalizedNode<?,?>> normalizedNode) {
        return new ImmutableAnyXmlNodeWithSchemaBuilder(schema, normalizedNode);
    }

    public DataSchemaNode getSchema() {
        return schema;
    }

    public Optional<NormalizedNode<?,?>> getNormalizedNode() {
        return normalizedNode;
    }

    @Override
    public AnyXmlNodeWithSchema build() {
        return new ImmutableXmlNodeWithSchema(getNodeIdentifier(), getValue(), getAttributes(),getSchema(),getNormalizedNode());
    }

    private static final class ImmutableXmlNodeWithSchema extends AbstractImmutableNormalizedValueAttrNode<NodeIdentifier, DOMSource> implements AnyXmlNodeWithSchema {
        private final DataSchemaNode schema;
        private Optional<NormalizedNode<?,?>> normalizedNode;

        ImmutableXmlNodeWithSchema(final NodeIdentifier nodeIdentifier, final DOMSource value, final Map<QName, String> attributes,
                final DataSchemaNode schema, final Optional<NormalizedNode<?,?>> normalizedNode) {
            super(nodeIdentifier, value, attributes);
            this.schema = schema;
            this.normalizedNode = normalizedNode;
        }

        @Override
        public DataSchemaNode getSchema() {
            return schema;
        }

        @Override
        public NormalizedNode<?, ?> getAsNormalizedNode() {
            if(! normalizedNode.isPresent()) {
                normalizedNode = Optional.of(null);//Utils.transformToNormalizedNodes(DOMSource, DataSchemaNode);
            }

            return normalizedNode.get();
        }
    }
}
