package org.opendaylight.yangtools.yang.data.util.tree;

public interface ValueTreeNode<V> extends NavigableTreeNode<ValueTreeNode<V>> {

    V getValue();
}
