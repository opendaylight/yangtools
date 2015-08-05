/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.retest.TestUtils;

public class YinFileLeafListStmtTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource
                ("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(10, modules.size());
    }

    @Test
    public void testLeafList() {
        Module testModule = TestUtils.findModule(modules, "ietf-netconf-monitoring");
        assertNotNull(testModule);

        ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName("netconf-state");
        assertNotNull(container);

        container = (ContainerSchemaNode) container.getDataChildByName("capabilities");
        assertNotNull(container);

        LeafListSchemaNode leafList = (LeafListSchemaNode) container.getDataChildByName("capability");
        assertNotNull(leafList);
        assertEquals("uri", leafList.getType().getQName().getLocalName());
        assertEquals("List of NETCONF capabilities supported by the server.", leafList.getDescription());
        assertFalse(leafList.isUserOrdered());
    }
}
