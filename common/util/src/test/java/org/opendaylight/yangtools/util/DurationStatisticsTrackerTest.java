/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;

/**
 * Unit tests for DurationStatsTracker.
 *
 * @author Thomas Pantelis
 */
public class DurationStatisticsTrackerTest {

    @Test
    public void test() {

        DurationStatisticsTracker tracker = DurationStatisticsTracker.createConcurrent();

        tracker.addDuration(10000);
        assertEquals(1, tracker.getTotalDurations(), "getTotalDurations");
        assertEquals(10000.0, tracker.getAverageDuration(), 0.1, "getAverageDuration");
        assertEquals(10000, tracker.getLongestDuration(), "getLongestDuration");
        assertEquals(10000, tracker.getShortestDuration(), "getShortestDuration");

        tracker.addDuration(30000);
        assertEquals(2, tracker.getTotalDurations(), "getTotalDurations");
        assertEquals(20000.0, tracker.getAverageDuration(), 0.1, "getAverageDuration");
        assertEquals(30000, tracker.getLongestDuration(), "getLongestDuration");
        assertEquals(10000, tracker.getShortestDuration(), "getShortestDuration");

        verifyDisplayableString("getDisplayableAverageDuration",
                tracker.getDisplayableAverageDuration(), "20.0");
        verifyDisplayableString("getDisplayableLongestDuration",
                tracker.getDisplayableLongestDuration(), "30.0");
        verifyDisplayableString("getDisplayableShortestDuration",
                tracker.getDisplayableShortestDuration(), "10.0");

        tracker.addDuration(10000);
        assertEquals(3, tracker.getTotalDurations(), "getTotalDurations");
        assertEquals(16666.0, tracker.getAverageDuration(), 1.0, "getAverageDuration");
        assertEquals(30000, tracker.getLongestDuration(), "getLongestDuration");
        assertEquals(10000, tracker.getShortestDuration(), "getShortestDuration");

        tracker.addDuration(5000);
        assertEquals(4, tracker.getTotalDurations(), "getTotalDurations");
        assertEquals(13750.0, tracker.getAverageDuration(), 1.0, "getAverageDuration");
        assertEquals(30000, tracker.getLongestDuration(), "getLongestDuration");
        assertEquals(5000, tracker.getShortestDuration(), "getShortestDuration");

        tracker.reset();
        assertEquals(0, tracker.getTotalDurations(), "getTotalDurations");
        assertEquals(0.0, tracker.getAverageDuration(), 0.1, "getAverageDuration");
        assertEquals(0, tracker.getLongestDuration(), "getLongestDuration");
        assertEquals(0, tracker.getShortestDuration(), "getShortestDuration");

        tracker.addDuration(10000);
        assertEquals(1, tracker.getTotalDurations(), "getTotalDurations");
        assertEquals(10000.0, tracker.getAverageDuration(), 0.1, "getAverageDuration");
        assertEquals(10000, tracker.getLongestDuration(), "getLongestDuration");
        assertEquals(10000, tracker.getShortestDuration(), "getShortestDuration");
    }

    private static void verifyDisplayableString(final String name, final String actual, final String expPrefix) {
        assertTrue(actual.startsWith(expPrefix),
                name + " starts with " + expPrefix + ". Actual: " + actual);
    }
}
