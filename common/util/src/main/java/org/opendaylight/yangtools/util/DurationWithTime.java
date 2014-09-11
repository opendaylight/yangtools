/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

/**
 * Utility holder for a duration/time of occurance.
 */
final class DurationWithTime {
    private final long duration;
    private final long timeMillis;

    DurationWithTime(final long duration, final long timeMillis) {
        this.duration = duration;
        this.timeMillis = timeMillis;
    }

    long getDuration() {
        return duration;
    }

    long getTimeMillis() {
        return timeMillis;
    }
}