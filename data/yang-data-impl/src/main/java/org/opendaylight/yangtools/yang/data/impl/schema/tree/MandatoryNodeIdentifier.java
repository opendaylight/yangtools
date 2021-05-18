/*
 * Copyright (c) 2021 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import static org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class MandatoryNodeIdentifier {
    // storing both Identifiers for mandatory leafs added via augmentation. Related issue: YANGTOOLS-1276
    private final YangInstanceIdentifier directId;
    private final YangInstanceIdentifier augmentedId;

    private MandatoryNodeIdentifier(final YangInstanceIdentifier directId,
        final YangInstanceIdentifier augmentedId) {
        this.directId = directId;
        this.augmentedId = augmentedId;
    }

    static MandatoryNodeIdentifier fromDirectMandatoryNode(final YangInstanceIdentifier parentId,
        final DataSchemaNode childNode) {
        return new MandatoryNodeIdentifier(buildChildYii(parentId, childNode),null);
    }

    static MandatoryNodeIdentifier fromAugmentedMandatoryNode(final DataNodeContainer schema,
        final YangInstanceIdentifier parentId, final DataSchemaNode childNode) {

        final Collection<? extends AugmentationSchemaNode> augmentations =
            ((AugmentationTarget) schema).getAvailableAugmentations();
        for (AugmentationSchemaNode augmentation : augmentations) {
            if (augmentation.findDataChildByName(childNode.getQName()).isPresent()) {
                final Set<QName> augQnames =
                    augmentation.getChildNodes().stream().map(DataSchemaNode::getQName)
                        .collect(Collectors.toSet());
                final YangInstanceIdentifier augmentedChildId = parentId.node(AugmentationIdentifier
                    .create(augQnames)).toOptimized();
                return new MandatoryNodeIdentifier(buildChildYii(parentId, childNode),augmentedChildId);
            }
        }
        throw new IllegalArgumentException(String.format("Node %s is marked as augmenting but is not present in the "
            + "schema of %s", childNode.getQName(), schema));
    }

    private static YangInstanceIdentifier buildChildYii(final YangInstanceIdentifier parentId,
        final DataSchemaNode childNode) {
        return parentId.node(NodeIdentifier.create(childNode.getQName())).toOptimized();
    }

    YangInstanceIdentifier getDirectId() {
        return directId;
    }

    YangInstanceIdentifier getAugmentedId() {
        return augmentedId;
    }
}
