/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concretization of AbstractTreeNode for leaf nodes which only contain data. Instances of this class report all
 * children as absent, subtree version equal to {@link #incarnation()} and do not support mutable view.
 */
@NonNullByDefault
final class ValueNode extends TreeNode {
    private static final Logger LOG = LoggerFactory.getLogger(ValueNode.class);

    ValueNode(final NormalizedNode data, final Version incarnation) {
        super(data, incarnation);
    }

    @Override
    public @Nullable TreeNode childByArg(final PathArgument arg) {
        LOG.warn("Attempted to access child {} of value-node {}", arg, this);
        return null;
    }

    @Override
    public Version subtreeVersion() {
        return incarnation();
    }

    @Override
    public MutableTreeNode toMutable(final Version nextSubtreeVersion) {
        /**
         * Value nodes can only we read/written/delete, which does a straight replace. That means they do not have
         * a need to be made mutable.
         */
        throw new UnsupportedOperationException("Attempted to mutate value-node " + this);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("value", data());
    }
}
