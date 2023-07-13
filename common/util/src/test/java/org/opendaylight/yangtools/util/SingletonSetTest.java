/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class SingletonSetTest {
    private static final String ELEMENT = "element";

    private static SingletonSet<?> nullSet() {
        return SingletonSet.of(null);
    }

    @Test
    void testNullSingleton() {
        final var s = nullSet();

        assertFalse(s.isEmpty());
        assertEquals(1, s.size());
        assertFalse(s.contains(""));
        assertTrue(s.contains(null));
        assertNull(s.getElement());
        assertEquals(0, s.hashCode());
        assertEquals(s, Collections.singleton(null));
        assertNotEquals(s, Collections.singleton(""));
        assertNotEquals("", s);
        assertEquals(s, s);
        assertNotEquals(null, s);
        assertEquals(Collections.singleton(null).toString(), s.toString());
    }

    @Test
    void testRegularSingleton() {
        final var s = SingletonSet.of(ELEMENT);

        assertFalse(s.isEmpty());
        assertEquals(1, s.size());
        assertFalse(s.contains(""));
        assertFalse(s.contains(null));
        assertTrue(s.contains(ELEMENT));

        assertSame(ELEMENT, s.getElement());
        assertEquals(ELEMENT.hashCode(), s.hashCode());
        assertEquals(s, Collections.singleton(ELEMENT));
        assertNotEquals(s, Collections.singleton(""));
        assertNotEquals(s, Collections.singleton(null));
        assertNotEquals("", s);
        assertEquals(s, s);
        assertNotEquals(null, s);
        assertEquals(Collections.singleton(ELEMENT).toString(), s.toString());
    }

    @Test
    void testIterator() {
        final var s = SingletonSet.of(ELEMENT);
        final var it = s.iterator();

        assertTrue(it.hasNext());
        assertSame(ELEMENT, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void testRejectedAdd() {
        final var s = nullSet();
        assertThrows(UnsupportedOperationException.class, () -> s.add(null));
    }

    @Test
    void testRejectedAddAll() {
        final var s = nullSet();
        assertThrows(UnsupportedOperationException.class, () -> s.addAll(null));
    }

    @Test
    void testRejectedClear() {
        final var s = nullSet();
        assertThrows(UnsupportedOperationException.class, () -> s.clear());
    }

    @Test
    void testRejectedRemove() {
        final var s = nullSet();
        assertThrows(UnsupportedOperationException.class, () -> s.remove(null));
    }

    @Test
    void testRejectedRemoveAll() {
        final var s = nullSet();
        assertThrows(UnsupportedOperationException.class, () -> s.removeAll(null));
    }

    @Test
    void testRejectedRetainAll() {
        final var s = nullSet();
        assertThrows(UnsupportedOperationException.class, () -> s.retainAll(null));
    }
}
