/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class ImmutableAugmentationNodeSchemaAwareBuilder extends ImmutableAugmentationNodeBuilder {

    private final AugmentationSchema schema;

    protected ImmutableAugmentationNodeSchemaAwareBuilder(AugmentationSchema schema) {
        super();
        this.schema = schema;
        // TODO no QName for augmentation
        super.withNodeIdentifier(new InstanceIdentifier.AugmentationIdentifier(null, getChildQNames(schema)));
    }

    static Set<QName> getChildQNames(AugmentationSchema schema) {
        Set<QName> qnames = Sets.newHashSet();

        for (DataSchemaNode dataSchemaNode : schema.getChildNodes()) {
            qnames.add(dataSchemaNode.getQName());
        }

        return qnames;
    }

    @Override
    public DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> withNodeIdentifier(InstanceIdentifier.AugmentationIdentifier nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }

    @Override
    public DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> withChild(DataContainerChild<?, ?> child) {
        Preconditions.checkArgument(schema.getDataChildByName(child.getNodeType()) != null, "Unknown child node: %s",
                child.getNodeType());
        return super.withChild(child);
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> get(AugmentationSchema schema) {
        return new ImmutableAugmentationNodeSchemaAwareBuilder(schema);
    }


}
