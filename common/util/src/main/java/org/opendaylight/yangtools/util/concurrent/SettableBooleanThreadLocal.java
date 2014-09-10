/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

/**
 * A reusable {@link ThreadLocal} which returns a {@link SettableBoolean}.
 */
final class SettableBooleanThreadLocal extends ThreadLocal<SettableBoolean> {
    @Override
    protected SettableBoolean initialValue() {
        return new SettableBoolean();
    }

    @Override
    public void set(final SettableBoolean value) {
        throw new UnsupportedOperationException("Resetting the value is not supported");
    }
}
