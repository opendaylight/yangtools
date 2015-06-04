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

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * Test ANTLR4 grammar capability to parse description statement in unknown node.
 *
 * Note: Everything under unknown node is unknown node.
 */
public class Bug1412Test {

    @Test
    public void test() throws Exception {

        Set<Module> modules = null;


        modules = TestUtils.loadModules(getClass().getResource("/bugs/bug1412").toURI());


        Module bug1412 = TestUtils.findModule(modules, "bug1412");
        assertNotNull(bug1412);

        ContainerSchemaNode node = (ContainerSchemaNode) bug1412.getDataChildByName("node");
        List<UnknownSchemaNode> unknownNodes = node.getUnknownSchemaNodes();
        assertEquals(1, unknownNodes.size());
        UnknownSchemaNode action = unknownNodes.get(0);

        Date revision = new SimpleDateFormat("yyyy-MM-dd").parse("2014-07-25");
        QNameModule qm = QNameModule.create(URI.create("urn:test:bug1412"), revision);
        QName expectedNodeType = QName.create("urn:test:bug1412:ext:definitions", "2014-07-25", "action");
        assertEquals(expectedNodeType, action.getNodeType());
        assertEquals("hello", action.getNodeParameter());
        QName expectedQName = QName.create(qm, "hello");
        assertEquals(expectedQName, action.getQName());

        unknownNodes = action.getUnknownSchemaNodes();
        assertEquals(2, unknownNodes.size());
        //FIXME: do fix in UnknownEffectiveStatementImpl class & uncomment once nature of nodes within unknown stmts is
        //FIXME: clarified (new statement parser approach)
//        assertEquals(4, unknownNodes.size());
//        UnknownSchemaNode info = null;
//        UnknownSchemaNode description = null;
//        UnknownSchemaNode actionPoint = null;
//        UnknownSchemaNode output = null;
//        for (UnknownSchemaNode un : unknownNodes) {
//            if ("info".equals(un.getNodeType().getLocalName())) {
//                info = un;
//            } else if ("description".equals(un.getNodeType().getLocalName())) {
//                description = un;
//            } else if ("actionpoint".equals(un.getNodeType().getLocalName())) {
//                actionPoint = un;
//            } else if ("output".equals(un.getNodeType().getLocalName())) {
//                output = un;
//            }
//        }
//        assertNotNull(info);
//        assertNotNull(description);
//        assertNotNull(actionPoint);
//        assertNotNull(output);

//        expectedNodeType = QName.create("urn:test:bug1412:ext:definitions", "2014-07-25", "info");
//        assertEquals(expectedNodeType, info.getNodeType());
//        assertEquals("greeting", info.getNodeParameter());
//
//        expectedNodeType = QName.create(qm, "description");
//        assertEquals(expectedNodeType, description.getNodeType());
//        assertEquals("say greeting", description.getNodeParameter());
//
//        expectedNodeType = QName.create("urn:test:bug1412:ext:definitions", "2014-07-25", "actionpoint");
//        assertEquals(expectedNodeType, actionPoint.getNodeType());
//        assertEquals("entry", actionPoint.getNodeParameter());
//
//        expectedNodeType = QName.create(qm, "output");
//        assertEquals(expectedNodeType, output.getNodeType());
//        assertEquals("", output.getNodeParameter());
    }

}
