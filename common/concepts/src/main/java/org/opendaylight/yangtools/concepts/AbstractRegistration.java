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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * Utility registration handle. It is a convenience for register-style method which can return an AutoCloseable realized
 * by a subclass of this class. Invoking the close() method triggers unregistration of the state the method installed.
 */
public abstract class AbstractRegistration implements Registration {
    private static final VarHandle CLOSED;

    // All access needs to go through this handle
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile byte closed;

    static {
        try {
            CLOSED = MethodHandles.lookup().findVarHandle(AbstractRegistration.class, "closed", byte.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

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
        return (byte) CLOSED.getAcquire(this) != 0;
    }

    /**
     * Query the state of this registration. Returns false if it was closed. Equivalent of {@code !isClosed()}.
     *
     * @return false if the registration was closed, true otherwise.
     */
    public final boolean notClosed() {
        return (byte) CLOSED.getAcquire(this) == 0;
    }

    @Override
    public final void close() {
        // We want full setVolatile() memory semantics here, as all state before calling this method
        // needs to be visible
        if (CLOSED.compareAndSet(this, (byte) 0, (byte) 1)) {
            removeRegistration();
        }
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("closed", isClosed());
    }
}
