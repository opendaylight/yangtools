/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshotCursor;

abstract class AbstractCursor<T extends AbstractCursorAware> implements DataTreeSnapshotCursor {
    private static final VarHandle CLOSED;

    static {
        try {
            CLOSED = MethodHandles.lookup().findVarHandle(AbstractCursor.class, "closed", boolean.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final YangInstanceIdentifier rootPath;
    private final T parent;

    private volatile boolean closed;

    AbstractCursor(final T parent, final YangInstanceIdentifier rootPath) {
        this.rootPath = requireNonNull(rootPath);
        this.parent = requireNonNull(parent);
    }

    final T getParent() {
        return parent;
    }

    final YangInstanceIdentifier getRootPath() {
        return rootPath;
    }


    final void ensureNotClosed() {
        checkState(!closed, "Modification cursor has been closed");
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
        if (CLOSED.compareAndSet(this, false, true)) {
            parent.closeCursor(this);
        }
    }
}
