/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * Utility registration handle. It is a convenience for register-style method which can return an AutoCloseable realized
 * by a subclass of this class. Invoking the close() method triggers unregistration of the state the method installed.
 */
public abstract class AbstractRegistration extends BaseRegistration {
    private static final VarHandle CLOSED;

    // All access needs to go through this handle, really.
    // NOTE: we really would like to use 'boolean' here, but we may have a Serializable subclass and we do not want
    //       to risk breakage for little benefit we would get in terms of our code here.
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

    @Override
    public final boolean isClosed() {
        return getAcquire() != 0;
    }

    @Override
    public final boolean notClosed() {
        return getAcquire() == 0;
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
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("closed", isClosed());
    }

    private byte getAcquire() {
        return (byte) CLOSED.getAcquire(this);
    }

    /**
     * Dance around <a href=""https://github.com/spotbugs/spotbugs/issues/2749">underlying issue</a>.
     */
    @Deprecated(forRemoval = true)
    final void spotbugs2749() {
        if (closed == 2) {
            closed = 3;
        }
    }
}
