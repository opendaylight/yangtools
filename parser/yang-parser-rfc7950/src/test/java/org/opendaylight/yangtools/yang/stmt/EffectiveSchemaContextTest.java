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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
import org.opendaylight.yangtools.yang.model.api.meta.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.spi.SimpleEffectiveModelContext;

class EffectiveSchemaContextTest extends AbstractYangTest {
    @Test
    void testEffectiveSchemaContext() {
        final var modelContext = assertInstanceOf(SimpleEffectiveModelContext.class, assertEffectiveModel(
            "/effective-schema-context-test/foo.yang",
            "/effective-schema-context-test/bar.yang",
            "/effective-schema-context-test/baz.yang"));

        assertEquals(3, modelContext.getDataDefinitions().size());
        assertEquals(3, modelContext.getChildNodes().size());
        assertEquals(3, modelContext.getNotifications().size());
        assertEquals(3, modelContext.getOperations().size());
        assertEquals(3, modelContext.getExtensions().size());

        for (var module : modelContext.getModuleStatements().values()) {
            assertEquals(1, module.requireDeclared().declaredSubstatements(UnrecognizedStatement.class).size());
        }

        assertNull(modelContext.dataChildByName(QName.create("foo-namespace", "2016-09-21", "foo-cont")));

        assertFalse(modelContext.findModule("foo", Revision.of("2016-08-21")).isPresent());
        assertFalse(modelContext.findModule(XMLNamespace.of("foo-namespace"), Revision.of("2016-08-21")).isPresent());

        assertFalse(modelContext.isAugmenting());
        assertFalse(modelContext.isAddedByUses());
        assertEquals(Optional.empty(), modelContext.effectiveConfig());
        assertFalse(modelContext.getWhenCondition().isPresent());
        assertEquals(0, modelContext.getMustConstraints().size());
        assertFalse(modelContext.getDescription().isPresent());
        assertFalse(modelContext.getReference().isPresent());
        assertEquals(SchemaContext.NAME, modelContext.getQName());
        assertEquals(Status.CURRENT, modelContext.getStatus());
        assertNotNull(modelContext.getUses());
        assertTrue(modelContext.getUses().isEmpty());
        assertNotNull(modelContext.getAvailableAugmentations());
        assertTrue(modelContext.getAvailableAugmentations().isEmpty());

        assertTrue(modelContext.findModule("foo", Revision.of("2016-09-21")).isPresent());
        assertEquals(3, modelContext.getModules().size());
        assertEquals(3, modelContext.getRootDeclaredStatements().size());
        assertEquals(3, modelContext.getModuleStatements().size());
    }
}
