/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.DefBuilder;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.grp.LstBuilder;
import org.opendaylight.yang.gen.v1.mdsal442.keydef.norev.grp.LstKey;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.ContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.KeyedBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.KeyedKey;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal298.rev180129.container.UnkeyedBuilder;
import org.opendaylight.yang.gen.v1.urn.test.pattern.rev170101.ContBuilder;

public class TestListSquashing {
    @Test
    public void testEmptyLeafList() {
        final var obj = new ContBuilder().setTest3(Set.of()).build();
        // Eventhough return type is Set, it should be retained
        assertEquals(Set.of(), obj.getTest3());
    }

    @Test
    public void testEmptyUserOrderedLeafList() {
        final var obj = new ContBuilder().setTest4(List.of()).build();
        // Eventhough return type is List, it should be retained
        assertEquals(List.of(), obj.getTest4());
    }

    @Test
    public void testEmptyUserOrderedList() {
        final var cont = new ContainerBuilder()
                .setKeyed(List.of())
                .setUnkeyed(List.of())
                .build();
        // Empty Lists should become null
        assertNull(cont.getKeyed());
        assertNull(cont.getUnkeyed());
    }

    @Test
    public void testUserOrderedList() {
        final var keyed = new KeyedBuilder().withKey(new KeyedKey("a")).build();
        final var unkeyed = new UnkeyedBuilder().build();
        final var cont = new ContainerBuilder()
                .setKeyed(List.of(keyed))
                .setUnkeyed(List.of(unkeyed))
                .build();
        // Non-empty Lists should be retained
        assertEquals(List.of(keyed), cont.getKeyed());
        assertEquals(List.of(unkeyed), cont.getUnkeyed());
    }

    @Test
    public void testEmptySystemOrderedList() {
        final var def = new DefBuilder().setLst(Map.of()).build();
        // Empty Map should become null
        assertNull(def.getLst());
    }

    @Test
    public void testSystemOrderedList() {
        final var lst = new LstBuilder().withKey(new LstKey("a")).build();
        final var cont = new DefBuilder().setLst(Map.of(lst.key(), lst)).build();
        // Non-empty Map should be retained
        assertEquals(Map.of(lst.key(), lst), cont.getLst());
    }
}
