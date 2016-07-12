/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
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

final class MinMaxElementsValidation extends ModificationApplyOperation {

    private static final Logger LOG = LoggerFactory.getLogger(MinMaxElementsValidation.class);
    private final ModificationApplyOperation delegate;
    private final Integer minElements;
    private final Integer maxElements;

    private MinMaxElementsValidation(final ModificationApplyOperation delegate, final Integer minElements,
                                     final Integer maxElements) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.minElements = minElements;
        this.maxElements = maxElements;
    }

    static ModificationApplyOperation from(final ModificationApplyOperation delegate, final DataSchemaNode schema) {
        final ConstraintDefinition constraints = schema.getConstraints();
        if (constraints == null || (constraints.getMinElements() == null && constraints.getMaxElements() == null)) {
            return delegate;
        }
        return new MinMaxElementsValidation(delegate, constraints.getMinElements(), constraints.getMaxElements());

    }

    private void validateMinMaxElements(final YangInstanceIdentifier path, final PathArgument id,
            final NormalizedNode<?, ?> data) throws DataValidationFailedException {
        final int children = numOfChildrenFromValue(data);
        if (minElements != null && minElements > children) {
            throw new DataValidationFailedException(path, String.format(
                    "%s does not have enough elements (%s), needs at least %s", id,
                    children, minElements));
        }
        if (maxElements != null && maxElements < children) {
            throw new DataValidationFailedException(path, String.format(
                    "%s has too many elements (%s), can have at most %s", id, children,
                    maxElements));
        }
    }

    private void checkMinMaxElements(final YangInstanceIdentifier path, final NodeModification nodeMod,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        if (!(nodeMod instanceof ModifiedNode)) {
            LOG.debug("Could not validate {}, does not implement expected class {}", nodeMod, ModifiedNode.class);
            return;
        }

        final ModifiedNode modification = (ModifiedNode) nodeMod;

        // We need to actually perform the operation to get deal with merge in a sane manner. We know the modification
        // is immutable, so the result of validation will probably not change.
        final Optional<TreeNode> maybeApplied = delegate.apply(modification, current, version);
        Verify.verify(maybeApplied.isPresent());

        final TreeNode applied = maybeApplied.get();
        validateMinMaxElements(path, modification.getIdentifier(), applied.getData());

        // Everything passed. We now have a snapshot of the result node, it would be too bad if we just threw it out.
        // We know what the result of an apply operation is going to be *if* the following are kept unchanged:
        // - the 'current' node
        // - the schemacontext (therefore, the fact this object is associated with the modification)
        //
        // So let's stash the result. We will pick it up during apply operation.
        modification.setValidatedNode(this, current, applied);
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

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return delegate.getChild(child);
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
                             final Version version) {
        final TreeNode validated = modification.getValidatedNode(this, storeMeta);
        if (validated != null) {
            return Optional.of(validated);
        }

        // FIXME: the result moved, make sure we enforce again
        return delegate.apply(modification, storeMeta, version);
    }

    @Override
    void checkApplicable(final YangInstanceIdentifier path, final NodeModification modification,
                         final Optional<TreeNode> current,
                         final Version version) throws DataValidationFailedException {
        delegate.checkApplicable(path, modification, current, version);
        checkMinMaxElements(path, modification, current, version);
    }

    @Override
    protected void verifyStructure(final NormalizedNode<?, ?> modification, final boolean verifyChildren) {
        delegate.verifyStructure(modification, verifyChildren);
    }

    @Override
    protected ChildTrackingPolicy getChildPolicy() {
        return delegate.getChildPolicy();
    }

    @Override
    void mergeIntoModifiedNode(final ModifiedNode node, final NormalizedNode<?, ?> value, final Version version) {
        delegate.mergeIntoModifiedNode(node, value, version);
    }

    @Override
    void recursivelyVerifyStructure(NormalizedNode<?, ?> value) {
        delegate.recursivelyVerifyStructure(value);
    }
}
