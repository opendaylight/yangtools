/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UnmodifiableCollectionTest {

    @Test
    void testUnmodifiableCollection() {
        final var immutableTestList = ImmutableList.<Integer>builder()
                .add(1)
                .add(2)
                .add(3)
                .add(4)
                .add(5).build();

        final var testUnmodifiableCollection = UnmodifiableCollection.create(immutableTestList);
        assertNotNull(testUnmodifiableCollection);

        // Note: this cannot be ImmutableList, because UnmodifiableCollection does recognize it and returns it as is,
        //       without converting it to an UnmodifiableCollection -- which is not what we want.
        final var testList = Arrays.asList(1, 2, 3, 4, 5);
        final var testUnmodifiableCollection2 = UnmodifiableCollection.create(testList);

        final var iterator = testUnmodifiableCollection2.iterator();
        assertNotNull(iterator);
        assertInstanceOf(UnmodifiableIterator.class, iterator);

        assertEquals(5, testUnmodifiableCollection2.size());

        assertFalse(testUnmodifiableCollection2.isEmpty());

        assertTrue(testUnmodifiableCollection2.contains(1));

        final var objectArray = testUnmodifiableCollection2.toArray();
        assertNotNull(objectArray);
        assertEquals(5, objectArray.length);

        final var integerArray = testUnmodifiableCollection2.toArray(
                new Integer[testUnmodifiableCollection2.size()]);
        assertNotNull(integerArray);
        assertEquals(5, integerArray.length);

        assertTrue(testUnmodifiableCollection2.containsAll(testUnmodifiableCollection));

        assertEquals("UnmodifiableCollection{" + testList + "}", testUnmodifiableCollection2.toString());
    }
}
