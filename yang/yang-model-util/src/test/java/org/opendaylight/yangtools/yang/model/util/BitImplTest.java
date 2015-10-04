/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;

public class BitImplTest {
    private QName qnameA;
    private SchemaPath schemaPathA;
    private SchemaPath schemaPathB;

    @Before
    public void setup() {

        // hashCode method test
        URI uriA = null;
        URI uriA1 = null;
        URI uriA2 = null;
        URI uriB1 = null;
        URI uriB2 = null;
        boolean urisInitiallized = false;
        try {
            uriA = new URI("some:uriA");
            uriA1 = new URI("some:uriA1");
            uriA2 = new URI("some:uriA2");
            uriB1 = new URI("some:uriB1");
            uriB2 = new URI("some:uriB2");
            urisInitiallized = true;

        } catch (URISyntaxException e) {
            e.printStackTrace();
            assertTrue("Not all required uri variables were instantiated.", urisInitiallized);

        }
        qnameA = QName.create(uriA, new Date(5000000), "some name");

        QName qnameA1 = QName.create(uriA1, new Date(6000000), "some nameA1");
        QName qnameA2 = QName.create(uriA2, new Date(7000000), "some nameA2");
        List<QName> qnamesA = new ArrayList<>();
        qnamesA.add(qnameA1);
        qnamesA.add(qnameA2);
        schemaPathA = SchemaPath.create(qnamesA, true);

        QName qnameB1 = QName.create(uriB1, new Date(6000000), "some nameB1");
        QName qnameB2 = QName.create(uriB2, new Date(7000000), "some nameB2");
        List<QName> qnamesB = new ArrayList<>();
        qnamesB.add(qnameB1);
        qnamesB.add(qnameB2);
        schemaPathB = SchemaPath.create(qnamesB, true);
    }

    @Test
    public void test() {

        BitImpl biB = null;
        BitImpl biA = new BitImpl(schemaPathA, "description", "reference", Status.CURRENT, null, 55L);

        assertEquals("biA should equals to itself", biA, biA);
        assertFalse("biA shouldn't equal to null", biA.equals(null));
        assertFalse("biA shouldn't equal to object of other type", biA.equals("str"));

        // test schemaPath
        biA = new BitImpl(schemaPathA, "description", "reference", Status.CURRENT, null, 55L);
        biB = new BitImpl(schemaPathA, "description", "reference", Status.CURRENT, null, 55L);
        assertEquals("biA should equal to biB", biA, biB);

        biA = new BitImpl(schemaPathA, "description", "reference", Status.CURRENT, null, 55L);
        biB = new BitImpl(schemaPathB, "description", "reference", Status.CURRENT, null, 55L);
        assertFalse("biA shouldn't equal to biB", biA.equals(biB));

        biA = new BitImpl(schemaPathA, "description", "reference", Status.CURRENT,null, 55L);

        // test of getter methods
        assertEquals("Incorrect value for qname.", qnameA, biA.getQName());
        assertEquals("Incorrect value for schema path.", schemaPathA, biA.getPath());
        assertEquals("Incorrect value for description.", "description", biA.getDescription());
        assertEquals("Incorrect value for reference.", "reference", biA.getReference());
        assertEquals("Incorrect value for status.", Status.CURRENT, biA.getStatus());
        assertEquals("Incorrect value for unknown nodes.", Collections.emptyList(), biA.getUnknownSchemaNodes());

        // test of toString method
        assertEquals("toString method doesn't return correct value", "Bit[name=some name, position=55]", biA.toString());

    }
}
