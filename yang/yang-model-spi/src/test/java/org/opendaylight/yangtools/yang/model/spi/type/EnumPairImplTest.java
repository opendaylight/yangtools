/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.spi.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

public class EnumPairImplTest {

    @Test
    public void testEnumPairImpl() {
        final UnknownSchemaNode mockedUnknownSchemaNodeA = mock(UnknownSchemaNode.class);
        final UnknownSchemaNode mockedUnknownSchemaNodeB = mock(UnknownSchemaNode.class);

        final Collection<UnknownSchemaNode> unknownSchemaNodes =
                ImmutableList.of(mockedUnknownSchemaNodeA, mockedUnknownSchemaNodeB);

        final EnumPair enumPair = EnumPairBuilder.create("enum-zero", 0).setStatus(Status.DEPRECATED)
                .setDescription("enum description").setReference("enum reference")
                .setUnknownSchemaNodes(unknownSchemaNodes).build();

        assertNotNull(enumPair);
        assertEquals(unknownSchemaNodes, enumPair.getUnknownSchemaNodes());
        assertEquals(Optional.of("enum description"), enumPair.getDescription());
        assertEquals(Optional.of("enum reference"), enumPair.getReference());
        assertEquals(Status.DEPRECATED, enumPair.getStatus());

        final EnumPair enumPair2 = EnumPairBuilder.create("enum-zero", 0).setStatus(Status.DEPRECATED)
                .setDescription("enum description").setReference("enum reference")
                .setUnknownSchemaNodes(unknownSchemaNodes).build();

        assertEquals(enumPair.hashCode(), enumPair2.hashCode());
        assertTrue(enumPair.equals(enumPair2));

        final EnumPair enumPair3 = EnumPairBuilder.create("enum-one", 1).setStatus(Status.DEPRECATED)
                .setDescription("enum description").setReference("enum reference")
                .setUnknownSchemaNodes(unknownSchemaNodes).build();

        assertFalse(enumPair2.equals(enumPair3));

        assertFalse(enumPair.equals("unequal"));
    }
}
