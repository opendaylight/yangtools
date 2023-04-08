/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class LeafrefStatementTest extends AbstractYangTest {
    @Test
    void testRequireInstanceInLeafrefs() {
        final var context = assertEffectiveModel("/rfc7950/leafref-stmt/foo.yang");

        final var foo = context.findModule("foo", Revision.of("2016-12-20")).orElseThrow();
        final var typeDefinitions = foo.getTypeDefinitions();
        assertEquals(1, typeDefinitions.size());

        final var leafrefTypeDefinition = assertInstanceOf(LeafrefTypeDefinition.class,
            typeDefinitions.iterator().next());
        assertTrue(leafrefTypeDefinition.requireInstance());

        final var leafrefA = assertInstanceOf(LeafSchemaNode.class,
            foo.getDataChildByName(QName.create(foo.getQNameModule(), "leafref-a")));
        assertRequireInstanceInLeafref(leafrefA, true);

        final var leafrefB = assertInstanceOf(LeafSchemaNode.class,
            foo.getDataChildByName(QName.create(foo.getQNameModule(), "leafref-b")));
        assertRequireInstanceInLeafref(leafrefB, true);

        final var leafrefC = assertInstanceOf(LeafSchemaNode.class,
            foo.getDataChildByName(QName.create(foo.getQNameModule(), "leafref-c")));
        assertRequireInstanceInLeafref(leafrefC, true);
    }

    private static void assertRequireInstanceInLeafref(final LeafSchemaNode leaf, final boolean requireInstance) {
        assertEquals(requireInstance, assertInstanceOf(LeafrefTypeDefinition.class, leaf.getType()).requireInstance());
    }

    @Test
    void testInvalidYang10() {
        assertInvalidSubstatementException(startsWith("REQUIRE_INSTANCE is not valid for TYPE"),
            "/rfc7950/leafref-stmt/foo10.yang");
    }
}
