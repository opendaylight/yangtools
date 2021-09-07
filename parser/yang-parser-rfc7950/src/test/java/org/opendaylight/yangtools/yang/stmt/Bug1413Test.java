/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;

/**
 * Test ANTLR4 grammar capability to parse unknown node in extension argument
 * declaration.
 *
 * <p>
 * Note: Everything under unknown node is unknown node.
 */
public class Bug1413Test {
    @Test
    public void test() throws Exception {
        Collection<? extends ExtensionDefinition> extensions = TestUtils.loadModules(
            getClass().getResource("/bugs/bug1413").toURI())
                .findModules("bug1413").iterator().next().getExtensionSchemaNodes();
        assertEquals(1, extensions.size());

        ExtensionDefinition info = extensions.iterator().next();
        assertEquals("text", info.getArgument());
        assertTrue(info.isYinElement());
    }
}
