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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.junit.Test;

public class TestConcurrentMapPutIfAbsent {
    private static final int COUNT = 50*1000;

    @Test
    public void testConcurrentMapPutIfAbsent () {
        final ConcurrentMap<Object, Object> map = new TrieMap<>();

        for (int i = 0; i < COUNT; i++) {
            assertNull(map.putIfAbsent (i, i));
            assertEquals(Integer.valueOf(i), map.putIfAbsent(i, i));
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

        final Map<ZeroHashInt, ZeroHashInt> map = new TrieMap<>();
        // Pre-populate an LNode
        assertNull(map.putIfAbsent(k1, v1));
        assertNull(map.putIfAbsent(k2, v2));
        assertNull(map.putIfAbsent(k3, v3));

        // Check with identical key
        assertSame(v3, map.putIfAbsent(k3, v3));
        // Check with equivalent key
        assertSame(v3, map.putIfAbsent(k3dup, v3));
    }
}
