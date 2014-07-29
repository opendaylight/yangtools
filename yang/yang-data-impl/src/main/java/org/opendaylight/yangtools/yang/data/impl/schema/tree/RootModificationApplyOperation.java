/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

import com.google.common.base.Optional;

public abstract class RootModificationApplyOperation implements ModificationApplyOperation {

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return getDelegate().getChild(child);
    }

    @Override
    public final void checkApplicable(final YangInstanceIdentifier path, final NodeModification modification, final Optional<TreeNode> current)
            throws DataValidationFailedException {
        getDelegate().checkApplicable(path, modification, current);
    }

    @Override
    public final Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> currentMeta, final Version version) {
        return getDelegate().apply(modification, currentMeta, version);
    }

    @Override
    public boolean equals(final Object obj) {
        return getDelegate().equals(obj);
    }

    @Override
    public int hashCode() {
        return getDelegate().hashCode();
    }

    @Override
    public String toString() {
        return getDelegate().toString();
    }

    @Override
    public void verifyStructure(final ModifiedNode modification) throws IllegalArgumentException {
        getDelegate().verifyStructure(modification);
    }

    abstract ModificationApplyOperation getDelegate();

    public abstract RootModificationApplyOperation snapshot();

    public abstract void upgradeIfPossible();



    public static RootModificationApplyOperation from(final ModificationApplyOperation resolver) {
        if(resolver instanceof RootModificationApplyOperation) {
            return ((RootModificationApplyOperation) resolver).snapshot();
        }
        return new NotUpgradable(resolver);
    }

    private static final class Upgradable extends RootModificationApplyOperation {

        private final LatestOperationHolder holder;
        private ModificationApplyOperation delegate;


        public Upgradable(final LatestOperationHolder holder, final ModificationApplyOperation delegate) {
            this.holder = holder;
            this.delegate = delegate;

        }

        @Override
        public void upgradeIfPossible() {
            ModificationApplyOperation holderCurrent = holder.getCurrent();
            if(holderCurrent != delegate) {
                // FIXME: Allow update only if there is addition of models, not removals.
                delegate = holderCurrent;
            }

        }

        @Override
        ModificationApplyOperation getDelegate() {
            return delegate;
        }

        @Override
        public RootModificationApplyOperation snapshot() {
            return new Upgradable(holder,getDelegate());
        }

    }

    private static final class NotUpgradable extends RootModificationApplyOperation {

        private final ModificationApplyOperation delegate;

        public NotUpgradable(final ModificationApplyOperation delegate) {
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

    public static class LatestOperationHolder {

        private ModificationApplyOperation current = new AlwaysFailOperation();

        public ModificationApplyOperation getCurrent() {
            return current;
        }

        public void setCurrent(final ModificationApplyOperation newApplyOper) {
            current = newApplyOper;
        }

        public RootModificationApplyOperation newSnapshot() {
            return new Upgradable(this,current);
        }

    }
}
