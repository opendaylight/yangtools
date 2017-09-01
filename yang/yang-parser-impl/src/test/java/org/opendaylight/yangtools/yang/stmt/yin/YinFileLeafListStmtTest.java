/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;
import org.xml.sax.SAXException;

public class YinFileLeafListStmtTest {

    private SchemaContext context;

    @Before
    public void init() throws URISyntaxException, ReactorException, SAXException, IOException {
        context = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, context.getModules().size());
    }

    @Test
    public void testLeafList() {
        final Module testModule = TestUtils.findModule(context, "ietf-netconf-monitoring").get();
        assertNotNull(testModule);

        ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName(QName.create(
                testModule.getQNameModule(), "netconf-state"));
        assertNotNull(container);

        container = (ContainerSchemaNode) container.getDataChildByName(QName.create(testModule.getQNameModule(),
                "capabilities"));
        assertNotNull(container);

        final LeafListSchemaNode leafList = (LeafListSchemaNode) container.getDataChildByName(QName.create(
                testModule.getQNameModule(), "capability"));
        assertNotNull(leafList);
        assertEquals("uri", leafList.getType().getQName().getLocalName());
        assertEquals("List of NETCONF capabilities supported by the server.", leafList.getDescription());
        assertFalse(leafList.isUserOrdered());
    }
}
