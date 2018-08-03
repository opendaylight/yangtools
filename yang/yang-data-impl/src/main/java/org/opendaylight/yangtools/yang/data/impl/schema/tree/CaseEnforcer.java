/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class CaseEnforcer implements Immutable {
    private final Map<NodeIdentifier, DataSchemaNode> children;
    private final Map<AugmentationIdentifier, AugmentationSchemaNode> augmentations;
    private final MandatoryLeafEnforcer enforcer;

    private CaseEnforcer(final Map<NodeIdentifier, DataSchemaNode> children,
                         final Map<AugmentationIdentifier, AugmentationSchemaNode> augmentations,
                         final MandatoryLeafEnforcer enforcer) {
        this.children = Preconditions.checkNotNull(children);
        this.augmentations = Preconditions.checkNotNull(augmentations);
        this.enforcer = Preconditions.checkNotNull(enforcer);
    }

    static CaseEnforcer forTree(final CaseSchemaNode schema, final DataTreeConfiguration treeConfig) {
        final TreeType type = treeConfig.getTreeType();
        final Builder<NodeIdentifier, DataSchemaNode> childrenBuilder = ImmutableMap.builder();
        final Builder<AugmentationIdentifier, AugmentationSchemaNode> augmentationsBuilder = ImmutableMap.builder();
        if (SchemaAwareApplyOperation.belongsToTree(type, schema)) {
            for (final DataSchemaNode child : schema.getChildNodes()) {
                if (SchemaAwareApplyOperation.belongsToTree(type, child)) {
                    childrenBuilder.put(NodeIdentifier.create(child.getQName()), child);
                }
            }
            for (final AugmentationSchemaNode augment : schema.getAvailableAugmentations()) {
                if (augment.getChildNodes().stream()
                        .anyMatch(child -> SchemaAwareApplyOperation.belongsToTree(type, child))) {
                    augmentationsBuilder.put(DataSchemaContextNode.augmentationIdentifierFrom(augment), augment);
                }
            }
        }

        final Map<NodeIdentifier, DataSchemaNode> children = childrenBuilder.build();
        final Map<AugmentationIdentifier, AugmentationSchemaNode> augmentations = augmentationsBuilder.build();
        return children.isEmpty() ? null
                : new CaseEnforcer(children, augmentations, MandatoryLeafEnforcer.forContainer(schema, treeConfig));
    }

    Set<Entry<NodeIdentifier, DataSchemaNode>> getChildEntries() {
        return children.entrySet();
    }

    Set<NodeIdentifier> getChildIdentifiers() {
        return children.keySet();
    }

    Set<Entry<AugmentationIdentifier, AugmentationSchemaNode>> getAugmentationEntries() {
        return augmentations.entrySet();
    }

    Set<AugmentationIdentifier> getAugmentationIdentifiers() {
        return augmentations.keySet();
    }

    Set<PathArgument> getAllChildIdentifiers() {
        return Sets.union(children.keySet(), augmentations.keySet());
    }

    void enforceOnTreeNode(final NormalizedNode<?, ?> normalizedNode) {
        enforcer.enforceOnData(normalizedNode);
    }
}
