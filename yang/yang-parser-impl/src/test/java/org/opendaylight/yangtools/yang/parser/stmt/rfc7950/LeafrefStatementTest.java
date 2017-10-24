/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class LeafrefStatementTest {

    @Test
    public void testRequireInstanceInLeafrefs() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/leafref-stmt/foo.yang");
        assertNotNull(schemaContext);

        final Module foo = schemaContext.findModule("foo", QName.parseRevision("2016-12-20")).get();
        final Set<TypeDefinition<?>> typeDefinitions = foo.getTypeDefinitions();
        assertEquals(1, typeDefinitions.size());

        final TypeDefinition<?> typeDefinition = typeDefinitions.iterator().next();
        final LeafrefTypeDefinition leafrefTypeDefinition = (LeafrefTypeDefinition) typeDefinition;
        assertTrue(leafrefTypeDefinition.requireInstance());

        final LeafSchemaNode leafrefA = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "leafref-a"));
        assertNotNull(leafrefA);
        assertRequireInstanceInLeafref(leafrefA, true);

        final LeafSchemaNode leafrefB = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "leafref-b"));
        assertNotNull(leafrefB);
        assertRequireInstanceInLeafref(leafrefB, true);

        final LeafSchemaNode leafrefC = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "leafref-c"));
        assertNotNull(leafrefC);
        assertRequireInstanceInLeafref(leafrefC, false);
    }

    private static void assertRequireInstanceInLeafref(final LeafSchemaNode leaf, final boolean requireInstance) {
        final LeafrefTypeDefinition leafrefTypeDefnition = (LeafrefTypeDefinition) leaf.getType();
        assertEquals(requireInstance, leafrefTypeDefnition.requireInstance());
    }

    @Test
    public void testInvalidYang10() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/leafref-stmt/foo10.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith("REQUIRE_INSTANCE is not valid for TYPE"));
        }
    }
}
