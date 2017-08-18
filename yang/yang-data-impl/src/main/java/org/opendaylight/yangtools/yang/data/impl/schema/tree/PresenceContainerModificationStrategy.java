/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * Presence container modification strategy. In addition to {@link ContainerModificationStrategy} it also enforces
 * presence of mandatory leaves.
 */
final class PresenceContainerModificationStrategy extends ContainerModificationStrategy {
    private final MandatoryLeafEnforcer enforcer;

    PresenceContainerModificationStrategy(final ContainerSchemaNode schemaNode, final DataTreeConfiguration treeConfig) {
        super(schemaNode, treeConfig);
        enforcer = MandatoryLeafEnforcer.forContainer(schemaNode, treeConfig);
    }

    @Override
    void verifyStructure(final NormalizedNode<?, ?> writtenValue, final boolean verifyChildren) {
        super.verifyStructure(writtenValue, verifyChildren);
        if (verifyChildren) {
            enforcer.enforceOnData(writtenValue);
        }
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        final TreeNode ret = super.applyMerge(modification, currentMeta, version);
        enforcer.enforceOnTreeNode(ret);
        return ret;
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification, final Optional<TreeNode> currentMeta,
            final Version version) {
        final TreeNode ret = super.applyWrite(modification, currentMeta, version);
        enforcer.enforceOnTreeNode(ret);
        return ret;
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        final TreeNode ret = super.applyTouch(modification, currentMeta, version);
        enforcer.enforceOnTreeNode(ret);
        return ret;
    }
}
