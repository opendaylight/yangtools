/*
 * Copyright (c) 2021 Vratko Polak and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.testing;

import static org.junit.Assert.assertEquals;

import org.opendaylight.yangtools.yang.data.api.IdList;

/**
 * A bunch of static methods useful for testing IdList implementations.
 */
public final class SequentialListTester {

    /**
     * Utility classes should have constructors hidden.
     */
    private SequentialListTester() {
    }

    /**
     * One iteration for the scale test.
     * During the iteration, two elements are added and one removed,
     * so the iteration argument value is also the size of list at the start of iteration.
     * Element payloads (and thus hashes and identifiers) are computed from iteration argument.
     * The two elements are added at the end, using insertLast.
     * Element is deleted from the start (by index 0).
     */
    private static <L extends IdList<String, StringedTestingElement>> void iterateOnce(
        final L list, final int iteration
    ) {
        // Starting from 1 as hashing involves addition.
        final StringedTestingElement element0 = new StringedTestingElement(2 * iteration + 2);
        final StringedTestingElement element1 = new StringedTestingElement(2 * iteration + 1);
        // Add first element.
        list.insertLast(element0);
        // Add second element.
        list.insertLast(element1);
        // Remove an element.
        list.remove(0);
    }

    /**
     * A scale test, good implementation take small time to finish.
     * Sequential access can favor some implementations,
     * but can disfavor implementation based on trees with proper balance.
     * The list argument should be instantiated empty list.
     * Scale gives number of iterations to execute.
     * When all iterations are done, hashes of the remaining element are accumulated,
     * and compared to the expected result, so also the iteration speed
     * affects overall test duration.
     * assertEquals is used to verify the expected result.
     */
    public static <L extends IdList<String, StringedTestingElement>> void testScaleN(
        final L list, final int scale, final int result
    ) {
        int accumulator = 65535;  // for myHash to have enough bits to do its thing

        for (int iteration = 0; iteration < scale; iteration++) {
            iterateOnce(list, iteration);
        }
        for (StringedTestingElement element : list) {
            // We use myHash to scramble the accumulator value, so that order matters.
            accumulator = MyHash.myHash(accumulator * 2) + element.getPayload();
        }
        assertEquals(result, accumulator);
    }

    /**
     * Perform test at scale 1.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, StringedTestingElement>> void testScale1(final L list) {
        testScaleN(list, 1, 123873);
    }

    /**
     * Perform test at scale 10.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, StringedTestingElement>> void testScale10(final L list) {
        testScaleN(list, 10, 66857457);
    }

    /**
     * Perform test at scale 100.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, StringedTestingElement>> void testScale100(final L list) {
        testScaleN(list, 100, 1427195467);
    }

    /**
     * Perform test at scale 1000.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, StringedTestingElement>> void testScale1000(final L list) {
        testScaleN(list, 1000, -321597959);
    }

    /**
     * Perform test at scale 10000.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, StringedTestingElement>> void testScale10000(final L list) {
        testScaleN(list, 10000, -1517334888);
    }

    /**
     * Perform test at scale 100000.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, StringedTestingElement>> void testScale100000(final L list) {
        testScaleN(list, 100000, -1244709764);
    }

    // One million takesmore than 60 minutes for SimpleIdList.
    /**
     * Perform test at scale 400000.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, StringedTestingElement>> void testScale400000(final L list) {
        testScaleN(list, 400000, -357945861);
    }

}
