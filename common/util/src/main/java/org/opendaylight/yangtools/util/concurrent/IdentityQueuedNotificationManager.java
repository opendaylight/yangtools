/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import com.google.common.annotations.Beta;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.util.ForwardingIdentityObject;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager.BatchedInvoker;

@Beta
@NonNullByDefault
public class IdentityQueuedNotificationManager<L, N>
        extends AbstractQueuedNotificationManager<ForwardingIdentityObject<L>, L, N> {
    public IdentityQueuedNotificationManager(final String name, final Executor executor, final int maxQueueCapacity,
        final BatchedInvoker<L, N> listenerInvoker) {
        super(name, executor, maxQueueCapacity, listenerInvoker);
    }

    @Override
    final ForwardingIdentityObject<L> wrap(final L listener) {
        return ForwardingIdentityObject.of(listener);
    }

    @Override
    final L unwrap(final ForwardingIdentityObject<L> key) {
        return key.getDelegate();
    }
}
