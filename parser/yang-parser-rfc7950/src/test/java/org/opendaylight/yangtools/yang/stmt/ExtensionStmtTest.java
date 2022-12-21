/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

class ExtensionStmtTest {
    @Test
    void testExtensionDefinition() throws ReactorException {
        final SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/model/bar.yang"))
            .buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModules("bar").iterator().next();
        assertNotNull(testModule);

        assertEquals(1, testModule.getExtensionSchemaNodes().size());

        final var extensions = testModule.getExtensionSchemaNodes();
        final ExtensionDefinition extension = extensions.iterator().next();
        assertEquals("opendaylight", extension.getQName().getLocalName());
        assertEquals("name", extension.getArgument());
        assertTrue(extension.isYinElement());
    }

    @Test
    void testExtensionUsage() throws ReactorException {
        final SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/semantic-statement-parser/ext-typedef.yang"))
            .addSource(sourceForResource("/semantic-statement-parser/ext-use.yang"))
            .buildEffective();
        assertNotNull(result);

        final Module testModule1 = result.findModules("ext-typedef").iterator().next();
        assertNotNull(testModule1);

        assertEquals(1, testModule1.getExtensionSchemaNodes().size());

        final var extensions = testModule1.getExtensionSchemaNodes();
        final ExtensionDefinition extensionDefinition = extensions.iterator().next();

        final Module testModule2 = result.findModules("ext-use").iterator().next();
        assertNotNull(testModule2);

        final LeafSchemaNode leaf = (LeafSchemaNode) testModule2.getDataChildByName(
            QName.create(testModule2.getQNameModule(), "value"));
        assertNotNull(leaf);

        final var unknownNodes = leaf.asEffectiveStatement().getDeclared()
            .declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, unknownNodes.size());
        final UnrecognizedStatement extensionUse = unknownNodes.iterator().next();
        assertEquals(extensionDefinition.getQName(), extensionUse.statementDefinition().getStatementName());
        assertEquals(extensionDefinition.getArgument(), extensionUse.statementDefinition().getArgumentDefinition()
            .orElseThrow().argumentName().getLocalName());

        assertEquals("key:value", extensionUse.argument());
    }
}
