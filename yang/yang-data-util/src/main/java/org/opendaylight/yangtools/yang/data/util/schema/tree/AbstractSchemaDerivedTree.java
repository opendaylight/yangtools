package org.opendaylight.yangtools.yang.data.util.schema.tree;

import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import com.google.common.base.Preconditions;


class GuavaCacheBasedSchemaDerivedTree<V> implements SchemaDerivedTree<V> {

    private final SchemaDerivedValueFactory<V> valueFactory;
    private final  SchemaDerivedTree<V> root;


    private abstract class ChildCachingNode implements DerivedTreeNode<V> {

    }

    private class RootNode<V> implements DerivedTreeNode<V>{



    }

    protected GuavaCacheBasedSchemaDerivedTree(final SchemaContext ctx,final SchemaDerivedValueFactory<V> factory) {
        valueFactory = Preconditions.checkNotNull(factory);
        root = new RootNode(factory.from(ctx));
    }

    @Override
    public SchemaDerivedTree<V> getRootNode() {
        return root;
    }




}
