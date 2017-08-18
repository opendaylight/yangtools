/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

/**
 * Represents a {@link ModificationApplyOperation} which is rooted at conceptual
 * top of data tree.
 *
 * <p>
 * This implementation differs from other implementations in this package that
 * is not immutable, but may be upgraded to newer state if available by
 * explicitly invoking {@link #upgradeIfPossible()} and also serves as factory
 * for deriving snapshot {@link RootModificationApplyOperation} which will not
 * be affected by upgrade of original one.
 *
 * <p>
 * There are two variations of this {@link ModificationApplyOperation}:
 * <ul>
 * <li>
 * <b>Upgradable</b> - operation may be upgraded to different backing
 * implementation by invoking {@link #upgradeIfPossible()}.</li>
 * <li><b>Not Upgradable</b> - operation is immutable, invocation of
 * {@link #upgradeIfPossible()} is no-op and method {@link #snapshot()} returns
 * pointer on same object.
 *
 * <h3>Upgradable Root Modification Operation</h3>
 *
 * Upgradable Root Modification Operation may be created using:
 * <ul>
 * <li> {@link #from(ModificationApplyOperation)} with other upgradable root
 * modification as an argument
 * <li>using factory {@link LatestOperationHolder} which instantiates Upgradable
 * Root Modification Operations and provides an option to set latest
 * implementation.
 * </ul>
 * <p>
 * Upgradable root operation is never upgraded to latest operation
 * automatically, but client code must explicitly invoke
 * {@link #upgradeIfPossible()} to get latest implementation.
 *
 * Note: This is helpful for implementing
 * {@link org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification}
 * which may be derived from
 * {@link org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree} before
 * update of schema and user actually writes data after schema update. During
 * update user did not invoked any operation.
 *
 */
abstract class RootModificationApplyOperation extends ModificationApplyOperation {

    @Override
    public final Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return getDelegate().getChild(child);
    }

    @Override
    final void checkApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        getDelegate().checkApplicable(path, modification, current, version);
    }

    @Override
    final Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> currentMeta,
            final Version version) {
        return getDelegate().apply(modification, currentMeta, version);
    }

    @Override
    public final boolean equals(final Object obj) {
        return getDelegate().equals(obj);
    }

    @Override
    public final int hashCode() {
        return getDelegate().hashCode();
    }

    @Override
    public final String toString() {
        return getDelegate().toString();
    }

    @Override
    final void verifyStructure(final NormalizedNode<?, ?> modification, final boolean verifyChildren)
            throws IllegalArgumentException {
        getDelegate().verifyStructure(modification, verifyChildren);
    }

    @Override
    void recursivelyVerifyStructure(final NormalizedNode<?, ?> value) {
        getDelegate().recursivelyVerifyStructure(value);
    }

    @Override
    final ChildTrackingPolicy getChildPolicy() {
        return getDelegate().getChildPolicy();
    }

    @Override
    final void mergeIntoModifiedNode(final ModifiedNode node, final NormalizedNode<?, ?> value, final Version version) {
        getDelegate().mergeIntoModifiedNode(node, value, version);
    }

    /**
     * Return the underlying delegate.
     *
     * @return Underlying delegate.
     */
    abstract ModificationApplyOperation getDelegate();

    /**
     * Creates a snapshot from this modification, which may have separate
     * upgrade lifecycle and is not affected by upgrades
     * <p>
     * Newly created snapshot uses backing implementation of this modi
     *
     * @return Derived {@link RootModificationApplyOperation} with separate
     *         upgrade lifecycle.
     */
    abstract RootModificationApplyOperation snapshot();

    /**
     * Upgrades backing implementation to latest available, if possible.
     * <p>
     * Latest implementation of {@link RootModificationApplyOperation} is
     * managed by {@link LatestOperationHolder} which was used to construct this
     * operation and latest operation is updated by
     * {@link LatestOperationHolder#setCurrent(ModificationApplyOperation)}.
     */
    abstract void upgradeIfPossible();

    static RootModificationApplyOperation from(final ModificationApplyOperation resolver) {
        if (resolver instanceof RootModificationApplyOperation) {
            return ((RootModificationApplyOperation) resolver).snapshot();
        }
        return new NotUpgradableModificationApplyOperation(resolver);
    }
}
