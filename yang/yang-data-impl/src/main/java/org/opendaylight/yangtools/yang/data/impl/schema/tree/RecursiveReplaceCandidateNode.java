package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;

final class RecursiveReplaceCandidateNode extends AbstractRecursiveCandidateNode {
    private final NormalizedNode<?, ?> oldData;

    public RecursiveReplaceCandidateNode(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> oldData,
            final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> newData) {
        super(newData);
        this.oldData = Preconditions.checkNotNull(oldData);
    }

    @Override
    public ModificationType getModificationType() {
        return ModificationType.WRITE;
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataAfter() {
        return super.dataOptional();
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataBefore() {
        return Optional.<NormalizedNode<?, ?>>of(oldData);
    }

    @Override
    protected DataTreeCandidateNode createContainer(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> childData) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected DataTreeCandidateNode createLeaf(final NormalizedNode<?, ?> childData) {
        // TODO Auto-generated method stub
        return null;
    }
}
