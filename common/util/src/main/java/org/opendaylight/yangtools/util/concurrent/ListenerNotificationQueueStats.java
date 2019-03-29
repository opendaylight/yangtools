/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static java.util.Objects.requireNonNull;

import java.beans.ConstructorProperties;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class used by the {@link QueuedNotificationManager} that contains a snapshot of notification
 * queue statistics for a listener.
 *
 * @author Thomas Pantelis
 * @see QueuedNotificationManager
 */
@NonNullByDefault
public class ListenerNotificationQueueStats {
    private final String listenerClassName;
    private final int currentQueueSize;

    @ConstructorProperties({ "listenerClassName", "currentQueueSize" })
    public ListenerNotificationQueueStats(final String listenerClassName, final int currentQueueSize) {
        this.listenerClassName = requireNonNull(listenerClassName);
        this.currentQueueSize = currentQueueSize;
    }

    /**
     * Returns the name of the listener class.
     */
    public String getListenerClassName() {
        return listenerClassName;
    }

    /**
     * Returns the current notification queue size.
     */
    public int getCurrentQueueSize() {
        return currentQueueSize;
    }
}
