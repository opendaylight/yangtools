/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

final class NotUpgradableModificationApplyOperation extends RootModificationApplyOperation {
    private final ModificationApplyOperation delegate;

    NotUpgradableModificationApplyOperation(final ModificationApplyOperation delegate) {
        this.delegate = delegate;
    }

    @Override
    public ModificationApplyOperation getDelegate() {
        return delegate;
    }

    @Override
    public void upgradeIfPossible() {
        // Intentional noop
    }

    @Override
    public RootModificationApplyOperation snapshot() {
        return this;
    }
}