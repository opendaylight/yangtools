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
public final class SemirandomListTester {

    /**
     * Utility classes should have constructors hidden.
     */
    private SemirandomListTester() {
    }

    /**
     * One iteration for the scale test.
     * During the iteration, two elements are added and one removed,
     * so the iteration argument value is also the size of list at the start of iteration.
     * Element payloads (and thus hashes and identifiers) are computed from iteration argument.
     * Lower bits of the hash are used for pseudorandom decisions,
     * so every run has the same decisions.
     * The first element is added either at the end or at the start.
     * The second element is added either before or after a pseudorandom index.
     * Either the firstly added or randomly selected (by index) element is deleted.
     */
    private static <L extends IdList<String, HashedTestingElement>> void iterateOnce(
        final L list, final int iteration
    ) {
        // Starting from 1 as hashing involves addition.
        final HashedTestingElement element0 = new HashedTestingElement(2 * iteration + 2);
        final HashedTestingElement element1 = new HashedTestingElement(2 * iteration + 1);
        final int intIdentifier0 = element0.intId();
        final String strIdentifier0 = Integer.toString(intIdentifier0);
        assertEquals(strIdentifier0, element0.getIdentifier());
        String targetId;
        int index0;
        // Add first element.
        if ((intIdentifier0 & 1) != 0) {
            index0 = iteration;
        } else {
            index0 = 0;
        }
        list.add(index0, element0);
        // Add second element.
        targetId = list.get(intIdentifier0 % (iteration + 1)).getIdentifier();
        if ((intIdentifier0 & 2) != 0) {
            list.insertAfter(targetId, element1);
        } else {
            list.insertBefore(targetId, element1);
        }
        // Remove an element.
        if ((intIdentifier0 & 4) != 0) {
            list.removeElement(strIdentifier0);
        } else {
            list.remove(list.get(iteration + 1 - (intIdentifier0 % (iteration + 2))));
        }
    }

    /**
     * A scale test, good implementation take small time to finish.
     * The list argument should be instantiated empty list.
     * Scale gives number of iterations to execute.
     * When all iterations are done, hashes of the remaining element are accumulated,
     * and compared to the expected result, so also the iteration speed
     * affects overall test duration.
     * assertEquals is used to verify the expected result.
     */
    public static <L extends IdList<String, HashedTestingElement>> void testScaleN(
        final L list, final int scale, final int result
    ) {
        int accumulator = 65535;  // for myHash to have enough bits to do its thing

        for (int iteration = 0; iteration < scale; iteration++) {
            iterateOnce(list, iteration);
        }
        for (HashedTestingElement element : list) {
            // We use myHash to scramble the accumulator value, so that order matters.
            accumulator = MyHash.myHash(accumulator * 2) + element.getPayload();
        }
        assertEquals(result, accumulator);
    }

    /**
     * Perform test at scale 1.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, HashedTestingElement>> void testScale1(final L list) {
        testScaleN(list, 1, 123874);
    }

    /**
     * Perform test at scale 10.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, HashedTestingElement>> void testScale10(final L list) {
        testScaleN(list, 10, 66860176);
    }

    /**
     * Perform test at scale 100.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, HashedTestingElement>> void testScale100(final L list) {
        testScaleN(list, 100, -1418088743);
    }

    /**
     * Perform test at scale 1000.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, HashedTestingElement>> void testScale1000(final L list) {
        testScaleN(list, 1000, -259230780);
    }

    /**
     * Perform test at scale 10000.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, HashedTestingElement>> void testScale10000(final L list) {
        testScaleN(list, 10000, -1777463826);
    }

    /**
     * Perform test at scale 100000.
     * The argument should be instantiated empty list.
     */
    public static <L extends IdList<String, HashedTestingElement>> void testScale100000(final L list) {
        testScaleN(list, 100000, 583763262);
    }

    // One million takesmore than 60 minutes for SimpleIdList.
    /**
     * Perform test at scale 400000.
     * The argument should be instantiated empty list.
     * FIXME: Set the expected aggregator value when this works with SimpleIdList.
     */
    public static <L extends IdList<String, HashedTestingElement>> void testScale400000(final L list) {
        testScaleN(list, 400000, 0);
    }

}
