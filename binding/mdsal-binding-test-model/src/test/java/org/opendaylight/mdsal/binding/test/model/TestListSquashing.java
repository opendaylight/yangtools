/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.Def;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.DefBuilder;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.grp.Lst;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.grp.LstBuilder;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.grp.LstKey;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.Container;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.ContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.Keyed;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.KeyedBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.KeyedKey;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.Unkeyed;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.UnkeyedBuilder;
import org.opendaylight.yang.gen.v1.urn.test.pattern.rev170101.Cont;
import org.opendaylight.yang.gen.v1.urn.test.pattern.rev170101.ContBuilder;

public class TestListSquashing {
    @Test
    public void testEmptyLeafList() {
        final Cont obj = new ContBuilder().setTest3(List.of()).build();
        // Eventhough return type is List, it should be retained
        assertEquals(List.of(), obj.getTest3());
    }

    @Test
    public void testEmptyUserOrderedList() {
        final Container cont = new ContainerBuilder()
                .setKeyed(List.of())
                .setUnkeyed(List.of())
                .build();
        // Empty Lists should become null
        assertNull(cont.getKeyed());
        assertNull(cont.getUnkeyed());
    }

    @Test
    public void testUserOrderedList() {
        final Keyed keyed = new KeyedBuilder().withKey(new KeyedKey("a")).build();
        final Unkeyed unkeyed = new UnkeyedBuilder().build();
        final Container cont = new ContainerBuilder()
                .setKeyed(List.of(keyed))
                .setUnkeyed(List.of(unkeyed))
                .build();
        // Non-empty Lists should be retained
        assertEquals(List.of(keyed), cont.getKeyed());
        assertEquals(List.of(unkeyed), cont.getUnkeyed());
    }

    @Test
    public void testEmptySystemOrderedList() {
        final Def cont = new DefBuilder().setLst(Map.of()).build();
        // Empty Map should become null
        assertNull(cont.getLst());
    }

    @Test
    public void testSystemOrderedList() {
        final Lst lst = new LstBuilder().withKey(new LstKey("a")).build();
        final Def cont = new DefBuilder().setLst(Map.of(lst.key(), lst)).build();
        // Non-empty Map should be retained
        assertEquals(Map.of(lst.key(), lst), cont.getLst());
    }
}
