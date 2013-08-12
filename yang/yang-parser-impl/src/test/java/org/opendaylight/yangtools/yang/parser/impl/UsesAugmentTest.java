/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;

public class UsesAugmentTest {
    private Set<Module> modules;

    /**
     * Structure of testing model:
     *
     * notification pcreq
     * |-- leaf version (U)
     * |-- list requests
     * |-- |-- container rp
     * |-- |-- |-- leaf priority (U)
     * |-- |-- |-- container box (U)
     * |-- |-- |-- |-- container order (A)
     * |-- |-- |-- |-- |-- leaf delete (U)
     * |-- |-- |-- |-- |-- |-- leaf setup (U)
     * |-- |-- |-- leaf processing-rule (U)
     * |-- |-- |-- leaf ignore (U)
     * |-- |-- path-key-expansion
     * |-- |-- |-- container path-key
     * |-- |-- |-- |-- list path-keys (U)
     * |-- |-- |-- |-- |-- leaf version (U, A)
     * |-- |-- |-- |-- |-- leaf processing-rule (U)
     * |-- |-- |-- |-- |-- leaf ignore (U)
     * |-- |-- container segment-computation
     * |-- |-- |-- container p2p
     * |-- |-- |-- |-- container endpoints
     * |-- |-- |-- |-- |-- leaf processing-rule (U)
     * |-- |-- |-- |-- |-- leaf ignore (U)
     * |-- |-- |-- |-- |-- container box (U)
     * |-- |-- |-- |-- |-- choice address-family (U)
     * |-- |-- |-- |-- |-- |-- case ipv4
     * |-- |-- |-- |-- |-- |-- |-- leaf source-ipv4-address
     * |-- |-- |-- |-- |-- |-- case ipv6
     * |-- |-- |-- |-- |-- |-- |-- leaf source-ipv6-address
     * |-- |-- |-- |-- container reported-route
     * |-- |-- |-- |-- |-- container bandwidth
     * |-- |-- |-- |-- |-- list subobjects(U)
     * |-- |-- |-- |-- |-- leaf processing-rule (U)
     * |-- |-- |-- |-- |-- leaf ignore (U)
     * |-- |-- |-- |-- container bandwidth (U)
     * |-- |-- |-- |-- |-- container bandwidth (U)
     * |-- |-- |-- |-- |-- leaf processing-rule (U)
     * |-- |-- |-- |-- |-- leaf ignore (U)
     * |-- list svec
     * |-- |-- list metric
     * |-- |-- |-- leaf metric-type (U)
     * |-- |-- |-- container box (U)
     * |-- |-- |-- leaf processing-rule (U)
     * |-- |-- |-- leaf ignore (U)
     * |-- |-- leaf link-diverse (U)
     * |-- |-- leaf processing-rule (U)
     * |-- |-- leaf ignore (U)
     *
     * U = added by uses
     * A = added by augment
     */
    @Test
    public void testAugmentInUses() throws Exception {
        modules = TestUtils.loadModules(getClass().getResource("/grouping-test").getPath());
        Module testModule = TestUtils.findModule(modules, "uses-grouping");

        // * notification pcreq
        Set<NotificationDefinition> notifications = testModule.getNotifications();
        assertEquals(1, notifications.size());
        NotificationDefinition pcreq = notifications.iterator().next();
        assertNotNull(pcreq);
        Set<DataSchemaNode> childNodes = pcreq.getChildNodes();
        assertEquals(3, childNodes.size());
        // * |-- leaf version (U)
        LeafSchemaNode version = (LeafSchemaNode)pcreq.getDataChildByName("version");
        assertNotNull(version);
        assertTrue(version.isAddedByUses());
        // * |-- list requests
        ListSchemaNode requests = (ListSchemaNode)pcreq.getDataChildByName("requests");
        assertNotNull(requests);
        assertFalse(requests.isAddedByUses());
        childNodes = requests.getChildNodes();
        assertEquals(3, childNodes.size());
        // * |-- |-- container rp
        ContainerSchemaNode rp = (ContainerSchemaNode)requests.getDataChildByName("rp");
        assertNotNull(rp);
        assertFalse(rp.isAddedByUses());
        childNodes = rp.getChildNodes();
        assertEquals(4, childNodes.size());
        // * |-- |-- |-- leaf priority (U)
        LeafSchemaNode priority = (LeafSchemaNode)rp.getDataChildByName("priority");
        assertNotNull(priority);
        assertTrue(priority.isAddedByUses());
        // * |-- |-- |-- container box (U)
        ContainerSchemaNode box = (ContainerSchemaNode)rp.getDataChildByName("box");
        assertNotNull(box);
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- |-- container order (A)
        ContainerSchemaNode order = (ContainerSchemaNode)box.getDataChildByName("order");
        assertNotNull(order);
        //assertFalse(order.isAddedByUses());
        assertTrue(order.isAugmenting());
        assertEquals(2, order.getChildNodes().size());
        // * |-- |-- |-- |-- |-- leaf processing-rule (U)
        LeafSchemaNode delete = (LeafSchemaNode)order.getDataChildByName("delete");
        assertNotNull(delete);
        assertTrue(delete.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore (U)
        LeafSchemaNode setup = (LeafSchemaNode)order.getDataChildByName("setup");
        assertNotNull(setup);
        assertTrue(setup.isAddedByUses());
        // * |-- |-- |-- leaf processing-rule (U)
        LeafSchemaNode processingRule = (LeafSchemaNode)rp.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- leaf ignore (U)
        LeafSchemaNode ignore = (LeafSchemaNode)rp.getDataChildByName("ignore");
        assertNotNull(ignore);
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- path-key-expansion
        ContainerSchemaNode pke = (ContainerSchemaNode)requests.getDataChildByName("path-key-expansion");
        assertNotNull(pke);
        assertFalse(pke.isAddedByUses());
        // * |-- |-- |-- path-key
        ContainerSchemaNode pathKey = (ContainerSchemaNode)pke.getDataChildByName("path-key");
        assertNotNull(pathKey);
        assertFalse(pathKey.isAddedByUses());
        assertEquals(3, pathKey.getChildNodes().size());
        // * |-- |-- |-- |-- list path-keys (U)
        ListSchemaNode pathKeys = (ListSchemaNode)pathKey.getDataChildByName("path-keys");
        assertNotNull(pathKeys);
        assertTrue(pathKeys.isAddedByUses());
        childNodes = pathKeys.getChildNodes();
        assertEquals(1, childNodes.size());
        // * |-- |-- |-- |-- |-- leaf version (U)
        version = (LeafSchemaNode)pathKeys.getDataChildByName("version");
        assertNotNull(version);
        assertTrue(version.isAddedByUses());
        assertFalse(version.isAugmenting());
        // * |-- |-- |-- |-- |-- leaf processing-rule (U)
        processingRule = (LeafSchemaNode)pathKey.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore (U)
        ignore = (LeafSchemaNode)pathKey.getDataChildByName("ignore");
        assertNotNull(ignore);
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- container segment-computation
        ContainerSchemaNode sc = (ContainerSchemaNode)requests.getDataChildByName("segment-computation");
        assertNotNull(sc);
        assertFalse(sc.isAddedByUses());
        // * |-- |-- |-- container p2p
        ContainerSchemaNode p2p = (ContainerSchemaNode)sc.getDataChildByName("p2p");
        assertNotNull(p2p);
        assertFalse(p2p.isAddedByUses());
        // * |-- |-- |-- |-- container endpoints
        ContainerSchemaNode endpoints = (ContainerSchemaNode)p2p.getDataChildByName("endpoints");
        assertNotNull(endpoints);
        assertFalse(endpoints.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule (U)
        processingRule = (LeafSchemaNode)endpoints.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore (U)
        ignore = (LeafSchemaNode)endpoints.getDataChildByName("ignore");
        assertNotNull(ignore);
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- container box
        box = (ContainerSchemaNode)endpoints.getDataChildByName("box");
        assertNotNull(box);
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- |-- |-- choice address-family (U)
        ChoiceNode af = (ChoiceNode)endpoints.getDataChildByName("address-family");
        assertNotNull(af);
        assertTrue(af.isAddedByUses());
        // * |-- |-- |-- |-- container reported-route
        ContainerSchemaNode reportedRoute = (ContainerSchemaNode)p2p.getDataChildByName("reported-route");
        assertNotNull(reportedRoute);
        assertFalse(reportedRoute.isAddedByUses());
        // * |-- |-- |-- |-- |-- container bandwidth
        ContainerSchemaNode bandwidth = (ContainerSchemaNode)reportedRoute.getDataChildByName("bandwidth");
        assertNotNull(bandwidth);
        assertFalse(bandwidth.isAddedByUses());
        // * |-- |-- |-- |-- |-- list subobjects
        ListSchemaNode subobjects = (ListSchemaNode)reportedRoute.getDataChildByName("subobjects");
        assertNotNull(subobjects);
        assertTrue(subobjects.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule (U)
        processingRule = (LeafSchemaNode)reportedRoute.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore (U)
        ignore = (LeafSchemaNode)reportedRoute.getDataChildByName("ignore");
        assertNotNull(ignore);
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- container bandwidth (U)
        bandwidth = (ContainerSchemaNode)p2p.getDataChildByName("bandwidth");
        assertNotNull(bandwidth);
        assertTrue(bandwidth.isAddedByUses());
        // * |-- |-- |-- |-- |-- container bandwidth (U)
        bandwidth = (ContainerSchemaNode)bandwidth.getDataChildByName("bandwidth");
        assertNotNull(bandwidth);
        assertTrue(bandwidth.isAddedByUses());


        // * |-- list svec
        ListSchemaNode svec = (ListSchemaNode)pcreq.getDataChildByName("svec");
        assertNotNull(svec);
        assertFalse(svec.isAddedByUses());
    }

}
