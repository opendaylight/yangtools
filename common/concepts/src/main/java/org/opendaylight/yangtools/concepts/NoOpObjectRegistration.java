/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implementation of {@link ObjectRegistration} which does nothing in its {@link #close()} method.
 *
 * @param <T> Type of registered object
 */
@Beta
@NonNullByDefault
public class NoOpObjectRegistration<T> implements Immutable, ObjectRegistration<T> {
    private final T instance;

    NoOpObjectRegistration(final T instance) {
        this.instance = requireNonNull(instance);
    }

    public static <T> ObjectRegistration<T> of(final T instance) {
        return new NoOpObjectRegistration<>(instance);
    }

    @Override
    public final T getInstance() {
        return instance;
    }

    @Override
    public final void close() {
        // No-op
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("instance", instance).toString();
    }
}
