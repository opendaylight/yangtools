/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;

public class Bug4456Test {
    @Test
    public void test() throws Exception {
        SchemaContext schema = StmtTestUtils.parseYangSources("/bugs/bug4456");
        assertNotNull(schema);

        Set<Module> modules = (Set<Module>) schema.findModules(XMLNamespace.of("foo"));
        assertEquals(1, modules.size());
        Module moduleFoo = modules.iterator().next();

        Collection<? extends ExtensionDefinition> extensionSchemaNodes = moduleFoo.getExtensionSchemaNodes();
        assertEquals(5, extensionSchemaNodes.size());
        for (ExtensionDefinition extensionDefinition : extensionSchemaNodes) {

            Collection<? extends UnrecognizedStatement> unknownSchemaNodes = extensionDefinition.asEffectiveStatement()
                .getDeclared().declaredSubstatements(UnrecognizedStatement.class);
            assertEquals(1, unknownSchemaNodes.size());
            UnrecognizedStatement unknownSchemaNode = unknownSchemaNodes.iterator().next();
            String unknownNodeExtensionDefName = unknownSchemaNode.statementDefinition().getStatementName()
                .getLocalName();

            Collection<? extends UnrecognizedStatement> subUnknownSchemaNodes =
                unknownSchemaNode.declaredSubstatements(UnrecognizedStatement.class);
            assertEquals(1, subUnknownSchemaNodes.size());
            UnrecognizedStatement subUnknownSchemaNode = subUnknownSchemaNodes.iterator().next();
            String subUnknownNodeExtensionDefName = subUnknownSchemaNode.statementDefinition().getStatementName()
                .getLocalName();

            switch (extensionDefinition.getQName().getLocalName()) {
                case "a":
                    assertEquals("b", unknownNodeExtensionDefName);
                    assertEquals("c", subUnknownNodeExtensionDefName);
                    break;
                case "b":
                    assertEquals("c", unknownNodeExtensionDefName);
                    assertEquals("a", subUnknownNodeExtensionDefName);
                    break;
                case "c":
                    assertEquals("a", unknownNodeExtensionDefName);
                    assertEquals("b", subUnknownNodeExtensionDefName);
                    break;
                case "r":
                    assertEquals("r", unknownNodeExtensionDefName);
                    assertEquals("r2", subUnknownNodeExtensionDefName);
                    break;
                case "r2":
                    assertEquals("r2", unknownNodeExtensionDefName);
                    assertEquals("r", subUnknownNodeExtensionDefName);
                    break;
                default:
                    fail("Unexpected extension definition");
            }
        }
    }
}
