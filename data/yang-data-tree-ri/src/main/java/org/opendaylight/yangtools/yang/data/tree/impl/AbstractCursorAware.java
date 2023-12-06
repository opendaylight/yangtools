/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractCursorAware {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCursorAware.class);
    private static final VarHandle CURSOR;

    static {
        try {
            CURSOR = MethodHandles.lookup().findVarHandle(AbstractCursorAware.class, "cursor", AbstractCursor.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile AbstractCursor<?> cursor;

    protected <T extends AbstractCursor<?>> T openCursor(final T cursorToOpen) {
        final var witness = (AbstractCursor<?>) CURSOR.compareAndExchange(this, null, cursorToOpen);
        if (witness != null) {
            throw new IllegalStateException(
                "Modification " + this + " has cursor attached at path " + witness.getRootPath());
        }
        return cursorToOpen;
    }

    final void closeCursor(final AbstractCursor<?> cursorToClose) {
        final var witness = (AbstractCursor<?>) CURSOR.compareAndExchange(this, cursorToClose, null);
        if (witness != cursorToClose) {
            LOG.warn("Attempted to close cursor {} while {} is open", cursorToClose, witness);
        }
    }
}
