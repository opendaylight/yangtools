package org.opendaylight.yangtools.yang.data.impl.schema.tree;


import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeUtils;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class InMemoryDataTreeSnapshot implements DataTreeSnapshot {
    private final ModificationApplyOperation applyOper;
    private final SchemaContext schemaContext;
    private final TreeNode rootNode;

    InMemoryDataTreeSnapshot(final SchemaContext schemaContext, final TreeNode rootNode,
            final ModificationApplyOperation applyOper) {
        this.schemaContext = Preconditions.checkNotNull(schemaContext);
        this.rootNode = Preconditions.checkNotNull(rootNode);
        this.applyOper = Preconditions.checkNotNull(applyOper);
    }

    TreeNode getRootNode() {
        return rootNode;
    }

    SchemaContext getSchemaContext() {
        return schemaContext;
    }

    @Override
    public Optional<NormalizedNode<?, ?>> readNode(final InstanceIdentifier path) {
        return NormalizedNodeUtils.findNode(rootNode.getData(), path);
    }

    @Override
    public InMemoryDataTreeModification newModification() {
        return new InMemoryDataTreeModification(this, applyOper);
    }

    @Override
    public String toString() {
        return rootNode.getSubtreeVersion().toString();
    }

}