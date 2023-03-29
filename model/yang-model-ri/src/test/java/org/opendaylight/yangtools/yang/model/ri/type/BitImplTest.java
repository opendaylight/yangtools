/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.URISyntaxException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.Status;

class BitImplTest {
    private static final Uint32 FIFTY_FIVE = Uint32.valueOf(55);

    @Test
    // We're testing equals()
    @SuppressWarnings({"ObjectEqualsNull", "EqualsBetweenInconvertibleTypes"})
    void test() throws URISyntaxException {

        // hashCode method test

        BitImpl biA = new BitImpl("someNameA2", FIFTY_FIVE, "description", "reference", Status.CURRENT, emptyList());

        assertEquals(biA, biA, "biA should equals to itsefl");
        assertNotEquals(null, biA, "biA shouldn't equal to null");
        assertNotEquals("str", biA, "biA shouldn't equal to object of other type");

         // // test schemaPath
        biA = new BitImpl("someNameA2", FIFTY_FIVE, "description", "reference", Status.CURRENT, emptyList());
        BitImpl biB = new BitImpl("someNameB2", FIFTY_FIVE, "description", "reference", Status.CURRENT, emptyList());
        assertNotEquals(biA, biB, "biA shouldn't equal to biB");

        biA = new BitImpl("someNameB2", FIFTY_FIVE, "description", "reference", Status.CURRENT, emptyList());
        biB = new BitImpl("someNameB2", FIFTY_FIVE, "description", "reference", Status.CURRENT, emptyList());
        assertEquals(biA, biB, "biA should equal to biB");

        biA = new BitImpl("someNameA2", FIFTY_FIVE, "description", "reference", Status.CURRENT, emptyList());
        biB = new BitImpl("someNameB2", FIFTY_FIVE, "description", "reference", Status.CURRENT, emptyList());
        assertNotEquals(biA, biB, "biA shouldn't equal to biB");

        biA = new BitImpl("someNameA2", FIFTY_FIVE, "description", "reference", Status.CURRENT, emptyList());
        biB = new BitImpl("someNameA2", FIFTY_FIVE, "description", "reference", Status.CURRENT, emptyList());
        assertEquals(biA, biB, "biA should equal to biB");

        biA = new BitImpl("someNameA2", FIFTY_FIVE, "description", "reference", Status.CURRENT, emptyList());

        // test of getter methods
        assertEquals("someNameA2", biA.getName(), "Incorrect value for qname.");
        assertEquals(Optional.of("description"), biA.getDescription(), "Incorrect value for description.");
        assertEquals(Optional.of("reference"), biA.getReference(), "Incorrect value for reference.");
        assertEquals(Status.CURRENT, biA.getStatus(), "Incorrect value for status.");
        assertEquals(emptyList(), biA.getUnknownSchemaNodes(), "Incorrect value for unknown nodes.");

        // test of toString method
        assertEquals("Bit[name=someNameA2, position=55]",
                biA.toString(),
                "toString method doesn't return correct value");
    }
}
