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

import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class Bug4456Test {
    @Test
    public void test() throws Exception {
        SchemaContext schema = StmtTestUtils.parseYangSources("/bugs/bug4456");
        assertNotNull(schema);

        Set<Module> modules = schema.findModuleByNamespace(new URI("foo"));
        assertEquals(1, modules.size());
        Module moduleFoo = modules.iterator().next();

        List<ExtensionDefinition> extensionSchemaNodes = moduleFoo.getExtensionSchemaNodes();
        assertEquals(5, extensionSchemaNodes.size());
        for (ExtensionDefinition extensionDefinition : extensionSchemaNodes) {

            List<UnknownSchemaNode> unknownSchemaNodes = extensionDefinition.getUnknownSchemaNodes();
            assertEquals(1, unknownSchemaNodes.size());
            UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.iterator().next();
            String unknownNodeExtensionDefName = unknownSchemaNode.getExtensionDefinition().getQName().getLocalName();

            List<UnknownSchemaNode> subUnknownSchemaNodes = unknownSchemaNode.getUnknownSchemaNodes();
            assertEquals(1, subUnknownSchemaNodes.size());
            UnknownSchemaNode subUnknownSchemaNode = subUnknownSchemaNodes.iterator().next();
            String subUnknownNodeExtensionDefName = subUnknownSchemaNode.getExtensionDefinition().getQName()
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
