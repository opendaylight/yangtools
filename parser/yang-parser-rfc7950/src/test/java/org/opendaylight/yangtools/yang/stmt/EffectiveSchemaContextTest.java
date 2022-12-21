/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveSchemaContext;

class EffectiveSchemaContextTest extends AbstractYangTest {
    @Test
    void testEffectiveSchemaContext() {
        final var schemaContext = assertEffectiveModel(
            "/effective-schema-context-test/foo.yang",
            "/effective-schema-context-test/bar.yang",
            "/effective-schema-context-test/baz.yang");

        assertEquals(3, schemaContext.getDataDefinitions().size());
        assertEquals(3, schemaContext.getChildNodes().size());
        assertEquals(3, schemaContext.getNotifications().size());
        assertEquals(3, schemaContext.getOperations().size());
        assertEquals(3, schemaContext.getExtensions().size());

        for (var module : schemaContext.getModuleStatements().values()) {
            assertEquals(1, module.getDeclared().declaredSubstatements(UnrecognizedStatement.class).size());
        }

        assertNull(schemaContext.dataChildByName(QName.create("foo-namespace", "2016-09-21", "foo-cont")));

        assertFalse(schemaContext.findModule("foo", Revision.of("2016-08-21")).isPresent());
        assertFalse(schemaContext.findModule(XMLNamespace.of("foo-namespace"), Revision.of("2016-08-21")).isPresent());

        assertFalse(schemaContext.isAugmenting());
        assertFalse(schemaContext.isAddedByUses());
        assertEquals(Optional.empty(), schemaContext.effectiveConfig());
        assertFalse(schemaContext.getWhenCondition().isPresent());
        assertEquals(0, schemaContext.getMustConstraints().size());
        assertFalse(schemaContext.getDescription().isPresent());
        assertFalse(schemaContext.getReference().isPresent());
        assertEquals(SchemaContext.NAME, schemaContext.getQName());
        assertEquals(Status.CURRENT, schemaContext.getStatus());
        assertNotNull(schemaContext.getUses());
        assertTrue(schemaContext.getUses().isEmpty());
        assertNotNull(schemaContext.getAvailableAugmentations());
        assertTrue(schemaContext.getAvailableAugmentations().isEmpty());

        assertTrue(schemaContext.findModule("foo", Revision.of("2016-09-21")).isPresent());
        assertEquals(3, schemaContext.getModules().size());
        assertEquals(3, ((EffectiveSchemaContext) schemaContext).getRootDeclaredStatements().size());
        assertEquals(3, schemaContext.getModuleStatements().size());
    }
}
