/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

/**
 * Simple container encapsulating a boolean flag, which can be toggled. It starts
 * off in the reset state.
 */
final class SettableBoolean {
    private boolean value = false;

    /**
     * Set the flag to its initial (false) state.
     */
    public void reset() {
        value = false;
    }

    /**
     * Set the flag.
     */
    public void set() {
        value = true;
    }

    /**
     * Query the flag.
     *
     * @return True if the flag has been set since instantiation or last {@link #reset()}.
     */
    public boolean isSet() {
        return value;
    }
}
