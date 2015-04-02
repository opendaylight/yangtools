package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;

final class RecursiveReplaceCandidateNode extends AbstractDataTreeCandidateNode {
    private final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> oldData;

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
    public DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
        // FIXME: this is a linear walk. We need a Map of these in order to
        //        do something like getChildMap().get(identifier);
        for (DataTreeCandidateNode c : getChildNodes()) {
            if (identifier.equals(c.getIdentifier())) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Collection<DataTreeCandidateNode> getChildNodes() {
        return deltaChildren(oldData, getData());
    }
}
