/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer;

/**
 * Base {@link TreeNode} implementation.
 */
@NonNullByDefault
public abstract sealed class BaseTreeNode extends TreeNode permits AbstractContainerNode, ValueNode {
    private final NormalizedNode data;
    private final Version version;

    BaseTreeNode(final NormalizedNode data, final Version version) {
        this.data = requireNonNull(data);
        this.version = requireNonNull(version);
    }

    /**
     * Create a new {@link BaseTreeNode} from a data node.
     *
     * @param data data node
     * @param version data node version
     * @return new BaseTreeNode instance, covering the data tree provided
     */
    public static final BaseTreeNode of(final NormalizedNode data, final Version version) {
        return switch (data) {
            case DistinctNodeContainer<?, ?> distinct -> {
                @SuppressWarnings("unchecked")
                final var container = (DistinctNodeContainer<?, NormalizedNode>) data;
                yield new SimpleContainerNode(container, version);
            }
            case OrderedNodeContainer<?> ordered -> new SimpleContainerNode(ordered, version);
            default -> new ValueNode(data, version);
        };
    }

    @Override
    public final NormalizedNode data() {
        return data;
    }

    @Override
    public final Version version() {
        return version;
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("version", version);
    }
}
