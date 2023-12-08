/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implementation of {@link ListenerRegistration} which does nothing in its {@link #close()} method.
 *
 * @param <T> Type of registered listener
 * @deprecated Use {@link NoOpObjectRegistration} instead
 */
@NonNullByDefault
@Deprecated(since = "12.0.0", forRemoval = true)
public final class NoOpListenerRegistration<T extends EventListener> extends NoOpObjectRegistration<T>
        implements ListenerRegistration<T> {
    private NoOpListenerRegistration(final T instance) {
        super(instance);
    }

    public static <T extends EventListener> ListenerRegistration<T> of(final T instance) {
        return new NoOpListenerRegistration<>(instance);
    }
}
