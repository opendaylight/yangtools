/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;

import java.util.Collection;
import java.util.Collections;

/**
 * General validator for container like statements, e.g. container, list-entry, choice, augment
 */
public class DataNodeContainerValidator {

    private final DataNodeContainer schema;
    private Collection<AugmentationSchema> augmentations;

    public DataNodeContainerValidator(DataNodeContainer schema) {
        this.schema = schema;
        augmentations = schema instanceof AugmentationTarget ? ((AugmentationTarget) schema)
                .getAvailableAugmentations() : Collections.<AugmentationSchema> emptyList();
    }

    private boolean isKnownChild(InstanceIdentifier.PathArgument child) {
        // check augmentation by comparing all child nodes
        if(child instanceof InstanceIdentifier.AugmentationIdentifier) {
            for (AugmentationSchema augmentationSchema : augmentations) {
                if(equalAugments(augmentationSchema, (InstanceIdentifier.AugmentationIdentifier) child)) {
                    return true;
                }
            }
            // check regular child node
        } else {
            return schema.getDataChildByName(child.getNodeType()) != null;
        }

        return false;
    }

    private Optional<AugmentationSchema> isAugmentChild(InstanceIdentifier.PathArgument child) {
        for (AugmentationSchema augmentationSchema : augmentations) {
            if(ImmutableAugmentationNodeSchemaAwareBuilder.getChildQNames(augmentationSchema).contains(child.getNodeType())) {
                return Optional.of(augmentationSchema);
            }
        }

        return Optional.absent();
    }

    // FIXME, need to compare Set of QNames(AugmentationIdentifier) with Set of DataSchemaNodes(AugmentationSchema)
    // throw away set is created just to compare
    // Or if augmentationSchemaNode had a QName, we would just compare a QName
    private boolean equalAugments(AugmentationSchema augmentationSchema, InstanceIdentifier.AugmentationIdentifier identifier) {
        return identifier.getPossibleChildNames().equals(ImmutableAugmentationNodeSchemaAwareBuilder.getChildQNames(augmentationSchema));
    }

    public void validateChild(InstanceIdentifier.PathArgument child) {
        Preconditions.checkArgument(isKnownChild(child), "Unknown child node: %s, does not belong to: %s", child.getNodeType(), schema);

        // FIXME make a cache for augmentation child sets in constructor
        Optional<AugmentationSchema> augmentChild = isAugmentChild(child);
        Preconditions.checkArgument(
                    augmentChild.isPresent() == false,
                    "Illegal node type, child nodes from augmentation are not permitted as direct children, must be wrapped in augmentation node, "
                            + "node: %s, from augmentation: %s, in parent: %s", child.getNodeType(), augmentChild, schema);
    }
}
