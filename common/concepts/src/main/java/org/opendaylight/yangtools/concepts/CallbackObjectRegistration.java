/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;

final class CallbackObjectRegistration<T> extends AbstractObjectRegistration<T> {
    private final Runnable callback;

    CallbackObjectRegistration(final T instance, final Runnable callback) {
        super(instance);
        this.callback = requireNonNull(callback);
    }

    @Override
    protected void removeRegistration() {
        callback.run();
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("callback", callback);
    }
}
