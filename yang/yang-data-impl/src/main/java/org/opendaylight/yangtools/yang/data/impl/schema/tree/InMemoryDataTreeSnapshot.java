package org.opendaylight.yangtools.yang.data.impl.schema.tree;


import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeUtils;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

final class InMemoryDataTreeSnapshot implements DataTreeSnapshot {
    private final RootModificationApplyOperation applyOper;
    private final SchemaContext schemaContext;
    private final NormalizedNode<?, ?> rootNode;

    InMemoryDataTreeSnapshot(final SchemaContext schemaContext, final NormalizedNode<?, ?> rootNode,
            final RootModificationApplyOperation applyOper) {
        this.schemaContext = Preconditions.checkNotNull(schemaContext);
        this.rootNode = Preconditions.checkNotNull(rootNode);
        this.applyOper = Preconditions.checkNotNull(applyOper);
    }

    NormalizedNode<?, ?> getRootNode() {
        return rootNode;
    }

    SchemaContext getSchemaContext() {
        return schemaContext;
    }

    @Override
    public Optional<NormalizedNode<?, ?>> readNode(final InstanceIdentifier path) {
        return NormalizedNodeUtils.findNode(rootNode, path);
    }

    @Override
    public InMemoryDataTreeModification newModification() {
        return new InMemoryDataTreeModification(this, applyOper);
    }

    @Override
    public String toString() {
        return "ReadOnly DataTree snapshot [data=" + getRootNode() + "]";
    }
}
