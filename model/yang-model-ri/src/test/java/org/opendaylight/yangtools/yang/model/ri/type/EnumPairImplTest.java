/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

class EnumPairImplTest {
    @Test
    void testEnumPairImpl() {
        final var mockedUnknownSchemaNodeA = mock(UnknownSchemaNode.class);
        final var mockedUnknownSchemaNodeB = mock(UnknownSchemaNode.class);

        final var unknownSchemaNodes = List.of(mockedUnknownSchemaNodeA, mockedUnknownSchemaNodeB);

        final var enumPair = EnumPairBuilder.create("enum-zero", 0).setStatus(Status.DEPRECATED)
                .setDescription("enum description").setReference("enum reference")
                .setUnknownSchemaNodes(unknownSchemaNodes).build();

        assertNotNull(enumPair);
        assertEquals(unknownSchemaNodes, enumPair.getUnknownSchemaNodes());
        assertEquals(Optional.of("enum description"), enumPair.getDescription());
        assertEquals(Optional.of("enum reference"), enumPair.getReference());
        assertEquals(Status.DEPRECATED, enumPair.getStatus());

        final var enumPair2 = EnumPairBuilder.create("enum-zero", 0).setStatus(Status.DEPRECATED)
                .setDescription("enum description").setReference("enum reference")
                .setUnknownSchemaNodes(unknownSchemaNodes).build();

        assertEquals(enumPair.hashCode(), enumPair2.hashCode());
        assertEquals(enumPair, enumPair2);

        final var enumPair3 = EnumPairBuilder.create("enum-one", 1).setStatus(Status.DEPRECATED)
                .setDescription("enum description").setReference("enum reference")
                .setUnknownSchemaNodes(unknownSchemaNodes).build();

        assertNotEquals(enumPair2, enumPair3);

        assertNotEquals("unequal", enumPair);
    }
}
