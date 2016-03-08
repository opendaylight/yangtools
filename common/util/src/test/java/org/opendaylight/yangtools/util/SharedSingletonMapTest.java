/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
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

        assertNotEquals(m, null);
        assertEquals(m, m);
        assertNotEquals(m, "");

        final Map<String, String> same = Collections.singletonMap("k1", "v1");
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

        final Set<String> set = m.keySet();
        assertTrue(set instanceof SingletonSet);
        assertTrue(set.contains("k1"));
    }

    @Test
    public void testOrderedCopyOf() {
        final Map<String, String> t = Collections.singletonMap("k1", "v1");
        final Map<String, String> m = SharedSingletonMap.orderedCopyOf(t);
        assertEquals(t, m);
        assertEquals(m, t);
    }

    @Test
    public void testUnorderedCopyOf() {
        final Map<String, String> t = Collections.singletonMap("k1", "v1");
        final Map<String, String> m = SharedSingletonMap.unorderedCopyOf(t);
        assertEquals(t, m);
        assertEquals(m, t);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyOrderedCopyOf() {
        SharedSingletonMap.orderedCopyOf(ImmutableMap.of());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyUnorderedCopyOf() {
        SharedSingletonMap.unorderedCopyOf(ImmutableMap.of());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testClear() {
        create().clear();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testPut() {
        create().put(null, null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testPutAll() {
        create().putAll(Collections.singletonMap("", ""));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRemove() {
        create().remove(null);
    }
}
