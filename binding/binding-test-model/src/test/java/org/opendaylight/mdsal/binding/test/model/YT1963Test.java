/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101.RootBuilder;
import org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101.root.OuterContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101.root.bug4798.choice.CaseABuilder;
import org.opendaylight.yang.gen.v1.urn.test.foo4798.rev160101.root.bug4798.choice._case.a.ListInCaseBuilder;
import org.opendaylight.yang.gen.v1.yt1963.norev.Cont1Builder;
import org.opendaylight.yang.gen.v1.yt1963.norev.cont.OneBuilder;
import org.opendaylight.yang.gen.v1.yt1963.norev.cont.ThreeBuilder;
import org.opendaylight.yang.gen.v1.yt1963.norev.cont.TwoBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;

class YT1963Test {
    @Test
    void augmentableEmpty() {
        assertEquals(961, new RootBuilder().build().hashCode());
    }

    @Test
    void augmentableFirst() {
        assertEquals(3007, new RootBuilder()
            .setBug4798Choice(new CaseABuilder()
                .setListInCase(BindingMap.of(new ListInCaseBuilder().setTestLeaf("test").build()))
                .build())
            .build().hashCode());
    }

    @Test
    void augmentableSecond() {
        assertEquals(992, new RootBuilder().setOuterContainer(new OuterContainerBuilder().build()).build().hashCode());
    }

    @Test
    void augmentableBoth() {
        assertEquals(3038, new RootBuilder()
            .setBug4798Choice(new CaseABuilder()
                .setListInCase(BindingMap.of(new ListInCaseBuilder().setTestLeaf("test").build()))
                .build())
            .setOuterContainer(new OuterContainerBuilder().build())
            .build().hashCode());
    }

    @Test
    void twoPropertyNone() {
        assertEquals(961, new TreeComplexUsesAugmentBuilder().build().hashCode());
    }

    @Test
    void twoPropertyFirst() {
        assertEquals(1922, new TreeComplexUsesAugmentBuilder()
            .setContainerWithUses(new ContainerWithUsesBuilder().build())
            .build().hashCode());
    }

    @Test
    void twoPropertySecond() {
        assertEquals(996, new TreeComplexUsesAugmentBuilder()
            .setListViaUses(BindingMap.of(new ListViaUsesBuilder().setName("foo").build()))
            .build().hashCode());
    }

    @Test
    void twoPropertySecondEmpty() {
        // squashed to null
        assertEquals(961, new TreeComplexUsesAugmentBuilder().setListViaUses(Map.of()).build().hashCode());
    }

    @Test
    void twoPropertyBoth() {
        assertEquals(1957, new TreeComplexUsesAugmentBuilder()
            .setContainerWithUses(new ContainerWithUsesBuilder().build())
            .setListViaUses(BindingMap.of(new ListViaUsesBuilder().setName("foo").build()))
            .build().hashCode());
    }

    @Test
    void threePropertyNone() {
        assertEquals(29791, new Cont1Builder().build().hashCode());
    }

    @Test
    void threePropertyFirst() {
        assertEquals(30752, new Cont1Builder().setOne(new OneBuilder().build()).build().hashCode());
    }

    @Test
    void threePropertySecond() {
        assertEquals(29792, new Cont1Builder().setTwo(new TwoBuilder().build()).build().hashCode());
    }

    @Test
    void threePropertyThird() {
        assertEquals(29822, new Cont1Builder().setThree(new ThreeBuilder().build()).build().hashCode());
    }

    @Test
    void threePropertyAll() {
        assertEquals(30784, new Cont1Builder()
            .setOne(new OneBuilder().build())
            .setTwo(new TwoBuilder().build())
            .setThree(new ThreeBuilder().build())
            .build().hashCode());
    }
}
