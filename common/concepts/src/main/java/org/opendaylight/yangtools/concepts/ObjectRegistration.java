/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Class representing a registration of an object. Such a registration is a proper resource and should be cleaned up
 * when no longer required, so references to the object can be removed. This mechanism lies above the usual Java
 * reference mechanism, as the entity where the object is registered may reside outside of the Java Virtual Machine.
 *
 * @param <T> Type of registered object
 */
public interface ObjectRegistration<T> extends Registration {
    /**
     * Return the object instance.
     *
     * @return Registered object.
     */
    @NonNull T getInstance();

    /**
     * Return a new {@link ObjectRegistration} which will run specified callback when it is {@link #close()}d.
     *
     * @param <T> Type of registered object
     * @param instance Object instance
     * @param callback Callback to invoke
     * @return A new {@link ObjectRegistration}
     * @throws NullPointerException if any argument is {@code null}
     */
    static <T> @NonNull ObjectRegistration<T> of(final T instance, final Runnable callback) {
        return new CallbackObjectRegistration<>(instance, callback);
    }
}
