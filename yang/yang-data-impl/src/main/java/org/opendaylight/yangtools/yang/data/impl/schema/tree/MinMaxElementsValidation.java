/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;


import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MinMaxElementsValidation extends SchemaAwareApplyOperation {

    private static final Logger LOG = LoggerFactory.getLogger(MinMaxElementsValidation.class);
    private final SchemaAwareApplyOperation delegate;
    private final Integer minElements;
    private final Integer maxElements;

    private MinMaxElementsValidation(final SchemaAwareApplyOperation delegate, final Integer minElements,
            final Integer maxElements) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.minElements = minElements;
        this.maxElements = maxElements;
    }

    static final SchemaAwareApplyOperation from(final SchemaAwareApplyOperation delegate, final DataSchemaNode schema) {
        final ConstraintDefinition constraints = schema.getConstraints();
        if (constraints == null || (constraints.getMinElements() == null && constraints.getMaxElements() == null)) {
            return delegate;
        }
        return new MinMaxElementsValidation(delegate, constraints.getMinElements(), constraints.getMaxElements());

    }

    private void checkMinMaxElements(final YangInstanceIdentifier path, final NodeModification nodeMod,
            final Optional<TreeNode> current) throws DataValidationFailedException {
        if (!(nodeMod instanceof ModifiedNode)) {
            LOG.debug("Could not validate {}, does not implement expected class {}", nodeMod, ModifiedNode.class);
            return;
        }
        final ModifiedNode modification = (ModifiedNode) nodeMod;
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
            throw new DataValidationFailedException(path, String.format(
                    "%s does not have enough elements (%s), needs at least %s", modification.getIdentifier(),
                    childrenTotal, minElements));
        }
        if (maxElements != null && maxElements < childrenTotal) {
            throw new DataValidationFailedException(path, String.format(
                    "%s has too many elements (%s), can have at most %s", modification.getIdentifier(), childrenTotal,
                    maxElements));
        }
    }

    private static int numOfChildrenFromValue(final NormalizedNode<?, ?> value) {
        if (value instanceof NormalizedNodeContainer) {
            return ((NormalizedNodeContainer<?, ?, ?>) value).getValue().size();
        } else if (value instanceof UnkeyedListNode) {
            return ((UnkeyedListNode) value).getSize();
        }

        throw new IllegalArgumentException(String.format(
                "Unexpected type '%s', expected types are NormalizedNodeContainer and UnkeyedListNode",
                value.getClass()));
    }

    private static int numOfChildrenFromChildMods(final ModifiedNode modification, final Optional<TreeNode> current) {
        int result = 0;
        for (final ModifiedNode modChild : modification.getChildren()) {
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
                    break;
                case NONE:
                case TOUCH:
                    // NOOP
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operation type: " + modChild.getOperation());
            }
        }
        return result;
    }

    @Override
    protected void checkTouchApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current) throws DataValidationFailedException {
        delegate.checkTouchApplicable(path, modification, current);
        checkMinMaxElements(path, modification, current);
    }

    @Override
    protected void checkMergeApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current) throws DataValidationFailedException {
        delegate.checkMergeApplicable(path, modification, current);
        checkMinMaxElements(path, modification, current);
    }

    @Override
    protected void checkWriteApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current) throws DataValidationFailedException {
        delegate.checkWriteApplicable(path, modification, current);
        checkMinMaxElements(path, modification, current);
    }


    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return delegate.getChild(child);
    }

    @Override
    protected void verifyStructure(final NormalizedNode<?, ?> modification) {
        delegate.verifyStructure(modification);
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        return delegate.applyMerge(modification, currentMeta, version);
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        return delegate.applyTouch(modification, currentMeta, version);
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification, final Optional<TreeNode> currentMeta,
            final Version version) {
        return delegate.applyWrite(modification, currentMeta, version);
    }

    @Override
    protected ChildTrackingPolicy getChildPolicy() {
        return delegate.getChildPolicy();
    }
}
