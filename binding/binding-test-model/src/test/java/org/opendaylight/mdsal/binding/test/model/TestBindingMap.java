/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.lal.norev.Foo;
import org.opendaylight.yang.gen.v1.lal.norev.FooBuilder;
import org.opendaylight.yang.gen.v1.lal.norev.foo.Bar;
import org.opendaylight.yang.gen.v1.lal.norev.foo.BarBuilder;
import org.opendaylight.yang.gen.v1.lal.norev.foo.BarKey;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;

public class TestBindingMap {
    private static final BarKey BAR_KEY_ONE = new BarKey(1);
    private static final BarKey BAR_KEY_TWO = new BarKey(2);
    private static final BarKey BAR_KEY_THREE = new BarKey(3);
    private static final BarKey BAR_KEY_FOUR = new BarKey(4);
    private static final Bar BAR_ONE = new BarBuilder().withKey(BAR_KEY_ONE).setName("one").build();
    private static final Bar BAR_TWO = new BarBuilder().withKey(BAR_KEY_TWO).setName("two").build();
    private static final Bar BAR_THREE = new BarBuilder().withKey(BAR_KEY_THREE).setName("three").build();
    private static final Bar BAR_FOUR = new BarBuilder().withKey(BAR_KEY_FOUR).setName("four").build();
    private static final List<Bar> BAR_LIST = List.of(BAR_TWO, BAR_ONE, BAR_FOUR, BAR_THREE);

    @Test
    public void ofTest() {
        final Foo foo = new FooBuilder()
                .setBar(BindingMap.of(BAR_ONE, BAR_TWO))
                .build();
        final Map<BarKey, Bar> bar = foo.getBar();
        assertNotNull(bar);
        assertEquals(bar.get(BAR_KEY_ONE), BAR_ONE);
        assertEquals(bar.get(BAR_KEY_TWO), BAR_TWO);
    }

    @Test
    public void builderTest() {
        final BindingMap.Builder<BarKey, Bar> builder = BindingMap.builder();
        for (Bar bar : BAR_LIST) {
            builder.add(bar);
        }
        final Foo foo = new FooBuilder()
                .setBar(builder.build())
                .build();
        final Map<BarKey, Bar> bar = foo.getBar();
        assertNotNull(bar);
        assertEquals(bar.get(BAR_KEY_ONE), BAR_ONE);
        assertEquals(bar.get(BAR_KEY_TWO), BAR_TWO);
        assertEquals(bar.get(BAR_KEY_THREE), BAR_THREE);
        assertEquals(bar.get(BAR_KEY_FOUR), BAR_FOUR);
    }

    @Test
    public void orderedTest() {
        final Foo foo = new FooBuilder()
                .setBar(BindingMap.ordered(BAR_TWO, BAR_ONE, BAR_FOUR, BAR_THREE))
                .build();
        final Map<BarKey, Bar> bar = foo.getBar();
        assertNotNull(bar);
        checkOrderedMap(bar, BAR_LIST);
    }

    @Test
    public void orderedBuilderTest() {
        final Foo foo = new FooBuilder()
                .setBar(BindingMap.<BarKey, Bar>orderedBuilder()
                        .add(BAR_TWO)
                        .add(BAR_ONE)
                        .addAll(BAR_FOUR, BAR_THREE)
                        .build())
                .build();
        final Map<BarKey, Bar> bar = foo.getBar();
        assertNotNull(bar);
        checkOrderedMap(bar, BAR_LIST);
    }

    private static void checkOrderedMap(final Map<BarKey, Bar> barMap, final List<Bar> barList) {
        final Iterator<Map.Entry<BarKey, Bar>> mapIt = barMap.entrySet().iterator();
        final Iterator<Bar> listIt = barList.iterator();
        while (mapIt.hasNext() && listIt.hasNext()) {
            assertEquals(mapIt.next().getValue(), listIt.next());
        }
        assertEquals(mapIt.hasNext(), listIt.hasNext());
    }
}
