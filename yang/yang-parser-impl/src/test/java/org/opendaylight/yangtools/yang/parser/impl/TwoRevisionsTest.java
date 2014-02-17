/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TwoRevisionsTest {

    @Test
    public void testTwoRevisions() throws Exception {
        YangModelParser parser = new YangParserImpl();

        Set<Module> modules = TestUtils.loadModules(getClass().getResource("/ietf").toURI());
        assertEquals(2, TestUtils.findModules(modules, "network-topology").size());

        SchemaContext schemaContext = parser.resolveSchemaContext(modules);
        assertEquals(2, TestUtils.findModules(schemaContext.getModules(), "network-topology").size());

    }

}
