/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

/**
 * Holder and factory for upgradable root modifications.
 *
 * <p>This class is factory for upgradable root modifications and provides an access to set latest backing
 * implementation.
 */
final class LatestOperationHolder {

    private ModificationApplyOperation current;

    /**
     * Return latest backing implementation.
     *
     * @return latest backing implementation
     */
    ModificationApplyOperation getCurrent() {
        return current;
    }

    /**
     * Sets latest backing implementation of associated {@link RootApplyStrategy}.
     *
     * <p>Note: This does not result in upgrading implementation of already existing
     * {@link RootApplyStrategy}. Users, who obtained instances using {@link #newSnapshot()}, deriving
     * {@link RootApplyStrategy} from this modification must explicitly invoke
     * {@link RootApplyStrategy#upgradeIfPossible()} on their instance to be updated to latest backing
     * implementation.
     *
     * @param newApplyOper New backing implementation
     */
    void setCurrent(final ModificationApplyOperation newApplyOper) {
        current = requireNonNull(newApplyOper);
    }

    /**
     * Creates new upgradable {@link RootApplyStrategy} associated with holder.
     *
     * @return New upgradable {@link RootApplyStrategy} with {@link #getCurrent()} used
     *         as the backing implementation.
     */
    RootApplyStrategy newSnapshot() {
        return new UpgradableRootApplyStrategy(this, current);
    }

}
