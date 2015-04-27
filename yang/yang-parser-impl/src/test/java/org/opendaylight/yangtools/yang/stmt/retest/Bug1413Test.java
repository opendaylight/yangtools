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
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * Test ANTLR4 grammar capability to parse unknown node in extension argument
 * declaration.
 *
 * Note: Everything under unknown node is unknown node.
 */
public class Bug1413Test {

    @Test
    public void test() throws Exception {
        Set<Module> modules = TestUtils.loadModules(getClass().getResource("/bugs/bug1413").toURI());
        Module bug1413 = TestUtils.findModule(modules, "bug1413");
        assertNotNull(bug1413);

        List<ExtensionDefinition> extensions = bug1413.getExtensionSchemaNodes();
        assertEquals(1, extensions.size());

        ExtensionDefinition info = extensions.get(0);
        assertEquals("text", info.getArgument());
        assertTrue(info.isYinElement());
    }

}
