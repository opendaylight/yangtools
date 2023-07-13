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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class UnmodifiableCollectionTest {

    @Test
    public void testUnmodifiableCollection() {
        final List<Integer> immutableTestList = ImmutableList.<Integer>builder()
                .add(1)
                .add(2)
                .add(3)
                .add(4)
                .add(5).build();

        final Collection<Integer> testUnmodifiableCollection = UnmodifiableCollection.create(immutableTestList);
        assertNotNull(testUnmodifiableCollection);

        // Note: this cannot be ImmutableList, because UnmodifiableCollection does recognize it and returns it as is,
        //       without converting it to an UnmodifiableCollection -- which is not what we want.
        final List<Integer> testList = Arrays.asList(1, 2, 3, 4, 5);
        final Collection<Integer> testUnmodifiableCollection2 = UnmodifiableCollection.create(testList);

        final Iterator<Integer> iterator = testUnmodifiableCollection2.iterator();
        assertNotNull(iterator);
        assertInstanceOf(UnmodifiableIterator.class, iterator);

        assertEquals(5, testUnmodifiableCollection2.size());

        assertFalse(testUnmodifiableCollection2.isEmpty());

        assertTrue(testUnmodifiableCollection2.contains(1));

        final Object[] objectArray = testUnmodifiableCollection2.toArray();
        assertNotNull(objectArray);
        assertEquals(5, objectArray.length);

        final Integer[] integerArray = testUnmodifiableCollection2.toArray(
                new Integer[testUnmodifiableCollection2.size()]);
        assertNotNull(integerArray);
        assertEquals(5, integerArray.length);

        assertTrue(testUnmodifiableCollection2.containsAll(testUnmodifiableCollection));

        assertEquals("UnmodifiableCollection{" + testList + "}", testUnmodifiableCollection2.toString());
    }
}
