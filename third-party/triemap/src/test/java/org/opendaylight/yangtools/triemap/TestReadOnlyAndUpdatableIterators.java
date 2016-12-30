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

import java.util.Iterator;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;

/***
 *
 * Test that read-only iterators do not allow for any updates.
 * Test that non read-only iterators allow for updates.
 *
 */
public class TestReadOnlyAndUpdatableIterators {
    TrieMap<Integer, Integer> bt;
    private static final int MAP_SIZE = 200;

    @Before
    public void setUp() {
        bt = new TrieMap <> ();
        for (int j = 0; j < MAP_SIZE; j++) {
            TestHelper.assertEquals (null, bt.put (Integer.valueOf (j), Integer.valueOf (j)));
        }
    }

    @Test
    public void testReadOnlyIterator () {
        Iterator<Entry<Integer, Integer>> it = bt.readOnlyIterator ();
        try {
            it.next().setValue (0);
            // It should have generated an exception, because it is a read-only iterator
            TestHelper.assertFalse (true);
        } catch (Exception e) {

        }
        try {
            it.remove ();
            // It should have generated an exception, because it is a read-only iterator
            TestHelper.assertFalse (true);
        } catch (Exception e) {

        }
    }

    @Test
    public void testReadOnlySnapshotReadOnlyIterator () {
        TrieMap<Integer, Integer> roSnapshot = bt.readOnlySnapshot ();
        Iterator<Entry<Integer, Integer>> it = roSnapshot.readOnlyIterator ();
        try {
            it.next().setValue (0);
            // It should have generated an exception, because it is a read-only iterator
            TestHelper.assertFalse (true);
        } catch (Exception e) {

        }
        try {
            it.remove ();
            // It should have generated an exception, because it is a read-only iterator
            TestHelper.assertFalse (true);
        } catch (Exception e) {

        }
    }

    @Test
    public void testReadOnlySnapshotIterator () {
        TrieMap<Integer, Integer> roSnapshot = bt.readOnlySnapshot ();
        Iterator<Entry<Integer, Integer>> it = roSnapshot.iterator ();
        try {
            it.next().setValue (0);
            // It should have generated an exception, because it is a read-only iterator
            TestHelper.assertFalse (true);
        } catch (Exception e) {

        }
        try {
            it.remove ();
            // It should have generated an exception, because it is a read-only iterator
            TestHelper.assertFalse (true);
        } catch (Exception e) {

        }
    }

    @Test
    public void testIterator () {
        Iterator<Entry<Integer, Integer>> it = bt.iterator ();
        try {
            it.next().setValue (0);
        } catch (Exception e) {
            // It should not have generated an exception, because it is a non read-only iterator
            TestHelper.assertFalse (true);
        }

        try {
            it.remove ();
        } catch (Exception e) {
            // It should not have generated an exception, because it is a non read-only iterator
            TestHelper.assertFalse (true);
        }

        // All changes are done on the original map
        TestHelper.assertEquals (MAP_SIZE - 1, bt.size ());
    }

    @Test
    public void testSnapshotIterator () {
        TrieMap<Integer, Integer> snapshot = bt.snapshot ();
        Iterator<Entry<Integer, Integer>> it = snapshot.iterator ();
        try {
            it.next().setValue (0);
        } catch (Exception e) {
            // It should not have generated an exception, because it is a non read-only iterator
            TestHelper.assertFalse (true);
        }
        try {
            it.remove ();
        } catch (Exception e) {
            // It should not have generated an exception, because it is a non read-only iterator
            TestHelper.assertFalse (true);
        }

        // All changes are done on the snapshot, not on the original map
        // Map size should remain unchanged
        TestHelper.assertEquals (MAP_SIZE, bt.size ());
        // snapshot size was changed
        TestHelper.assertEquals (MAP_SIZE-1, snapshot.size ());
    }
}
