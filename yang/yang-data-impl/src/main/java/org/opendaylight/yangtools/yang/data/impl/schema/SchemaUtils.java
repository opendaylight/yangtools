/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

// FIXME: 7.0.0: find a better place for these methods
@Deprecated
public final class SchemaUtils {
    private SchemaUtils() {
        // Hidden on purpose
    }

    public static Optional<CaseSchemaNode> detectCase(final ChoiceSchemaNode schema, final DataContainerChild child) {
        for (final CaseSchemaNode choiceCaseNode : schema.getCases()) {
            if (child instanceof AugmentationNode
                    && belongsToCaseAugment(choiceCaseNode, (AugmentationIdentifier) child.getIdentifier())
                    || choiceCaseNode.findDataChildByName(child.getNodeType()).isPresent()) {
                return Optional.of(choiceCaseNode);
            }
        }

        return Optional.empty();
    }

    private static boolean belongsToCaseAugment(final CaseSchemaNode caseNode,
            final AugmentationIdentifier childToProcess) {
        for (final AugmentationSchemaNode augmentationSchema : caseNode.getAvailableAugmentations()) {

            final Set<QName> currentAugmentChildNodes = new HashSet<>();
            for (final DataSchemaNode dataSchemaNode : augmentationSchema.getChildNodes()) {
                currentAugmentChildNodes.add(dataSchemaNode.getQName());
            }

            if (childToProcess.getPossibleChildNames().equals(currentAugmentChildNodes)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Tries to find in {@code parent} which is dealed as augmentation target node with QName as {@code child}. If such
     * node is found then it is returned, else null.
     *
     * @param parent parent node
     * @param child child node
     * @return augmentation schema
     */
    public static AugmentationSchemaNode findCorrespondingAugment(final DataSchemaNode parent,
            final DataSchemaNode child) {
        if (!(parent instanceof AugmentationTarget) || parent instanceof ChoiceSchemaNode) {
            return null;
        }

        for (final AugmentationSchemaNode augmentation : ((AugmentationTarget) parent).getAvailableAugmentations()) {
            final Optional<DataSchemaNode> childInAugmentation = augmentation.findDataChildByName(child.getQName());
            if (childInAugmentation.isPresent()) {
                return augmentation;
            }
        }
        return null;
    }
}
