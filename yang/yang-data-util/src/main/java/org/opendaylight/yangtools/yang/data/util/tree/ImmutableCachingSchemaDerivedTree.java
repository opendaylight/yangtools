package org.opendaylight.yangtools.yang.data.util.tree;

import com.google.common.base.Optional;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

class ImmutableCachingTree<V> {

    private SchemaDerivedTreeNodeFactory<V> valueFactory;

    ValueTreeNode<V> root;

    public Optional<V> getValue(final YangInstanceIdentifier value) {
        final ValueTreeNode<V> node = getNode(value);
        return node != null ? Optional.fromNullable(node.getValue()) : Optional.<V>absent();
    }

    public ValueTreeNode<V> getRoot() {
        return root;
    }

    private @Nullable ValueTreeNode<V> getNode(final YangInstanceIdentifier value) {
        ValueTreeNode<V> current = getRoot();
        for(final PathArgument arg : value.getPathArguments()) {
            final ValueTreeNode<V> potential = current.getChild(arg);
            if(potential == null) {
                return null;
            }
            current = potential;
        }
        return current;
    }

}
