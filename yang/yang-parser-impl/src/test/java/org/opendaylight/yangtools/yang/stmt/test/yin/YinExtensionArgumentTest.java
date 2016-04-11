/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.retest.TestUtils;

public class YinExtensionArgumentTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource
                ("/semantic-statement-parser/yin/extension-argument-name").toURI());
        assertEquals(2, modules.size());
    }

    @Test
    public void ExtensionDefinitionTest() {
        Module testModule = TestUtils.findModule(modules, "semantic-version");
        assertNotNull(testModule);

        List<ExtensionDefinition> extensions = testModule.getExtensionSchemaNodes();
        assertEquals(1, extensions.size());

        ExtensionDefinition extension = extensions.get(0);

        assertEquals("semantic-version", extension.getQName().getLocalName());
        assertEquals("semantic-version", extension.getArgument());
    }

    @Test
    public void ExtensionParameterTest() {
        Module testModule = TestUtils.findModule(modules, "foo");
        assertNotNull(testModule);

        UnknownSchemaNode node = testModule.getUnknownSchemaNodes().get(0);
        assertEquals("0.1.1", node.getNodeParameter());
    }
}
