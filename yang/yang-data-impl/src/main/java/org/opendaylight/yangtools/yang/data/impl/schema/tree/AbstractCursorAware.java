/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractCursorAware {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCursorAware.class);
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<AbstractCursorAware, AbstractCursor> CURSOR_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(AbstractCursorAware.class, AbstractCursor.class, "cursor");
    private volatile AbstractCursor<?> cursor = null;

    protected <T extends AbstractCursor<?>> T openCursor(final T cursor) {
        final boolean success = CURSOR_UPDATER.compareAndSet(this, null, cursor);
        Preconditions.checkState(success, "Modification %s has cursor attached at path %s", this,
            this.cursor.getRootPath());
        return cursor;
    }

    final void closeCursor(final AbstractCursor<?> cursor) {
        final boolean success = CURSOR_UPDATER.compareAndSet(this, cursor, null);
        if (!success) {
            LOG.warn("Attempted to close cursor {} while {} is open", cursor, this.cursor);
        }
    }
}
