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
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.lang.ref.Reference;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for {@link Registration} implementations.
 */
@NonNullByDefault
public abstract class BaseRegistration implements Registration {

    // Note: these should live in Registration, really

    public static final BaseRegistration of(final AutoCloseable autoCloseable) {
        // Note: we do not check for the argument being BaseRegistration on purpose because we guarantee identity-based
        //       equality. That implies the argument to this method and the result of this method could be stored in
        //       the same Set -- in which case they need to be treated as two separate objects.
        return new ResourceRegistration(autoCloseable);
    }

    public static final BaseRegistration of(final Cleanable cleanable) {
        return new CleanableRegistration(cleanable);
    }

    public static final BaseRegistration of(final Cleaner cleaner, final Object obj, final Runnable action) {
        return of(cleaner.register(obj, action));
    }

    public static final BaseRegistration of(final Reference<?> reference) {
        return new ReferenceRegistration(reference);
    }

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
