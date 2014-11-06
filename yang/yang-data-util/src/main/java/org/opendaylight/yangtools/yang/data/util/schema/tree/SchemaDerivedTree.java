package org.opendaylight.yangtools.yang.data.util.schema.tree;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

public interface SchemaDerivedTree<V> extends StoreTreeNode<SchemaDerivedTree<V>> {

    public interface DerivedTreeNode<V> extends StoreTreeNode<DerivedTreeNode<V>>, Supplier<V> {

        @Override
        public Optional<DerivedTreeNode<V>> getChild(PathArgument child);

        @Override
        V get();

    }

    SchemaDerivedTree<V> getRootNode();

}
