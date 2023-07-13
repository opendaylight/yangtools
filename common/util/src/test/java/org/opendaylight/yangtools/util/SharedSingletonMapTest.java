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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SharedSingletonMapTest {
    private static UnmodifiableMapPhase<String, String> create() {
        return SharedSingletonMap.orderedOf("k1", "v1");
    }

    @Test
    void testSimpleOperations() {
        final var m = create();

        assertFalse(m.isEmpty());
        assertEquals(1, m.size());

        assertTrue(m.containsKey("k1"));
        assertFalse(m.containsKey(null));
        assertFalse(m.containsKey("v1"));

        assertTrue(m.containsValue("v1"));
        assertFalse(m.containsValue(null));
        assertFalse(m.containsValue("k1"));

        assertEquals("v1", m.get("k1"));
        assertNull(m.get(null));
        assertNull(m.get("v1"));

        assertNotEquals(null, m);
        assertEquals(m, m);
        assertNotEquals("", m);

        final var same = Collections.singletonMap("k1", "v1");
        assertEquals(same.toString(), m.toString());
        assertEquals(same, m);
        assertEquals(m, same);
        assertEquals(same.entrySet(), m.entrySet());
        assertEquals(same.values(), m.values());

        // Perform twice to exercise the cache
        assertEquals(same.hashCode(), m.hashCode());
        assertEquals(same.hashCode(), m.hashCode());

        assertNotEquals(m, Collections.singletonMap(null, null));
        assertNotEquals(m, Collections.singletonMap("k1", null));
        assertNotEquals(m, Collections.singletonMap(null, "v1"));
        assertNotEquals(m, Collections.singletonMap("k1", "v2"));
        assertNotEquals(m, ImmutableMap.of("k1", "v1", "k2", "v2"));

        final var set = m.keySet();
        assertInstanceOf(SingletonSet.class, set);
        assertTrue(set.contains("k1"));
    }

    @Test
    void testOrderedCopyOf() {
        final var t = Collections.singletonMap("k1", "v1");
        final var m = SharedSingletonMap.orderedCopyOf(t);
        assertEquals(t, m);
        assertEquals(m, t);
    }

    @Test
    void testUnorderedCopyOf() {
        final var t = Collections.singletonMap("k1", "v1");
        final var m = SharedSingletonMap.unorderedCopyOf(t);
        assertEquals(t, m);
        assertEquals(m, t);
    }

    @Test
    void testEmptyOrderedCopyOf() {
        assertThrows(IllegalArgumentException.class, () -> SharedSingletonMap.orderedCopyOf(ImmutableMap.of()));
    }

    @Test
    void testEmptyUnorderedCopyOf() {
        assertThrows(IllegalArgumentException.class, () -> SharedSingletonMap.unorderedCopyOf(ImmutableMap.of()));
    }

    @Test
    void testClear() {
        assertThrows(UnsupportedOperationException.class, () -> create().clear());
    }

    @Test
    void testPut() {
        assertThrows(UnsupportedOperationException.class, () -> create().put(null, null));
    }

    @Test
    void testPutAll() {
        assertThrows(UnsupportedOperationException.class, () -> create().putAll(Collections.singletonMap("", "")));
    }

    @Test
    void testRemove() {
        assertThrows(UnsupportedOperationException.class, () -> create().remove(null));
    }
}
