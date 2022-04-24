/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractDataTreeSnapshot implements DataTreeSnapshot {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataTreeSnapshot.class);
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<AbstractDataTreeSnapshot, AbstractCursor> CURSOR_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(AbstractDataTreeSnapshot.class, AbstractCursor.class, "cursor");
    private volatile AbstractCursor<?> cursor = null;

    protected <T extends AbstractCursor<?>> T openCursor(final T cursorToOpen) {
        final boolean success = CURSOR_UPDATER.compareAndSet(this, null, cursorToOpen);
        checkState(success, "Modification %s has cursor attached at path %s", this, cursor.getRootPath());
        return cursorToOpen;
    }

    final void closeCursor(final AbstractCursor<?> cursorToClose) {
        final boolean success = CURSOR_UPDATER.compareAndSet(this, cursorToClose, null);
        if (!success) {
            LOG.warn("Attempted to close cursor {} while {} is open", cursorToClose, cursor);
        }
    }
}
