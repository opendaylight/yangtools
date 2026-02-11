/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A simple base for {@link TreeNode}.
 */
@NonNullByDefault
abstract sealed class AbstractTreeNode extends TreeNode permits AbstractContainerNode, ValueNode {
    private final NormalizedNode data;
    private final Version incarnation;

    AbstractTreeNode(final NormalizedNode data, final Version incarnation) {
        this.data = requireNonNull(data);
        this.incarnation = requireNonNull(incarnation);
    }

    @Override
    public final NormalizedNode data() {
        return data;
    }

    @Override
    public final Version incarnation() {
        return incarnation;
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("version", incarnation);
    }
}
