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
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Simple base class for classes which wish to implement {@link Delegator} interface and are not otherwise constrained
 * in their class hierarchy.
 *
 * @param <T> Type of delegate
 */
@Beta
@NonNullByDefault
public abstract class AbstractDelegator<T> implements Delegator<T> {
    private final T delegate;

    protected AbstractDelegator(final T delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public final T getDelegate() {
        return delegate;
    }
}
