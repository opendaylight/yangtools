/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static java.util.Objects.requireNonNull;

import java.util.List;

final class QueuedNotificationManagerMXBeanImpl implements QueuedNotificationManagerMXBean {
    private final QueuedNotificationManager<?, ?> manager;

    QueuedNotificationManagerMXBeanImpl(final QueuedNotificationManager<?, ?> manager) {
        this.manager = requireNonNull(manager);
    }

    /**
     * Returns a list of stat instances for each current listener notification task in progress.
     */
    @Override
    public List<ListenerNotificationQueueStats> getCurrentListenerQueueStats() {
        return manager.getListenerNotificationQueueStats();
    }

    /**
     * Returns the configured maximum listener queue size.
     */
    @Override
    public int getMaxListenerQueueSize() {
        return manager.getMaxQueueCapacity();
    }
}
