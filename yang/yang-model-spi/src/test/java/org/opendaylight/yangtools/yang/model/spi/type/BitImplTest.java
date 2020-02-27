/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URISyntaxException;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Status;

public class BitImplTest {

    @Test
    // We're testing equals()
    @SuppressWarnings({"ObjectEqualsNull", "EqualsBetweenInconvertibleTypes"})
    public void test() throws URISyntaxException {

        // hashCode method test

        BitImpl biA = new BitImpl("someNameA2", 55L, "description", "reference", Status.CURRENT, emptyList());

        assertEquals("biA should equals to itsefl", biA, biA);
        assertFalse("biA shouldn't equal to null", biA.equals(null));
        assertFalse("biA shouldn't equal to object of other type", biA.equals("str"));

         // // test schemaPath
        biA = new BitImpl("someNameA2", 55L, "description", "reference", Status.CURRENT, emptyList());
        BitImpl biB = new BitImpl("someNameB2", 55L, "description", "reference", Status.CURRENT, emptyList());
        assertFalse("biA shouldn't equal to biB", biA.equals(biB));

        biA = new BitImpl("someNameB2", 55L, "description", "reference", Status.CURRENT, emptyList());
        biB = new BitImpl("someNameB2", 55L, "description", "reference", Status.CURRENT, emptyList());
        assertEquals("biA should equal to biB", biA, biB);

        biA = new BitImpl("someNameA2", 55L, "description", "reference", Status.CURRENT, emptyList());
        biB = new BitImpl("someNameB2", 55L, "description", "reference", Status.CURRENT, emptyList());
        assertFalse("biA shouldn't equal to biB", biA.equals(biB));

        biA = new BitImpl("someNameA2", 55L, "description", "reference", Status.CURRENT, emptyList());
        biB = new BitImpl("someNameA2", 55L, "description", "reference", Status.CURRENT, emptyList());
        assertEquals("biA should equal to biB", biA, biB);

        biA = new BitImpl("someNameA2", 55L, "description", "reference", Status.CURRENT, emptyList());

        // test of getter methods
        assertEquals("Incorrect value for qname.", "someNameA2", biA.getName());
        assertEquals("Incorrect value for description.", Optional.of("description"), biA.getDescription());
        assertEquals("Incorrect value for reference.", Optional.of("reference"), biA.getReference());
        assertEquals("Incorrect value for status.", Status.CURRENT, biA.getStatus());
        assertEquals("Incorrect value for unknown nodes.", emptyList(), biA.getUnknownSchemaNodes());

        // test of toString method
        assertEquals("toString method doesn't return correct value", "Bit[name=someNameA2, position=55]",
                biA.toString());
    }
}
