package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
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
        final Integer minElements = schema.getConstraints().getMinElements();
        final Integer maxElements = schema.getConstraints().getMaxElements();
        if (minElements == 0 && maxElements == Integer.MAX_VALUE) {
            return;
        }

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
            result = ((NormalizedNodeContainer) value).getValue().size();
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
