/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A path to a descendant which must exist. This really should be equivalent to YangInstanceIdentifier, but for now we
 * need to deal with up to two possible paths -- one with AugmentationIdentifiers and one without.
 */
// FIXME: 8.0.0: remove this structure and just keep the 'path' YangInstanceIdentifier
final class MandatoryDescendant implements Immutable {
    private static final Logger LOG = LoggerFactory.getLogger(MandatoryDescendant.class);

    // Correctly-nested path, mirroring Augmentation and choice/case structure
    private final @NonNull YangInstanceIdentifier path;
    // Legacy trivial path,
    private final @Nullable YangInstanceIdentifier legacyPath;

    private MandatoryDescendant(final YangInstanceIdentifier path, final YangInstanceIdentifier legacyPath) {
        this.path = requireNonNull(path);
        this.legacyPath = legacyPath;
    }

    static @NonNull MandatoryDescendant create(final YangInstanceIdentifier parentId,
            final DataNodeContainer parentSchema, final DataSchemaNode childSchema) {
        return new MandatoryDescendant(parentId.node(NodeIdentifier.create(childSchema.getQName())).toOptimized(),
            null);
    }

    void enforceOnData(final NormalizedNode data) {
        // Find try primary path first ...
        if (NormalizedNodes.findNode(data, path).isPresent()) {
            return;
        }
        // ... if we have a legacy path, try that as well ...
        if (legacyPath != null) {
            if (NormalizedNodes.findNode(data, legacyPath).isPresent()) {
                // .. this should not really be happening ...
                LOG.debug("Found {} at alternate path {}", path, legacyPath);
                return;
            }
        }

        // ... not found, report the error
        throw new IllegalArgumentException(String.format("Node %s is missing mandatory descendant %s",
            data.getIdentifier(), path));
    }

    static AugmentationSchemaNode getAugIdentifierOfChild(final DataNodeContainer parent, final DataSchemaNode child) {
        verify(parent instanceof AugmentationTarget,
            "Unexpected augmenting child %s in non-target parent %s", child, parent);
        return ((AugmentationTarget) parent).getAvailableAugmentations().stream()
            .filter(augment -> augment.findDataChildByName(child.getQName()).isPresent())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format(
                "Node %s is marked as augmenting but is not present in the schema of %s", child.getQName(), parent)));
    }

    @Override
    public String toString() {
        return legacyPath == null ? path.toString() : "(" + path + " || " + legacyPath + ")";
    }
}
