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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.LeafrefTypeBuilder;

public class LeafrefTest {

    @Test
    public void testMethodsOfLeafrefTest() {
        final SchemaPath schemaPath = SchemaPath.create(false, QName.create("Cont1"), QName.create("List1"));
        final RevisionAwareXPathImpl revision = new RevisionAwareXPathImpl("/test:Cont1/test:List1", false);
        final RevisionAwareXPathImpl revision2 = new RevisionAwareXPathImpl("/test:Cont1/test:List2", false);

        final LeafrefTypeDefinition leafref = BaseTypes.leafrefTypeBuilder(schemaPath).setPathStatement(revision)
            .build();
        final LeafrefTypeDefinition leafref2 = BaseTypes.leafrefTypeBuilder(schemaPath).setPathStatement(revision2)
            .build();
        final LeafrefTypeDefinition leafref3 = BaseTypes.leafrefTypeBuilder(schemaPath).setPathStatement(revision)
            .build();
        final LeafrefTypeDefinition leafref4 = leafref;

        assertNotNull("Object 'leafref' shouldn't be null.", leafref);
        assertNull("Base type of 'leafref' should be null.", leafref.getBaseType());
        assertNull("Units of 'leafref' should be empty.", leafref.getUnits());
        assertNull("Leafref does not have a default value", leafref.getDefaultValue());
        assertEquals(QName.create("List1"), leafref.getQName());
        assertEquals("SchemaPath of 'leafref' is '/Cont1/List1'.", schemaPath, leafref.getPath());
        assertNull(leafref.getDescription());
        assertNull(leafref.getReference());
        assertEquals("Status of 'leafref' is current.", Status.CURRENT, leafref.getStatus());
        assertTrue("Object 'leafref' shouldn't have any unknown schema nodes.",
                leafref.getUnknownSchemaNodes().isEmpty());
        assertEquals("Revision aware XPath of 'leafref' should be '/test:Cont1/test:List1'.", revision,
                leafref.getPathStatement());
        assertNotNull("String representation of 'leafref' shouldn't be null.", leafref.toString());
        assertNotEquals("Hash codes of two different object of type Leafref shouldn't be equal.", leafref.hashCode(),
                leafref2.hashCode());
        assertTrue("Objects of type Leafref should be equal.", leafref.equals(leafref3));
        assertTrue("Objects of type Leafref should be equal.", leafref.equals(leafref4));
        assertFalse("Objects of type Leafref shouldn't be equal.", leafref.equals(leafref2));
        assertFalse("Objects shouldn't be equal.", leafref.equals(null));
        assertFalse("Objects shouldn't be equal.", leafref.equals("test"));
    }

    @Test
    public void testRequireInstanceSubstatement() {
        final SchemaPath schemaPath = SchemaPath.create(true, QName.create("my-cont"), QName.create("my-leafref"));
        final RevisionAwareXPathImpl path = new RevisionAwareXPathImpl("../my-leaf", false);

        LeafrefTypeBuilder leafrefTypeBuilder = BaseTypes.leafrefTypeBuilder(schemaPath).setPathStatement(path);

        leafrefTypeBuilder.setRequireInstance(false);
        LeafrefTypeDefinition leafref = leafrefTypeBuilder.build();
        assertFalse(leafref.requireInstance());

        leafrefTypeBuilder.setRequireInstance(true);
        leafref = leafrefTypeBuilder.build();
        assertTrue(leafref.requireInstance());

        leafrefTypeBuilder.setRequireInstance(true);
        leafref = leafrefTypeBuilder.build();
        assertTrue(leafref.requireInstance());

        try {
            leafrefTypeBuilder.setRequireInstance(false);
            fail("An IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException ex) {
            assertEquals("Cannot switch off require-instance in type AbsoluteSchemaPath{path=[my-cont, my-leafref]}",
                    ex.getMessage());
        }

    }
}
