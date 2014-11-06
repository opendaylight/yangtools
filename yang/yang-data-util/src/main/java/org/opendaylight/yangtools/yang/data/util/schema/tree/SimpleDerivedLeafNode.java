package org.opendaylight.yangtools.yang.data.util.schema.tree;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.schema.tree.SchemaDerivedTree.DerivedTreeNode;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

class SimpleDerivedLeafNode<V> implements DerivedTreeNode<V> {

    private final V value;

    public SimpleDerivedLeafNode(final V value) {
        this.value = Preconditions.checkNotNull(value);
    }

    @Override
    public V get() {
        return value;
    }

    @Override
    public Optional<DerivedTreeNode<V>> getChild(final PathArgument child) {
        throw new UnsupportedOperationException("Leaf node does not have children.");
    }
}