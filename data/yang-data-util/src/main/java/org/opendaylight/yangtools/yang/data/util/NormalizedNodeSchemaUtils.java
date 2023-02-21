/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
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

// FIXME: 8.0.0: re-examine usefulness of these methods
@Beta
public final class NormalizedNodeSchemaUtils {
    private NormalizedNodeSchemaUtils() {
        // Hidden on purpose
    }

    public static Optional<CaseSchemaNode> detectCase(final ChoiceSchemaNode schema, final DataContainerChild child) {
        if (child instanceof AugmentationNode augment) {
            return detectCase(schema, augment);
        }

        final QName childId = child.getIdentifier().getNodeType();
        for (final CaseSchemaNode choiceCaseNode : schema.getCases()) {
            if (choiceCaseNode.dataChildByName(childId) != null) {
                return Optional.of(choiceCaseNode);
            }
        }
        return Optional.empty();
    }

    public static Optional<CaseSchemaNode> detectCase(final ChoiceSchemaNode schema, final AugmentationNode child) {
        final AugmentationIdentifier childId = child.getIdentifier();
        for (final CaseSchemaNode choiceCaseNode : schema.getCases()) {
            if (belongsToCaseAugment(choiceCaseNode, childId)) {
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
        if (parent instanceof AugmentationTarget target && !(parent instanceof ChoiceSchemaNode)) {
            for (final AugmentationSchemaNode augmentation : target.getAvailableAugmentations()) {
                if (augmentation.dataChildByName(child.getQName()) != null) {
                    return augmentation;
                }
            }
        }
        return null;
    }
}
