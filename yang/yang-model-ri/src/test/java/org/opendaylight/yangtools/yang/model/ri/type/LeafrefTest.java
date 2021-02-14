/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

public class LeafrefTest {
    @Test
    public void testMethodsOfLeafrefTest() {
        final QName qname = QName.create("test", "List1");
        final PathExpression revision = mock(PathExpression.class);
        final PathExpression revision2 = mock(PathExpression.class);

        final LeafrefTypeDefinition leafref = BaseTypes.leafrefTypeBuilder(qname).setPathStatement(revision).build();
        final LeafrefTypeDefinition leafref2 = BaseTypes.leafrefTypeBuilder(qname).setPathStatement(revision2).build();
        final LeafrefTypeDefinition leafref3 = BaseTypes.leafrefTypeBuilder(qname).setPathStatement(revision).build();
        final LeafrefTypeDefinition leafref4 = leafref;

        assertNotNull("Object 'leafref' shouldn't be null.", leafref);
        assertNull("Base type of 'leafref' should be null.", leafref.getBaseType());
        assertEquals(Optional.empty(), leafref.getUnits());
        assertEquals(Optional.empty(), leafref.getDefaultValue());
        assertEquals(qname, leafref.getQName());
        assertFalse(leafref.getDescription().isPresent());
        assertFalse(leafref.getReference().isPresent());
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
        final QName qname = QName.create("test", "my-leafref");
        final PathExpression path = mock(PathExpression.class);
        final LeafrefTypeBuilder leafrefTypeBuilder = BaseTypes.leafrefTypeBuilder(qname).setPathStatement(path);

        assertTrue(leafrefTypeBuilder.build().requireInstance());

        leafrefTypeBuilder.setRequireInstance(false);
        final LeafrefTypeDefinition falseLeafref = leafrefTypeBuilder.build();
        assertFalse(falseLeafref.requireInstance());

        leafrefTypeBuilder.setRequireInstance(true);
        final LeafrefTypeDefinition trueLeafref = leafrefTypeBuilder.build();
        assertTrue(trueLeafref.requireInstance());

        final RequireInstanceRestrictedTypeBuilder<LeafrefTypeDefinition> falseBuilder =
                RestrictedTypes.newLeafrefBuilder(falseLeafref, qname);
        assertFalse(falseBuilder.build().requireInstance());

        final RequireInstanceRestrictedTypeBuilder<LeafrefTypeDefinition> trueBuilder =
                RestrictedTypes.newLeafrefBuilder(trueLeafref, qname);
        assertTrue(trueBuilder.build().requireInstance());
    }
}
