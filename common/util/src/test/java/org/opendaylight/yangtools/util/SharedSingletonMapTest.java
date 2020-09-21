/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class SharedSingletonMapTest {
    private static UnmodifiableMapPhase<String, String> create() {
        return SharedSingletonMap.orderedOf("k1", "v1");
    }

    @Test
    public void testSimpleOperations() {
        final Map<String, String> m = create();

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

        assertFalse(m.equals(null));
        assertTrue(m.equals(m));
        assertFalse(m.equals(""));

        final Map<String, String> same = Collections.singletonMap("k1", "v1");
        assertEquals(same.toString(), m.toString());
        assertTrue(same.equals(m));
        assertTrue(m.equals(same));
        assertEquals(same.entrySet(), m.entrySet());
        assertEquals(same.values(), m.values());

        // Perform twice to exercise the cache
        assertEquals(same.hashCode(), m.hashCode());
        assertEquals(same.hashCode(), m.hashCode());

        assertFalse(m.equals(Collections.singletonMap(null, null)));
        assertFalse(m.equals(Collections.singletonMap("k1", null)));
        assertFalse(m.equals(Collections.singletonMap(null, "v1")));
        assertFalse(m.equals(Collections.singletonMap("k1", "v2")));
        assertFalse(m.equals(ImmutableMap.of("k1", "v1", "k2", "v2")));

        final Set<String> set = m.keySet();
        assertThat(set, instanceOf(SingletonSet.class));
        assertTrue(set.contains("k1"));
    }

    @Test
    public void testOrderedCopyOf() {
        final Map<String, String> t = Collections.singletonMap("k1", "v1");
        final Map<String, String> m = SharedSingletonMap.orderedCopyOf(t);
        assertTrue(t.equals(m));
        assertTrue(m.equals(t));
    }

    @Test
    public void testUnorderedCopyOf() {
        final Map<String, String> t = Collections.singletonMap("k1", "v1");
        final Map<String, String> m = SharedSingletonMap.unorderedCopyOf(t);
        assertTrue(t.equals(m));
        assertTrue(m.equals(t));
    }

    @Test
    public void testEmptyOrderedCopyOf() {
        assertThrows(IllegalArgumentException.class, () -> SharedSingletonMap.orderedCopyOf(ImmutableMap.of()));
    }

    @Test
    public void testEmptyUnorderedCopyOf() {
        assertThrows(IllegalArgumentException.class, () -> SharedSingletonMap.unorderedCopyOf(ImmutableMap.of()));
    }

    @Test
    public void testClear() {
        assertThrows(UnsupportedOperationException.class, () -> create().clear());
    }

    @Test
    public void testPut() {
        assertThrows(UnsupportedOperationException.class, () -> create().put(null, null));
    }

    @Test
    public void testPutAll() {
        assertThrows(UnsupportedOperationException.class, () -> create().putAll(Collections.singletonMap("", "")));
    }

    @Test
    public void testRemove() {
        assertThrows(UnsupportedOperationException.class, () -> create().remove(null));
    }
}
