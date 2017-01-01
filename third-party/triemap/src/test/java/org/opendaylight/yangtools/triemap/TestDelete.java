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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestDelete {
    @Test(expected = NullPointerException.class)
    public void testNullSimple() {
        new TrieMap<>().remove(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullKey() {
        new TrieMap<>().remove(null, "");
    }

    @Test(expected = NullPointerException.class)
    public void testNullValue() {
        new TrieMap<>().remove("", null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullBoth() {
        new TrieMap<>().remove(null, null);
    }

    @Test
    public void testClear() {
        final TrieMap<Integer, Integer> bt = new TrieMap<>();
        bt.put(1, 1);
        bt.clear();
        assertTrue(bt.isEmpty());
        assertEquals(0, bt.size());
    }

    @Test
    public void testDelete () {
        final TrieMap<Integer, Integer> bt = new TrieMap<> ();

        for (int i = 0; i < 10000; i++) {
            assertNull(bt.put(Integer.valueOf(i), Integer.valueOf(i)));
            assertEquals(Integer.valueOf(i), bt.get(Integer.valueOf(i)));
        }

        checkAddInsert(bt, 536);
        checkAddInsert(bt, 4341);
        checkAddInsert(bt, 8437);

        for (int i = 0; i < 10000; i++) {
            boolean removed = null != bt.remove(Integer.valueOf(i));
            assertTrue(removed);
            final Object lookup = bt.get(Integer.valueOf(i));
            assertNull(lookup);
        }

        bt.toString ();
    }

    /**
     * Test if the Map.remove(Object, Object) method works correctly for hash collisions, which are handled by LNode.
     */
    @Test
    public void testRemoveObjectLNode() {
        final TrieMap<ZeroHashInt, ZeroHashInt> bt = new TrieMap<> ();

        for (int i = 0; i < 100; i++) {
            final ZeroHashInt v = new ZeroHashInt(i);
            assertNull(bt.put(v, v));
        }

        for (int i = 0; i < 100; i++) {
            final ZeroHashInt v = new ZeroHashInt(i);
            assertTrue(bt.remove(v, v));
        }
    }

    private static void checkAddInsert (final TrieMap<Integer, Integer> bt, final int k) {
        final Integer v = Integer.valueOf(k);
        bt.remove (v);
        Integer foundV = bt.get(v);
        assertNull(foundV);
        assertNull(bt.put (v, v));
        foundV = bt.get(v);
        assertEquals(v, foundV);

        assertEquals(v, bt.put(v, Integer.valueOf(-1)));
        assertEquals(Integer.valueOf(-1), bt.put(v, v));
    }
}
