/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.jmx;

import java.beans.ConstructorProperties;
import java.util.List;

/**
 * A bean class that holds various QueuedNotificationManager statistic metrics. This class is
 * suitable for mapping to the MXBean CompositeDataSupport type.
 *
 * @author Thomas Pantelis
 * @see QueuedNotificationManagerMXBeanImpl
 */
public class QueuedNotificationManagerStats {

    private final int maxListenerQueueSize;
    private final List<ListenerNotificationQueueStats> currentListenerQueueStats;

    @ConstructorProperties({"maxListenerQueueSize","currentListenerQueueStats"})
    public QueuedNotificationManagerStats( int maxListenerQueueSize,
            List<ListenerNotificationQueueStats> currentListenerQueueStats ) {
        super();
        this.maxListenerQueueSize = maxListenerQueueSize;
        this.currentListenerQueueStats = currentListenerQueueStats;
    }

    public List<ListenerNotificationQueueStats> getCurrentListenerQueueStats() {
        return currentListenerQueueStats;
    }

    public int getMaxListenerQueueSize() {
        return maxListenerQueueSize;
    }
}
