/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.LoggerFactory;

/**
 * Internal machinery to deal with {@link AccessController}: if we detect Java 24+ at runtime, we do not touch
 * {@code AccessController} and rely on <a href="https://openjdk.org/jeps/486">JEP-486</a> semantics instead.
 */
// TODO: can we simplify this with a multi-release jar?
@NonNullByDefault
public abstract sealed class AccessControllerCompat {
    /**
     * Java >=24: {@link AccessController#doPrivileged(PrivilegedAction)} is a no-op wrapper. Inline its behaviour
     * without touching it, as it may disappear in later versions of Java.
     */
    // FIXME: assume this behaviour and eliminate this entire abstract once we require Java 24+
    private static final class NoAccessController extends AccessControllerCompat {
        @Override
        <T> T privilegedGet(final Supplier<T> supplier) {
            return supplier.get();
        }
    }

    /**
     * Java <24: defer to {@link AccessController#doPrivileged(PrivilegedAction)}.
     */
    private static final class WithAccessController extends AccessControllerCompat {
        @Override
        @SuppressWarnings({ "deprecation", "removal" })
        <T> T privilegedGet(final Supplier<T> supplier) {
            return AccessController.doPrivileged((PrivilegedAction<T>) supplier::get);
        }
    }

    private static final AccessControllerCompat INSTANCE;

    static {
        final String str;
        if (Runtime.version().feature() >= 24) {
            str = ">=24";
            INSTANCE = new NoAccessController();
        } else {
            str = "<24";
            INSTANCE = new WithAccessController();
        }
        LoggerFactory.getLogger(AccessControllerCompat.class).debug("Assuming Java {} AccessController semantics", str);
    }

    public static final <T> T get(final Supplier<T> supplier) {
        return INSTANCE.privilegedGet(supplier);
    }

    abstract <T> T privilegedGet(Supplier<T> supplier);
}
