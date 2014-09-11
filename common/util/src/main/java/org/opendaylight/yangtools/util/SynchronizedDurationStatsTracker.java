/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

/**
 * Non-concurrent implementation, useful for non-contended cases.
 */
final class SynchronizedDurationStatsTracker extends DurationStatisticsTracker {
    private long durationSum = 0;
    private long durationCount = 0;
    private long longestDuration = 0;
    private long longestTimestamp = 0;
    private long shortestDuration = 0;
    private long shortestTimestamp = 0;

    SynchronizedDurationStatsTracker() {
        // Hidden on purpose
    }

    @Override
    public synchronized void addDuration(final long duration) {
        durationSum += duration;
        durationCount++;

        if (shortestTimestamp == 0 || shortestDuration > duration) {
            shortestDuration = duration;
            shortestTimestamp = System.currentTimeMillis();
        }
        if (longestTimestamp == 0 || longestDuration < duration) {
            longestDuration = duration;
            longestTimestamp = System.currentTimeMillis();
        }
    }

    @Override
    public synchronized double getAverageDuration() {
        return durationCount == 0 ? 0 : ((double) durationSum) / durationCount;
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
        longestTimestamp = 0;
        shortestTimestamp = 0;
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
