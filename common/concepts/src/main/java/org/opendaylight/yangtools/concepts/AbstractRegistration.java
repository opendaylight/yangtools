/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility registration handle. It is a convenience for register-style method
 * which can return an AutoCloseable realized by a subclass of this class.
 * Invoking the close() method triggers unregistration of the state the method
 * installed.
 */
public abstract class AbstractRegistration implements AutoCloseable {
    private AtomicBoolean closed = new AtomicBoolean();
    
    /**
     * Remove the state referenced by this registration. This method is
     * guaranteed to be called at most once. The referenced state must be
     * retained until this method is invoked.
     */
    protected abstract void removeRegistration();

    @Override
    public final void close() {
        if (closed.compareAndSet(false, true)) {
            removeRegistration();
        }
    }
}
