/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

class UsesAugmentTest extends AbstractYangTest {
    private static final QNameModule UG =
        QNameModule.of("urn:opendaylight:params:xml:ns:yang:uses-grouping", "2013-07-30");
    private static final QNameModule GD =
        QNameModule.of("urn:opendaylight:params:xml:ns:yang:grouping-definitions", "2013-09-04");

    private static EffectiveModelContext CONTEXT;

    @BeforeAll
    static void beforeClass() throws Exception {
        CONTEXT = assertEffectiveModelDir("/grouping-test");
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
     * |-- |-- leaf ignore (U)
     *
     * U = added by uses A = added by augment
     *
     * @throws Exception if exception occurs
     */
    @Test
    void testAugmentInUses() throws Exception {
        final Module testModule = CONTEXT.findModules("uses-grouping").iterator().next();

        // * notification pcreq
        final Collection<? extends NotificationDefinition> notifications = testModule.getNotifications();
        assertEquals(1, notifications.size());
        final NotificationDefinition pcreq = notifications.iterator().next();
        assertNotNull(pcreq);
        assertEquals(QName.create(UG, "pcreq"), pcreq.getQName());
        Collection<? extends DataSchemaNode> childNodes = pcreq.getChildNodes();
        assertEquals(4, childNodes.size());
        // * |-- leaf version
        LeafSchemaNode version = (LeafSchemaNode) pcreq.getDataChildByName(QName.create(testModule.getQNameModule(),
            "version"));
        assertNotNull(version);
        assertEquals(QName.create(UG, "version"), version.getQName());
        assertEquals(QName.create(UG, "version"), version.getType().getQName());
        assertEquals(BaseTypes.uint8Type(), version.getType().getBaseType().getBaseType());
        assertTrue(version.isAddedByUses());
        // * |-- leaf type
        LeafSchemaNode type = (LeafSchemaNode) pcreq.getDataChildByName(QName.create(testModule.getQNameModule(),
            "type"));
        assertNotNull(type);
        assertTrue(type.isAddedByUses());
        assertEquals(QName.create(UG, "type"), type.getQName());
        assertEquals(QName.create(GD, "int-ext"), type.getType().getQName());
        final UnionTypeDefinition union = (UnionTypeDefinition) type.getType().getBaseType();
        assertEquals(QName.create(GD, "union"), union.getQName());
        assertEquals(2, union.getTypes().size());
        // * |-- list requests
        final ListSchemaNode requests = (ListSchemaNode) pcreq.getDataChildByName(QName.create(
            testModule.getQNameModule(), "requests"));
        assertNotNull(requests);
        assertEquals(QName.create(UG, "requests"), requests.getQName());
        assertFalse(requests.isAddedByUses());
        childNodes = requests.getChildNodes();
        assertEquals(3, childNodes.size());
        // * |-- |-- container rp
        final ContainerSchemaNode rp = (ContainerSchemaNode) requests.getDataChildByName(QName.create(
            testModule.getQNameModule(), "rp"));
        assertNotNull(rp);
        assertEquals(QName.create(UG, "rp"), rp.getQName());
        assertFalse(rp.isAddedByUses());
        childNodes = rp.getChildNodes();
        assertEquals(4, childNodes.size());
        // * |-- |-- |-- leaf processing-rule
        LeafSchemaNode processingRule = (LeafSchemaNode) rp.getDataChildByName(QName.create(
            testModule.getQNameModule(), "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(QName.create(UG, "processing-rule"), processingRule.getQName());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- leaf ignore
        LeafSchemaNode ignore = (LeafSchemaNode) rp.getDataChildByName(QName.create(testModule.getQNameModule(),
            "ignore"));
        assertNotNull(ignore);
        assertEquals(QName.create(UG, "ignore"), ignore.getQName());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- leaf priority
        final LeafSchemaNode priority = (LeafSchemaNode) rp.getDataChildByName(QName.create(
            testModule.getQNameModule(), "priority"));
        assertNotNull(priority);
        assertEquals(QName.create(UG, "priority"), priority.getQName());
        // TODO
        // assertEquals(QName.create(UG, "uint8"), priority.getType().getQName());
        assertEquals(BaseTypes.uint8Type(), priority.getType().getBaseType());
        assertTrue(priority.isAddedByUses());
        // * |-- |-- |-- container box
        ContainerSchemaNode box = (ContainerSchemaNode) rp.getDataChildByName(QName.create(testModule.getQNameModule(),
            "box"));
        assertNotNull(box);
        assertEquals(QName.create(UG, "box"), box.getQName());
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- |-- container order
        final ContainerSchemaNode order = (ContainerSchemaNode) box.getDataChildByName(QName.create(
            testModule.getQNameModule(), "order"));
        assertNotNull(order);
        assertEquals(QName.create(UG, "order"), order.getQName());
        assertTrue(order.isAddedByUses());
        assertTrue(order.isAugmenting());
        assertEquals(2, order.getChildNodes().size());
        // * |-- |-- |-- |-- |-- leaf delete
        final LeafSchemaNode delete = (LeafSchemaNode) order.getDataChildByName(QName.create(
            testModule.getQNameModule(), "delete"));
        assertNotNull(delete);
        assertEquals(QName.create(UG, "delete"), delete.getQName());
        assertEquals(BaseTypes.uint32Type(), delete.getType());
        assertTrue(delete.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf setup
        final LeafSchemaNode setup = (LeafSchemaNode) order.getDataChildByName(QName.create(
            testModule.getQNameModule(), "setup"));
        assertNotNull(setup);
        assertEquals(QName.create(UG, "setup"), setup.getQName());
        assertEquals(BaseTypes.uint32Type(), setup.getType());
        assertTrue(setup.isAddedByUses());
        // * |-- |-- path-key-expansion
        final ContainerSchemaNode pke = (ContainerSchemaNode) requests.getDataChildByName(QName.create(
            testModule.getQNameModule(), "path-key-expansion"));
        assertNotNull(pke);
        assertEquals(QName.create(UG, "path-key-expansion"), pke.getQName());
        assertFalse(pke.isAddedByUses());
        // * |-- |-- |-- path-key
        final ContainerSchemaNode pathKey = (ContainerSchemaNode) pke.getDataChildByName(QName.create(
            testModule.getQNameModule(), "path-key"));
        assertNotNull(pathKey);
        assertEquals(QName.create(UG, "path-key"), pathKey.getQName());
        assertFalse(pathKey.isAddedByUses());
        assertEquals(3, pathKey.getChildNodes().size());
        // * |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) pathKey.getDataChildByName(QName.create(testModule.getQNameModule(),
            "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(QName.create(UG, "processing-rule"), processingRule.getQName());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) pathKey.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        assertEquals(QName.create(UG, "ignore"), ignore.getQName());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- list path-keys
        final ListSchemaNode pathKeys = (ListSchemaNode) pathKey.getDataChildByName(QName.create(
            testModule.getQNameModule(), "path-keys"));
        assertNotNull(pathKeys);
        assertEquals(QName.create(UG, "path-keys"), pathKeys.getQName());
        assertTrue(pathKeys.isAddedByUses());
        childNodes = pathKeys.getChildNodes();
        assertEquals(2, childNodes.size());
        // * |-- |-- |-- |-- |-- leaf version
        version = (LeafSchemaNode) pathKeys.getDataChildByName(QName.create(testModule.getQNameModule(), "version"));
        assertNotNull(version);
        assertEquals(QName.create(UG, "version"), version.getQName());
        assertInstanceOf(Uint8TypeDefinition.class, version.getType());
        assertEquals(BaseTypes.uint8Type(), version.getType().getBaseType().getBaseType());
        assertTrue(version.isAddedByUses());
        assertTrue(version.isAugmenting());
        // * |-- |-- |-- |-- |-- leaf type
        type = (LeafSchemaNode) pathKeys.getDataChildByName(QName.create(testModule.getQNameModule(), "type"));
        assertNotNull(type);
        assertEquals(QName.create(UG, "type"), type.getQName());
        assertInstanceOf(UnionTypeDefinition.class, type.getType());
        assertTrue(type.isAddedByUses());
        assertTrue(type.isAugmenting());
        // * |-- |-- container segment-computation
        final ContainerSchemaNode sc = (ContainerSchemaNode) requests.getDataChildByName(QName.create(
            testModule.getQNameModule(), "segment-computation"));
        assertNotNull(sc);
        assertEquals(QName.create(UG, "segment-computation"), sc.getQName());
        assertFalse(sc.isAddedByUses());
        // * |-- |-- |-- container p2p
        final ContainerSchemaNode p2p = (ContainerSchemaNode) sc.getDataChildByName(QName.create(
            testModule.getQNameModule(), "p2p"));
        assertNotNull(p2p);
        assertEquals(QName.create(UG, "p2p"), p2p.getQName());
        assertFalse(p2p.isAddedByUses());
        // * |-- |-- |-- |-- container endpoints
        final ContainerSchemaNode endpoints = (ContainerSchemaNode) p2p.getDataChildByName(QName.create(
            testModule.getQNameModule(), "endpoints"));
        assertNotNull(endpoints);
        assertEquals(QName.create(UG, "endpoints"), endpoints.getQName());
        assertFalse(endpoints.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) endpoints.getDataChildByName(QName.create(testModule.getQNameModule(),
            "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(QName.create(UG, "processing-rule"), processingRule.getQName());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) endpoints.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        assertEquals(QName.create(UG, "ignore"), ignore.getQName());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- container box
        box = (ContainerSchemaNode) endpoints.getDataChildByName(QName.create(testModule.getQNameModule(), "box"));
        assertNotNull(box);
        assertEquals(QName.create(UG, "box"), box.getQName());
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- |-- |-- choice address-family
        final ChoiceSchemaNode af = (ChoiceSchemaNode) endpoints.getDataChildByName(QName.create(
            testModule.getQNameModule(), "address-family"));
        assertNotNull(af);
        assertEquals(QName.create(UG, "address-family"), af.getQName());
        assertTrue(af.isAddedByUses());
        // * |-- |-- |-- |-- container reported-route
        final ContainerSchemaNode reportedRoute = (ContainerSchemaNode) p2p.getDataChildByName(QName.create(
            testModule.getQNameModule(), "reported-route"));
        assertNotNull(reportedRoute);
        assertEquals(QName.create(UG, "reported-route"), reportedRoute.getQName());
        assertFalse(reportedRoute.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) reportedRoute.getDataChildByName(QName.create(testModule.getQNameModule(),
            "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(QName.create(UG, "processing-rule"), processingRule.getQName());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) reportedRoute.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        assertEquals(QName.create(UG, "ignore"), ignore.getQName());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- list subobjects
        final ListSchemaNode subobjects = (ListSchemaNode) reportedRoute.getDataChildByName(QName.create(
            testModule.getQNameModule(), "subobjects"));
        assertNotNull(subobjects);
        assertEquals(QName.create(UG, "subobjects"), subobjects.getQName());
        assertTrue(subobjects.isAddedByUses());
        // * |-- |-- |-- |-- |-- container bandwidth
        ContainerSchemaNode bandwidth = (ContainerSchemaNode) reportedRoute.getDataChildByName(QName.create(
            testModule.getQNameModule(), "bandwidth"));
        assertNotNull(bandwidth);
        assertEquals(QName.create(UG, "bandwidth"), bandwidth.getQName());
        assertFalse(bandwidth.isAddedByUses());
        // * |-- |-- |-- |-- container bandwidth
        bandwidth = (ContainerSchemaNode) p2p
            .getDataChildByName(QName.create(testModule.getQNameModule(), "bandwidth"));
        assertNotNull(bandwidth);
        assertEquals(QName.create(UG, "bandwidth"), bandwidth.getQName());
        assertTrue(bandwidth.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) bandwidth.getDataChildByName(QName.create(testModule.getQNameModule(),
            "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(QName.create(UG, "processing-rule"), processingRule.getQName());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) bandwidth.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        assertEquals(QName.create(UG, "ignore"), ignore.getQName());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- |-- |-- |-- container bandwidth
        final ContainerSchemaNode bandwidthInner = (ContainerSchemaNode) bandwidth.getDataChildByName(QName.create(
            testModule.getQNameModule(), "bandwidth"));
        assertNotNull(bandwidthInner);
        assertEquals(QName.create(UG, "bandwidth"), bandwidth.getQName());
        assertTrue(bandwidthInner.isAddedByUses());
        // * |-- list svec
        final ListSchemaNode svec = (ListSchemaNode) pcreq.getDataChildByName(QName.create(testModule.getQNameModule(),
            "svec"));
        assertNotNull(svec);
        assertEquals(QName.create(UG, "svec"), svec.getQName());
        assertFalse(svec.isAddedByUses());
        // * |-- |-- leaf link-diverse
        final LeafSchemaNode linkDiverse = (LeafSchemaNode) svec.getDataChildByName(QName.create(
            testModule.getQNameModule(), "link-diverse"));
        assertNotNull(linkDiverse);
        assertEquals(QName.create(UG, "link-diverse"), linkDiverse.getQName());
        assertEquals(BaseTypes.booleanType(), linkDiverse.getType().getBaseType());
        assertTrue(linkDiverse.isAddedByUses());
        // * |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) svec.getDataChildByName(QName.create(testModule.getQNameModule(),
            "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(QName.create(UG, "processing-rule"), processingRule.getQName());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- leaf ignore
        ignore = (LeafSchemaNode) svec.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        assertEquals(QName.create(UG, "ignore"), ignore.getQName());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
        // * |-- |-- list metric
        final ListSchemaNode metric = (ListSchemaNode) svec.getDataChildByName(QName.create(
            testModule.getQNameModule(), "metric"));
        assertNotNull(metric);
        assertEquals(QName.create(UG, "metric"), metric.getQName());
        assertFalse(metric.isAddedByUses());
        // * |-- |-- |-- leaf metric-type
        final LeafSchemaNode metricType = (LeafSchemaNode) metric.getDataChildByName(QName.create(
            testModule.getQNameModule(), "metric-type"));
        assertNotNull(metricType);
        assertEquals(QName.create(UG, "metric-type"), metricType.getQName());
        assertEquals(BaseTypes.uint8Type(), metricType.getType());
        assertTrue(metricType.isAddedByUses());
        // * |-- |-- |-- box
        box = (ContainerSchemaNode) metric.getDataChildByName(QName.create(testModule.getQNameModule(), "box"));
        assertNotNull(box);
        assertEquals(QName.create(UG, "box"), box.getQName());
        assertTrue(box.isAddedByUses());
        // * |-- |-- |-- leaf processing-rule
        processingRule = (LeafSchemaNode) metric.getDataChildByName(QName.create(testModule.getQNameModule(),
            "processing-rule"));
        assertNotNull(processingRule);
        assertEquals(QName.create(UG, "processing-rule"), processingRule.getQName());
        assertEquals(BaseTypes.booleanType(), processingRule.getType());
        assertTrue(processingRule.isAddedByUses());
        // * |-- |-- |-- leaf ignore
        ignore = (LeafSchemaNode) metric.getDataChildByName(QName.create(testModule.getQNameModule(), "ignore"));
        assertNotNull(ignore);
        assertEquals(QName.create(UG, "ignore"), ignore.getQName());
        assertEquals(BaseTypes.booleanType(), ignore.getType());
        assertTrue(ignore.isAddedByUses());
    }

    @Test
    void testTypedefs() throws Exception {
        final Module testModule = CONTEXT.findModules("grouping-definitions").iterator().next();
        final Collection<? extends TypeDefinition<?>> types = testModule.getTypeDefinitions();

        TypeDefinition<?> intExt = null;
        for (final TypeDefinition<?> td : types) {
            if ("int-ext".equals(td.getQName().getLocalName())) {
                intExt = td;
            }
        }
        assertNotNull(intExt);

        assertEquals(QName.create(GD, "int-ext"), intExt.getQName());

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
        assertEquals(QName.create(GD, "union"), union.getQName());
    }
}
