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
    private static final long NOT_SET = -1;

    // Hot fields in the order in which they are accessed
    private long durationSum = 0;
    private long durationCount = 0;
    private long shortestDuration = NOT_SET;
    private long longestDuration = NOT_SET;

    // Cold fields, longest has a higher chance of being accessed
    private long longestTimestamp;
    private long shortestTimestamp;

    SynchronizedDurationStatsTracker() {
        // Hidden on purpose
    }

    @Override
    public synchronized void addDuration(final long duration) {
        durationSum += duration;
        durationCount++;

        if (duration < shortestDuration || shortestDuration == NOT_SET) {
            shortestDuration = duration;
            shortestTimestamp = System.currentTimeMillis();
        }
        if (duration > longestDuration) {
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
        longestDuration = NOT_SET;
        shortestDuration = NOT_SET;
    }

    @Override
    protected synchronized DurationWithTime getShortest() {
        return shortestDuration == NOT_SET ? null : new DurationWithTime(shortestDuration, shortestTimestamp);
    }

    @Override
    protected synchronized DurationWithTime getLongest() {
        return longestDuration == NOT_SET ? null : new DurationWithTime(longestDuration, longestTimestamp);
    }
}
