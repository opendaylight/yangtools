/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedListBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LazyBindingListTest extends AbstractBindingCodecTest {
    @Test
    public void testLazyList() {
        final List<NestedList> nested = new ArrayList<>();
        for (int i = 0; i < 2 * LazyBindingList.LAZY_CUTOFF; ++i) {
            nested.add(new NestedListBuilder().setName(String.valueOf(i)).build());
        }
        final TopLevelList expected = new TopLevelListBuilder()
                .setName("test")
                .setNestedList(nested)
                .build();
        final TopLevelList actual = thereAndBackAgain(
            InstanceIdentifier.create(Top.class).child(TopLevelList.class, expected.key()), expected);

        final List<NestedList> list = actual.getNestedList();
        assertThat(list, instanceOf(LazyBindingList.class));

        // Equality does all the right things to check happy paths
        assertEquals(expected.getNestedList(), list);
        assertEquals(expected.getNestedList().hashCode(), list.hashCode());

        // Make sure the list performs proper caching
        assertSame(list.get(LazyBindingList.LAZY_CUTOFF), list.get(LazyBindingList.LAZY_CUTOFF));

        // Test throws, just for completeness' sake
        assertThrows(UnsupportedOperationException.class, () -> list.add(null));
        assertThrows(UnsupportedOperationException.class, () -> list.addAll(null));
        assertThrows(UnsupportedOperationException.class, () -> list.addAll(0, null));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(null));
        assertThrows(UnsupportedOperationException.class, () -> list.removeAll(null));
        assertThrows(UnsupportedOperationException.class, () -> list.replaceAll(null));
        assertThrows(UnsupportedOperationException.class, () -> list.retainAll(null));
        assertThrows(UnsupportedOperationException.class, () -> list.sort(null));
        assertThrows(UnsupportedOperationException.class, () -> list.clear());
    }

    @Test
    public void testSingletonList() {
        final TopLevelList expected = new TopLevelListBuilder()
                .setName("test")
                .setNestedList(List.of(new NestedListBuilder().setName(String.valueOf("one")).build()))
                .build();
        final TopLevelList actual = thereAndBackAgain(
            InstanceIdentifier.create(Top.class).child(TopLevelList.class, expected.key()), expected);

        final List<NestedList> list = actual.getNestedList();
        assertThat(list, not(instanceOf(LazyBindingList.class)));
        assertEquals(expected.getNestedList(), list);
    }
}
