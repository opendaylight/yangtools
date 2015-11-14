/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class UsesAugmentTest {
    private static final URI UG_NS = URI.create("urn:opendaylight:params:xml:ns:yang:uses-grouping");
    private static final URI GD_NS = URI.create("urn:opendaylight:params:xml:ns:yang:grouping-definitions");
    private Date UG_REV;
    private Date GD_REV;

    private Set<Module> modules;

    @Before
    public void init() throws FileNotFoundException, ParseException {
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        UG_REV = simpleDateFormat.parse("2013-07-30");
        GD_REV = simpleDateFormat.parse("2013-09-04");
    }

    /**
     * Structure of testing model:
     *
     * notification pcreq
     * |-- leaf version (U)
     * |-- leaf type (U)
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
     * |-- |-- |-- |-- |-- leaf version (U)
     * |-- |-- |-- |-- |-- leaf type (U)
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
     * U = added by uses A = added by augment
     */
    @Test
    public void testAugmentInUses() throws Exception {
        modules = TestUtils.loadModules(getClass().getResource("/grouping-test").toURI());
        Module testModule = TestUtils.findModule(modules, "uses-grouping");

        Deque<QName> path = new ArrayDeque<>();

        // * notification pcreq
        Set<NotificationDefinition> notifications = testModule.getNotifications();
        assertEquals(1, notifications.size());
        NotificationDefinition pcreq = notifications.iterator().next();
        assertNotNull(pcreq);
        QName expectedQName = QName.create(UG_NS, UG_REV, "pcreq");
        path.offer(expectedQName);
        SchemaPath expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, pcreq.getPath());
        Collection<DataSchemaNode> childNodes = pcreq.getChildNodes();
        assertEquals(4, childNodes.size());
        // * |-- leaf version
        LeafSchemaNode version = (LeafSchemaNode) pcreq.getDataChildByName("version");
        assertNotNull(version);
        expectedQName = QName.create(UG_NS, UG_REV, "version");
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, version.getPath());
        expectedQName = QName.create(UG_NS, UG_REV, "version");
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(true, pcreq.getQName(), expectedQName);
        assertEquals(expectedPath, version.getType().getPath());
        assertEquals(BaseTypes.uint8Type(), version.getType().getBaseType().getBaseType());
        assertTrue(version.isAddedByUses());
        // * |-- leaf type
        LeafSchemaNode type = (LeafSchemaNode) pcreq.getDataChildByName("type");
        assertNotNull(type);
        expectedQName = QName.create(UG_NS, UG_REV, "type");
        assertTrue(type.isAddedByUses());
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, type.getPath());
        expectedQName = QName.create(GD_NS, GD_REV, "int-ext");
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(true, expectedQName);
        assertEquals(expectedPath, type.getType().getPath());
        UnionTypeDefinition union = (UnionTypeDefinition) type.getType().getBaseType();
        assertEquals(SchemaPath.create(true, expectedQName, QName.create(expectedQName, "union")), union.getPath());
        assertEquals(2, union.getTypes().size());
        // * |-- list requests
        ListSchemaNode requests = (ListSchemaNode) pcreq.getDataChildByName("requests");
        assertNotNull(requests);
        expectedQName = QName.create(UG_NS, UG_REV, "requests");
        assertEquals(expectedQName, requests.getQName());
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, requests.getPath());
        assertFalse(requests.isAddedByUses());
        childNodes = requests.getChildNodes();
        assertEquals(3, childNodes.size());
        // * |-- |-- container rp
        ContainerSchemaNode rp = (ContainerSchemaNode) requests.getDataChildByName("rp");
        assertNotNull(rp);
        expectedQName = QName.create(UG_NS, UG_REV, "rp");
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, rp.getPath());
        assertFalse(rp.isAddedByUses());
        childNodes = rp.getChildNodes();
        assertEquals(4, childNodes.size());
        // * |-- |-- |-- leaf processing-rule
        LeafSchemaNode processingRule = (LeafSchemaNode) rp.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        expectedQName = QName.create(UG_NS, UG_REV, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- leaf ignore
        LeafSchemaNode ignore = (LeafSchemaNode) rp.getDataChildByName("ignore");
        assertNotNull(ignore);
        expectedQName = QName.create(UG_NS, UG_REV, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- leaf priority
        LeafSchemaNode priority = (LeafSchemaNode) rp.getDataChildByName("priority");
        assertNotNull(priority);
        expectedQName = QName.create(UG_NS, UG_REV, "priority");
        assertEquals(expectedQName, priority.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, priority.getPath());
        expectedQName = QName.create(UG_NS, UG_REV, "uint8");
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        // TODO
        //assertEquals(expectedPath, priority.getType().getPath());
        assertEquals(BaseTypes.uint8Type(), priority.getType().getBaseType());
        assertTrue(priority.isAddedByUses());
        // * |-- |-- |-- container box
        ContainerSchemaNode box = (ContainerSchemaNode) rp.getDataChildByName("box");
        assertNotNull(box);
        expectedQName = QName.create(UG_NS, UG_REV, "box");
        assertEquals(expectedQName, box.getQName());
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, box.getPath());
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- |-- container order
        ContainerSchemaNode order = (ContainerSchemaNode) box.getDataChildByName("order");
        assertNotNull(order);
        expectedQName = QName.create(UG_NS, UG_REV, "order");
        assertEquals(expectedQName, order.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, order.getPath());
        assertTrue(order.isAddedByUses());
        assertTrue(order.isAugmenting());
        assertEquals(2, order.getChildNodes().size());
        // * |-- |-- |-- |-- |-- leaf delete
        LeafSchemaNode delete = (LeafSchemaNode) order.getDataChildByName("delete");
        assertNotNull(delete);
        expectedQName = QName.create(UG_NS, UG_REV, "delete");
        assertEquals(expectedQName, delete.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, delete.getPath());
        assertEquals(BaseTypes.uint32Type(), delete.getType());
        assertTrue(delete.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf setup
        LeafSchemaNode setup = (LeafSchemaNode) order.getDataChildByName("setup");
        assertNotNull(setup);
        expectedQName = QName.create(UG_NS, UG_REV, "setup");
        assertEquals(expectedQName, setup.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, setup.getPath());
        assertEquals(BaseTypes.uint32Type(), setup.getType());
        assertTrue(setup.isAddedByUses());
        // * |-- |-- path-key-expansion
        ContainerSchemaNode pke = (ContainerSchemaNode) requests.getDataChildByName("path-key-expansion");
        assertNotNull(pke);
        expectedQName = QName.create(UG_NS, UG_REV, "path-key-expansion");
        assertEquals(expectedQName, pke.getQName());
        path.pollLast();
        path.pollLast();
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, pke.getPath());
        assertFalse(pke.isAddedByUses());
        // * |-- |-- |-- path-key
        ContainerSchemaNode pathKey = (ContainerSchemaNode) pke.getDataChildByName("path-key");
        assertNotNull(pathKey);
        expectedQName = QName.create(UG_NS, UG_REV, "path-key");
        assertEquals(expectedQName, pathKey.getQName());
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, pathKey.getPath());
        assertFalse(pathKey.isAddedByUses());
        assertEquals(3, pathKey.getChildNodes().size());
        // * |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) pathKey.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        expectedQName = QName.create(UG_NS, UG_REV, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) pathKey.getDataChildByName("ignore");
        assertNotNull(ignore);
        expectedQName = QName.create(UG_NS, UG_REV, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- list path-keys
        ListSchemaNode pathKeys = (ListSchemaNode) pathKey.getDataChildByName("path-keys");
        assertNotNull(pathKeys);
        expectedQName = QName.create(UG_NS, UG_REV, "path-keys");
        assertEquals(expectedQName, pathKeys.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, pathKeys.getPath());
        assertTrue(pathKeys.isAddedByUses());
        childNodes = pathKeys.getChildNodes();
        assertEquals(2, childNodes.size());
        // * |-- |-- |-- |-- |-- leaf version
        version = (LeafSchemaNode) pathKeys.getDataChildByName("version");
        assertNotNull(version);
        expectedQName = QName.create(UG_NS, UG_REV, "version");
        assertEquals(expectedQName, version.getQName());
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, version.getPath());
        assertTrue(version.getType() instanceof UnsignedIntegerTypeDefinition);
        assertEquals(BaseTypes.uint8Type(), version.getType().getBaseType().getBaseType());
        assertTrue(version.isAddedByUses());
        assertTrue(version.isAugmenting());
        // * |-- |-- |-- |-- |-- leaf type
        type = (LeafSchemaNode) pathKeys.getDataChildByName("type");
        assertNotNull(type);
        expectedQName = QName.create(UG_NS, UG_REV, "type");
        assertEquals(expectedQName, type.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, type.getPath());
        assertTrue(type.getType() instanceof UnionTypeDefinition);
        assertTrue(type.isAddedByUses());
        assertTrue(type.isAugmenting());
        // * |-- |-- container segment-computation
        ContainerSchemaNode sc = (ContainerSchemaNode) requests.getDataChildByName("segment-computation");
        assertNotNull(sc);
        expectedQName = QName.create(UG_NS, UG_REV, "segment-computation");
        assertEquals(expectedQName, sc.getQName());
        path.pollLast();
        path.pollLast();
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, sc.getPath());
        assertFalse(sc.isAddedByUses());
        // * |-- |-- |-- container p2p
        ContainerSchemaNode p2p = (ContainerSchemaNode) sc.getDataChildByName("p2p");
        assertNotNull(p2p);
        expectedQName = QName.create(UG_NS, UG_REV, "p2p");
        assertEquals(expectedQName, p2p.getQName());
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, p2p.getPath());
        assertFalse(p2p.isAddedByUses());
        // * |-- |-- |-- |-- container endpoints
        ContainerSchemaNode endpoints = (ContainerSchemaNode) p2p.getDataChildByName("endpoints");
        assertNotNull(endpoints);
        expectedQName = QName.create(UG_NS, UG_REV, "endpoints");
        assertEquals(expectedQName, endpoints.getQName());
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, endpoints.getPath());
        assertFalse(endpoints.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) endpoints.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        expectedQName = QName.create(UG_NS, UG_REV, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) endpoints.getDataChildByName("ignore");
        assertNotNull(ignore);
        expectedQName = QName.create(UG_NS, UG_REV, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- container box
        box = (ContainerSchemaNode) endpoints.getDataChildByName("box");
        assertNotNull(box);
        expectedQName = QName.create(UG_NS, UG_REV, "box");
        assertEquals(expectedQName, box.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, box.getPath());
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- |-- |-- choice address-family
        ChoiceSchemaNode af = (ChoiceSchemaNode) endpoints.getDataChildByName("address-family");
        assertNotNull(af);
        expectedQName = QName.create(UG_NS, UG_REV, "address-family");
        assertEquals(expectedQName, af.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, af.getPath());
        assertTrue(af.isAddedByUses());
        // * |-- |-- |-- |-- container reported-route
        ContainerSchemaNode reportedRoute = (ContainerSchemaNode) p2p.getDataChildByName("reported-route");
        assertNotNull(reportedRoute);
        expectedQName = QName.create(UG_NS, UG_REV, "reported-route");
        assertEquals(expectedQName, reportedRoute.getQName());
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, reportedRoute.getPath());
        assertFalse(reportedRoute.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) reportedRoute.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        expectedQName = QName.create(UG_NS, UG_REV, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) reportedRoute.getDataChildByName("ignore");
        assertNotNull(ignore);
        expectedQName = QName.create(UG_NS, UG_REV, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- list subobjects
        ListSchemaNode subobjects = (ListSchemaNode) reportedRoute.getDataChildByName("subobjects");
        assertNotNull(subobjects);
        expectedQName = QName.create(UG_NS, UG_REV, "subobjects");
        assertEquals(expectedQName, subobjects.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, subobjects.getPath());
        assertTrue(subobjects.isAddedByUses());
        // * |-- |-- |-- |-- |-- container bandwidth
        ContainerSchemaNode bandwidth = (ContainerSchemaNode) reportedRoute.getDataChildByName("bandwidth");
        assertNotNull(bandwidth);
        expectedQName = QName.create(UG_NS, UG_REV, "bandwidth");
        assertEquals(expectedQName, bandwidth.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, bandwidth.getPath());
        assertFalse(bandwidth.isAddedByUses());
        // * |-- |-- |-- |-- container bandwidth
        bandwidth = (ContainerSchemaNode) p2p.getDataChildByName("bandwidth");
        assertNotNull(bandwidth);
        expectedQName = QName.create(UG_NS, UG_REV, "bandwidth");
        assertEquals(expectedQName, bandwidth.getQName());
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, bandwidth.getPath());
        assertTrue(bandwidth.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) bandwidth.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        expectedQName = QName.create(UG_NS, UG_REV, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) bandwidth.getDataChildByName("ignore");
        assertNotNull(ignore);
        expectedQName = QName.create(UG_NS, UG_REV, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- container bandwidth
        ContainerSchemaNode bandwidthInner = (ContainerSchemaNode) bandwidth.getDataChildByName("bandwidth");
        assertNotNull(bandwidthInner);
        expectedQName = QName.create(UG_NS, UG_REV, "bandwidth");
        assertEquals(expectedQName, bandwidth.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, bandwidthInner.getPath());
        assertTrue(bandwidthInner.isAddedByUses());
        // * |-- list svec
        ListSchemaNode svec = (ListSchemaNode) pcreq.getDataChildByName("svec");
        assertNotNull(svec);
        expectedQName = QName.create(UG_NS, UG_REV, "svec");
        assertEquals(expectedQName, svec.getQName());
        path.pollLast();
        path.pollLast();
        path.pollLast();
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, svec.getPath());
        assertFalse(svec.isAddedByUses());
        // * |-- |-- leaf link-diverse
        LeafSchemaNode linkDiverse = (LeafSchemaNode) svec.getDataChildByName("link-diverse");
        assertNotNull(linkDiverse);
        expectedQName = QName.create(UG_NS, UG_REV, "link-diverse");
        assertEquals(expectedQName, linkDiverse.getQName());
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, linkDiverse.getPath());
        assertTrue(linkDiverse.getType() instanceof BooleanTypeDefinition);
        assertEquals(BaseTypes.booleanType(), linkDiverse.getType().getBaseType());
        assertTrue(linkDiverse.isAddedByUses());
        // * |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) svec.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        expectedQName = QName.create(UG_NS, UG_REV, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- leaf ignore
        ignore = (LeafSchemaNode) svec.getDataChildByName("ignore");
        assertNotNull(ignore);
        expectedQName = QName.create(UG_NS, UG_REV, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- list metric
        ListSchemaNode metric = (ListSchemaNode) svec.getDataChildByName("metric");
        assertNotNull(metric);
        expectedQName = QName.create(UG_NS, UG_REV, "metric");
        assertEquals(expectedQName, metric.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, metric.getPath());
        assertFalse(metric.isAddedByUses());
        // * |-- |-- |-- leaf metric-type
        LeafSchemaNode metricType = (LeafSchemaNode) metric.getDataChildByName("metric-type");
        assertNotNull(metricType);
        expectedQName = QName.create(UG_NS, UG_REV, "metric-type");
        assertEquals(expectedQName, metricType.getQName());
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, metricType.getPath());
        assertEquals(BaseTypes.uint8Type(), metricType.getType());
        assertTrue(metricType.isAddedByUses());
        // * |-- |-- |-- box
        box = (ContainerSchemaNode) metric.getDataChildByName("box");
        assertNotNull(box);
        expectedQName = QName.create(UG_NS, UG_REV, "box");
        assertEquals(expectedQName, box.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, box.getPath());
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) metric.getDataChildByName("processing-rule");
        assertNotNull(processingRule);
        expectedQName = QName.create(UG_NS, UG_REV, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) metric.getDataChildByName("ignore");
        assertNotNull(ignore);
        expectedQName = QName.create(UG_NS, UG_REV, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath= SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
    }

    @Test
    public void testTypedefs() throws URISyntaxException, SourceException, ReactorException {
        modules = TestUtils.loadModules(getClass().getResource("/grouping-test").toURI());
        Module testModule = TestUtils.findModule(modules, "grouping-definitions");
        Set<TypeDefinition<?>> types = testModule.getTypeDefinitions();

        TypeDefinition<?> intExt = null;
        for(TypeDefinition<?> td : types) {
            if("int-ext".equals(td.getQName().getLocalName())) {
                intExt = td;
            }
        }
        assertNotNull(intExt);

        SchemaPath expectedPath = SchemaPath.create(true, QName.create(GD_NS, GD_REV, "int-ext"));
        assertEquals(expectedPath, intExt.getPath());

        UnionTypeDefinition union = (UnionTypeDefinition)intExt.getBaseType();

        TypeDefinition<?> uint8 = null;
        TypeDefinition<?> pv = null;
        for(TypeDefinition<?> td : union.getTypes()) {
            if("uint8".equals(td.getQName().getLocalName())) {
                uint8 = td;
            } else if("protocol-version".equals(td.getQName().getLocalName())) {
                pv = td;
            }
        }
        assertNotNull(uint8);
        assertNotNull(pv);

        expectedPath = SchemaPath.create(true, QName.create(GD_NS, GD_REV, "int-ext"), QName.create(GD_NS, GD_REV, "union"));
        assertEquals(expectedPath, union.getPath());
    }

}
