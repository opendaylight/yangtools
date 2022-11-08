/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SynchronizedDurationStatsTrackerTest {
    @Test
    public void testAllMethodsOfSynchronizedDurationStatsTracker() {
        final SynchronizedDurationStatsTracker statsTracker = new SynchronizedDurationStatsTracker();
        statsTracker.addDuration(1000);
        statsTracker.addDuration(2000);
        statsTracker.addDuration(3000);

        assertEquals("Shortest recorded duration should be '1000'.", 1000, statsTracker.getShortest().duration());
        assertEquals("Average recorded duration should be '2000'.", 2000, statsTracker.getAverageDuration(), 0.0001);
        assertEquals("Longest recorded duration should be '3000'.", 3000, statsTracker.getLongest().duration());
        assertEquals("Total recorded duration count should be '3'.", 3, statsTracker.getTotalDurations());

        statsTracker.reset();

        assertNull("Shortest recorded duration should be 'null'.", statsTracker.getShortest());
        assertEquals("Average recorded duration should be '0'.", 0, statsTracker.getAverageDuration(), 0.0001);
        assertNull("Longest recorded duration should be '0'.", statsTracker.getLongest());
        assertEquals("Total recorded duration should be '0'.", 0, statsTracker.getTotalDurations());
    }
}
