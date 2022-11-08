/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.primitives.UnsignedLong;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Concurrent version of {@link DurationStatisticsTracker}.
 */
// TODO: once DurationStatsTracker is gone make this class final
class ConcurrentDurationStatisticsTracker extends DurationStatisticsTracker {
    private static final VarHandle LONGEST;
    private static final VarHandle SHORTEST;

    static {
        final var lookup = MethodHandles.lookup();
        try {
            LONGEST = lookup.findVarHandle(
                ConcurrentDurationStatisticsTracker.class, "longest", DurationWithTime.class);
            SHORTEST = lookup.findVarHandle(
                ConcurrentDurationStatisticsTracker.class, "shortest", DurationWithTime.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final AtomicLongFieldUpdater<ConcurrentDurationStatisticsTracker> COUNT_UPDATER =
            AtomicLongFieldUpdater.newUpdater(ConcurrentDurationStatisticsTracker.class, "count");
    private static final AtomicLongFieldUpdater<ConcurrentDurationStatisticsTracker> SUM_UPDATER =
            AtomicLongFieldUpdater.newUpdater(ConcurrentDurationStatisticsTracker.class, "sum");

    private volatile long sum;
    private volatile long count;
    private volatile DurationWithTime longest;
    private volatile DurationWithTime shortest;

    ConcurrentDurationStatisticsTracker() {
        // Hidden on purpose
    }

    @Override
    public final void addDuration(final long duration) {
        // First update the quick stats
        SUM_UPDATER.addAndGet(this, duration);
        COUNT_UPDATER.incrementAndGet(this);

        /*
         * Now the hairy 'min/max' things. The notion of "now" we cache, so the first time we use it, we do not call it
         * twice. We populate it lazily, though.
         *
         * The longest/shortest stats both are encapsulated in an object, so we update them atomically and we minimize
         * the number of volatile operations.
         */
        final var currentShortest = (DurationWithTime) SHORTEST.getAcquire(this);
        if (currentShortest == null || duration < currentShortest.getDuration()) {
            updateShortest(currentShortest, duration);
        }
        final var currentLongest = (DurationWithTime) LONGEST.getAcquire(this);
        if (currentLongest == null || duration > currentLongest.getDuration()) {
            updateLongest(currentLongest, duration);
        }
    }

    private void updateShortest(final DurationWithTime prev, final long duration) {
        final var newObj = new DurationWithTime(duration, System.currentTimeMillis());

        var expected = prev;
        while (true) {
            final var witness = (DurationWithTime) SHORTEST.compareAndExchangeRelease(this, expected, newObj);
            if (witness == expected || duration >= witness.getDuration()) {
                break;
            }
            expected = witness;
        }
    }

    private void updateLongest(final DurationWithTime prev, final long duration) {
        final var newObj = new DurationWithTime(duration, System.currentTimeMillis());

        var expected = prev;
        while (true) {
            final var witness = (DurationWithTime) LONGEST.compareAndExchangeRelease(this, expected, newObj);
            if (witness == expected || duration <= witness.getDuration()) {
                break;
            }
            expected = witness;
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
