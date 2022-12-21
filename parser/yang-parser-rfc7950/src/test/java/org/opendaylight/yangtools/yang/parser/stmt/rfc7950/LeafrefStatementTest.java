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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class LeafrefStatementTest extends AbstractYangTest {

    @Test
    void testRequireInstanceInLeafrefs() {
        final var context = assertEffectiveModel("/rfc7950/leafref-stmt/foo.yang");

        final Module foo = context.findModule("foo", Revision.of("2016-12-20")).get();
        final Collection<? extends TypeDefinition<?>> typeDefinitions = foo.getTypeDefinitions();
        assertEquals(1, typeDefinitions.size());

        final TypeDefinition<?> typeDefinition = typeDefinitions.iterator().next();
        final LeafrefTypeDefinition leafrefTypeDefinition = (LeafrefTypeDefinition) typeDefinition;
        assertTrue(leafrefTypeDefinition.requireInstance());

        final LeafSchemaNode leafrefA = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
            "leafref-a"));
        assertRequireInstanceInLeafref(leafrefA, true);

        final LeafSchemaNode leafrefB = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
            "leafref-b"));
        assertRequireInstanceInLeafref(leafrefB, true);

        final LeafSchemaNode leafrefC = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
            "leafref-c"));
        assertRequireInstanceInLeafref(leafrefC, true);
    }

    private static void assertRequireInstanceInLeafref(final LeafSchemaNode leaf, final boolean requireInstance) {
        final LeafrefTypeDefinition leafrefTypeDefnition = (LeafrefTypeDefinition) leaf.getType();
        assertEquals(requireInstance, leafrefTypeDefnition.requireInstance());
    }

    @Test
    void testInvalidYang10() {
        assertInvalidSubstatementException(startsWith("REQUIRE_INSTANCE is not valid for TYPE"),
            "/rfc7950/leafref-stmt/foo10.yang");
    }
}
