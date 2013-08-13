/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class UsesAugmentTest {
    private final URI ns = URI.create("urn:opendaylight:params:xml:ns:yang:uses-grouping");
    private Date rev;
    private final String prefix = "ug";

    private Set<Module> modules;

    @Before
    public void init() throws FileNotFoundException, ParseException {
        rev = TestUtils.simpleDateFormat.parse("2013-07-30");
    }


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
        assertEquals(createPath("pcreq"), pcreq.getPath());
        Set<DataSchemaNode> childNodes = pcreq.getChildNodes();
        assertEquals(3, childNodes.size());
        // * |-- leaf version (U)
        LeafSchemaNode version = (LeafSchemaNode)pcreq.getDataChildByName("version");
        assertNotNull(version);
        assertEquals(createPath("pcreq", "version"), version.getPath());
        assertTrue(version.isAddedByUses());
        // * |-- list requests
        ListSchemaNode requests = (ListSchemaNode)pcreq.getDataChildByName("requests");
        assertNotNull(requests);
        assertEquals(createPath("pcreq", "requests"), requests.getPath());
        assertFalse(requests.isAddedByUses());
        childNodes = requests.getChildNodes();
        assertEquals(3, childNodes.size());
        // * |-- |-- container rp
        ContainerSchemaNode rp = (ContainerSchemaNode)requests.getDataChildByName("rp");
        assertNotNull(rp);
        assertEquals(createPath("pcreq", "requests", "rp"), rp.getPath());
        assertFalse(rp.isAddedByUses());
        childNodes = rp.getChildNodes();
        assertEquals(4, childNodes.size());
        // * |-- |-- |-- leaf priority (U)
        LeafSchemaNode priority = (LeafSchemaNode)rp.getDataChildByName("priority");
        assertNotNull(priority);
        assertEquals(createPath("pcreq", "requests", "rp", "priority"), priority.getPath());
        assertTrue(priority.isAddedByUses());
        // * |-- |-- |-- container box (U)
        ContainerSchemaNode box = (ContainerSchemaNode)rp.getDataChildByName("box");
        assertNotNull(box);
        assertEquals(createPath("pcreq", "requests", "rp", "box"), box.getPath());
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- |-- container order (A)
        ContainerSchemaNode order = (ContainerSchemaNode)box.getDataChildByName("order");
        assertNotNull(order);
        assertEquals(createPath("pcreq", "requests", "rp", "box", "order"), order.getPath());
        assertFalse(order.isAddedByUses());
        assertTrue(order.isAugmenting());
        assertEquals(2, order.getChildNodes().size());
        // * |-- |-- |-- |-- |-- leaf processing-rule (U)
        LeafSchemaNode delete = (LeafSchemaNode)order.getDataChildByName("delete");
        assertNotNull(delete);
        assertEquals(createPath("pcreq", "requests", "rp", "box", "order", "delete"), delete.getPath());
        assertTrue(delete.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore (U)
        LeafSchemaNode setup = (LeafSchemaNode)order.getDataChildByName("setup");
        assertNotNull(setup);
        assertEquals(createPath("pcreq", "requests", "rp", "box", "order", "setup"), setup.getPath());
        assertTrue(setup.isAddedByUses());
        // * |-- |-- |-- leaf processing-rule (U)
        LeafSchemaNode processingRule = (LeafSchemaNode)rp.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        assertEquals(createPath("pcreq", "requests", "rp", "processing-rule"), processingRule.getPath());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- leaf ignore (U)
        LeafSchemaNode ignore = (LeafSchemaNode)rp.getDataChildByName("ignore");
        assertNotNull(ignore);
        assertEquals(createPath("pcreq", "requests", "rp", "ignore"), ignore.getPath());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- path-key-expansion
        ContainerSchemaNode pke = (ContainerSchemaNode)requests.getDataChildByName("path-key-expansion");
        assertNotNull(pke);
        assertEquals(createPath("pcreq", "requests", "path-key-expansion"), pke.getPath());
        assertFalse(pke.isAddedByUses());
        // * |-- |-- |-- path-key
        ContainerSchemaNode pathKey = (ContainerSchemaNode)pke.getDataChildByName("path-key");
        assertNotNull(pathKey);
        assertEquals(createPath("pcreq", "requests", "path-key-expansion", "path-key"), pathKey.getPath());
        assertFalse(pathKey.isAddedByUses());
        assertEquals(3, pathKey.getChildNodes().size());
        // * |-- |-- |-- |-- list path-keys (U)
        ListSchemaNode pathKeys = (ListSchemaNode)pathKey.getDataChildByName("path-keys");
        assertNotNull(pathKeys);
        assertEquals(createPath("pcreq", "requests", "path-key-expansion", "path-key", "path-keys"), pathKeys.getPath());
        assertTrue(pathKeys.isAddedByUses());
        childNodes = pathKeys.getChildNodes();
        assertEquals(1, childNodes.size());
        // * |-- |-- |-- |-- |-- leaf version (U)
        version = (LeafSchemaNode)pathKeys.getDataChildByName("version");
        assertNotNull(version);
        assertEquals(createPath("pcreq", "requests", "path-key-expansion", "path-key", "path-keys", "version"), version.getPath());
        assertTrue(version.isAddedByUses());
        assertFalse(version.isAugmenting());
        // * |-- |-- |-- |-- |-- leaf processing-rule (U)
        processingRule = (LeafSchemaNode)pathKey.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        assertEquals(createPath("pcreq", "requests", "path-key-expansion", "path-key", "processing-rule"), processingRule.getPath());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore (U)
        ignore = (LeafSchemaNode)pathKey.getDataChildByName("ignore");
        assertNotNull(ignore);
        assertEquals(createPath("pcreq", "requests", "path-key-expansion", "path-key", "ignore"), ignore.getPath());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- container segment-computation
        ContainerSchemaNode sc = (ContainerSchemaNode)requests.getDataChildByName("segment-computation");
        assertNotNull(sc);
        assertEquals(createPath("pcreq", "requests", "segment-computation"), sc.getPath());
        assertFalse(sc.isAddedByUses());
        // * |-- |-- |-- container p2p
        ContainerSchemaNode p2p = (ContainerSchemaNode)sc.getDataChildByName("p2p");
        assertNotNull(p2p);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p"), p2p.getPath());
        assertFalse(p2p.isAddedByUses());
        // * |-- |-- |-- |-- container endpoints
        ContainerSchemaNode endpoints = (ContainerSchemaNode)p2p.getDataChildByName("endpoints");
        assertNotNull(endpoints);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "endpoints"), endpoints.getPath());
        assertFalse(endpoints.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule (U)
        processingRule = (LeafSchemaNode)endpoints.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "endpoints", "processing-rule"), processingRule.getPath());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore (U)
        ignore = (LeafSchemaNode)endpoints.getDataChildByName("ignore");
        assertNotNull(ignore);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "endpoints", "ignore"), ignore.getPath());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- container box
        box = (ContainerSchemaNode)endpoints.getDataChildByName("box");
        assertNotNull(box);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "endpoints", "box"), box.getPath());
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- |-- |-- choice address-family (U)
        ChoiceNode af = (ChoiceNode)endpoints.getDataChildByName("address-family");
        assertNotNull(af);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "endpoints", "address-family"), af.getPath());
        assertTrue(af.isAddedByUses());
        // * |-- |-- |-- |-- container reported-route
        ContainerSchemaNode reportedRoute = (ContainerSchemaNode)p2p.getDataChildByName("reported-route");
        assertNotNull(reportedRoute);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "reported-route"), reportedRoute.getPath());
        assertFalse(reportedRoute.isAddedByUses());
        // * |-- |-- |-- |-- |-- container bandwidth
        ContainerSchemaNode bandwidth = (ContainerSchemaNode)reportedRoute.getDataChildByName("bandwidth");
        assertNotNull(bandwidth);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "reported-route", "bandwidth"), bandwidth.getPath());
        assertFalse(bandwidth.isAddedByUses());
        // * |-- |-- |-- |-- |-- list subobjects
        ListSchemaNode subobjects = (ListSchemaNode)reportedRoute.getDataChildByName("subobjects");
        assertNotNull(subobjects);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "reported-route", "subobjects"), subobjects.getPath());
        assertTrue(subobjects.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule (U)
        processingRule = (LeafSchemaNode)reportedRoute.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "reported-route", "processing-rule"), processingRule.getPath());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore (U)
        ignore = (LeafSchemaNode)reportedRoute.getDataChildByName("ignore");
        assertNotNull(ignore);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "reported-route", "ignore"), ignore.getPath());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- container bandwidth (U)
        bandwidth = (ContainerSchemaNode)p2p.getDataChildByName("bandwidth");
        assertNotNull(bandwidth);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "bandwidth"), bandwidth.getPath());
        assertTrue(bandwidth.isAddedByUses());
        // * |-- |-- |-- |-- |-- container bandwidth (U)
        ContainerSchemaNode bandwidthInner = (ContainerSchemaNode)bandwidth.getDataChildByName("bandwidth");
        assertNotNull(bandwidthInner);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "bandwidth", "bandwidth"), bandwidthInner.getPath());
        assertTrue(bandwidthInner.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule (U)
        processingRule = (LeafSchemaNode)bandwidth.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "bandwidth", "processing-rule"), processingRule.getPath());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore (U)
        ignore = (LeafSchemaNode)bandwidth.getDataChildByName("ignore");
        assertNotNull(ignore);
        assertEquals(createPath("pcreq", "requests", "segment-computation", "p2p", "bandwidth", "ignore"), ignore.getPath());
        assertTrue(ignore.isAddedByUses());
        // * |-- list svec
        ListSchemaNode svec = (ListSchemaNode)pcreq.getDataChildByName("svec");
        assertNotNull(svec);
        assertEquals(createPath("pcreq", "svec"), svec.getPath());
        assertFalse(svec.isAddedByUses());
        // * |-- |-- list metric
        ListSchemaNode metric = (ListSchemaNode)svec.getDataChildByName("metric");
        assertNotNull(metric);
        assertEquals(createPath("pcreq", "svec", "metric"), metric.getPath());
        assertFalse(metric.isAddedByUses());
        // * |-- |-- |-- leaf metric-type (U)
        LeafSchemaNode metricType = (LeafSchemaNode)metric.getDataChildByName("metric-type");
        assertNotNull(metricType);
        assertEquals(createPath("pcreq", "svec", "metric", "metric-type"), metricType.getPath());
        assertTrue(metricType.isAddedByUses());
        // * |-- |-- |-- box (U)
        box = (ContainerSchemaNode)metric.getDataChildByName("box");
        assertNotNull(box);
        assertEquals(createPath("pcreq", "svec", "metric", "box"), box.getPath());
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- leaf processing-rule (U)
        processingRule = (LeafSchemaNode)metric.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        assertEquals(createPath("pcreq", "svec", "metric", "processing-rule"), processingRule.getPath());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- leaf ignore (U)
        ignore = (LeafSchemaNode)metric.getDataChildByName("ignore");
        assertNotNull(ignore);
        assertEquals(createPath("pcreq", "svec", "metric", "ignore"), ignore.getPath());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- leaf link-diverse (U)
        LeafSchemaNode linkDiverse = (LeafSchemaNode)svec.getDataChildByName("link-diverse");
        assertNotNull(linkDiverse);
        assertEquals(createPath("pcreq", "svec", "link-diverse"), linkDiverse.getPath());
        assertTrue(linkDiverse.isAddedByUses());
        // * |-- |-- leaf processing-rule (U)
        processingRule = (LeafSchemaNode)svec.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        assertEquals(createPath("pcreq", "svec", "processing-rule"), processingRule.getPath());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- leaf ignore (U)
        ignore = (LeafSchemaNode)svec.getDataChildByName("ignore");
        assertNotNull(ignore);
        assertEquals(createPath("pcreq", "svec", "ignore"), ignore.getPath());
        assertTrue(ignore.isAddedByUses());
    }

    private SchemaPath createPath(String... names) {
        List<QName> path = new ArrayList<>();
        for(String name : names) {
            path.add(new QName(ns, rev, prefix, name));
        }
        return new SchemaPath(path, true);
    }

}
