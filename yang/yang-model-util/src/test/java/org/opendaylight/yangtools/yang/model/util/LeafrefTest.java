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

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;

public class LeafrefTest {

    @Test
    public void testMethodsOfLeafrefTest() {
        final SchemaPath schemaPath = SchemaPath.create(false, QName.create("Cont1"), QName.create("List1"));
        final RevisionAwareXPathImpl revision = new RevisionAwareXPathImpl("/test:Cont1/test:List1", false);
        final RevisionAwareXPathImpl revision2 = new RevisionAwareXPathImpl("/test:Cont1/test:List2", false);

        final Leafref leafref = Leafref.create(schemaPath, revision);
        final Leafref leafref2 = Leafref.create(schemaPath, revision2);
        final Leafref leafref3 = Leafref.create(schemaPath, revision);
        final Leafref leafref4 = leafref;

        assertNotNull("Object 'leafref' shouldn't be null.", leafref);
        assertNull("Base type of 'leafref' should be null.", leafref.getBaseType());
        assertTrue("Units of 'leafref' should be empty.", leafref.getUnits().isEmpty());
        assertNull("Leafref does not have a default value", leafref.getDefaultValue());
        assertEquals("QName of 'leafref' is value '(urn:ietf:params:xml:ns:yang:1)leafref'.",
                BaseTypes.constructQName("leafref"), leafref.getQName());
        assertEquals("SchemaPath of 'leafref' is '/Cont1/List1'.", schemaPath, leafref.getPath());
        assertEquals("Description of 'leafref' is 'The leafref type is used to reference a particular leaf instance in the data tree.'",
                "The leafref type is used to reference a particular leaf instance in the data tree.", leafref.getDescription());
        assertEquals("Reference of 'leafref' is 'https://tools.ietf.org/html/rfc6020#section-9.9'.", "https://tools.ietf.org/html/rfc6020#section-9.9", leafref.getReference());
        assertEquals("Status of 'leafref' is current.", Status.CURRENT, leafref.getStatus());
        assertTrue("Object 'leafref' shouldn't have any unknown schema nodes.", leafref.getUnknownSchemaNodes().isEmpty());
        assertEquals("Revision aware XPath of 'leafref' should be '/test:Cont1/test:List1'.", revision, leafref.getPathStatement());
        assertNotNull("String representation of 'leafref' shouldn't be null.", leafref.toString());
        assertNotEquals("Hash codes of two different object of type Leafref shouldn't be equal.", leafref.hashCode(), leafref2.hashCode());
        assertTrue("Objects of type Leafref should be equal.", leafref.equals(leafref3));
        assertTrue("Objects of type Leafref should be equal.", leafref.equals(leafref4));
        assertFalse("Objects of type Leafref shouldn't be equal.", leafref.equals(leafref2));
        assertFalse("Objects shouldn't be equal.", leafref.equals(null));
        assertFalse("Objects shouldn't be equal.", leafref.equals("test"));
    }
}
