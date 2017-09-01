/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class UsesAugmentTest {

    private static final QNameModule UG = QNameModule.create(
        URI.create("urn:opendaylight:params:xml:ns:yang:uses-grouping"), QName.parseRevision("2013-07-30"));
    private static final QNameModule GD = QNameModule.create(
        URI.create("urn:opendaylight:params:xml:ns:yang:grouping-definitions"), QName.parseRevision("2013-09-04"));

    private SchemaContext context;

    @Before
    public void init() throws ReactorException, IOException, YangSyntaxErrorException, URISyntaxException {
        context = TestUtils.loadModules(getClass().getResource("/grouping-test").toURI());
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
     *
     * @throws Exception if exception occurs
     */
    @Test
    public void testAugmentInUses() throws Exception {
        final Module testModule = TestUtils.findModule(context, "uses-grouping").get();

        final Deque<QName> path = new ArrayDeque<>();

        // * notification pcreq
        final Set<NotificationDefinition> notifications = testModule.getNotifications();
        assertEquals(1, notifications.size());
        final NotificationDefinition pcreq = notifications.iterator().next();
        assertNotNull(pcreq);
        QName expectedQName = QName.create(UG, "pcreq");
        path.offer(expectedQName);
        SchemaPath expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, pcreq.getPath());
        Collection<DataSchemaNode> childNodes = pcreq.getChildNodes();
        assertEquals(4, childNodes.size());
        // * |-- leaf version
        LeafSchemaNode version = (LeafSchemaNode) pcreq.getDataChildByName(QName.create(testModule.getQNameModule(),
                "version"));
        assertNotNull(version);
        expectedQName = QName.create(UG, "version");
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, version.getPath());
        expectedQName = QName.create(UG, "version");
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(true, pcreq.getQName(), expectedQName);
        assertEquals(expectedPath, version.getType().getPath());
        assertEquals(BaseTypes.uint8Type(), version.getType().getBaseType().getBaseType());
        assertTrue(version.isAddedByUses());
        // * |-- leaf type
        LeafSchemaNode type = (LeafSchemaNode) pcreq.getDataChildByName(QName.create(testModule.getQNameModule(),
                "type"));
        assertNotNull(type);
        expectedQName = QName.create(UG, "type");
        assertTrue(type.isAddedByUses());
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, type.getPath());
        expectedQName = QName.create(GD, "int-ext");
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(true, expectedQName);
        assertEquals(expectedPath, type.getType().getPath());
        final UnionTypeDefinition union = (UnionTypeDefinition) type.getType().getBaseType();
        assertEquals(SchemaPath.create(true, expectedQName, QName.create(expectedQName, "union")), union.getPath());
        assertEquals(2, union.getTypes().size());
        // * |-- list requests
        final ListSchemaNode requests = (ListSchemaNode) pcreq.getDataChildByName(QName.create(
                testModule.getQNameModule(), "requests"));
        assertNotNull(requests);
        expectedQName = QName.create(UG, "requests");
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
        final ContainerSchemaNode rp = (ContainerSchemaNode) requests.getDataChildByName(QName.create(
                testModule.getQNameModule(), "rp"));
        assertNotNull(rp);
        expectedQName = QName.create(UG, "rp");
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, rp.getPath());
        assertFalse(rp.isAddedByUses());
        childNodes = rp.getChildNodes();
        assertEquals(4, childNodes.size());
        // * |-- |-- |-- leaf processing-rule
        LeafSchemaNode processingRule = (LeafSchemaNode) rp.getDataChildByName(QName.create(
                testModule.getQNameModule(), "processing-rule"));
        assertNotNull(processingRule);
        expectedQName = QName.create(UG, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- leaf ignore
        LeafSchemaNode ignore = (LeafSchemaNode) rp.getDataChildByName(QName.create(testModule.getQNameModule(),
                "ignore"));
        assertNotNull(ignore);
        expectedQName = QName.create(UG, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- leaf priority
        final LeafSchemaNode priority = (LeafSchemaNode) rp.getDataChildByName(QName.create(
                testModule.getQNameModule(), "priority"));
        assertNotNull(priority);
        expectedQName = QName.create(UG, "priority");
        assertEquals(expectedQName, priority.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, priority.getPath());
        expectedQName = QName.create(UG, "uint8");
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        // TODO
        // assertEquals(expectedPath, priority.getType().getPath());
        assertEquals(BaseTypes.uint8Type(), priority.getType().getBaseType());
        assertTrue(priority.isAddedByUses());
        // * |-- |-- |-- container box
        ContainerSchemaNode box = (ContainerSchemaNode) rp.getDataChildByName(QName.create(testModule.getQNameModule(),
                "box"));
        assertNotNull(box);
        expectedQName = QName.create(UG, "box");
        assertEquals(expectedQName, box.getQName());
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, box.getPath());
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- |-- container order
        final ContainerSchemaNode order = (ContainerSchemaNode) box.getDataChildByName(QName.create(
                testModule.getQNameModule(), "order"));
        assertNotNull(order);
        expectedQName = QName.create(UG, "order");
        assertEquals(expectedQName, order.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, order.getPath());
        assertTrue(order.isAddedByUses());
        assertTrue(order.isAugmenting());
        assertEquals(2, order.getChildNodes().size());
        // * |-- |-- |-- |-- |-- leaf delete
        final LeafSchemaNode delete = (LeafSchemaNode) order.getDataChildByName(QName.create(
                testModule.getQNameModule(), "delete"));
        assertNotNull(delete);
        expectedQName = QName.create(UG, "delete");
        assertEquals(expectedQName, delete.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, delete.getPath());
        assertEquals(BaseTypes.uint32Type(), delete.getType());
        assertTrue(delete.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf setup
        final LeafSchemaNode setup = (LeafSchemaNode) order.getDataChildByName(QName.create(
                testModule.getQNameModule(), "setup"));
        assertNotNull(setup);
        expectedQName = QName.create(UG, "setup");
        assertEquals(expectedQName, setup.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, setup.getPath());
        assertEquals(BaseTypes.uint32Type(), setup.getType());
        assertTrue(setup.isAddedByUses());
        // * |-- |-- path-key-expansion
        final ContainerSchemaNode pke = (ContainerSchemaNode) requests.getDataChildByName(QName.create(
                testModule.getQNameModule(), "path-key-expansion"));
        assertNotNull(pke);
        expectedQName = QName.create(UG, "path-key-expansion");
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
        final ContainerSchemaNode pathKey = (ContainerSchemaNode) pke.getDataChildByName(QName.create(
                testModule.getQNameModule(), "path-key"));
        assertNotNull(pathKey);
        expectedQName = QName.create(UG, "path-key");
        assertEquals(expectedQName, pathKey.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, pathKey.getPath());
        assertFalse(pathKey.isAddedByUses());
        assertEquals(3, pathKey.getChildNodes().size());
        // * |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) pathKey.getDataChildByName(QName.create(testModule.getQNameModule(),
                "processing-rule"));
        assertNotNull(processingRule);
        expectedQName = QName.create(UG, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) pathKey.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        expectedQName = QName.create(UG, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- list path-keys
        final ListSchemaNode pathKeys = (ListSchemaNode) pathKey.getDataChildByName(QName.create(
                testModule.getQNameModule(), "path-keys"));
        assertNotNull(pathKeys);
        expectedQName = QName.create(UG, "path-keys");
        assertEquals(expectedQName, pathKeys.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, pathKeys.getPath());
        assertTrue(pathKeys.isAddedByUses());
        childNodes = pathKeys.getChildNodes();
        assertEquals(2, childNodes.size());
        // * |-- |-- |-- |-- |-- leaf version
        version = (LeafSchemaNode) pathKeys.getDataChildByName(QName.create(testModule.getQNameModule(), "version"));
        assertNotNull(version);
        expectedQName = QName.create(UG, "version");
        assertEquals(expectedQName, version.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, version.getPath());
        assertTrue(version.getType() instanceof UnsignedIntegerTypeDefinition);
        assertEquals(BaseTypes.uint8Type(), version.getType().getBaseType().getBaseType());
        assertTrue(version.isAddedByUses());
        assertTrue(version.isAugmenting());
        // * |-- |-- |-- |-- |-- leaf type
        type = (LeafSchemaNode) pathKeys.getDataChildByName(QName.create(testModule.getQNameModule(), "type"));
        assertNotNull(type);
        expectedQName = QName.create(UG, "type");
        assertEquals(expectedQName, type.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, type.getPath());
        assertTrue(type.getType() instanceof UnionTypeDefinition);
        assertTrue(type.isAddedByUses());
        assertTrue(type.isAugmenting());
        // * |-- |-- container segment-computation
        final ContainerSchemaNode sc = (ContainerSchemaNode) requests.getDataChildByName(QName.create(
                testModule.getQNameModule(), "segment-computation"));
        assertNotNull(sc);
        expectedQName = QName.create(UG, "segment-computation");
        assertEquals(expectedQName, sc.getQName());
        path.pollLast();
        path.pollLast();
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, sc.getPath());
        assertFalse(sc.isAddedByUses());
        // * |-- |-- |-- container p2p
        final ContainerSchemaNode p2p = (ContainerSchemaNode) sc.getDataChildByName(QName.create(
                testModule.getQNameModule(), "p2p"));
        assertNotNull(p2p);
        expectedQName = QName.create(UG, "p2p");
        assertEquals(expectedQName, p2p.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, p2p.getPath());
        assertFalse(p2p.isAddedByUses());
        // * |-- |-- |-- |-- container endpoints
        final ContainerSchemaNode endpoints = (ContainerSchemaNode) p2p.getDataChildByName(QName.create(
                testModule.getQNameModule(), "endpoints"));
        assertNotNull(endpoints);
        expectedQName = QName.create(UG, "endpoints");
        assertEquals(expectedQName, endpoints.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, endpoints.getPath());
        assertFalse(endpoints.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) endpoints.getDataChildByName(QName.create(testModule.getQNameModule(),
                "processing-rule"));
        assertNotNull(processingRule);
        expectedQName = QName.create(UG, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) endpoints.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        expectedQName = QName.create(UG, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- container box
        box = (ContainerSchemaNode) endpoints.getDataChildByName(QName.create(testModule.getQNameModule(), "box"));
        assertNotNull(box);
        expectedQName = QName.create(UG, "box");
        assertEquals(expectedQName, box.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, box.getPath());
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- |-- |-- choice address-family
        final ChoiceSchemaNode af = (ChoiceSchemaNode) endpoints.getDataChildByName(QName.create(
                testModule.getQNameModule(), "address-family"));
        assertNotNull(af);
        expectedQName = QName.create(UG, "address-family");
        assertEquals(expectedQName, af.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, af.getPath());
        assertTrue(af.isAddedByUses());
        // * |-- |-- |-- |-- container reported-route
        final ContainerSchemaNode reportedRoute = (ContainerSchemaNode) p2p.getDataChildByName(QName.create(
                testModule.getQNameModule(), "reported-route"));
        assertNotNull(reportedRoute);
        expectedQName = QName.create(UG, "reported-route");
        assertEquals(expectedQName, reportedRoute.getQName());
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, reportedRoute.getPath());
        assertFalse(reportedRoute.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) reportedRoute.getDataChildByName(QName.create(testModule.getQNameModule(),
                "processing-rule"));
        assertNotNull(processingRule);
        expectedQName = QName.create(UG, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) reportedRoute.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        expectedQName = QName.create(UG, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- list subobjects
        final ListSchemaNode subobjects = (ListSchemaNode) reportedRoute.getDataChildByName(QName.create(
                testModule.getQNameModule(), "subobjects"));
        assertNotNull(subobjects);
        expectedQName = QName.create(UG, "subobjects");
        assertEquals(expectedQName, subobjects.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, subobjects.getPath());
        assertTrue(subobjects.isAddedByUses());
        // * |-- |-- |-- |-- |-- container bandwidth
        ContainerSchemaNode bandwidth = (ContainerSchemaNode) reportedRoute.getDataChildByName(QName.create(
                testModule.getQNameModule(), "bandwidth"));
        assertNotNull(bandwidth);
        expectedQName = QName.create(UG, "bandwidth");
        assertEquals(expectedQName, bandwidth.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, bandwidth.getPath());
        assertFalse(bandwidth.isAddedByUses());
        // * |-- |-- |-- |-- container bandwidth
        bandwidth = (ContainerSchemaNode) p2p
                .getDataChildByName(QName.create(testModule.getQNameModule(), "bandwidth"));
        assertNotNull(bandwidth);
        expectedQName = QName.create(UG, "bandwidth");
        assertEquals(expectedQName, bandwidth.getQName());
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, bandwidth.getPath());
        assertTrue(bandwidth.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) bandwidth.getDataChildByName(QName.create(testModule.getQNameModule(),
                "processing-rule"));
        assertNotNull(processingRule);
        expectedQName = QName.create(UG, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) bandwidth.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        expectedQName = QName.create(UG, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- container bandwidth
        final ContainerSchemaNode bandwidthInner = (ContainerSchemaNode) bandwidth.getDataChildByName(QName.create(
                testModule.getQNameModule(), "bandwidth"));
        assertNotNull(bandwidthInner);
        expectedQName = QName.create(UG, "bandwidth");
        assertEquals(expectedQName, bandwidth.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, bandwidthInner.getPath());
        assertTrue(bandwidthInner.isAddedByUses());
        // * |-- list svec
        final ListSchemaNode svec = (ListSchemaNode) pcreq.getDataChildByName(QName.create(testModule.getQNameModule(),
                "svec"));
        assertNotNull(svec);
        expectedQName = QName.create(UG, "svec");
        assertEquals(expectedQName, svec.getQName());
        path.pollLast();
        path.pollLast();
        path.pollLast();
        path.pollLast();
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, svec.getPath());
        assertFalse(svec.isAddedByUses());
        // * |-- |-- leaf link-diverse
        final LeafSchemaNode linkDiverse = (LeafSchemaNode) svec.getDataChildByName(QName.create(
                testModule.getQNameModule(), "link-diverse"));
        assertNotNull(linkDiverse);
        expectedQName = QName.create(UG, "link-diverse");
        assertEquals(expectedQName, linkDiverse.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, linkDiverse.getPath());
        assertEquals(BaseTypes.booleanType(), linkDiverse.getType().getBaseType());
        assertTrue(linkDiverse.isAddedByUses());
        // * |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) svec.getDataChildByName(QName.create(testModule.getQNameModule(),
                "processing-rule"));
        assertNotNull(processingRule);
        expectedQName = QName.create(UG, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- leaf ignore
        ignore = (LeafSchemaNode) svec.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        expectedQName = QName.create(UG, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- list metric
        final ListSchemaNode metric = (ListSchemaNode) svec.getDataChildByName(QName.create(
                testModule.getQNameModule(), "metric"));
        assertNotNull(metric);
        expectedQName = QName.create(UG, "metric");
        assertEquals(expectedQName, metric.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, metric.getPath());
        assertFalse(metric.isAddedByUses());
        // * |-- |-- |-- leaf metric-type
        final LeafSchemaNode metricType = (LeafSchemaNode) metric.getDataChildByName(QName.create(
                testModule.getQNameModule(), "metric-type"));
        assertNotNull(metricType);
        expectedQName = QName.create(UG, "metric-type");
        assertEquals(expectedQName, metricType.getQName());
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, metricType.getPath());
        assertEquals(BaseTypes.uint8Type(), metricType.getType());
        assertTrue(metricType.isAddedByUses());
        // * |-- |-- |-- box
        box = (ContainerSchemaNode) metric.getDataChildByName(QName.create(testModule.getQNameModule(), "box"));
        assertNotNull(box);
        expectedQName = QName.create(UG, "box");
        assertEquals(expectedQName, box.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, box.getPath());
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) metric.getDataChildByName(QName.create(testModule.getQNameModule(),
                "processing-rule"));
        assertNotNull(processingRule);
        expectedQName = QName.create(UG, "processing-rule");
        assertEquals(expectedQName, processingRule.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, processingRule.getPath());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) metric.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        expectedQName = QName.create(UG, "ignore");
        assertEquals(expectedQName, ignore.getQName());
        path.pollLast();
        path.offer(expectedQName);
        expectedPath = SchemaPath.create(path, true);
        assertEquals(expectedPath, ignore.getPath());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
    }

    @Test
    public void testTypedefs() throws Exception {
        final Module testModule = TestUtils.findModule(context, "grouping-definitions").get();
        final Set<TypeDefinition<?>> types = testModule.getTypeDefinitions();

        TypeDefinition<?> intExt = null;
        for (final TypeDefinition<?> td : types) {
            if ("int-ext".equals(td.getQName().getLocalName())) {
                intExt = td;
            }
        }
        assertNotNull(intExt);

        SchemaPath expectedPath = SchemaPath.create(true, QName.create(GD, "int-ext"));
        assertEquals(expectedPath, intExt.getPath());

        final UnionTypeDefinition union = (UnionTypeDefinition) intExt.getBaseType();

        TypeDefinition<?> uint8 = null;
        TypeDefinition<?> pv = null;
        for (final TypeDefinition<?> td : union.getTypes()) {
            if ("uint8".equals(td.getQName().getLocalName())) {
                uint8 = td;
            } else if ("protocol-version".equals(td.getQName().getLocalName())) {
                pv = td;
            }
        }
        assertNotNull(uint8);
        assertNotNull(pv);

        expectedPath = SchemaPath.create(true, QName.create(GD, "int-ext"), QName.create(GD, "union"));
        assertEquals(expectedPath, union.getPath());
    }

}
