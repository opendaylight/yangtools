/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;

public class BitImplTest {

    @Test
    // We're testing equals()
    @SuppressWarnings({"ObjectEqualsNull", "EqualsBetweenInconvertibleTypes"})
    public void test() throws URISyntaxException {

        // hashCode method test
        final URI uriA1 = new URI("some:uriA1");
        final URI uriA2 = new URI("some:uriA2");
        final URI uriB1 = new URI("some:uriB1");
        final URI uriB2 = new URI("some:uriB2");

        QName qnameA1 = QName.create(uriA1, new Date(6000000), "some nameA1");
        QName qnameA2 = QName.create(uriA2, new Date(7000000), "some nameA2");
        SchemaPath schemaPathA = SchemaPath.create(true, qnameA1, qnameA2);

        QName qnameB1 = QName.create(uriB1, new Date(6000000), "some nameB1");
        QName qnameB2 = QName.create(uriB2, new Date(7000000), "some nameB2");
        SchemaPath schemaPathB = SchemaPath.create(true, qnameB1, qnameB2);

        BitImpl biA = new BitImpl(schemaPathA, 55L, "description", "reference", Status.CURRENT, emptyList());

        assertEquals("biA should equals to itsefl", biA, biA);
        assertFalse("biA shouldn't equal to null", biA.equals(null));
        assertFalse("biA shouldn't equal to object of other type", biA.equals("str"));

         // // test schemaPath
        biA = new BitImpl(schemaPathA, 55L, "description", "reference", Status.CURRENT, emptyList());
        VitImpl BitImpl biB = new BitImpl(schemaPathB, 55L, "description", "reference", Status.CURRENT, emptyList());
        assertFalse("biA shouldn't equal to biB", biA.equals(biB));

        biA = new BitImpl(schemaPathB, 55L, "description", "reference", Status.CURRENT, emptyList());
        biB = new BitImpl(schemaPathB, 55L, "description", "reference", Status.CURRENT, emptyList());
        assertEquals("biA should equal to biB", biA, biB);

        biA = new BitImpl(schemaPathA, 55L, "description", "reference", Status.CURRENT, emptyList());
        biB = new BitImpl(schemaPathB, 55L, "description", "reference", Status.CURRENT, emptyList());
        assertFalse("biA shouldn't equal to biB", biA.equals(biB));

        biA = new BitImpl(schemaPathA, 55L, "description", "reference", Status.CURRENT, emptyList());
        biB = new BitImpl(schemaPathA, 55L, "description", "reference", Status.CURRENT, emptyList());
        assertEquals("biA should equal to biB", biA, biB);

        biA = new BitImpl(schemaPathA, 55L, "description", "reference", Status.CURRENT, emptyList());

        // test of getter methods
        assertEquals("Incorrect value for qname.", qnameA2, biA.getQName());
        assertEquals("Incorrect value for schema path.", schemaPathA, biA.getPath());
        assertEquals("Incorrect value for description.", "description", biA.getDescription());
        assertEquals("Incorrect value for reference.", "reference", biA.getReference());
        assertEquals("Incorrect value for status.", Status.CURRENT, biA.getStatus());
        assertEquals("Incorrect value for unknown nodes.", emptyList(), biA.getUnknownSchemaNodes());

        // test of toString method
        assertEquals("toString method doesn't return correct value", "Bit[name=some nameA2, position=55]", biA.toString());
    }
}
