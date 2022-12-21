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

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveSchemaContext;

class EffectiveSchemaContextTest {
    @Test
    void testEffectiveSchemaContext() throws ReactorException, ParseException, URISyntaxException, IOException,
        YangSyntaxErrorException {
        final EffectiveSchemaContext schemaContext = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(StmtTestUtils.sourceForResource("/effective-schema-context-test/foo.yang"))
            .addSource(StmtTestUtils.sourceForResource("/effective-schema-context-test/bar.yang"))
            .addSource(StmtTestUtils.sourceForResource("/effective-schema-context-test/baz.yang"))
            .buildEffective();
        assertNotNull(schemaContext);

        final Collection<? extends DataSchemaNode> dataDefinitions = schemaContext.getDataDefinitions();
        assertEquals(3, dataDefinitions.size());

        final Collection<? extends DataSchemaNode> childNodes = schemaContext.getChildNodes();
        assertEquals(3, childNodes.size());

        final Collection<? extends NotificationDefinition> notifications = schemaContext.getNotifications();
        assertEquals(3, notifications.size());

        final Collection<? extends RpcDefinition> rpcs = schemaContext.getOperations();
        assertEquals(3, rpcs.size());

        final Collection<? extends ExtensionDefinition> extensions = schemaContext.getExtensions();
        assertEquals(3, extensions.size());

        for (ModuleEffectiveStatement module : schemaContext.getModuleStatements().values()) {
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
        assertEquals(3, schemaContext.getRootDeclaredStatements().size());
        assertEquals(3, schemaContext.getModuleStatements().size());
    }
}
