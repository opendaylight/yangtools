/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.primitives.UnsignedLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Concurrent version of {@link DurationStatisticsTracker}.
 */
// TODO: once DurationStatsTracker is gone make this class final
class ConcurrentDurationStatisticsTracker extends DurationStatisticsTracker {

    private static final AtomicReferenceFieldUpdater<ConcurrentDurationStatisticsTracker, DurationWithTime>
        LONGEST_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ConcurrentDurationStatisticsTracker.class,
                DurationWithTime.class, "longest");

    private static final AtomicReferenceFieldUpdater<ConcurrentDurationStatisticsTracker, DurationWithTime>
        SHORTEST_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ConcurrentDurationStatisticsTracker.class,
                DurationWithTime.class, "shortest");

    private static final AtomicLongFieldUpdater<ConcurrentDurationStatisticsTracker> COUNT_UPDATER =
            AtomicLongFieldUpdater.newUpdater(ConcurrentDurationStatisticsTracker.class, "count");
    private static final AtomicLongFieldUpdater<ConcurrentDurationStatisticsTracker> SUM_UPDATER =
            AtomicLongFieldUpdater.newUpdater(ConcurrentDurationStatisticsTracker.class, "sum");

    private volatile long sum = 0;
    private volatile long count = 0;
    private volatile DurationWithTime longest = null;
    private volatile DurationWithTime shortest = null;

    ConcurrentDurationStatisticsTracker() {
        // Hidden on purpose
    }

    @Override
    public final void addDuration(final long duration) {
        // First update the quick stats
        SUM_UPDATER.addAndGet(this, duration);
        COUNT_UPDATER.incrementAndGet(this);

        /*
         * Now the hairy 'min/max' things. The notion of "now" we cache,
         * so the first time we use it, we do not call it twice. We populate
         * it lazily, though.
         *
         * The longest/shortest stats both are encapsulated in an object,
         * so we update them atomically and we minimize the number of volatile
         * operations.
         */
        DurationWithTime current = shortest;
        if (current == null || duration < current.getDuration()) {
            final DurationWithTime newObj = new DurationWithTime(duration, System.currentTimeMillis());
            while (!SHORTEST_UPDATER.weakCompareAndSet(this, current, newObj)) {
                current = shortest;
                if (current != null && duration >= current.getDuration()) {
                    break;
                }
            }
        }

        current = longest;
        if (current == null || duration > current.getDuration()) {
            final DurationWithTime newObj = new DurationWithTime(duration, System.currentTimeMillis());
            while (!LONGEST_UPDATER.weakCompareAndSet(this, current, newObj)) {
                current = longest;
                if (current != null && duration <= current.getDuration()) {
                    break;
                }
            }
        }
    }

    @Override
    public final long getTotalDurations() {
        return count;
    }

    @Override
    public final double getAverageDuration() {
        final long myCount = count;
        return myCount == 0 ? 0 : UnsignedLong.fromLongBits(sum).doubleValue() / myCount;
    }

    @Override
    public final synchronized void reset() {
        // Synchronized is just to make sure we do not have concurrent resets :)
        longest = null;
        shortest = null;
        count = 0;
        sum = 0;
    }

    @Override
    protected final DurationWithTime getLongest() {
        return longest;
    }

    @Override
    protected final DurationWithTime getShortest() {
        return shortest;
    }
}
