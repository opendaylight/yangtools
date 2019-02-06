/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshotCursor;

abstract class AbstractCursor<T extends AbstractCursorAware> implements DataTreeSnapshotCursor {
    @SuppressWarnings("rawtypes")
    private static final AtomicIntegerFieldUpdater<AbstractCursor> CLOSED_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(AbstractCursor.class, "closed");
    private final YangInstanceIdentifier rootPath;
    private final T parent;
    // closed isn't unused, it's updated by CLOSED_UPDATER but data-flow analysers can't see that
    @SuppressWarnings("unused")
    private volatile int closed;

    AbstractCursor(final T parent, final YangInstanceIdentifier rootPath) {
        this.rootPath = Preconditions.checkNotNull(rootPath);
        this.parent = Preconditions.checkNotNull(parent);
    }

    final T getParent() {
        return parent;
    }

    final YangInstanceIdentifier getRootPath() {
        return rootPath;
    }


    final void ensureNotClosed() {
        Preconditions.checkState(closed == 0, "Modification cursor has been closed");
    }

    @Override
    public final void enter(final PathArgument... path) {
        enter(Arrays.asList(path));
    }

    @Override
    public final void exit() {
        exit(1);
    }

    @Override
    public final void close() {
        if (CLOSED_UPDATER.compareAndSet(this, 0, 1)) {
            parent.closeCursor(this);
        }
    }

}
