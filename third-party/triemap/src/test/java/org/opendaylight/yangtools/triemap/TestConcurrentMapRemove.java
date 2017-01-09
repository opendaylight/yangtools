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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.junit.Test;

public class TestConcurrentMapRemove {
    private static final int COUNT = 50 * 1000;

    @Test
    public void testConcurrentMapRemove() {
        final ConcurrentMap<Integer, Object> map = TrieMap.create();

        for (int i = 128; i < COUNT; i++) {
            assertFalse(map.remove(i, i));
            assertNull(map.put(i, i));
            assertFalse(map.remove(i, "lol"));
            assertTrue(map.containsKey(i));
            assertTrue(map.remove(i, i));
            assertFalse(map.containsKey(i));
            assertNull(map.put(i, i));
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

        final Map<ZeroHashInt, ZeroHashInt> map = TrieMap.create();
        // Pre-populate an LNode
        assertNull(map.putIfAbsent(k1, v1));
        assertNull(map.putIfAbsent(k2, v2));
        assertNull(map.putIfAbsent(k3, v3));

        assertFalse(map.remove(k3, v2));
        assertTrue(map.remove(k3dup, v3dup));
    }
}
