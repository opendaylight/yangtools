/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
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

public class YangExtensionArgumentTest {

    private List<ExtensionDefinition> extensions;
    private List<UnknownSchemaNode> unknownSchemaNodes;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        Set<Module> modules = TestUtils.loadModules(getClass().getResource
                ("/semantic-statement-parser/extension-argument-name")
                .toURI());

        assertEquals(2, modules.size());

        Module semanticVersion = TestUtils.findModule(modules, "semantic-version");
        assertNotNull(semanticVersion);

        extensions = semanticVersion.getExtensionSchemaNodes();
        assertEquals(2, extensions.size());

        Module foo = TestUtils.findModule(modules, "foo");
        assertNotNull(foo);

        unknownSchemaNodes = foo.getUnknownSchemaNodes();
        assertEquals(2, unknownSchemaNodes.size());
    }

    @Test
    public void ExtensionDefinitionDefaultTest() {
        ExtensionDefinition extension = extensions.get(0);
        assertEquals("extension", extension.getQName().getLocalName());
        assertEquals("name", extension.getArgument());
    }

    @Test
    public void ExtensionDefinitionCustomTest() {
        ExtensionDefinition extension = extensions.get(1);
        assertEquals("semantic-version", extension.getQName().getLocalName());
        assertEquals("semantic-version", extension.getArgument());
    }

    @Test
    public void ExtensionParameterDefaultTest() {
        UnknownSchemaNode node = unknownSchemaNodes.get(0);
        assertEquals("1.0.0-value", node.getNodeParameter());
    }

    @Test
    public void ExtensionParameterCustomTest() {
        UnknownSchemaNode node = unknownSchemaNodes.get(1);
        assertEquals("0.1.1", node.getNodeParameter());
    }
}
