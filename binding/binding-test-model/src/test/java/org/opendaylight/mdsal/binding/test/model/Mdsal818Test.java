/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedListKey;
import org.opendaylight.yangtools.binding.DataObjectWildcard;

class Mdsal818Test {
    @Test
    void simpleWildcardInstanceIdentitifer() {
        final var id = DataObjectWildcard.builder(Top.class).child(TopLevelList.class).build();
        assertNull(id.tryToIdentifier());
        assertFalse(id instanceof DataObjectWildcard.WithKey);
    }

    @Test
    void simpleKeyedInstanceIdentitifer() {
        final var first = assertInstanceOf(DataObjectWildcard.WithKey.class, DataObjectWildcard.builder(Top.class)
            .child(TopLevelList.class, new TopLevelListKey("foo"))
            .build());
        assertNotNull(first.tryToIdentifier());
    }

    @Test
    void wildcardKeyedInstanceIdentitifer() {
        final var first = assertInstanceOf(DataObjectWildcard.WithKey.class, DataObjectWildcard.builder(Top.class)
            .child(TopLevelList.class)
            .child(NestedList.class, new NestedListKey("foo"))
            .build());
        assertNull(first.tryToIdentifier());
    }
}
