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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class TestMapIterator {
    @Test
    public void testMapIterator() {
        final Random random = new Random();

        for (int i = 0; i < 60 * 1000; i += 400 + random.nextInt(400)) {
            final Map<Integer, Integer> bt = TrieMap.create();
            for (int j = 0; j < i; j++) {
                assertNull(bt.put(Integer.valueOf(j), Integer.valueOf(j)));
            }
            int count = 0;
            final Set<Integer> set = new HashSet<>();
            for (final Entry<Integer, Integer> e : bt.entrySet()) {
                set.add(e.getKey());
                count++;
            }
            for (final Integer j : set) {
                assertTrue(bt.containsKey(j));
            }
            for (final Integer j : bt.keySet()) {
                assertTrue(set.contains(j));
            }

            assertEquals(i, count);
            assertEquals(i, bt.size());

            for (Entry<Integer, Integer> e : bt.entrySet()) {
                assertSame(e.getValue(), bt.get(e.getKey()));
                e.setValue(e.getValue() + 1);
                assertEquals((Object)e.getValue(), e.getKey() + 1);
                assertEquals(e.getValue(), bt.get(e.getKey()));
                e.setValue(e.getValue() - 1);
            }

            final Iterator<Integer> it = bt.keySet().iterator();
            while (it.hasNext()) {
                final Integer k = it.next();
                assertTrue(bt.containsKey(k));
                it.remove();
                assertFalse(bt.containsKey(k));
            }

            assertEquals(0, bt.size());
            assertTrue(bt.isEmpty());
        }
    }

    @Test
    public void testMapImmutableIterator() {
        final Random random = new Random();

        for (int i = 0; i < 60 * 1000; i += 400 + random.nextInt(400)) {
            final Map<Integer, Integer> bt = TrieMap.create();
            for (int j = 0; j < i; j++) {
                assertNull(bt.put(Integer.valueOf(j), Integer.valueOf(j)));
            }
            int count = 0;
            final Set<Integer> set = new HashSet<>();
            for (final Entry<Integer, Integer> e : bt.entrySet()) {
                set.add(e.getKey());
                count++;
            }
            for (final Integer j : set) {
                assertTrue(bt.containsKey(j));
            }
            for (final Integer j : bt.keySet()) {
                assertTrue(set.contains(j));
            }

            assertEquals(i, count);
            assertEquals(i, bt.size());
        }
    }

    private static void failAdvance(final Iterator<?> it) {
        assertFalse(it.hasNext());
        it.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyIterator() {
        failAdvance(TrieMap.create().iterator());
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyReadOnlyIterator() {
        failAdvance(TrieMap.create().immutableIterator());
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyReadOnlySnapshotIterator() {
        failAdvance(TrieMap.create().immutableSnapshot().iterator());
    }
}
