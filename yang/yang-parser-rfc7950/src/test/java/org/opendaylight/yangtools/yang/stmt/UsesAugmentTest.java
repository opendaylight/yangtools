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
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class UsesAugmentTest {

    private static final QNameModule UG = QNameModule.create(
        URI.create("urn:opendaylight:params:xml:ns:yang:uses-grouping"), Revision.of("2013-07-30"));
    private static final QNameModule GD = QNameModule.create(
        URI.create("urn:opendaylight:params:xml:ns:yang:grouping-definitions"), Revision.of("2013-09-04"));

    private EffectiveModelContext context;

    @Before
    public void init() throws ReactorException, IOException, YangSyntaxErrorException, URISyntaxException {
        context = TestUtils.loadModules(getClass().getResource("/grouping-test").toURI());
    }

    /*
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
     * |-- |-- leaf ignore
     *
     * U = added by uses A = added by augment
     *
     * @throws Exception if exception occurs
     */
    @Test
    public void testAugmentInUses() {
        final Module testModule = TestUtils.findModule(context, "uses-grouping").get();

        // * notification pcreq
        final Collection<? extends NotificationDefinition> notifications = testModule.getNotifications();
        assertEquals(1, notifications.size());
        final NotificationDefinition pcreq = notifications.iterator().next();
        assertNotNull(pcreq);
        final QName pcreqQName = QName.create(UG, "pcreq");
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        final EffectiveStatement<QName, ?> notificationStmt = stack.enterSchemaTree(pcreqQName);
        assertEquals(notificationStmt, pcreq);
        Collection<? extends DataSchemaNode> childNodes = pcreq.getChildNodes();
        assertEquals(4, childNodes.size());
        // * |-- leaf version
        LeafSchemaNode version = (LeafSchemaNode) pcreq.getDataChildByName(QName.create(testModule.getQNameModule(),
                "version"));
        assertNotNull(version);
        final QName versionQName = QName.create(UG, "version");
        final EffectiveStatement<QName, ?> versionStmt = stack.enterSchemaTree(versionQName);
        assertEquals(versionStmt, version);
        stack.clear();
        final EffectiveStatement<QName, ?> versionStmt2 = stack.enterSchemaTree(pcreqQName, versionQName);
        stack.exit();
        assertEquals(((TypedDataSchemaNode) versionStmt2).getType(), version.getType());
        assertEquals(BaseTypes.uint8Type(), version.getType().getBaseType().getBaseType());
        assertTrue(version.isAddedByUses());
        // * |-- leaf type
        LeafSchemaNode type = (LeafSchemaNode) pcreq.getDataChildByName(QName.create(testModule.getQNameModule(),
                "type"));
        assertNotNull(type);
        final QName typeQName = QName.create(UG, "type");
        assertTrue(type.isAddedByUses());
        final EffectiveStatement<QName, ?> typeStmt = stack.enterSchemaTree(typeQName);
        assertEquals(typeStmt, type);
        stack.clear();
        assertEquals(stack.enterTypedef(QName.create(GD, "int-ext")).getTypeDefinition(), type.getType());
        final UnionTypeDefinition union = (UnionTypeDefinition) type.getType().getBaseType();
        assertEquals(((TypedDataSchemaNode) typeStmt).getType().getBaseType(), union);
        assertEquals(2, union.getTypes().size());
        // * |-- list requests
        final ListSchemaNode requests = (ListSchemaNode) pcreq.getDataChildByName(QName.create(
                testModule.getQNameModule(), "requests"));
        assertNotNull(requests);
        final QName requestsQName = QName.create(UG, "requests");
        assertEquals(requestsQName, requests.getQName());
        stack.clear();
        assertEquals(stack.enterSchemaTree(pcreqQName, requestsQName), requests);
        assertFalse(requests.isAddedByUses());
        childNodes = requests.getChildNodes();
        assertEquals(3, childNodes.size());
        // * |-- |-- container rp
        final ContainerSchemaNode rp = (ContainerSchemaNode) requests.getDataChildByName(QName.create(
                testModule.getQNameModule(), "rp"));
        assertNotNull(rp);
        assertEquals(stack.enterSchemaTree(QName.create(UG, "rp")), rp);
        assertFalse(rp.isAddedByUses());
        childNodes = rp.getChildNodes();
        assertEquals(4, childNodes.size());
        // * |-- |-- |-- leaf processing-rule
        LeafSchemaNode processingRule = (LeafSchemaNode) rp.getDataChildByName(QName.create(
                testModule.getQNameModule(), "processing-rule"));
        assertNotNull(processingRule);
        final QName processingRuleQName = QName.create(UG, "processing-rule");
        assertEquals(processingRuleQName, processingRule.getQName());
        assertEquals(stack.enterSchemaTree(processingRuleQName), processingRule);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- leaf ignore
        LeafSchemaNode ignore = (LeafSchemaNode) rp.getDataChildByName(QName.create(testModule.getQNameModule(),
                "ignore"));
        assertNotNull(ignore);
        final QName ignoreQName = QName.create(UG, "ignore");
        assertEquals(ignoreQName, ignore.getQName());
        assertEquals(stack.enterSchemaTree(ignoreQName), ignore);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- leaf priority
        final LeafSchemaNode priority = (LeafSchemaNode) rp.getDataChildByName(QName.create(
                testModule.getQNameModule(), "priority"));
        assertNotNull(priority);
        final QName priorityQName = QName.create(UG, "priority");
        assertEquals(priorityQName, priority.getQName());
        assertEquals(stack.enterSchemaTree(priorityQName), priority);
        final QName uint8QName = QName.create(GD, "uint8");
        assertEquals(uint8QName, stack.currentStatement().streamEffectiveSubstatements(TypeEffectiveStatement.class)
                .findAny().get().getTypeDefinition().getQName());
        stack.exit();
        assertEquals(BaseTypes.uint8Type(), priority.getType().getBaseType());
        assertTrue(priority.isAddedByUses());
        // * |-- |-- |-- container box
        ContainerSchemaNode box = (ContainerSchemaNode) rp.getDataChildByName(QName.create(testModule.getQNameModule(),
                "box"));
        assertNotNull(box);
        final QName boxQName = QName.create(UG, "box");
        assertEquals(boxQName, box.getQName());
        assertEquals(stack.enterSchemaTree(boxQName), box);
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- |-- container order
        final ContainerSchemaNode order = (ContainerSchemaNode) box.getDataChildByName(QName.create(
                testModule.getQNameModule(), "order"));
        assertNotNull(order);
        final QName orderQName = QName.create(UG, "order");
        assertEquals(orderQName, order.getQName());
        assertEquals(stack.enterSchemaTree(orderQName), order);
        assertTrue(order.isAddedByUses());
        assertTrue(order.isAugmenting());
        assertEquals(2, order.getChildNodes().size());
        // * |-- |-- |-- |-- |-- leaf delete
        final LeafSchemaNode delete = (LeafSchemaNode) order.getDataChildByName(QName.create(
                testModule.getQNameModule(), "delete"));
        assertNotNull(delete);
        final QName deleteQName = QName.create(UG, "delete");
        assertEquals(deleteQName, delete.getQName());
        assertEquals(stack.enterSchemaTree(deleteQName), delete);
        stack.exit();
        assertEquals(BaseTypes.uint32Type(), delete.getType());
        assertTrue(delete.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf setup
        final LeafSchemaNode setup = (LeafSchemaNode) order.getDataChildByName(QName.create(
                testModule.getQNameModule(), "setup"));
        assertNotNull(setup);
        final QName setupQName = QName.create(UG, "setup");
        assertEquals(setupQName, setup.getQName());
        assertEquals(stack.enterSchemaTree(setupQName), setup);
        stack.exit(4);
        assertEquals(BaseTypes.uint32Type(), setup.getType());
        assertTrue(setup.isAddedByUses());
        // * |-- |-- path-key-expansion
        final ContainerSchemaNode pke = (ContainerSchemaNode) requests.getDataChildByName(QName.create(
                testModule.getQNameModule(), "path-key-expansion"));
        assertNotNull(pke);
        final QName pathKeyExpansionQName = QName.create(UG, "path-key-expansion");
        assertEquals(pathKeyExpansionQName, pke.getQName());
        assertEquals(stack.enterSchemaTree(pathKeyExpansionQName), pke);
        assertFalse(pke.isAddedByUses());
        // * |-- |-- |-- path-key
        final ContainerSchemaNode pathKey = (ContainerSchemaNode) pke.getDataChildByName(QName.create(
                testModule.getQNameModule(), "path-key"));
        assertNotNull(pathKey);
        final QName pathKeyQName = QName.create(UG, "path-key");
        assertEquals(pathKeyQName, pathKey.getQName());
        assertEquals(stack.enterSchemaTree(pathKeyQName), pathKey);
        assertFalse(pathKey.isAddedByUses());
        assertEquals(3, pathKey.getChildNodes().size());
        // * |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) pathKey.getDataChildByName(QName.create(testModule.getQNameModule(),
                "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(processingRuleQName, processingRule.getQName());
        assertEquals(stack.enterSchemaTree(processingRuleQName), processingRule);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) pathKey.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        assertEquals(ignoreQName, ignore.getQName());
        assertEquals(stack.enterSchemaTree(ignoreQName), ignore);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- list path-keys
        final ListSchemaNode pathKeys = (ListSchemaNode) pathKey.getDataChildByName(QName.create(
                testModule.getQNameModule(), "path-keys"));
        assertNotNull(pathKeys);
        final QName pathKeysQName = QName.create(UG, "path-keys");
        assertEquals(pathKeysQName, pathKeys.getQName());
        assertEquals(stack.enterSchemaTree(pathKeysQName), pathKeys);
        assertTrue(pathKeys.isAddedByUses());
        childNodes = pathKeys.getChildNodes();
        assertEquals(2, childNodes.size());
        // * |-- |-- |-- |-- |-- leaf version
        version = (LeafSchemaNode) pathKeys.getDataChildByName(QName.create(testModule.getQNameModule(), "version"));
        assertNotNull(version);
        assertEquals(versionQName, version.getQName());
        assertEquals(stack.enterSchemaTree(versionQName), version);
        stack.exit();
        assertTrue(version.getType() instanceof Uint8TypeDefinition);
        assertEquals(BaseTypes.uint8Type(), version.getType().getBaseType().getBaseType());
        assertTrue(version.isAddedByUses());
        assertTrue(version.isAugmenting());
        // * |-- |-- |-- |-- |-- leaf type
        type = (LeafSchemaNode) pathKeys.getDataChildByName(QName.create(testModule.getQNameModule(), "type"));
        assertNotNull(type);
        assertEquals(typeQName, type.getQName());
        assertEquals(stack.enterSchemaTree(typeQName), type);
        stack.exit(4);
        assertTrue(type.getType() instanceof UnionTypeDefinition);
        assertTrue(type.isAddedByUses());
        assertTrue(type.isAugmenting());
        // * |-- |-- container segment-computation
        final ContainerSchemaNode sc = (ContainerSchemaNode) requests.getDataChildByName(QName.create(
                testModule.getQNameModule(), "segment-computation"));
        assertNotNull(sc);
        final QName segmentComputationQName = QName.create(UG, "segment-computation");
        assertEquals(segmentComputationQName, sc.getQName());
        assertEquals(stack.enterSchemaTree(segmentComputationQName), sc);
        assertFalse(sc.isAddedByUses());
        // * |-- |-- |-- container p2p
        final ContainerSchemaNode p2p = (ContainerSchemaNode) sc.getDataChildByName(QName.create(
                testModule.getQNameModule(), "p2p"));
        assertNotNull(p2p);
        final QName p2pQName = QName.create(UG, "p2p");
        assertEquals(p2pQName, p2p.getQName());
        assertEquals(stack.enterSchemaTree(p2pQName), p2p);
        assertFalse(p2p.isAddedByUses());
        // * |-- |-- |-- |-- container endpoints
        final ContainerSchemaNode endpoints = (ContainerSchemaNode) p2p.getDataChildByName(QName.create(
                testModule.getQNameModule(), "endpoints"));
        assertNotNull(endpoints);
        final QName endpointsQName = QName.create(UG, "endpoints");
        assertEquals(endpointsQName, endpoints.getQName());
        assertEquals(stack.enterSchemaTree(endpointsQName), endpoints);
        assertFalse(endpoints.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) endpoints.getDataChildByName(QName.create(testModule.getQNameModule(),
                "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(processingRuleQName, processingRule.getQName());
        assertEquals(stack.enterSchemaTree(processingRuleQName), processingRule);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) endpoints.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        assertEquals(ignoreQName, ignore.getQName());
        assertEquals(stack.enterSchemaTree(ignoreQName), ignore);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- container box
        box = (ContainerSchemaNode) endpoints.getDataChildByName(QName.create(testModule.getQNameModule(), "box"));
        assertNotNull(box);
        assertEquals(boxQName, box.getQName());
        assertEquals(stack.enterSchemaTree(boxQName), box);
        stack.exit();
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- |-- |-- choice address-family
        final ChoiceSchemaNode af = (ChoiceSchemaNode) endpoints.getDataChildByName(QName.create(
                testModule.getQNameModule(), "address-family"));
        assertNotNull(af);
        final QName addressFamilyQName = QName.create(UG, "address-family");
        assertEquals(addressFamilyQName, af.getQName());
        assertEquals(stack.enterSchemaTree(addressFamilyQName), af);
        stack.exit(2);
        assertTrue(af.isAddedByUses());
        // * |-- |-- |-- |-- container reported-route
        final ContainerSchemaNode reportedRoute = (ContainerSchemaNode) p2p.getDataChildByName(QName.create(
                testModule.getQNameModule(), "reported-route"));
        assertNotNull(reportedRoute);
        final QName reportedRouteQName = QName.create(UG, "reported-route");
        assertEquals(reportedRouteQName, reportedRoute.getQName());
        assertEquals(stack.enterSchemaTree(reportedRouteQName), reportedRoute);
        assertFalse(reportedRoute.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) reportedRoute.getDataChildByName(QName.create(testModule.getQNameModule(),
                "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(processingRuleQName, processingRule.getQName());
        assertEquals(stack.enterSchemaTree(processingRuleQName), processingRule);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) reportedRoute.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        assertEquals(ignoreQName, ignore.getQName());
        assertEquals(stack.enterSchemaTree(ignoreQName), ignore);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- list subobjects
        final ListSchemaNode subobjects = (ListSchemaNode) reportedRoute.getDataChildByName(QName.create(
                testModule.getQNameModule(), "subobjects"));
        assertNotNull(subobjects);
        final QName subobjectsQName = QName.create(UG, "subobjects");
        assertEquals(subobjectsQName, subobjects.getQName());
        assertEquals(stack.enterSchemaTree(subobjectsQName), subobjects);
        stack.exit();
        assertTrue(subobjects.isAddedByUses());
        // * |-- |-- |-- |-- |-- container bandwidth
        ContainerSchemaNode bandwidth = (ContainerSchemaNode) reportedRoute.getDataChildByName(QName.create(
                testModule.getQNameModule(), "bandwidth"));
        assertNotNull(bandwidth);
        final QName bandwidthQName = QName.create(UG, "bandwidth");
        assertEquals(bandwidthQName, bandwidth.getQName());
        assertEquals(stack.enterSchemaTree(bandwidthQName), bandwidth);
        stack.exit(2);
        assertFalse(bandwidth.isAddedByUses());
        // * |-- |-- |-- |-- container bandwidth
        bandwidth = (ContainerSchemaNode) p2p
                .getDataChildByName(QName.create(testModule.getQNameModule(), "bandwidth"));
        assertNotNull(bandwidth);
        assertEquals(bandwidthQName, bandwidth.getQName());
        assertEquals(stack.enterSchemaTree(bandwidthQName), bandwidth);
        assertTrue(bandwidth.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) bandwidth.getDataChildByName(QName.create(testModule.getQNameModule(),
                "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(processingRuleQName, processingRule.getQName());
        assertEquals(stack.enterSchemaTree(processingRuleQName), processingRule);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) bandwidth.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        assertEquals(ignoreQName, ignore.getQName());
        assertEquals(stack.enterSchemaTree(ignoreQName), ignore);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- container bandwidth
        final ContainerSchemaNode bandwidthInner = (ContainerSchemaNode) bandwidth.getDataChildByName(QName.create(
                testModule.getQNameModule(), "bandwidth"));
        assertNotNull(bandwidthInner);
        assertEquals(bandwidthQName, bandwidth.getQName());
        assertEquals(stack.enterSchemaTree(bandwidthQName), bandwidthInner);
        stack.exit(5);
        assertTrue(bandwidthInner.isAddedByUses());
        // * |-- list svec
        final ListSchemaNode svec = (ListSchemaNode) pcreq.getDataChildByName(QName.create(testModule.getQNameModule(),
                "svec"));
        assertNotNull(svec);
        final QName svecQName = QName.create(UG, "svec");
        assertEquals(svecQName, svec.getQName());
        assertEquals(stack.enterSchemaTree(svecQName), svec);
        assertFalse(svec.isAddedByUses());
        // * |-- |-- leaf link-diverse
        final LeafSchemaNode linkDiverse = (LeafSchemaNode) svec.getDataChildByName(QName.create(
                testModule.getQNameModule(), "link-diverse"));
        assertNotNull(linkDiverse);
        final QName linkDiverseQName = QName.create(UG, "link-diverse");
        assertEquals(linkDiverseQName, linkDiverse.getQName());
        assertEquals(stack.enterSchemaTree(linkDiverseQName), linkDiverse);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), linkDiverse.getType().getBaseType());
        assertTrue(linkDiverse.isAddedByUses());
        // * |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) svec.getDataChildByName(QName.create(testModule.getQNameModule(),
                "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(processingRuleQName, processingRule.getQName());
        assertEquals(stack.enterSchemaTree(processingRuleQName), processingRule);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- leaf ignore
        ignore = (LeafSchemaNode) svec.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        assertEquals(ignoreQName, ignore.getQName());
        assertEquals(stack.enterSchemaTree(ignoreQName), ignore);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- list metric
        final ListSchemaNode metric = (ListSchemaNode) svec.getDataChildByName(QName.create(
                testModule.getQNameModule(), "metric"));
        assertNotNull(metric);
        final QName metricQName = QName.create(UG, "metric");
        assertEquals(metricQName, metric.getQName());
        assertEquals(stack.enterSchemaTree(metricQName), metric);
        assertFalse(metric.isAddedByUses());
        // * |-- |-- |-- leaf metric-type
        final LeafSchemaNode metricType = (LeafSchemaNode) metric.getDataChildByName(QName.create(
                testModule.getQNameModule(), "metric-type"));
        assertNotNull(metricType);
        final QName metricTypeQName = QName.create(UG, "metric-type");
        assertEquals(metricTypeQName, metricType.getQName());
        assertEquals(stack.enterSchemaTree(metricTypeQName), metricType);
        stack.exit();
        assertEquals(BaseTypes.uint8Type(), metricType.getType());
        assertTrue(metricType.isAddedByUses());
        // * |-- |-- |-- box
        box = (ContainerSchemaNode) metric.getDataChildByName(QName.create(testModule.getQNameModule(), "box"));
        assertNotNull(box);
        assertEquals(boxQName, box.getQName());
        assertEquals(stack.enterSchemaTree(boxQName), box);
        stack.exit();
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) metric.getDataChildByName(QName.create(testModule.getQNameModule(),
                "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(processingRuleQName, processingRule.getQName());
        assertEquals(stack.enterSchemaTree(processingRuleQName), processingRule);
        stack.exit();
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) metric.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        assertEquals(ignoreQName, ignore.getQName());
        assertEquals(stack.enterSchemaTree(ignoreQName), ignore);
        stack.clear();
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
    }

    @Test
    public void testTypedefs() {
        final Module testModule = TestUtils.findModule(context, "grouping-definitions").get();
        final Collection<? extends TypeDefinition<?>> types = testModule.getTypeDefinitions();

        TypeDefinition<?> intExt = null;
        for (final TypeDefinition<?> td : types) {
            if ("int-ext".equals(td.getQName().getLocalName())) {
                intExt = td;
            }
        }
        assertNotNull(intExt);

        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        final TypedefEffectiveStatement intExtStmt = stack.enterTypedef(QName.create(GD, "int-ext"));
        stack.clear();
        assertEquals(intExtStmt.getTypeDefinition(), intExt);

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

        assertEquals(intExtStmt.getTypeDefinition().getBaseType(), union);
    }

}
