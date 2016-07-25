/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;

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
        assertEquals("getTotalDurations", 1, tracker.getTotalDurations());
        assertEquals("getAverageDuration", 10000.0, tracker.getAverageDuration(), 0.1);
        assertEquals("getLongestDuration", 10000, tracker.getLongestDuration());
        assertEquals("getShortestDuration", 10000, tracker.getShortestDuration());

        tracker.addDuration(30000);
        assertEquals("getTotalDurations", 2, tracker.getTotalDurations());
        assertEquals("getAverageDuration", 20000.0, tracker.getAverageDuration(), 0.1);
        assertEquals("getLongestDuration", 30000, tracker.getLongestDuration());
        assertEquals("getShortestDuration", 10000, tracker.getShortestDuration());

        verifyDisplayableString("getDisplayableAverageDuration",
                tracker.getDisplayableAverageDuration(), "20.0");
        verifyDisplayableString("getDisplayableLongestDuration",
                tracker.getDisplayableLongestDuration(), "30.0");
        verifyDisplayableString("getDisplayableShortestDuration",
                tracker.getDisplayableShortestDuration(), "10.0");

        tracker.addDuration(10000);
        assertEquals("getTotalDurations", 3, tracker.getTotalDurations());
        assertEquals("getAverageDuration", 16666.0, tracker.getAverageDuration(), 1.0);
        assertEquals("getLongestDuration", 30000, tracker.getLongestDuration());
        assertEquals("getShortestDuration", 10000, tracker.getShortestDuration());

        tracker.addDuration(5000);
        assertEquals("getTotalDurations", 4, tracker.getTotalDurations());
        assertEquals("getAverageDuration", 13750.0, tracker.getAverageDuration(), 1.0);
        assertEquals("getLongestDuration", 30000, tracker.getLongestDuration());
        assertEquals("getShortestDuration", 5000, tracker.getShortestDuration());

        tracker.reset();
        assertEquals("getTotalDurations", 0, tracker.getTotalDurations());
        assertEquals("getAverageDuration", 0.0, tracker.getAverageDuration(), 0.1);
        assertEquals("getLongestDuration", 0, tracker.getLongestDuration());
        assertEquals("getShortestDuration", 0, tracker.getShortestDuration());

        tracker.addDuration(10000);
        assertEquals("getTotalDurations", 1, tracker.getTotalDurations());
        assertEquals("getAverageDuration", 10000.0, tracker.getAverageDuration(), 0.1);
        assertEquals("getLongestDuration", 10000, tracker.getLongestDuration());
        assertEquals("getShortestDuration", 10000, tracker.getShortestDuration());
    }

    private static void verifyDisplayableString(final String name, final String actual, final String expPrefix) {
        assertEquals(name + " starts with " + expPrefix + ". Actual: " + actual,
                true, actual.startsWith(expPrefix));
    }
}
