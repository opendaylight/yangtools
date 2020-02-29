/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Utility registration handle. It is a convenience for register-style method which can return an AutoCloseable realized
 * by a subclass of this class. Invoking the close() method triggers unregistration of the state the method installed.
 */
public abstract class AbstractRegistration implements Registration {
    private static final AtomicIntegerFieldUpdater<AbstractRegistration> CLOSED_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(AbstractRegistration.class, "closed");
    private volatile int closed = 0;

    /**
     * Remove the state referenced by this registration. This method is guaranteed to be called at most once.
     * Referenced state must be retained until this method is invoked.
     */
    protected abstract void removeRegistration();

    /**
     * Query the state of this registration. Returns true if it was closed. Equivalent of {@code !notClosed()}.
     *
     * @return true if the registration was closed, false otherwise.
     */
    public final boolean isClosed() {
        return closed != 0;
    }

    /**
     * Query the state of this registration. Returns false if it was closed. Equivalent of {@code !isClosed()}.
     *
     * @return false if the registration was closed, true otherwise.
     */
    public final boolean notClosed() {
        return closed == 0;
    }

    @Override
    public final void close() {
        if (CLOSED_UPDATER.compareAndSet(this, 0, 1)) {
            removeRegistration();
        }
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("closed", closed);
    }
}
