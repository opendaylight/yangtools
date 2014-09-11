/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.twitter.jsr166e.LongAdder;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that calculates and tracks time duration statistics.
 *
 * @author Thomas Pantelis
 * @author Robert Varga
 */
public class DurationStatsTracker {
    private static final AtomicReferenceFieldUpdater<DurationStatsTracker, DurationWithTime> LONGEST_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(DurationStatsTracker.class, DurationWithTime.class, "longest");
    private static final AtomicReferenceFieldUpdater<DurationStatsTracker, DurationWithTime> SHORTEST_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(DurationStatsTracker.class, DurationWithTime.class, "shortest");
    private static final Logger LOG = LoggerFactory.getLogger(DurationStatsTracker.class);
    private static final DecimalFormat DECIMAL_FORMAT;

    private final LongAdder durationsSum = new LongAdder();
    private final LongAdder durationsCount = new LongAdder();

    private volatile DurationWithTime longest = null;
    private volatile DurationWithTime shortest = null;

    static {
        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DECIMAL_FORMAT = new DecimalFormat("0.00", symbols);
    }

    /**
     * Add a duration to track.
     *
     * @param duration
     *            the duration in nanoseconds.
     */
    public void addDuration(final long duration) {
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
        DurationWithTime current = shortest;
        if (current == null || current.getDuration() > duration) {
            final DurationWithTime newObj = new DurationWithTime(duration, System.currentTimeMillis());
            while (!SHORTEST_UPDATER.compareAndSet(this, current, newObj)) {
                current = shortest;
                if (current != null && current.getDuration() <= duration) {
                    break;
                }
            }
        }

        current = longest;
        if (current == null || current.getDuration() < duration) {
            final DurationWithTime newObj = new DurationWithTime(duration, System.currentTimeMillis());
            while (!LONGEST_UPDATER.compareAndSet(this, current, newObj)) {
                current = longest;
                if (current != null && current.getDuration() >= duration) {
                    break;
                }
            }
        }
    }

    /**
     * Returns the total number of tracked durations.
     */
    public long getTotalDurations() {
        return durationsCount.sum();
    }

    private static long getDuration(final DurationWithTime current) {
        return current == null ? 0L : current.getDuration();
    }

    private static long getTimeMillis(final DurationWithTime current) {
        return current == null ? 0L : current.getTimeMillis();
    }

    /**
     * Returns the longest duration in nanoseconds.
     */
    public long getLongestDuration() {
        return getDuration(longest);
    }

    /**
     * Returns the shortest duration in nanoseconds.
     */
    public long getShortestDuration() {
        return getDuration(shortest);
    }

    /**
     * Returns the average duration in nanoseconds.
     */
    public double getAverageDuration() {
        final long sum = durationsSum.sum();
        final long count = durationsCount.sum();

        return count == 0 ? 0 : ((double) sum) / count;
    }

    /**
     * Returns the time stamp of the longest duration.
     */
    public long getTimeOfLongestDuration() {
        return getTimeMillis(longest);
    }

    /**
     * Returns the time stamp of the shortest duration.
     */
    public long getTimeOfShortestDuration() {
        return getTimeMillis(shortest);
    }

    /**
     * Resets all statistics back to their defaults.
     */
    public synchronized void reset() {
        longest = null;
        shortest = null;
        durationsCount.reset();
        durationsSum.reset();
    }

    /**
     * Returns the average duration as a displayable String with units, e.g.
     * "12.34 ms".
     */
    public String getDisplayableAverageDuration() {
        return formatDuration(getAverageDuration(), null);
    }

    /**
     * Returns the shortest duration as a displayable String with units and the
     * date/time at which it occurred, e.g. "12.34 ms at 08/02/2014 12:30:24".
     */
    public String getDisplayableShortestDuration() {
        return formatDuration(shortest);
    }

    /**
     * Returns the longest duration as a displayable String with units and the
     * date/time at which it occurred, e.g. "12.34 ms at 08/02/2014 12:30:24".
     */
    public String getDisplayableLongestDuration() {
        return formatDuration(longest);
    }

    /**
     * Returns formatted value of number, e.g. "12.34". Always is used dot as
     * decimal separator.
     */
    private static synchronized String formatDecimalValue(final double value) {
        return DECIMAL_FORMAT.format(value);
    }

    private static String formatDuration(final DurationWithTime current) {
        if (current != null) {
            return formatDuration(current.getDuration(), current.getTimeMillis());
        } else {
            return formatDuration(0, null);
        }
    }

    private static String formatDuration(final double duration, final Long timeStamp) {
        final TimeUnit unit = chooseUnit((long) duration);
        final double value = duration / NANOSECONDS.convert(1, unit);

        final StringBuilder sb = new StringBuilder();
        sb.append(formatDecimalValue(value));
        sb.append(' ');
        sb.append(abbreviate(unit));

        if (timeStamp != null) {
            sb.append(String.format(" at %1$tD %1$tT", new Date(timeStamp)));
        }

        return sb.toString();
    }

    private static TimeUnit chooseUnit(final long nanos) {
        // TODO: this could be inlined, as we are doing needless divisions
        if (NANOSECONDS.toSeconds(nanos) > 0) {
            return SECONDS;
        }
        if (NANOSECONDS.toMillis(nanos) > 0) {
            return MILLISECONDS;
        }
        if (NANOSECONDS.toMicros(nanos) > 0) {
            return MICROSECONDS;
        }
        return NANOSECONDS;
    }

    private static String abbreviate(final TimeUnit unit) {
        switch (unit) {
        case NANOSECONDS:
            return "ns";
        case MICROSECONDS:
            return "\u03bcs"; // μs
        case MILLISECONDS:
            return "ms";
        case SECONDS:
            return "s";
        case MINUTES:
            return "m";
        case HOURS:
            return "h";
        case DAYS:
            return "d";
        }

        LOG.warn("Unhandled time unit {}", unit);
        return "";
    }
}
