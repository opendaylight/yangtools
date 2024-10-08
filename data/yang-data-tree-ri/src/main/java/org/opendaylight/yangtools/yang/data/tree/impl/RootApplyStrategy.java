/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import com.google.common.collect.ForwardingObject;
import java.util.Objects;

/**
 * Represents a {@link ModificationApplyOperation} which is rooted at conceptual top of data tree.
 *
 * <p>This implementation differs from other implementations in this package that is not immutable, but may be upgraded
 * to a newer state if available by explicitly invoking {@link #upgradeIfPossible()} and also serves as factory for
 * deriving snapshot {@link RootApplyStrategy} which will not be affected by upgrade of original one.
 *
 * <p>There are two variations of this {@link ModificationApplyOperation}:
 * <ul>
 * <li>
 * <b>Upgradable</b> - operation may be upgraded to different backing
 * implementation by invoking {@link #upgradeIfPossible()}.</li>
 * <li><b>Not Upgradable</b> - operation is immutable, invocation of
 * {@link #upgradeIfPossible()} is no-op and method {@link #snapshot()} returns
 * pointer on same object.
 * </ul>
 * <h3>Upgradable Root Modification Operation</h3>
 * Upgradable Root Modification Operation may be created using:
 * <ul>
 * <li> {@link #from(ModificationApplyOperation)} with other upgradable root
 * modification as an argument
 * <li>using factory {@link LatestOperationHolder} which instantiates Upgradable
 * Root Modification Operations and provides an option to set latest
 * implementation.
 * </ul>
 *
 * <p>Upgradable root operation is never upgraded to latest operation automatically, but client code must explicitly
 * invoke {@link #upgradeIfPossible()} to get latest implementation.
 *
 * <p>Note: This is helpful for implementing {@link org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification}
 * which may be derived from {@link org.opendaylight.yangtools.yang.data.tree.api.DataTree} before update of schema and
 * user actually writes data after schema update. During update user did not invoked any operation.
 */
abstract class RootApplyStrategy extends ForwardingObject {

    static RootApplyStrategy from(final ModificationApplyOperation resolver) {
        return new NotUpgradableRootApplyStrategy(resolver);
    }

    @Override
    protected abstract ModificationApplyOperation delegate();

    @Override
    public final boolean equals(final Object obj) {
        return Objects.equals(delegate(), obj);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(delegate());
    }

    @Override
    public final String toString() {
        return Objects.toString(delegate());
    }

    /**
     * Creates a snapshot from this modification, which may have separate upgrade lifecycle and is not affected by
     * upgrades.
     *
     * <p>Newly created snapshot uses backing implementation of this modification.
     *
     * @return Derived {@link RootApplyStrategy} with separate
     *         upgrade lifecycle.
     */
    abstract RootApplyStrategy snapshot();

    /**
     * Upgrades backing implementation to latest available, if possible.
     *
     * <p>Latest implementation of {@link RootApplyStrategy} is managed by {@link LatestOperationHolder} which was used
     * to construct this operation and latest operation is updated by
     * {@link LatestOperationHolder#setCurrent(ModificationApplyOperation)}.
     */
    abstract void upgradeIfPossible();
}
