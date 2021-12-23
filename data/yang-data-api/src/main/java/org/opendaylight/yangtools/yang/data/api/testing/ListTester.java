/*
 * Copyright (c) 2021 Vratko Polak and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.testing;

import org.opendaylight.yangtools.yang.data.api.IndexedList;

/**
 * A bunch of static methods useful for testing IndexedList implementations.
 */
public final class ListTester {

    /**
     * Utility classes should have constructors hidden.
     */
    private ListTester() {
    }

    /**
     * One iteration for the scale test.
     * During the iteration, two elements are added and one removed,
     * so the iteration argument value is also the size of list at the start of iteration.
     * Element payloads (and thus hashes and identifiers) are compited from iteration argument.
     * Lower bits of the hash are used for pseudorandom decisions,
     * so every run has the same decisions.
     * The first element is added either at the end or at the start.
     * The second element is added either before or after a pseudorandom index.
     * Either the firstly added or randomly selected (by index) element is deleted.
     */
    private static <L extends IndexedList<String, TestingElement>> void iterateOnce(final L list, final int iteration) {
        final TestingElement element0 = new TestingElement(2 * iteration + 1);
        final TestingElement element1 = new TestingElement(2 * iteration);
        final int intIdentifier0 = element0.intId();
        final String strIdentifier0 = Integer.toString(intIdentifier0);
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
            list.remove(strIdentifier0);
        } else {
            list.remove(-intIdentifier0 % (iteration + 2));
        }
    }

    /**
     * A scale test, good implementation take small time to finish.
     * The list argument should be instantiated empty list.
     * Scale gives number of iterations to execute.
     * When all iterations are done, hashes of the remaining element are accumulated,
     * and compared to the expected result, so also the iteration speed
     * affects overall test duration.
     */
    public static <L extends IndexedList<String, TestingElement>> boolean testScaleN(
        final L list, final int scale, final int result
    ) {
        int accumulator = 0;

        for (int iteration = 0; iteration < scale; iteration++) {
            iterateOnce(list, iteration);
        }
        for (TestingElement element : list) {
            // We use myHash to scrable the accumulator value, so that order matters.
            accumulator = MyHash.myHash(accumulator) + element.intId();
        }
        return accumulator == result;
    }

    /**
     * Perform test at scale 1.
     * The argument should be instantiated empty list.
     * FIXME: Set the expected aggregator value when this works with SimpleIndexedList.
     */
    public static <L extends IndexedList<String, TestingElement>> boolean testScale1(final L list) {
        return testScaleN(list, 1, 0);
    }

    /**
     * Perform test at scale 1.
     * The argument should be instantiated empty list.
     * FIXME: Set the expected aggregator value when this works with SimpleIndexedList.
     */
    public static <L extends IndexedList<String, TestingElement>> boolean testScale10(final L list) {
        return testScaleN(list, 10, 0);
    }

    /**
     * Perform test at scale 1.
     * The argument should be instantiated empty list.
     * FIXME: Set the expected aggregator value when this works with SimpleIndexedList.
     */
    public static <L extends IndexedList<String, TestingElement>> boolean testScale100(final L list) {
        return testScaleN(list, 100, 0);
    }

    /**
     * Perform test at scale 1.
     * The argument should be instantiated empty list.
     * FIXME: Set the expected aggregator value when this works with SimpleIndexedList.
     */
    public static <L extends IndexedList<String, TestingElement>> boolean testScale1000(final L list) {
        return testScaleN(list, 1000, 0);
    }

    /**
     * Perform test at scale 1.
     * The argument should be instantiated empty list.
     * FIXME: Set the expected aggregator value when this works with SimpleIndexedList.
     */
    public static <L extends IndexedList<String, TestingElement>> boolean testScale10000(final L list) {
        return testScaleN(list, 10000, 0);
    }

    /**
     * Perform test at scale 1.
     * The argument should be instantiated empty list.
     * FIXME: Set the expected aggregator value when this works with SimpleIndexedList.
     */
    public static <L extends IndexedList<String, TestingElement>> boolean testScale100000(final L list) {
        return testScaleN(list, 100000, 0);
    }

    /**
     * Perform test at scale 1.
     * The argument should be instantiated empty list.
     * FIXME: Set the expected aggregator value when this works with SimpleIndexedList.
     */
    public static <L extends IndexedList<String, TestingElement>> boolean testScale1000000(final L list) {
        return testScaleN(list, 1000000, 0);
    }

}
