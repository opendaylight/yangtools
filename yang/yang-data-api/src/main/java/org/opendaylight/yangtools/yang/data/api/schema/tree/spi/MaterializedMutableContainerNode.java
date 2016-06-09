/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import java.util.List;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

import com.google.common.base.Optional;
import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class MaterializedMutableContainerNode extends AbstractMutableContainerNode {
    MaterializedMutableContainerNode(final AbstractContainerNode parent, final Map<PathArgument, TreeNode> children) {
        super(parent, children);
    }

    @Override
    public Optional<TreeNode> getChild(final PathArgument child) {
        return Optional.fromNullable(getModifiedChild(child));
    }

    @Override
    public Map<List<YangInstanceIdentifier>, TreeNodeIndex> getIndexes() {
        // TODO Auto-generated method stub
        return null;
    }
}
