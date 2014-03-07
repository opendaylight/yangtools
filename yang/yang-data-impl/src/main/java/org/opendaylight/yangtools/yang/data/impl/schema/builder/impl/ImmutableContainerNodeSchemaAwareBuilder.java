/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public final class ImmutableContainerNodeSchemaAwareBuilder extends ImmutableContainerNodeBuilder {

    private final ContainerSchemaNode schema;

    private ImmutableContainerNodeSchemaAwareBuilder(ContainerSchemaNode schema) {
        super();
        this.schema = schema;
        super.withNodeIdentifier(new InstanceIdentifier.NodeIdentifier(schema.getQName()));
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> get(ContainerSchemaNode schema) {
        return new ImmutableContainerNodeSchemaAwareBuilder(schema);
    }

    @Override
    public DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> withNodeIdentifier(InstanceIdentifier.NodeIdentifier nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }

    @Override
    public DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> withChild(DataContainerChild<?, ?> child) {
        Preconditions.checkArgument(isKnownChild(child), "Unknown child node: %s", child.getNodeType());

//        FIXME make a cache for augmentation child sets in constructor

        Optional<AugmentationSchema> augmentChild = isAugmentChild(child);
        Preconditions
                .checkArgument(
                        augmentChild.isPresent() == false,
                        "Illegal node type, child nodes from augmentation are not permitted as direct children, must be wrapped in augmentation node, " +
                                "node: %s, from augmentation: %s",
                        child.getNodeType(), augmentChild);
        return super.withChild(child);
    }

    private Optional<AugmentationSchema> isAugmentChild(DataContainerChild<?, ?> child) {
        for (AugmentationSchema augmentationSchema : schema.getAvailableAugmentations()) {
            if(ImmutableAugmentationNodeSchemaAwareBuilder.getChildQNames(augmentationSchema).contains(child.getNodeType())) {
                return Optional.of(augmentationSchema);
            }
        }

        return Optional.absent();
    }

    private boolean isKnownChild(DataContainerChild<?, ?> child) {
        // check augmentation by comparing all child nodes
        if(child.getIdentifier() instanceof InstanceIdentifier.AugmentationIdentifier) {
            for (AugmentationSchema augmentationSchema : schema.getAvailableAugmentations()) {
                if(equalAugments(augmentationSchema, (InstanceIdentifier.AugmentationIdentifier) child.getIdentifier())) {
                    return true;
                }
            }
        // check regular child node
        } else {
            return schema.getDataChildByName(child.getNodeType()) != null;
        }

        return false;
    }

    @Override
    public ContainerNode build() {
        // TODO check when statements for augmentations
        return super.build();
    }

    // FIXME, need to compare Set of QNames(AugmentationIdentifier) with Set of DataSchemaNodes(AugmentationSchema)
    // throw away set is created just to compare
    // Or if augmentationSchemaNode had a QName, we would just compare a QName
    private boolean equalAugments(AugmentationSchema augmentationSchema, InstanceIdentifier.AugmentationIdentifier identifier) {
        return identifier.getPossibleChildNames().equals(ImmutableAugmentationNodeSchemaAwareBuilder.getChildQNames(augmentationSchema));
    }
}
