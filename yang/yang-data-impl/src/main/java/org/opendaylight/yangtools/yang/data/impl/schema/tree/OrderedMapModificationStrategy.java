package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class OrderedMapModificationStrategy extends NormalizedNodeContainerModificationStrategy {
    private final Optional<ModificationApplyOperation> entryStrategy;

    OrderedMapModificationStrategy(final ListSchemaNode schema) {
        super(OrderedMapNode.class);
        entryStrategy = Optional.<ModificationApplyOperation> of(new ListEntryModificationStrategy(schema));
    }

    @Override
    boolean isOrdered() {
        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected NormalizedNodeContainerBuilder createBuilder(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof OrderedMapNode);
        return ImmutableOrderedMapNodeBuilder.create((OrderedMapNode) original);
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final YangInstanceIdentifier.PathArgument identifier) {
        if (identifier instanceof YangInstanceIdentifier.NodeIdentifierWithPredicates) {
            return entryStrategy;
        }
        return Optional.absent();
    }

    @Override
    public String toString() {
        return "OrderedMapModificationStrategy [entry=" + entryStrategy + "]";
    }
}