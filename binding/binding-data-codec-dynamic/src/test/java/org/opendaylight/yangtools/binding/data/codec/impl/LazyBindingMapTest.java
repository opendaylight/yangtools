/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedListBuilder;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectWildcard;
import org.opendaylight.yangtools.binding.KeyAware;

public class LazyBindingMapTest extends AbstractBindingCodecTest {
    private static Top TOP;

    @BeforeClass
    public static void prepareTop() {
        final Map<TopLevelListKey, TopLevelList> map = new HashMap<>();
        for (int i = 0; i < 2 * LazyBindingMap.LAZY_CUTOFF; i++) {
            final TopLevelList item = new TopLevelListBuilder().setName(String.valueOf(i)).build();
            map.put(item.key(), item);
        }

        TOP = new TopBuilder().setTopLevelList(map).build();
    }

    @Test
    public void testSimpleEquals() {
        final Top actual = prepareData();
        assertThat(actual.getTopLevelList(), instanceOf(LazyBindingMap.class));
        // AbstractMap.equals() goes through its entrySet and performs lookup for each key, hence it is excercising
        // primarily LookupState
        assertEquals(TOP, actual);
    }

    @Test
    public void testEqualEntrySet() {
        final Top actual = prepareData();
        // Check equality based on entry set. This primarily exercises IterState
        assertEquals(TOP.getTopLevelList().entrySet(), actual.getTopLevelList().entrySet());
    }

    @Test
    public void testEqualKeySet() {
        final Top actual = prepareData();
        // Check equality based on key set. This primarily exercises IterState
        assertEquals(TOP.getTopLevelList().keySet(), actual.getTopLevelList().keySet());
    }

    @Test
    public void testIterKeySetLookup() {
        final Top actual = prepareData();
        // Forces IterState but then switches to key lookups
        assertTrue(actual.getTopLevelList().keySet().containsAll(TOP.getTopLevelList().keySet()));
    }

    @Test
    public void testIterEntrySetLookup() {
        final Top actual = prepareData();
        // Forces IterState but then switches to value lookups
        assertTrue(actual.getTopLevelList().entrySet().containsAll(TOP.getTopLevelList().entrySet()));
    }

    @Test
    public void testIterValueIteration() {
        assertSameIteratorObjects(prepareData().getTopLevelList().values());
    }

    @Test
    public void testLookupValueIteration() {
        final Map<TopLevelListKey, TopLevelList> list = prepareData().getTopLevelList();
        // Force lookup state instantiation
        assertFalse(list.containsKey(new TopLevelListKey("blah")));

        assertSameIteratorObjects(list.values());
    }

    @Test
    public void testIterKeysetIteration() {
        assertSameIteratorObjects(prepareData().getTopLevelList().keySet());
    }

    @Test
    public void testLookupKeysetIteration() {
        final Map<TopLevelListKey, TopLevelList> list = prepareData().getTopLevelList();
        // Force lookup state instantiation
        assertFalse(list.containsKey(new TopLevelListKey("blah")));

        assertSameIteratorObjects(list.keySet());
    }

    private static void assertSameIteratorObjects(final Collection<?> collection) {
        final var iter2 = collection.iterator();

        for (Object element : collection) {
            // Both iterators should return same values
            assertSame(element, iter2.next());
        }
        assertFalse(iter2.hasNext());
    }

    @Test
    public void testIterSameViews() {
        final Map<TopLevelListKey, TopLevelList> list = prepareData().getTopLevelList();
        assertSame(list.values(), list.values());
        assertSame(list.keySet(), list.keySet());
        assertSame(list.entrySet(), list.entrySet());
    }

    @Test
    public void testLookupSameViews() {
        final Map<TopLevelListKey, TopLevelList> list = prepareData().getTopLevelList();
        // Force lookup state instantiation
        assertFalse(list.containsKey(new TopLevelListKey("blah")));

        // Careful now ... first compare should  end up changing the iteration of keyset/entryset
        final Set<TopLevelListKey> keySet1 = list.keySet();
        final Set<TopLevelListKey> keySet2 = list.keySet();
        final Set<Entry<TopLevelListKey, TopLevelList>> entrySet1 = list.entrySet();
        final Set<Entry<TopLevelListKey, TopLevelList>> entrySet2 = list.entrySet();

        // .. right here ...
        assertSame(list.values(), list.values());
        // ... so this should end up iterating slightly differently
        assertEquals(new HashSet<>(list.values()), new HashSet<>(list.values()));

        // ... and as we do not reuse keyset/entryset, we need to run full compare
        assertEquals(keySet1, keySet2);
        assertEquals(keySet1, new HashSet<>(keySet2));
        assertEquals(entrySet1, entrySet2);
        assertEquals(entrySet1, new HashSet<>(entrySet2));
    }

    @Test
    public void testIterSameSize() {
        final Map<TopLevelListKey, TopLevelList> list = prepareData().getTopLevelList();
        // Force lookup state instantiation
        assertFalse(list.containsKey(new TopLevelListKey("blah")));

        assertEquals(list.size(), list.entrySet().size());
        assertEquals(list.size(), list.size());
        assertEquals(list.size(), list.size());
    }

    @Test
    public void testLookupSameSize() {
        final Map<TopLevelListKey, TopLevelList> list = prepareData().getTopLevelList();
        assertEquals(list.size(), list.entrySet().size());
        assertEquals(list.size(), list.size());
        assertEquals(list.size(), list.size());
    }

    @Test
    public void testImmutableThrows() {
        final Map<TopLevelListKey, TopLevelList> list = prepareData().getTopLevelList();
        // Various asserts for completeness' sake
        assertThrows(UnsupportedOperationException.class, () -> list.clear());
        assertThrows(UnsupportedOperationException.class, () -> list.remove(null));
        assertThrows(UnsupportedOperationException.class, () -> list.putAll(null));
    }

    @Test
    public void testLookupContainsValueThrows() {
        final Map<TopLevelListKey, TopLevelList> list = prepareData().getTopLevelList();
        assertThrows(NullPointerException.class, () -> list.containsValue(null));
        assertThrows(ClassCastException.class, () -> list.containsValue(mock(DataObject.class)));
    }

    @Test
    public void testLookupContainsKeyThrows() {
        final Map<TopLevelListKey, TopLevelList> list = prepareData().getTopLevelList();
        assertThrows(NullPointerException.class, () -> list.containsKey(null));
        assertThrows(ClassCastException.class, () -> list.containsKey(mock(KeyAware.class)));
    }

    @Test
    public void testLookupKey() {
        final Map<TopLevelListKey, TopLevelList> list = prepareData().getTopLevelList();
        for (TopLevelListKey key : TOP.getTopLevelList().keySet()) {
            assertTrue(list.containsKey(key));
        }

        assertFalse(list.containsKey(new TopLevelListKey("blah")));
    }

    @Test
    public void testLookupValue() {
        final Map<TopLevelListKey, TopLevelList> list = prepareData().getTopLevelList();
        for (TopLevelList val : TOP.getTopLevelList().values()) {
            assertTrue(list.containsValue(val));
        }

        assertFalse(list.containsValue(new TopLevelListBuilder().setName("blah").build()));

        // We checked this key, but this is a different object
        assertFalse(list.containsValue(new TopLevelListBuilder(TOP.getTopLevelList().values().iterator().next())
            .setNestedList(List.of(new NestedListBuilder().setName("foo").build()))
            .build()));
    }

    private Top prepareData() {
        return thereAndBackAgain(DataObjectWildcard.create(Top.class), TOP);
    }
}
