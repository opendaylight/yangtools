/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.primitives.UnsignedLong;

/**
 * Non-concurrent implementation, useful for non-contended cases.
 */
final class SynchronizedDurationStatsTracker extends DurationStatisticsTracker {
    private static final long NEVER = 0;

    // Hot fields in the order in which they are accessed
    private long durationSum = 0;
    private long durationCount = 0;
    private long shortestDuration = 0;
    private long longestDuration = 0;

    // Cold fields, longest has a higher chance of being accessed
    private long longestTimestamp = NEVER;
    private long shortestTimestamp = NEVER;

    SynchronizedDurationStatsTracker() {
        // Hidden on purpose
    }

    @Override
    public synchronized void addDuration(final long duration) {
        durationSum += duration;
        durationCount++;

        if (duration < shortestDuration || shortestTimestamp == NEVER) {
            shortestDuration = duration;
            shortestTimestamp = System.currentTimeMillis();
        }
        if (duration > longestDuration || longestTimestamp == NEVER) {
            longestDuration = duration;
            longestTimestamp = System.currentTimeMillis();
        }
    }

    @Override
    public synchronized double getAverageDuration() {
        return durationCount == 0 ? 0 : UnsignedLong.fromLongBits(durationSum).doubleValue() / durationCount;
    }

    @Override
    public synchronized long getTotalDurations() {
        return durationCount;
    }

    @Override
    public synchronized void reset() {
        durationSum = 0;
        durationCount = 0;
        longestDuration = 0;
        shortestDuration = 0;
        longestTimestamp = NEVER;
        shortestTimestamp = NEVER;
    }

    @Override
    protected synchronized DurationWithTime getShortest() {
        return shortestTimestamp == 0 ? null : new DurationWithTime(shortestDuration, shortestTimestamp);
    }

    @Override
    protected synchronized DurationWithTime getLongest() {
        return longestTimestamp == 0 ? null : new DurationWithTime(longestDuration, longestTimestamp);
    }
}
