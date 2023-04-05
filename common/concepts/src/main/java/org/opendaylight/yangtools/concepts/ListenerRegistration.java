/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Class representing a {@link Registration} of an {@link EventListener}. This interface provides the additional
 * guarantee that the process of unregistration cannot fail for predictable reasons.
 *
 * @param <T> Type of registered listener
 */
public interface ListenerRegistration<T extends EventListener> extends ObjectRegistration<T> {
    /**
     * Unregister the listener. Note that invocations enqueued to the listener are not subject to synchronization
     * rules, and events may be delivered to the listener after this method completes.
     *
     * <p>
     * While the interface contract allows an implementation to ignore the occurrence of RuntimeExceptions,
     * implementations are strongly encouraged to deal with such exceptions internally and to ensure invocations of
     * this method do not fail in such circumstances.
     */
    @Override
    void close();

    /**
     * Return a new {@link ObjectRegistration} which will run specified callback when it is {@link #close()}d.
     *
     * @param <T> Type of registered listener
     * @param instance Listener instance
     * @param callback Callback to invoke
     * @return A new {@link ObjectRegistration}
     * @throws NullPointerException if any argument is {@code null}
     */
    static <T extends EventListener> @NonNull ListenerRegistration<T> of(final T instance, final Runnable callback) {
        return new CallbackListenerRegistration<>(instance, callback);
    }
}
