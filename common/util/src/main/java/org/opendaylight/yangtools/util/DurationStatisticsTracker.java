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

import com.google.common.annotations.Beta;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class that calculates and tracks time duration statistics.
 *
 * @author Thomas Pantelis
 * @author Robert Varga
 */
@Beta
public abstract class DurationStatisticsTracker {
    private static final DecimalFormat DECIMAL_FORMAT;

    static {
        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DECIMAL_FORMAT = new DecimalFormat("0.00", symbols);
    }

    /**
     * Create a concurrent {@link DurationStatisticsTracker}, which performs well in very contended environments.
     *
     * @return A new instance.
     */
    public static DurationStatisticsTracker createConcurrent() {
        return new ConcurrentDurationStatisticsTracker();
    }

    /**
     * Create a synchronized {@link DurationStatisticsTracker}, which performs well in non-contended environments.
     *
     * @return A new instance.
     */
    public static DurationStatisticsTracker createSynchronized() {
        return new SynchronizedDurationStatsTracker();
    }

    /**
     * Add a duration to track.
     *
     * @param duration non-negative duration in nanoseconds.
     */
    public abstract void addDuration(long duration);

    /**
     * Returns the average duration in nanoseconds.
     *
     * @return average duration in nanoseconds.
     */
    public abstract double getAverageDuration();

    /**
     * Returns the total number of tracked durations.
     *
     * @return Total number of measurements accumulated since last {@link #reset()}.
     */
    public abstract long getTotalDurations();

    /**
     * Resets all statistics back to their defaults.
     */
    public abstract void reset();

    /**
     * Returns the longest duration in nanoseconds.
     *
     * @return the longest duration in nanoseconds.
     */
    public final long getLongestDuration() {
        return getDuration(getLongest());
    }

    /**
     * Returns the shortest duration in nanoseconds.
     *
     * @return the shortest duration in nanoseconds.
     */
    public final long getShortestDuration() {
        return getDuration(getShortest());
    }

    /**
     * Returns the average duration as a displayable String with units, e.g. {@code 12.34 ms}.
     *
     * @return the average duration in human-readable form.
     */
    public final String getDisplayableAverageDuration() {
        return formatDuration(getAverageDuration(), null);
    }

    /**
     * Returns the longest duration as a displayable String with units and the date/time at which it occurred, e.g.
     * {@code 12.34 ms at 08/02/2014 12:30:24}.
     *
     * @return The longest duration and when it has occurred in human-readable form.
     */
    public final String getDisplayableLongestDuration() {
        return formatDuration(getLongest());
    }

    /**
     * Returns the shortest duration as a displayable String with units and the date/time at which it occurred, e.g.
     * {@code 12.34 ms at 08/02/2014 12:30:24}.
     *
     * @return The shortest duration and when it has occurred in human-readable form.
     */
    public final String getDisplayableShortestDuration() {
        return formatDuration(getShortest());
    }

    /**
     * Returns the time stamp of the longest duration.
     *
     * @return the time stamp of the longest duration.
     */
    public final long getTimeOfLongestDuration() {
        return getTimeMillis(getLongest());
    }

    /**
     * Returns the time stamp of the shortest duration.
     *
     * @return the time stamp of the shortest duration.
     */
    public final long getTimeOfShortestDuration() {
        return getTimeMillis(getShortest());
    }

    /**
     * Get the shortest recorded duration and the time when it was recorded.
     *
     * @return Duration and timestamp.
     */
    abstract DurationWithTime getShortest();

    /**
     * Get the longest recorded duration and the time when it was recorded.
     *
     * @return Duration and timestamp.
     */
    abstract DurationWithTime getLongest();

    /**
     * Returns formatted value of number, e.g. "12.34". Always is used dot as decimal separator.
     */
    private static synchronized String formatDecimalValue(final double value) {
        return DECIMAL_FORMAT.format(value);
    }

    private static long getDuration(final DurationWithTime current) {
        return current == null ? 0L : current.duration();
    }

    private static long getTimeMillis(final DurationWithTime current) {
        return current == null ? 0L : current.timeMillis();
    }

    private static String formatDuration(final double duration, final Long timeStamp) {
        final TimeUnit unit = chooseUnit((long) duration);
        final double value = duration / NANOSECONDS.convert(1, unit);

        final StringBuilder sb = new StringBuilder();
        sb.append(formatDecimalValue(value)).append(' ').append(abbreviate(unit));

        if (timeStamp != null) {
            sb.append(String.format(" at %1$tD %1$tT", new Date(timeStamp)));
        }

        return sb.toString();
    }

    private static String formatDuration(final DurationWithTime current) {
        return current == null ? formatDuration(0, null) : formatDuration(current.duration(), current.timeMillis());
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
        return switch (unit) {
            case NANOSECONDS -> "ns";
            case MICROSECONDS -> "μs";
            case MILLISECONDS -> "ms";
            case SECONDS -> "s";
            case MINUTES -> "m";
            case HOURS -> "h";
            case DAYS -> "d";
        };
    }
}
