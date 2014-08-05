package org.opendaylight.yangtools.yang.data.impl.schema.tree;


import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A factory for creating in-memory data trees.
 */
public final class InMemoryDataTreeFactory implements DataTreeFactory {
    private static final InMemoryDataTreeFactory INSTANCE = new InMemoryDataTreeFactory();

    private InMemoryDataTreeFactory() {
        // Never instantiated externally
    }

    @Override
    public InMemoryDataTree create() {
        final NodeIdentifier root = new NodeIdentifier(SchemaContext.NAME);
        final NormalizedNode<?, ?> data = Builders.containerBuilder().withNodeIdentifier(root).build();

        return new InMemoryDataTree(TreeNodeFactory.createTreeNodeRecursively(data, Version.initial()), null);
    }

    /**
     * Get an instance of this factory. This method cannot fail.
     *
     * @return Data tree factory instance.
     */
    public static final InMemoryDataTreeFactory getInstance() {
        return INSTANCE;
    }
}
