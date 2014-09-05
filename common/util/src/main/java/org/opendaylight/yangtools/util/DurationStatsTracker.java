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

import com.google.common.util.concurrent.AtomicDouble;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class that calculates and tracks time duration statistics.
 *
 * @author Thomas Pantelis
 */
public class DurationStatsTracker {

    private static final DecimalFormat decimalFormat;

    private final AtomicLong totalDurations = new AtomicLong();
    private final AtomicLong longestDuration = new AtomicLong();
    private volatile long timeOfLongestDuration;
    private final AtomicLong shortestDuration = new AtomicLong(Long.MAX_VALUE);
    private volatile long timeOfShortestDuration;
    private final AtomicDouble averageDuration = new AtomicDouble();

    static {
        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat("0.00", symbols);
    }

    /**
     * Add a duration to track.
     *
     * @param duration
     *            the duration in nanoseconds.
     */
    public void addDuration(long duration) {

        double currentAve = averageDuration.get();
        long currentTotal = totalDurations.get();

        long newTotal = currentTotal + 1;

        // Calculate moving cumulative average.
        double newAve = currentAve * currentTotal / newTotal + (double) duration / (double) newTotal;

        averageDuration.compareAndSet(currentAve, newAve);
        totalDurations.compareAndSet(currentTotal, newTotal);

        long longest = longestDuration.get();
        if (duration > longest) {
            if (longestDuration.compareAndSet(longest, duration)) {
                timeOfLongestDuration = System.currentTimeMillis();
            }
        }

        long shortest = shortestDuration.get();
        if (duration < shortest) {
            if (shortestDuration.compareAndSet(shortest, duration)) {
                timeOfShortestDuration = System.currentTimeMillis();
            }
        }
    }

    /**
     * Returns the total number of tracked durations.
     */
    public long getTotalDurations() {
        return totalDurations.get();
    }

    /**
     * Returns the longest duration in nanoseconds.
     */
    public long getLongestDuration() {
        return longestDuration.get();
    }

    /**
     * Returns the shortest duration in nanoseconds.
     */
    public long getShortestDuration() {
        long shortest = shortestDuration.get();
        return shortest < Long.MAX_VALUE ? shortest : 0;
    }

    /**
     * Returns the average duration in nanoseconds.
     */
    public double getAverageDuration() {
        return averageDuration.get();
    }

    /**
     * Returns the time stamp of the longest duration.
     */
    public long getTimeOfLongestDuration() {
        return timeOfLongestDuration;
    }

    /**
     * Returns the time stamp of the shortest duration.
     */
    public long getTimeOfShortestDuration() {
        return timeOfShortestDuration;
    }

    /**
     * Resets all statistics back to their defaults.
     */
    public void reset() {
        totalDurations.set(0);
        longestDuration.set(0);
        timeOfLongestDuration = 0;
        shortestDuration.set(Long.MAX_VALUE);
        timeOfShortestDuration = 0;
        averageDuration.set(0.0);
    }

    /**
     * Returns the average duration as a displayable String with units, e.g.
     * "12.34 ms".
     */
    public String getDisplayableAverageDuration() {
        return formatDuration(getAverageDuration(), 0);
    }

    /**
     * Returns the shortest duration as a displayable String with units and the
     * date/time at which it occurred, e.g. "12.34 ms at 08/02/2014 12:30:24".
     */
    public String getDisplayableShortestDuration() {
        return formatDuration(getShortestDuration(), getTimeOfShortestDuration());
    }

    /**
     * Returns the longest duration as a displayable String with units and the
     * date/time at which it occurred, e.g. "12.34 ms at 08/02/2014 12:30:24".
     */
    public String getDisplayableLongestDuration() {
        return formatDuration(getLongestDuration(), getTimeOfLongestDuration());
    }

    /**
     * Returns formatted value of number, e.g. "12.34". Always is used dot as
     * decimal separator.
     */
    private static synchronized String formatDecimalValue(double value) {
        return decimalFormat.format(value);
    }

    private String formatDuration(double duration, long timeStamp) {
        TimeUnit unit = chooseUnit((long) duration);
        double value = duration / NANOSECONDS.convert(1, unit);

        return timeStamp > 0 ? String.format("%s %s at %3$tD %3$tT", formatDecimalValue(value), abbreviate(unit),
                new Date(timeStamp)) : String.format("%s %s", formatDecimalValue(value), abbreviate(unit));
    }

    private static TimeUnit chooseUnit(long nanos) {
        if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
            return SECONDS;
        }

        if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MILLISECONDS;
        }

        if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MICROSECONDS;
        }

        return NANOSECONDS;
    }

    private static String abbreviate(TimeUnit unit) {
        switch (unit) {
        case NANOSECONDS:
            return "ns";
        case MICROSECONDS:
            return "\u03bcs"; // μs
        case MILLISECONDS:
            return "ms";
        case SECONDS:
            return "s";
        default:
            return "";
        }
    }
}
