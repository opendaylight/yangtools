/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.Status;

class LeafrefTest {
    @Test
    void testMethodsOfLeafrefTest() {
        final var qname = QName.create("test", "List1");
        final var revision = mock(PathExpression.class);
        final var revision2 = mock(PathExpression.class);

        final var leafref = BaseTypes.leafrefTypeBuilder(qname).setPathStatement(revision).build();
        final var leafref2 = BaseTypes.leafrefTypeBuilder(qname).setPathStatement(revision2).build();
        final var leafref3 = BaseTypes.leafrefTypeBuilder(qname).setPathStatement(revision).build();
        final var leafref4 = leafref;

        assertNotNull(leafref, "Object 'leafref' shouldn't be null.");
        assertNull(leafref.getBaseType(), "Base type of 'leafref' should be null.");
        assertEquals(Optional.empty(), leafref.getUnits());
        assertEquals(Optional.empty(), leafref.getDefaultValue());
        assertEquals(qname, leafref.getQName());
        assertEquals(Optional.empty(), leafref.getDescription());
        assertEquals(Optional.empty(), leafref.getReference());
        assertEquals(Status.CURRENT, leafref.getStatus(), "Status of 'leafref' is current.");
        assertTrue(leafref.getUnknownSchemaNodes().isEmpty(),
                "Object 'leafref' shouldn't have any unknown schema nodes.");
        assertEquals(revision,
                leafref.getPathStatement(),
                "Revision aware XPath of 'leafref' should be '/test:Cont1/test:List1'.");
        assertNotNull(leafref.toString(), "String representation of 'leafref' shouldn't be null.");
        assertNotEquals(leafref.hashCode(),
                leafref2.hashCode(),
                "Hash codes of two different object of type Leafref shouldn't be equal.");
        assertEquals(leafref, leafref3, "Objects of type Leafref should be equal.");
        assertEquals(leafref, leafref4, "Objects of type Leafref should be equal.");
        assertNotEquals(leafref, leafref2, "Objects of type Leafref shouldn't be equal.");
        assertNotEquals(null, leafref, "Objects shouldn't be equal.");
        assertNotEquals("test", leafref, "Objects shouldn't be equal.");
    }

    @Test
    void testRequireInstanceSubstatement() {
        final var qname = QName.create("test", "my-leafref");
        final var path = mock(PathExpression.class);
        final var leafrefTypeBuilder = BaseTypes.leafrefTypeBuilder(qname).setPathStatement(path);

        assertTrue(leafrefTypeBuilder.build().requireInstance());

        leafrefTypeBuilder.setRequireInstance(false);
        final var falseLeafref = leafrefTypeBuilder.build();
        assertFalse(falseLeafref.requireInstance());

        leafrefTypeBuilder.setRequireInstance(true);
        final var trueLeafref = leafrefTypeBuilder.build();
        assertTrue(trueLeafref.requireInstance());

        final var falseBuilder = RestrictedTypes.newLeafrefBuilder(falseLeafref, qname);
        assertFalse(falseBuilder.build().requireInstance());

        final var trueBuilder = RestrictedTypes.newLeafrefBuilder(trueLeafref, qname);
        assertTrue(trueBuilder.build().requireInstance());
    }
}
