/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A forwarding {@link ModificationApplyOperation}. Useful for strategies which do not deal with data layout, but rather
 * perform additional validation.
 */
abstract sealed class AbstractValidation extends ModificationApplyOperation
        permits MinMaxElementsValidation, UniqueValidation {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractValidation.class);

    private final @NonNull ModificationApplyOperation delegate;

    AbstractValidation(final ModificationApplyOperation delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public final ModificationApplyOperation childByArg(final PathArgument arg) {
        return delegate.childByArg(arg);
    }

    @Override
    final ChildTrackingPolicy getChildPolicy() {
        return delegate.getChildPolicy();
    }

    @Override
    final void mergeIntoModifiedNode(final ModifiedNode node, final NormalizedNode value, final Version version) {
        delegate.mergeIntoModifiedNode(node, value, version);
    }

    @Override
    final void quickVerifyStructure(final NormalizedNode modification) {
        delegate.quickVerifyStructure(modification);
    }

    @Override
    final void recursivelyVerifyStructure(final NormalizedNode value) {
        delegate.recursivelyVerifyStructure(value);
    }

    @Override
    final TreeNode apply(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        var validated = modification.validatedNode(this, currentMeta);
        if (validated != null) {
            return validated.treeNode();
        }

        // This might also mean the delegate is maintaining validation
        if (delegate instanceof AbstractValidation) {
            validated = modification.validatedNode(delegate, currentMeta);
            if (validated != null) {
                return validated.treeNode();
            }
        }

        // Deal with the result moving on us
        final var ret = delegate.apply(modification, currentMeta, version);
        if (ret != null) {
            enforceOnData(ret.getData());
        }
        return ret;
    }

    @Override
    final void checkApplicable(final ModificationPath path, final NodeModification modification,
            final TreeNode currentMeta, final Version version) throws DataValidationFailedException {
        delegate.checkApplicable(path, modification, currentMeta, version);
        if (!(modification instanceof ModifiedNode modified)) {
            // FIXME: 7.0.0: turn this into a verify?
            LOG.debug("Could not validate {}, does not implement expected class {}", modification, ModifiedNode.class);
            return;
        }

        if (delegate instanceof AbstractValidation) {
            checkApplicable(path, verifyNotNull(modified.validatedNode(delegate, currentMeta)).treeNode());
            return;
        }

        // We need to actually perform the operation to deal with merge in a sane manner. We know the modification
        // is immutable, so the result of validation will probably not change. Note we should not be checking number
        final var applied = delegate.apply(modified, currentMeta, version);
        checkApplicable(path, applied);

        // Everything passed. We now have a snapshot of the result node, it would be too bad if we just threw it out.
        // We know what the result of an apply operation is going to be *if* the following are kept unchanged:
        // - the 'current' node
        // - the effective model context (therefore, the fact this object is associated with the modification)
        //
        // So let's stash the result. We will pick it up during apply operation.
        modified.setValidatedNode(this, currentMeta, applied);
    }

    private void checkApplicable(final ModificationPath path, final @Nullable TreeNode applied)
            throws DataValidationFailedException {
        if (applied != null) {
            // We only enforce min/max on present data and rely on MandatoryLeafEnforcer to take care of the empty case
            enforceOnData(path, applied.getData());
        }
    }

    @Override
    void fullVerifyStructure(final NormalizedNode modification) {
        delegate.fullVerifyStructure(modification);
        enforceOnData(modification);
    }

    final @NonNull ModificationApplyOperation delegate() {
        return delegate;
    }

    abstract void enforceOnData(ModificationPath path, NormalizedNode value) throws DataValidationFailedException;

    abstract void enforceOnData(@NonNull NormalizedNode data);

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("delegate", delegate);
    }
}
