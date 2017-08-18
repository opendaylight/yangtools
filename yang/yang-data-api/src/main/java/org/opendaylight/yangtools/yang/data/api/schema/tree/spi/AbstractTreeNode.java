/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A very basic data tree node. Contains some versioned data.
 */
abstract class AbstractTreeNode implements TreeNode {
    private final NormalizedNode<?, ?> data;
    private final Version version;

    protected AbstractTreeNode(final NormalizedNode<?, ?> data, final Version version) {
        this.data = requireNonNull(data);
        this.version = requireNonNull(version);
    }

    @Override
    public final PathArgument getIdentifier() {
        return data.getIdentifier();
    }

    @Override
    public final Version getVersion() {
        return version;
    }

    @Override
    public final NormalizedNode<?, ?> getData() {
        return data;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).add("version", version)).toString();
    }

    protected abstract ToStringHelper addToStringAttributes(ToStringHelper helper);
}
