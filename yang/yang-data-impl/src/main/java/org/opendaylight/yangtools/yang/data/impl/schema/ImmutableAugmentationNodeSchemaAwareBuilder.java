/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
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
    public ImmutableAugmentationNodeBuilder withNodeIdentifier(InstanceIdentifier.AugmentationIdentifier nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }

    @Override
    public ImmutableAugmentationNodeBuilder withChildren(Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children) {
        return super.withChildren(children);
    }

    @Override
    public ImmutableAugmentationNodeBuilder withChild(DataContainerChild<?, ?> child) {
        Preconditions.checkArgument(schema.getDataChildByName(child.getNodeType()) != null, "Unknown child node: %s",
                child.getNodeType());
        return super.withChild(child);
    }

    public static ImmutableAugmentationNodeSchemaAwareBuilder get(AugmentationSchema schema) {
        return new ImmutableAugmentationNodeSchemaAwareBuilder(schema);
    }


}
