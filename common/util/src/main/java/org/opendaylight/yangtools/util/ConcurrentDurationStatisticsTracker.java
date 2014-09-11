/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.twitter.jsr166e.LongAdder;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Concurrent version of {@link DurationStatisticsTracker}.
 */
// TODO: once DurationStatsTracker is gone make this class final
class ConcurrentDurationStatisticsTracker extends DurationStatisticsTracker {
    private static final AtomicReferenceFieldUpdater<ConcurrentDurationStatisticsTracker, DurationWithTime> LONGEST_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(ConcurrentDurationStatisticsTracker.class, DurationWithTime.class, "longest");
    private static final AtomicReferenceFieldUpdater<ConcurrentDurationStatisticsTracker, DurationWithTime> SHORTEST_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(ConcurrentDurationStatisticsTracker.class, DurationWithTime.class, "shortest");

    private final LongAdder durationsSum = new LongAdder();
    private final LongAdder durationsCount = new LongAdder();

    private volatile DurationWithTime longest = null;
    private volatile DurationWithTime shortest = null;

    ConcurrentDurationStatisticsTracker() {
        // Hidden on purpose
    }

    @Override
    public final void addDuration(final long duration) {
        // First update the quick stats
        durationsSum.add(duration);
        durationsCount.increment();

        /*
         * Now the hairy 'min/max' things. The notion of "now" we cache,
         * so the first time we use it, we do not call it twice. We populate
         * it lazily, though.
         *
         * The longest/shortest stats both are encapsulated in an object,
         * so we update them atomically and we minimize the number of volatile
         * operations.
         */
        Long now = null;
        DurationWithTime current = shortest;
        if (current == null || current.getDuration() > duration) {
            now = getMillis(now);

            final DurationWithTime newObj = new DurationWithTime(duration, now);
            while (!SHORTEST_UPDATER.compareAndSet(this, current, newObj)) {
                current = shortest;
                if (current != null && current.getDuration() <= duration) {
                    break;
                }
            }
        }

        current = longest;
        if (current == null || current.getDuration() < duration) {
            now = getMillis(now);

            final DurationWithTime newObj = new DurationWithTime(duration, now);
            while (!LONGEST_UPDATER.compareAndSet(this, current, newObj)) {
                current = longest;
                if (current != null && current.getDuration() >= duration) {
                    break;
                }
            }
        }
    }

    @Override
    public final long getTotalDurations() {
        return durationsCount.sum();
    }

    @Override
    public final double getAverageDuration() {
        final long sum = durationsSum.sum();
        final long count = durationsCount.sum();

        return count == 0 ? 0 : ((double) sum) / count;
    }

    @Override
    public final synchronized void reset() {
        // Synchronized is just to make sure we do not have concurrent resets :)
        longest = null;
        shortest = null;
        durationsCount.reset();
        durationsSum.reset();
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
