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
    // Utility key/value class which attacks the hasing function, causing all objects to be put into a single
    // bucket.
    private final class ConstantHashString {
        private final int i;

        ConstantHashString(final int i) {
            this.i = i;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(final Object o) {
            return o instanceof ConstantHashString && i == ((ConstantHashString) o).i;
        }
    }

    @Test
    public void testDelete () {
        final TrieMap<Integer, Integer> bt = new TrieMap<> ();

        for (int i = 0; i < 10000; i++) {
            assertNull(bt.put(Integer.valueOf(i), Integer.valueOf(i)));
            assertEquals(Integer.valueOf(i), bt.lookup(Integer.valueOf(i)));
        }

        checkAddInsert(bt, 536);
        checkAddInsert(bt, 4341);
        checkAddInsert(bt, 8437);

        for (int i = 0; i < 10000; i++) {
            boolean removed = null != bt.remove(Integer.valueOf(i));
            assertTrue(removed);
            final Object lookup = bt.lookup (Integer.valueOf(i));
            assertNull(lookup);
        }

        bt.toString ();
    }

    /**
     * Test if the Map.remove(Object, Object) method works correctly for hash collisions, which are handled by LNode.
     */
    @Test
    public void testRemoveObjectLNode() {
        final TrieMap<ConstantHashString, ConstantHashString> bt = new TrieMap<> ();

        for (int i = 0; i < 10000; i++) {
            final ConstantHashString v = new ConstantHashString(i);
            assertNull(bt.put(v, v));
        }

        for (int i = 0; i < 10000; i++) {
            final ConstantHashString v = new ConstantHashString(i);
            assertTrue(bt.remove(v, v));
        }
    }

    private static void checkAddInsert (final TrieMap<Integer, Integer> bt, final int k) {
        final Integer v = Integer.valueOf(k);
        bt.remove (v);
        Object foundV = bt.lookup(v);
        assertNull(foundV);
        assertNull(bt.put (v, v));
        foundV = bt.lookup(v);
        assertEquals(v, foundV);

        assertEquals(v, bt.put(v, Integer.valueOf(-1)));
        assertEquals(Integer.valueOf(-1), bt.put(v, v));
    }
}
