/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.RequiredElementCountException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MinMaxElementsValidation extends ModificationApplyOperation {
    private static final Logger LOG = LoggerFactory.getLogger(MinMaxElementsValidation.class);

    private final SchemaAwareApplyOperation delegate;
    private final int minElements;
    private final int maxElements;

    private MinMaxElementsValidation(final SchemaAwareApplyOperation delegate, final Integer minElements,
            final Integer maxElements) {
        this.delegate = requireNonNull(delegate);
        this.minElements = minElements != null ? minElements : 0;
        this.maxElements = maxElements != null ? maxElements : Integer.MAX_VALUE;
    }

    static ModificationApplyOperation from(final SchemaAwareApplyOperation delegate, final DataSchemaNode schema) {
        if (!(schema instanceof ElementCountConstraintAware)) {
            return delegate;
        }
        final Optional<ElementCountConstraint> optConstraint = ((ElementCountConstraintAware) schema)
                .getElementCountConstraint();
        if (!optConstraint.isPresent()) {
            return delegate;
        }

        final ElementCountConstraint constraint = optConstraint.get();
        return new MinMaxElementsValidation(delegate, constraint.getMinElements(), constraint.getMaxElements());
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
            final Version version) {
        Optional<TreeNode> ret = modification.getValidatedNode(this, storeMeta);
        if (ret == null) {
            // Deal with the result moving on us
            ret = delegate.apply(modification, storeMeta, version);
            if (ret.isPresent()) {
                checkChildren(ret.get().getData());
            }
        }

        return ret;
    }

    @Override
    void checkApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        delegate.checkApplicable(path, modification, current, version);

        if (!(modification instanceof ModifiedNode)) {
            LOG.debug("Could not validate {}, does not implement expected class {}", modification, ModifiedNode.class);
            return;
        }
        final ModifiedNode modified = (ModifiedNode) modification;

        // We need to actually perform the operation to deal with merge in a sane manner. We know the modification
        // is immutable, so the result of validation will probably not change. Note we should not be checking number
        final Optional<TreeNode> maybeApplied = delegate.apply(modified, current, version);
        if (maybeApplied.isPresent()) {
            // We only enforce min/max on present data and rely on MandatoryLeafEnforcer to take care of the empty case
            validateMinMaxElements(path, maybeApplied.get().getData());
        }

        // Everything passed. We now have a snapshot of the result node, it would be too bad if we just threw it out.
        // We know what the result of an apply operation is going to be *if* the following are kept unchanged:
        // - the 'current' node
        // - the schemacontext (therefore, the fact this object is associated with the modification)
        //
        // So let's stash the result. We will pick it up during apply operation.
        modified.setValidatedNode(this, current, maybeApplied);
    }

    @Override
    void verifyStructure(final NormalizedNode<?, ?> modification, final boolean verifyChildren) {
        delegate.verifyStructure(modification, verifyChildren);
        if (verifyChildren) {
            checkChildren(modification);
        }
    }

    @Override
    ChildTrackingPolicy getChildPolicy() {
        return delegate.getChildPolicy();
    }

    @Override
    void mergeIntoModifiedNode(final ModifiedNode node, final NormalizedNode<?, ?> value, final Version version) {
        delegate.mergeIntoModifiedNode(node, value, version);
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return delegate.getChild(child);
    }

    @Override
    void recursivelyVerifyStructure(final NormalizedNode<?, ?> value) {
        delegate.recursivelyVerifyStructure(value);
    }

    private void validateMinMaxElements(final ModificationPath path, final NormalizedNode<?, ?> value)
            throws DataValidationFailedException {
        final PathArgument id = value.getIdentifier();
        final int children = numOfChildrenFromValue(value);
        if (minElements > children) {
            throw new RequiredElementCountException(path.toInstanceIdentifier(), minElements, maxElements, children,
                "%s does not have enough elements (%s), needs at least %s", id, children, minElements);
        }
        if (maxElements < children) {
            throw new RequiredElementCountException(path.toInstanceIdentifier(), minElements, maxElements, children,
                "%s has too many elements (%s), can have at most %s", id, children, maxElements);
        }
    }

    private void checkChildren(final NormalizedNode<?, ?> value) {
        final PathArgument id = value.getIdentifier();
        final int children = numOfChildrenFromValue(value);
        checkArgument(minElements <= children, "Node %s does not have enough elements (%s), needs at least %s", id,
                children, minElements);
        checkArgument(maxElements >= children, "Node %s has too many elements (%s), can have at most %s", id, children,
                maxElements);
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
}
