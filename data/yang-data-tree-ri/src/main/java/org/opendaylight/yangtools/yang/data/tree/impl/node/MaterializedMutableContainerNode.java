/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class MaterializedMutableContainerNode extends AbstractMutableContainerNode {
    MaterializedMutableContainerNode(final AbstractContainerNode parent, final Version subtreeVersion,
            final Map<PathArgument, TreeNode> children) {
        super(parent, subtreeVersion, children);
    }

    @Override
    public TreeNode childByArg(final PathArgument arg) {
        return getModifiedChild(arg);
    }
}
