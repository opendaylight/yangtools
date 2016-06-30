/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A container node which has been modified. It tracks the subtree version and all modified children.
 */
abstract class AbstractModifiedContainerNode extends AbstractContainerNode {
    private final Map<PathArgument, TreeNode> children;
    private final Version subtreeVersion;

    protected AbstractModifiedContainerNode(final NormalizedNode<?, ?> data, final Version version,
            final Map<PathArgument, TreeNode> children, final Version subtreeVersion) {
        this(data, version, children, subtreeVersion, ImmutableMap.of());
    }

    public AbstractModifiedContainerNode(final NormalizedNode<?, ?> data, final Version version,
            final Map<PathArgument, TreeNode> children, final Version subtreeVersion,
            final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> indexes) {
        super(data, version, indexes);
        this.subtreeVersion = Preconditions.checkNotNull(subtreeVersion);
        this.children = Preconditions.checkNotNull(children);
    }

    protected final TreeNode getModifiedChild(final PathArgument childId) {
        return children.get(childId);
    }

    protected final Map<PathArgument, TreeNode> snapshotChildren() {
        return MapAdaptor.getDefaultInstance().takeSnapshot(children);
    }

    @Override
    public final Version getSubtreeVersion() {
        return subtreeVersion;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("subtreeVersion", subtreeVersion).add("children", children);
    }
}
