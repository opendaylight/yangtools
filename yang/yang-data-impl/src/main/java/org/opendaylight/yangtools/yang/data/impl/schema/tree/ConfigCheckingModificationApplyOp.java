package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class ConfigCheckingModificationApplyOp extends ModificationApplyOperation {

    private final DataSchemaNode schema;
    private final ModificationApplyOperation delegate;

    public ConfigCheckingModificationApplyOp(DataSchemaNode schema, ModificationApplyOperation delegate) {
        this.schema = schema;
        this.delegate = delegate;
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta, final Version version) {
        return delegate.apply(modification, storeMeta, version);
    }

    @Override
    void checkApplicable(final YangInstanceIdentifier path, final NodeModification modification, final Optional<TreeNode> current) throws DataValidationFailedException {
        delegate.checkApplicable(path, modification, current);
    }

    @Override
    void verifyStructure(final ModifiedNode modification) throws IllegalArgumentException {
        delegate.verifyStructure(modification);
        if (modification.getOperation().equals(LogicalOperation.WRITE)) {
            final NormalizedNode<?, ?> data = modification.getWrittenValue();
            Preconditions.checkArgument(schema != null,
                    "Child %s is not present in schema tree.", data.getIdentifier());
            Preconditions.checkNotNull(data);
            if (!schema.isConfiguration()) {
                throw new DataValidationException(String.format("Node %s doesn't exist in configuration context.",
                        schema.getQName()));
            }
            if (data instanceof NormalizedNodeContainer) {
                for (final NormalizedNode<?, ?> dataChild : ((NormalizedNodeContainer<?, ?, ?>) data).getValue()) {
                    final Optional<ModificationApplyOperation> childApplyOpPotential = getChild(dataChild.getIdentifier());
                    if (childApplyOpPotential.isPresent()) {
                        final ModificationApplyOperation childApplyOp = childApplyOpPotential.get();
                        final ModifiedNode modifiedNodeChild = ModifiedNode.createWriteModificationFromData(
                                dataChild.getIdentifier(), dataChild);
                        childApplyOp.verifyStructure(modifiedNodeChild);
                    }
                }
            }
        }
    }

    @Override
    ChildTrackingPolicy getChildPolicy() {
        return delegate.getChildPolicy();
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final YangInstanceIdentifier.PathArgument child) {
        final Optional<ModificationApplyOperation> childModOpPotential = delegate.getChild(child);
        if (childModOpPotential.isPresent()) {
            final ModificationApplyOperation childModOp = childModOpPotential.get();
            final DataSchemaNode childSchema = getDataSchemaNodeChild(child);
            return Optional.<ModificationApplyOperation>of(new ConfigCheckingModificationApplyOp(childSchema, childModOp));
        } else {
            return Optional.absent();
        }
    }

    private DataSchemaNode getDataSchemaNodeChild(final YangInstanceIdentifier.PathArgument child) {
        DataSchemaNode schemaNode = schema;
        // we intentionally skip through anything that is not instance of NodeIdentifier because we cannot check
        // config statement for Augmentations and NodeIdentifierWithPredicates or NodeIdentifierWithValue doesn't
        // provide any new information for us in schema tree context
        if (child instanceof YangInstanceIdentifier.NodeIdentifier) {
            if (schemaNode instanceof DataNodeContainer) {
                schemaNode = ((DataNodeContainer) schemaNode).getDataChildByName(
                        child.getNodeType());
            } else if (schemaNode instanceof ChoiceSchemaNode) {
                final Set<ChoiceCaseNode> cases = ((ChoiceSchemaNode) schemaNode).getCases();
                schemaNode = null;
                for (final ChoiceCaseNode caseNode : cases) {
                    schemaNode = caseNode.getDataChildByName(child.getNodeType());
                    if (schemaNode != null) {
                        break;
                    }
                }
            }
        }
        return schemaNode;
    }
}
