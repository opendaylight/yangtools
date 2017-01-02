/*
 * (C) Copyright 2016 Pantheon Technologies, s.r.o. and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opendaylight.yangtools.triemap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.junit.Test;

public class TestConcurrentMapReplace {
    private static final int COUNT = 50*1000;

    @Test
    public void testConcurrentMapReplace () {
        final ConcurrentMap<Integer, Object> map = TrieMap.create();

        for (int i = 0; i < COUNT; i++) {
            assertNull(map.replace(i, "lol"));
            assertFalse(map.replace(i, i, "lol2"));
            assertNull(map.put(i, i));
            assertEquals(Integer.valueOf(i), map.replace(i, "lol"));
            assertFalse(map.replace(i, i, "lol2"));
            assertTrue(map.replace(i, "lol", i));
        }
    }

    @Test
    public void testConflictingHash() {
        final ZeroHashInt k1 = new ZeroHashInt(1);
        final ZeroHashInt k2 = new ZeroHashInt(2);
        final ZeroHashInt k3 = new ZeroHashInt(3);
        final ZeroHashInt k3dup = new ZeroHashInt(3);
        final ZeroHashInt v1 = new ZeroHashInt(4);
        final ZeroHashInt v2 = new ZeroHashInt(5);
        final ZeroHashInt v3 = new ZeroHashInt(6);
        final ZeroHashInt v3dup = new ZeroHashInt(6);
        final ZeroHashInt k4 = new ZeroHashInt(7);

        final Map<ZeroHashInt, ZeroHashInt> map = TrieMap.create();
        assertNull(map.put(k3, v3));

        // First check for SNode
        assertNull(map.replace(k1, v1));
        assertFalse(map.replace(k1, v1, v2));
        assertFalse(map.replace(k3, v1, v3));
        assertFalse(map.replace(k3dup, v1, v3dup));
        assertTrue(map.replace(k3dup, v3dup, v1));
        assertTrue(map.replace(k3dup, v1, v3));

        // Bump up to LNode
        assertNull(map.put(k1, v1));
        assertNull(map.put(k2, v2));

        // Completely mismatched
        assertFalse(map.replace(k1, v2, v3));

        // Identical value match
        assertTrue(map.replace(k2, v2, v3));
        // Equivalent value match
        assertTrue(map.replace(k2, v3dup, v2));

        // Equivalent match
        assertTrue(map.replace(k3dup, v3dup, v2));

        // No match
        assertNull(map.replace(k4, v1));
        assertFalse(map.replace(k4, v1, v2));
    }
}
