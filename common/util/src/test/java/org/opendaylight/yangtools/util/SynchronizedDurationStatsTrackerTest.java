/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SynchronizedDurationStatsTrackerTest {
    @Test
    void testAllMethodsOfSynchronizedDurationStatsTracker() {
        final var statsTracker = new SynchronizedDurationStatsTracker();
        statsTracker.addDuration(1000);
        statsTracker.addDuration(2000);
        statsTracker.addDuration(3000);

        assertEquals(1000, statsTracker.getShortest().duration(), "Shortest recorded duration should be '1000'.");
        assertEquals(2000, statsTracker.getAverageDuration(), 0.0001, "Average recorded duration should be '2000'.");
        assertEquals(3000, statsTracker.getLongest().duration(), "Longest recorded duration should be '3000'.");
        assertEquals(3, statsTracker.getTotalDurations(), "Total recorded duration count should be '3'.");

        statsTracker.reset();

        assertNull(statsTracker.getShortest(), "Shortest recorded duration should be 'null'.");
        assertEquals(0, statsTracker.getAverageDuration(), 0.0001, "Average recorded duration should be '0'.");
        assertNull(statsTracker.getLongest(), "Longest recorded duration should be '0'.");
        assertEquals(0, statsTracker.getTotalDurations(), "Total recorded duration should be '0'.");
    }
}
