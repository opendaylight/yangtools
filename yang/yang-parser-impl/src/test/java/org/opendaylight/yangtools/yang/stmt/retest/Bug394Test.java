/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * Test antlr grammar capability to parse nested unknown nodes.
 */
public class Bug394Test {

    @Test
    public void testParseList() throws Exception {
        Set<Module> modules = TestUtils.loadModules(getClass().getResource("/bugs/bug394-retest").toURI());
        Module bug394 = TestUtils.findModule(modules, "bug394");
        assertNotNull(bug394);
        Module bug394_ext = TestUtils.findModule(modules, "bug394-ext");
        assertNotNull(bug394_ext);

        ContainerSchemaNode logrecords = (ContainerSchemaNode) bug394.getDataChildByName("logrecords");
        assertNotNull(logrecords);

        List<UnknownSchemaNode> nodes = logrecords.getUnknownSchemaNodes();
        assertEquals(2, nodes.size());

        List<ExtensionDefinition> extensions = bug394_ext.getExtensionSchemaNodes();
        assertEquals(3, extensions.size());

        assertTrue(extensions.contains(nodes.get(0).getExtensionDefinition()));
        assertTrue(extensions.contains(nodes.get(1).getExtensionDefinition()));
    }

}
