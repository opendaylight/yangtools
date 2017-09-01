/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

/**
 * Implementation of Upgradable {@link RootModificationApplyOperation}
 *
 * <p>
 * This implementation is associated with {@link LatestOperationHolder}
 * which holds latest available implementation, which may be used for
 * upgrade.
 *
 * <p>
 * Upgrading {@link LatestOperationHolder} will not affect any instance,
 * unless client invoked {@link #upgradeIfPossible()} which will result in
 * changing delegate to the latest one.
 */
final class UpgradableModificationApplyOperation extends RootModificationApplyOperation {
    private final LatestOperationHolder holder;
    private ModificationApplyOperation delegate;

    UpgradableModificationApplyOperation(final LatestOperationHolder holder,
        final ModificationApplyOperation delegate) {
        this.holder = holder;
        this.delegate = delegate;
    }

    @Override
    void upgradeIfPossible() {
        ModificationApplyOperation holderCurrent = holder.getCurrent();
        if (holderCurrent != delegate) {
            // FIXME: Allow update only if there is addition of models, not
            // removals.
            delegate = holderCurrent;
        }
    }

    @Override
    ModificationApplyOperation getDelegate() {
        return delegate;
    }

    @Override
    RootModificationApplyOperation snapshot() {
        return new UpgradableModificationApplyOperation(holder, getDelegate());
    }

}