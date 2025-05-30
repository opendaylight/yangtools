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
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for {@link Registration} implementations.
 */
@NonNullByDefault
public abstract class BaseRegistration implements Registration {
    /**
     * Query the state of this registration. Returns true if it was closed. Equivalent of {@code !notClosed()}.
     *
     * @return true if the registration was closed, false otherwise.
     */
    public abstract boolean isClosed();

    /**
     * Query the state of this registration. Returns false if it was closed. Equivalent of {@code !isClosed()}.
     *
     * @return false if the registration was closed, true otherwise.
     */
    public abstract boolean notClosed();

    @Override
    public abstract void close();

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected abstract ToStringHelper addToStringAttributes(ToStringHelper toStringHelper);
}
