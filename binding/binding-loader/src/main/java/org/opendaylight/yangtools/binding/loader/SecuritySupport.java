/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.loader;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Internal machinery to deal with {@link AccessController}: if we detect Java 24+ at runtime, we do not touch
 * {@code AccessController} and rely on <a href="https://openjdk.org/jeps/486">JEP-486</a> semantics instead.
 */
// TODO: can we simplify this with a multi-release jar?
@NonNullByDefault
abstract sealed class SecuritySupport {
    /**
     * Java <24: defer to {@link AccessController#doPrivileged(PrivilegedAction)}.
     */
    private static final class WithAccessController extends SecuritySupport {
        @Override
        @SuppressWarnings({ "deprecation", "removal" })
        <T> T privilegedGet(final Supplier<T> supplier) {
            return AccessController.doPrivileged((PrivilegedAction<T>) supplier::get);
        }
    }

    /**
     * Java >=24: {@link AccessController#doPrivileged(PrivilegedAction)} is a no-op wrapper. Inline its behaviour
     * without touching it, as it may disappear in later versions of Java.
     */
    // FIXME: assume this behaviour and eliminate this entire abstract once we require Java 24+
    private static final class JEP486 extends SecuritySupport {
        @Override
        <T> T privilegedGet(final Supplier<T> supplier) {
            return supplier.get();
        }
    }

    private static final SecuritySupport INSTANCE =
        Runtime.version().feature() >= 24 ? new WithAccessController() : new JEP486();

    static final <T> T get(final Supplier<T> supplier) {
        return INSTANCE.privilegedGet(supplier);
    }

    abstract <T> T privilegedGet(Supplier<T> supplier);
}