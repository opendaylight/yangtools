package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class ListConstraintsValidator {

    public static <T extends DataSchemaNode> void checkMinMaxElements(final YangInstanceIdentifier path,
                                                                      final T schema, final ModifiedNode modification,
                                                                      final Optional<TreeNode> current)
            throws DataValidationFailedException {
        // TODO: replace null checks for default values checks when Bug 2685 is fixed
        if (schema.getConstraints().getMinElements() == null && schema.getConstraints().getMaxElements() == null) {
            return;
        }
        // TODO: remove when Bug 2685 is fixed
        final int minElements = schema.getConstraints().getMinElements() != null ?
                schema.getConstraints().getMinElements() : 0;
        final int maxElements = schema.getConstraints().getMaxElements() != null ?
                schema.getConstraints().getMaxElements() : Integer.MAX_VALUE;

        int childrenBefore = 0;
        int childrenAfter = 0;
        if (current.isPresent()) {
            childrenBefore = numOfChildrenFromValue(current.get().getData());
        }
        if (modification.getWrittenValue() != null) {
            childrenAfter = numOfChildrenFromValue(modification.getWrittenValue());
        }

        final int childrenTotal = childrenBefore + childrenAfter + numOfChildrenFromChildMods(modification, current);

        if (minElements > childrenTotal || maxElements < childrenTotal) {
            throw new DataValidationFailedException(path,
                    String.format("Number of elements '%d' of '%s' is not in allowed range <%d,%d>",
                            childrenTotal, schema.getQName().getLocalName(), minElements, maxElements));
        }
    }

    private static int numOfChildrenFromValue(NormalizedNode<?, ?> value) {
        int result;
        if (value instanceof NormalizedNodeContainer) {
            // TODO: implement size for NormalizedNodeContainer ?
            result = Iterables.size(((NormalizedNodeContainer) value).getValue());
        } else if (value instanceof UnkeyedListNode) {
            result = ((UnkeyedListNode) value).getSize();
        } else {
            throw new IllegalArgumentException("Unexpected type '" + value.getClass() +
                    "', expected types are NormalizedNodeContainer and UnkeyedListNode");
        }
        return result;
    }

    private static int numOfChildrenFromChildMods(final ModifiedNode modification, final Optional<TreeNode> current) {
        int result = 0;
        for (ModifiedNode modChild : modification.getChildren()) {
            switch (modChild.getType()) {
                case WRITE:
                    result++;
                    break;
                case MERGE:
                    if (!current.isPresent()) {
                        result++;
                    }
                    break;
                case DELETE:
                    result--;
            }
        }
        return result;
    }
}
