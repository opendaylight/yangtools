/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import java.util.Collection;
import java.util.Map;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.spi.node.AbstractNormalizedNode;

public abstract class AbstractImmutableDataContainerNode<K extends PathArgument, N extends DataContainerNode>
        extends AbstractNormalizedNode<K, N> implements DataContainerNode {
    private final Map<PathArgument, Object> children;

    protected AbstractImmutableDataContainerNode(final Map<PathArgument, Object> children, final K nodeIdentifier) {
        super(nodeIdentifier);
        this.children = ImmutableOffsetMap.unorderedCopyOf(children);
    }

    @Override
    public final DataContainerChild childByArg(final PathArgument child) {
        return LazyLeafOperations.getChild(children, child);
    }

    @Override
    public final Collection<DataContainerChild> body() {
        return new LazyValues(children);
    }

    @Override
    public final int size() {
        return children.size();
    }

    /**
     * DO NOT USE THIS METHOD.
     *
     * <p>
     * This is an implementation-internal API and no outside users should use it. If you do, you are asking for trouble,
     * as the returned object is not guaranteed to conform to java.util.Map interface, nor is its contents well-defined.
     *
     * @return An unmodifiable view if this node's children.
     */
    public final Map<PathArgument, Object> getChildren() {
        return children;
    }

    @Override
    protected int valueHashCode() {
        return children.hashCode();
    }

    @Override
    protected boolean valueEquals(final N other) {
        if (other instanceof AbstractImmutableDataContainerNode<?, ?> immutableContainer) {
            return children.equals(immutableContainer.children);
        }
        if (size() != other.size()) {
            return false;
        }
        for (var child : body()) {
            if (!child.equals(other.childByArg(child.getIdentifier()))) {
                return false;
            }
        }
        return true;
    }
}
