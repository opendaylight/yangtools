/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concretization of AbstractTreeNode for leaf nodes which only contain data.
 * Instances of this class report all children as absent, subtree version
 * equal to this node's version and do not support mutable view.
 */
final class ValueNode extends AbstractTreeNode {
    private static final Logger LOG = LoggerFactory.getLogger(ValueNode.class);

    protected ValueNode(final NormalizedNode<?, ?> data, final Version version) {
        super(data, version);
    }

    @Override
    public Optional<TreeNode> getChild(final PathArgument childId) {
        LOG.warn("Attempted to access child {} of value-node {}", childId, this);
        return Optional.empty();
    }

    @Override
    public Version getSubtreeVersion() {
        return getVersion();
    }

    @Override
    public MutableTreeNode mutable() {
        /**
         * Value nodes can only we read/written/delete, which does a straight
         * replace. That means they don't haver need to be made mutable.
         */
        throw new UnsupportedOperationException(String.format("Attempted to mutate value-node %s", this));
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("value", getData());
    }
}
