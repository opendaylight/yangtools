/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;

/**
 * Test ANTLR4 grammar capability to parse unknown node in extension argument
 * declaration.
 *
 * <p>
 * Note: Everything under unknown node is unknown node.
 */
class Bug1413Test extends AbstractYangTest {
    @Test
    void test() throws Exception {
        var extensions = assertEffectiveModelDir("/bugs/bug1413").findModules("bug1413").iterator().next()
            .getExtensionSchemaNodes();
        assertEquals(1, extensions.size());

        ExtensionDefinition info = extensions.iterator().next();
        assertEquals("text", info.getArgument());
        assertTrue(info.isYinElement());
    }
}
