/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * Structural containers are special in that they appear when implied by child
 * nodes and disappear whenever they are empty.
 *
 * FIXME: BUG-2399: right now this behaves just like a presence container
 */
final class StructuralContainerModificationStrategy extends AbstractContainerModificationStrategy {
    private static final TreeNode ABSENT_META = new TreeNode() {
        @Override
        public PathArgument getIdentifier() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<TreeNode> getChild(final PathArgument child) {
            return Optional.absent();
        }

        @Override
        public Version getVersion() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Version getSubtreeVersion() {
            throw new UnsupportedOperationException();
        }

        @Override
        public NormalizedNode<?, ?> getData() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MutableTreeNode mutable() {
            throw new UnsupportedOperationException();
        }
    };

    StructuralContainerModificationStrategy(final ContainerSchemaNode schemaNode) {
        super(schemaNode);
    }

    @Override
    protected void checkTouchApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current) throws DataValidationFailedException {
        /*
         * Structural containers are created as needed, therefore TOUCH is a no-op.
         * Cascade checking to child containers, providing a fake meta node in case
         * we don't have a current one.
         */
        checkChildPreconditions(path, modification, current.isPresent() ? current.get() : ABSENT_META);
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final Optional<TreeNode> current, final Version version) {
        final TreeNode currentMeta;

        if (current.isPresent()) {
            currentMeta = current.get();
        } else {
            // Create a fake node on which normal application will run
            currentMeta = TreeNodeFactory.createTreeNode(ImmutableNodes.containerNode(getSchema().getQName()), version);
        }

        /*
         * Apply TOUCH as normal and then check if there are any child nodes left. If there
         * are none, turn this operation into a whole-sale DELETE.
         */
        final TreeNode ret = applyTouch(modification, currentMeta, version);
        final NormalizedNodeContainer<?, ?, ?> data = (NormalizedNodeContainer<?, ?, ?>) ret.getData();
        if (data.getValue().isEmpty()) {
            modification.resolveModificationType(ModificationType.DELETE);
            return null;
        } else {
            return ret;
        }
    }
}
