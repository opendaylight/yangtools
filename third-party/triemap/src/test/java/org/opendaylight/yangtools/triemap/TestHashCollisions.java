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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TestHashCollisions {
    @Test
    public void testHashCollisions () {
        final TrieMap<Object, Object> bt = new TrieMap<>();

        insertStrings(bt);
        insertChars(bt);
        insertInts(bt);
        insertBytes(bt);

        removeStrings(bt);
        removeChars(bt);
        removeInts(bt);
        removeBytes(bt);

        insertStrings(bt);
        insertInts(bt);
        insertBytes(bt);
        insertChars(bt);

        removeBytes(bt);
        removeStrings(bt);
        removeChars(bt);
        removeInts(bt);

        insertStrings(bt);
        insertInts(bt);
        insertBytes(bt);
        insertChars(bt);

        removeStrings(bt);
        removeChars(bt);
        removeInts(bt);
        removeBytes(bt);

        insertStrings(bt);
        insertInts(bt);
        insertBytes(bt);
        insertChars(bt);

        removeChars(bt);
        removeInts(bt);
        removeBytes(bt);
        removeStrings(bt);

        insertStrings(bt);
        insertInts(bt);
        insertBytes(bt);
        insertChars(bt);

        removeInts(bt);
        removeBytes(bt);
        removeStrings(bt);
        removeChars(bt);
    }

    private static void insertChars (final TrieMap<Object, Object> bt) {
        assertNull(bt.put('a', 'a'));
        assertNull(bt.put('b', 'b'));
        assertNull(bt.put('c', 'c'));
        assertNull(bt.put('d', 'd'));
        assertNull(bt.put('e', 'e'));

        assertEquals('a', bt.put('a', 'a'));
        assertEquals('b', bt.put('b', 'b'));
        assertEquals('c', bt.put('c', 'c'));
        assertEquals('d', bt.put('d', 'd'));
        assertEquals('e', bt.put('e', 'e'));
    }

    private static void insertStrings (final TrieMap<Object, Object> bt) {
        assertNull(bt.put("a", "a"));
        assertNull(bt.put("b", "b"));
        assertNull(bt.put("c", "c"));
        assertNull(bt.put("d", "d"));
        assertNull(bt.put("e", "e"));

        assertEquals("a", bt.put("a", "a"));
        assertEquals("b", bt.put("b", "b"));
        assertEquals("c", bt.put("c", "c"));
        assertEquals("d", bt.put("d", "d"));
        assertEquals("e", bt.put("e", "e"));
    }

    private static void insertBytes (final TrieMap<Object, Object> bt) {
        for (byte i = 0; i < 128 && i >= 0; i++) {
            final Byte bigB = Byte.valueOf(i);
            assertNull(bt.put(bigB, bigB));
            assertEquals(bigB, bt.put(bigB, bigB));
        }
    }

    private static void insertInts (final TrieMap<Object, Object> bt) {
        for (int i = 0; i < 128; i++) {
            final Integer bigI = Integer.valueOf(i);
            assertNull(bt.put(bigI, bigI));
            assertEquals(bigI, bt.put(bigI, bigI));
        }
    }

    private static void removeChars (final TrieMap<Object, Object> bt) {
        assertNotNull(bt.get('a'));
        assertNotNull(bt.get('b'));
        assertNotNull(bt.get('c'));
        assertNotNull(bt.get('d'));
        assertNotNull(bt.get('e'));

        assertNotNull(bt.remove('a'));
        assertNotNull(bt.remove('b'));
        assertNotNull(bt.remove('c'));
        assertNotNull(bt.remove('d'));
        assertNotNull(bt.remove('e'));

        assertNull(bt.remove('a'));
        assertNull(bt.remove('b'));
        assertNull(bt.remove('c'));
        assertNull(bt.remove('d'));
        assertNull(bt.remove('e'));

        assertNull(bt.get('a'));
        assertNull(bt.get('b'));
        assertNull(bt.get('c'));
        assertNull(bt.get('d'));
        assertNull(bt.get('e'));
    }

    private static void removeStrings (final TrieMap<Object, Object> bt) {
        assertNotNull(bt.get("a"));
        assertNotNull(bt.get("b"));
        assertNotNull(bt.get("c"));
        assertNotNull(bt.get("d"));
        assertNotNull(bt.get("e"));

        assertNotNull(bt.remove("a"));
        assertNotNull(bt.remove("b"));
        assertNotNull(bt.remove("c"));
        assertNotNull(bt.remove("d"));
        assertNotNull(bt.remove("e"));

        assertNull(bt.remove("a"));
        assertNull(bt.remove("b"));
        assertNull(bt.remove("c"));
        assertNull(bt.remove("d"));
        assertNull(bt.remove("e"));

        assertNull(bt.get("a"));
        assertNull(bt.get("b"));
        assertNull(bt.get("c"));
        assertNull(bt.get("d"));
        assertNull(bt.get("e"));
    }

    private static void removeInts (final TrieMap<Object, Object> bt) {
        for (int i = 0; i < 128; i++) {
            final Integer bigI = Integer.valueOf(i);
            assertNotNull(bt.get(bigI));
            assertNotNull(bt.remove(bigI));
            assertNull(bt.remove(bigI));
            assertNull(bt.get(bigI));
        }
    }

    private static void removeBytes (final TrieMap<Object, Object> bt) {
        for (byte i = 0; i < 128 && i >= 0; i++) {
            final Byte bigB = Byte.valueOf(i);
            assertNotNull(bt.get(bigB));
            assertNotNull(bt.remove(bigB));
            assertNull(bt.remove(bigB));
            assertNull(bt.get(bigB));
        }
    }
}
