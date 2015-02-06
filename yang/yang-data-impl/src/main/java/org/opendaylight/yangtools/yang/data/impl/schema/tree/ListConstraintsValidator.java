/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
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
        if (minElements == null && maxElements == null) {
            // Shortcut for unconstrained elements
            return;
        }

        final int childrenBefore;
        if (current.isPresent()) {
            childrenBefore = numOfChildrenFromValue(current.get().getData());
        } else {
            childrenBefore = 0;
        }

        final int childrenAfter;
        if (modification.getWrittenValue() != null) {
            childrenAfter = numOfChildrenFromValue(modification.getWrittenValue());
        } else {
            childrenAfter = 0;
        }

        final int childrenTotal = childrenBefore + childrenAfter + numOfChildrenFromChildMods(modification, current);
        if (minElements != null && minElements > childrenTotal) {
            throw new DataValidationFailedException(path,
                String.format("%s does not have enough elements (%s), needs at least %s",
                schema.getQName(), childrenTotal, minElements));
        }
        if (maxElements != null && maxElements < childrenTotal) {
            throw new DataValidationFailedException(path,
                String.format("%s has too many elements (%s), can have at most %s",
                schema.getQName(), childrenTotal, maxElements));
        }
    }

    private static int numOfChildrenFromValue(final NormalizedNode<?, ?> value) {
        if (value instanceof NormalizedNodeContainer) {
            return ((NormalizedNodeContainer<?, ?, ?>) value).getValue().size();
        } else if (value instanceof UnkeyedListNode) {
            return ((UnkeyedListNode) value).getSize();
        }

        throw new IllegalArgumentException(String.format("Unexpected type '%s', expected types are NormalizedNodeContainer and UnkeyedListNode", value.getClass()));
    }

    private static int numOfChildrenFromChildMods(final ModifiedNode modification, final Optional<TreeNode> current) {
        int result = 0;
        for (ModifiedNode modChild : modification.getChildren()) {
            switch (modChild.getOperation()) {
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
