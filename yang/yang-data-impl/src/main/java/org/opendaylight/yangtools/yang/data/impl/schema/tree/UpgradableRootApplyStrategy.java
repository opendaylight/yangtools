/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

/**
 * Implementation of Upgradable {@link RootApplyStrategy}
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
final class UpgradableRootApplyStrategy extends RootApplyStrategy {
    private final LatestOperationHolder holder;
    private ModificationApplyOperation delegate;

    UpgradableRootApplyStrategy(final LatestOperationHolder holder,
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
    protected ModificationApplyOperation delegate() {
        return delegate;
    }

    @Override
    RootApplyStrategy snapshot() {
        return new UpgradableRootApplyStrategy(holder, delegate);
    }
}