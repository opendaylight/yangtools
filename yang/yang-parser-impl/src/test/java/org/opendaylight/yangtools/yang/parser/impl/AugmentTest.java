/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.FileNotFoundException;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

public class AugmentTest {
    private static final URI fooNS = URI.create("urn:opendaylight.foo");
    private static final URI barNS = URI.create("urn:opendaylight.bar");
    private static final URI bazNS = URI.create("urn:opendaylight.baz");
    private static Date fooRev;
    private static Date barRev;
    private static Date bazRev;
    private static QName q0;
    private static QName q1;
    private static QName q2;

    private Set<Module> modules;

    @BeforeClass
    public static void init() throws FileNotFoundException, ParseException {
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        fooRev = simpleDateFormat.parse("2013-10-13");
        barRev = simpleDateFormat.parse("2013-10-14");
        bazRev = simpleDateFormat.parse("2013-10-15");

        q0 = QName.create(barNS, barRev, "interfaces");
        q1 = QName.create(barNS, barRev, "ifEntry");
        q2 = QName.create(bazNS, bazRev, "augment-holder");
    }

    @Test
    public void testAugmentParsing() throws Exception {
        modules = TestUtils.loadModules(getClass().getResource("/augment-test/augment-in-augment").toURI());
        SchemaPath expectedSchemaPath;
        List<QName> qnames = new ArrayList<>();
        qnames.add(q0);
        qnames.add(q1);
        qnames.add(q2);

        // foo.yang
        Module module1 = TestUtils.findModule(modules, "foo");
        Set<AugmentationSchema> augmentations = module1.getAugmentations();
        assertEquals(1, augmentations.size());
        AugmentationSchema augment = augmentations.iterator().next();
        assertNotNull(augment);

        expectedSchemaPath = SchemaPath.create(qnames, true);
        assertEquals(expectedSchemaPath, augment.getTargetPath());

        Collection<DataSchemaNode> augmentChildren = augment.getChildNodes();
        assertEquals(4, augmentChildren.size());
        for (DataSchemaNode dsn : augmentChildren) {
            TestUtils.checkIsAugmenting(dsn, false);
        }

        LeafSchemaNode ds0ChannelNumber = (LeafSchemaNode) augment.getDataChildByName("ds0ChannelNumber");
        LeafSchemaNode interfaceId = (LeafSchemaNode) augment.getDataChildByName("interface-id");
        ContainerSchemaNode schemas = (ContainerSchemaNode) augment.getDataChildByName("schemas");
        ChoiceSchemaNode odl = (ChoiceSchemaNode) augment.getDataChildByName("odl");

        assertNotNull(ds0ChannelNumber);
        assertNotNull(interfaceId);
        assertNotNull(schemas);
        assertNotNull(odl);

        // leaf ds0ChannelNumber
        QName qname = QName.create(fooNS, fooRev, "ds0ChannelNumber");
        qnames.add(qname);
        assertEquals(qname, ds0ChannelNumber.getQName());
        expectedSchemaPath = SchemaPath.create(qnames, true);
        assertEquals(expectedSchemaPath, ds0ChannelNumber.getPath());
        assertFalse(ds0ChannelNumber.isAugmenting());
        // type of leaf ds0ChannelNumber
        QName typeQName = BaseTypes.constructQName("string");
        List<QName> typePath = Collections.singletonList(typeQName);
        expectedSchemaPath = SchemaPath.create(typePath, true);
        assertEquals(expectedSchemaPath, ds0ChannelNumber.getType().getPath());

        // leaf interface-id
        qname = QName.create(fooNS, fooRev, "interface-id");
        assertEquals(qname, interfaceId.getQName());
        qnames.set(3, qname);
        expectedSchemaPath = SchemaPath.create(qnames, true);
        assertEquals(expectedSchemaPath, interfaceId.getPath());
        assertFalse(interfaceId.isAugmenting());

        // container schemas
        qname = QName.create(fooNS, fooRev, "schemas");
        assertEquals(qname, schemas.getQName());
        qnames.set(3, qname);
        expectedSchemaPath = SchemaPath.create(qnames, true);
        assertEquals(expectedSchemaPath, schemas.getPath());
        assertFalse(schemas.isAugmenting());

        // choice odl
        qname = QName.create(fooNS, fooRev, "odl");
        assertEquals(qname, odl.getQName());
        qnames.set(3, qname);
        expectedSchemaPath = SchemaPath.create(qnames, true);
        assertEquals(expectedSchemaPath, odl.getPath());
        assertFalse(odl.isAugmenting());

        // baz.yang
        Module module3 = TestUtils.findModule(modules, "baz");
        augmentations = module3.getAugmentations();
        assertEquals(3, augmentations.size());
        AugmentationSchema augment1 = null;
        AugmentationSchema augment2 = null;
        AugmentationSchema augment3 = null;
        for (AugmentationSchema as : augmentations) {
            if (as.getWhenCondition() == null) {
                augment3 = as;
            } else if ("if:ifType='ds0'".equals(as.getWhenCondition().toString())) {
                augment1 = as;
            } else if ("if:ifType='ds2'".equals(as.getWhenCondition().toString())) {
                augment2 = as;
            }
        }
        assertNotNull(augment1);
        assertNotNull(augment2);
        assertNotNull(augment3);

        assertEquals(1, augment1.getChildNodes().size());
        ContainerSchemaNode augmentHolder = (ContainerSchemaNode) augment1.getDataChildByName("augment-holder");
        assertNotNull(augmentHolder);

        assertEquals(1, augment2.getChildNodes().size());
        ContainerSchemaNode augmentHolder2 = (ContainerSchemaNode) augment2.getDataChildByName("augment-holder2");
        assertNotNull(augmentHolder2);

        assertEquals(1, augment3.getChildNodes().size());
        LeafSchemaNode pause = (LeafSchemaNode) augment3.getDataChildByName("pause");
        assertNotNull(pause);
    }

    @Test
    public void testAugmentResolving() throws Exception {
        modules = TestUtils.loadModules(getClass().getResource("/augment-test/augment-in-augment").toURI());
        Module module2 = TestUtils.findModule(modules, "bar");
        ContainerSchemaNode interfaces = (ContainerSchemaNode) module2.getDataChildByName("interfaces");
        ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName("ifEntry");

        SchemaPath expectedPath;
        List<QName> qnames = new ArrayList<>();
        qnames.add(q0);
        qnames.add(q1);
        qnames.add(q2);

        // baz.yang
        // augment "/br:interfaces/br:ifEntry" {
        ContainerSchemaNode augmentHolder = (ContainerSchemaNode) ifEntry.getDataChildByName("augment-holder");
        TestUtils.checkIsAugmenting(augmentHolder, true);
        assertEquals(q2, augmentHolder.getQName());
        expectedPath = SchemaPath.create(qnames, true);
        assertEquals(expectedPath, augmentHolder.getPath());

        // foo.yang
        // augment "/br:interfaces/br:ifEntry/bz:augment-holder"
        LeafSchemaNode ds0ChannelNumber = (LeafSchemaNode) augmentHolder.getDataChildByName("ds0ChannelNumber");
        LeafSchemaNode interfaceId = (LeafSchemaNode) augmentHolder.getDataChildByName("interface-id");
        ContainerSchemaNode schemas = (ContainerSchemaNode) augmentHolder.getDataChildByName("schemas");
        ChoiceSchemaNode odl = (ChoiceSchemaNode) augmentHolder.getDataChildByName("odl");

        assertNotNull(ds0ChannelNumber);
        assertNotNull(interfaceId);
        assertNotNull(schemas);
        assertNotNull(odl);

        // leaf ds0ChannelNumber
        QName qname = QName.create(fooNS, fooRev, "ds0ChannelNumber");
        assertEquals(qname, ds0ChannelNumber.getQName());
        qnames.add(qname);
        expectedPath = SchemaPath.create(qnames, true);
        assertEquals(expectedPath, ds0ChannelNumber.getPath());

        // leaf interface-id
        qname = QName.create(fooNS, fooRev, "interface-id");
        assertEquals(qname, interfaceId.getQName());
        qnames.set(3, qname);
        expectedPath = SchemaPath.create(qnames, true);
        assertEquals(expectedPath, interfaceId.getPath());

        // container schemas
        qname = QName.create(fooNS, fooRev, "schemas");
        assertEquals(qname, schemas.getQName());
        qnames.set(3, qname);
        expectedPath = SchemaPath.create(qnames, true);
        assertEquals(expectedPath, schemas.getPath());

        // choice odl
        qname = QName.create(fooNS, fooRev, "odl");
        assertEquals(qname, odl.getQName());
        qnames.set(3, qname);
        expectedPath = SchemaPath.create(qnames, true);
        assertEquals(expectedPath, odl.getPath());
    }

    @Test
    public void testAugmentedChoice() throws Exception {
        modules = TestUtils.loadModules(getClass().getResource("/augment-test/augment-in-augment").toURI());
        Module module2 = TestUtils.findModule(modules, "bar");
        ContainerSchemaNode interfaces = (ContainerSchemaNode) module2.getDataChildByName("interfaces");
        ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName("ifEntry");
        ContainerSchemaNode augmentedHolder = (ContainerSchemaNode) ifEntry.getDataChildByName("augment-holder");
        TestUtils.checkIsAugmenting(augmentedHolder, true);

        // foo.yang
        // augment "/br:interfaces/br:ifEntry/bz:augment-holder"
        ChoiceSchemaNode odl = (ChoiceSchemaNode) augmentedHolder.getDataChildByName("odl");
        assertNotNull(odl);
        Set<ChoiceCaseNode> cases = odl.getCases();
        assertEquals(4, cases.size());

        ChoiceCaseNode id = null;
        ChoiceCaseNode node1 = null;
        ChoiceCaseNode node2 = null;
        ChoiceCaseNode node3 = null;

        for (ChoiceCaseNode ccn : cases) {
            if ("id".equals(ccn.getQName().getLocalName())) {
                id = ccn;
            } else if ("node1".equals(ccn.getQName().getLocalName())) {
                node1 = ccn;
            } else if ("node2".equals(ccn.getQName().getLocalName())) {
                node2 = ccn;
            } else if ("node3".equals(ccn.getQName().getLocalName())) {
                node3 = ccn;
            }
        }

        assertNotNull(id);
        assertNotNull(node1);
        assertNotNull(node2);
        assertNotNull(node3);

        SchemaPath expectedPath;
        List<QName> qnames = new ArrayList<>();
        qnames.add(q0);
        qnames.add(q1);
        qnames.add(q2);
        qnames.add(QName.create(fooNS, fooRev, "odl"));

        // case id
        QName qname = QName.create(fooNS, fooRev, "id");
        assertEquals(qname, id.getQName());
        qnames.add(qname);
        expectedPath = SchemaPath.create(qnames, true);
        assertEquals(expectedPath, id.getPath());
        Collection<DataSchemaNode> idChildren = id.getChildNodes();
        assertEquals(1, idChildren.size());

        // case node1
        qname = QName.create(fooNS, fooRev, "node1");
        assertEquals(qname, node1.getQName());
        qnames.set(4, qname);
        expectedPath = SchemaPath.create(qnames, true);
        assertEquals(expectedPath, node1.getPath());
        Collection<DataSchemaNode> node1Children = node1.getChildNodes();
        assertTrue(node1Children.isEmpty());

        // case node2
        qname = QName.create(fooNS, fooRev, "node2");
        assertEquals(qname, node2.getQName());
        qnames.set(4, qname);
        expectedPath = SchemaPath.create(qnames, true);
        assertEquals(expectedPath, node2.getPath());
        Collection<DataSchemaNode> node2Children = node2.getChildNodes();
        assertTrue(node2Children.isEmpty());

        // case node3
        qname = QName.create(fooNS, fooRev, "node3");
        assertEquals(qname, node3.getQName());
        qnames.set(4, qname);
        expectedPath = SchemaPath.create(qnames, true);
        assertEquals(expectedPath, node3.getPath());
        Collection<DataSchemaNode> node3Children = node3.getChildNodes();
        assertEquals(1, node3Children.size());

        // test cases
        qnames.clear();
        qnames.add(q0);
        qnames.add(q1);
        qnames.add(q2);
        qnames.add(QName.create(fooNS, fooRev, "odl"));

        // case id child
        qnames.add(QName.create(fooNS, fooRev, "id"));
        qnames.add(QName.create(fooNS, fooRev, "id"));
        LeafSchemaNode caseIdChild = (LeafSchemaNode) idChildren.iterator().next();
        assertNotNull(caseIdChild);
        expectedPath = SchemaPath.create(qnames, true);
        assertEquals(expectedPath, caseIdChild.getPath());

        // case node3 child
        qnames.set(4, QName.create(fooNS, fooRev, "node3"));
        qnames.set(5, QName.create(fooNS, fooRev, "node3"));
        ContainerSchemaNode caseNode3Child = (ContainerSchemaNode) node3Children.iterator().next();
        assertNotNull(caseNode3Child);
        expectedPath = SchemaPath.create(qnames, true);
        assertEquals(expectedPath, caseNode3Child.getPath());
    }

    @Test
    public void testAugmentRpc() throws Exception {
        modules = TestUtils.loadModules(getClass().getResource("/augment-test/rpc").toURI());
        final URI NS_BAR = URI.create("urn:opendaylight:bar");
        final URI NS_FOO = URI.create("urn:opendaylight:foo");
        final Date revision = new SimpleDateFormat("yyyy-MM-dd").parse("2013-10-11");

        Module bar = TestUtils.findModule(modules, "bar");
        Set<RpcDefinition> rpcs = bar.getRpcs();
        assertEquals(2, rpcs.size());

        RpcDefinition submit = null;
        for (RpcDefinition rpc : rpcs) {
            if ("submit".equals(rpc.getQName().getLocalName())) {
                submit = rpc;
                break;
            }
        }
        assertNotNull(submit);

        QName submitQName = QName.create(NS_BAR, revision, "submit");
        assertEquals(submitQName, submit.getQName());
        ContainerSchemaNode input = submit.getInput();
        QName inputQName = QName.create(NS_BAR, revision, "input");
        assertEquals(inputQName, input.getQName());
        ChoiceSchemaNode arguments = (ChoiceSchemaNode) input.getDataChildByName("arguments");
        QName argumentsQName = QName.create(NS_BAR, revision, "arguments");
        assertEquals(argumentsQName, arguments.getQName());
        assertFalse(arguments.isAugmenting());
        Set<ChoiceCaseNode> cases = arguments.getCases();
        assertEquals(3, cases.size());

        ChoiceCaseNode attach = null;
        ChoiceCaseNode create = null;
        ChoiceCaseNode destroy = null;
        for (ChoiceCaseNode child : cases) {
            if ("attach".equals(child.getQName().getLocalName())) {
                attach = child;
            } else if ("create".equals(child.getQName().getLocalName())) {
                create = child;
            } else if ("destroy".equals(child.getQName().getLocalName())) {
                destroy = child;
            }
        }
        assertNotNull(attach);
        assertNotNull(create);
        assertNotNull(destroy);

        assertTrue(attach.isAugmenting());
        assertTrue(create.isAugmenting());
        assertTrue(destroy.isAugmenting());

        SchemaPath expectedPath;
        QName[] qnames = new QName[4];
        qnames[0] = submitQName;
        qnames[1] = inputQName;
        qnames[2] = argumentsQName;

        // case attach
        qnames[3] = QName.create(NS_FOO, revision, "attach");
        assertEquals(qnames[3], attach.getQName());
        expectedPath = SchemaPath.create(Arrays.asList(qnames), true);
        assertEquals(expectedPath, attach.getPath());
        Collection<DataSchemaNode> attachChildren = attach.getChildNodes();
        assertEquals(1, attachChildren.size());

        // case create
        qnames[3] = QName.create(NS_FOO, revision, "create");
        assertEquals(qnames[3], create.getQName());
        expectedPath = SchemaPath.create(Arrays.asList(qnames), true);
        assertEquals(expectedPath, create.getPath());
        Collection<DataSchemaNode> createChildren = create.getChildNodes();
        assertEquals(1, createChildren.size());

        // case attach
        qnames[3] = QName.create(NS_FOO, revision, "destroy");
        assertEquals(qnames[3], destroy.getQName());
        expectedPath = SchemaPath.create(Arrays.asList(qnames), true);
        assertEquals(expectedPath, destroy.getPath());
        Collection<DataSchemaNode> destroyChildren = destroy.getChildNodes();
        assertEquals(1, destroyChildren.size());
    }

    @Test
    public void testAugmentInUsesResolving() throws Exception {
        modules = TestUtils.loadModules(getClass().getResource("/augment-test/augment-in-uses").toURI());
        assertEquals(1, modules.size());

        Module test = modules.iterator().next();
        DataNodeContainer links = (DataNodeContainer) test.getDataChildByName("links");
        DataNodeContainer link = (DataNodeContainer) links.getDataChildByName("link");
        DataNodeContainer nodes = (DataNodeContainer) link.getDataChildByName("nodes");
        ContainerSchemaNode node = (ContainerSchemaNode) nodes.getDataChildByName("node");
        Set<AugmentationSchema> augments = node.getAvailableAugmentations();
        assertEquals(1, augments.size());
        assertEquals(1, node.getChildNodes().size());
        LeafSchemaNode id = (LeafSchemaNode) node.getDataChildByName("id");
        assertTrue(id.isAugmenting());
    }

    @Test
    public void caseShortHandAugmentingTest() throws Exception {
        modules = TestUtils.loadModules(getClass().getResource("/choice-case-type-test-models").toURI());

        Module findModule = TestUtils.findModule(modules, "choice-monitoring");

        ContainerSchemaNode netconf =  (ContainerSchemaNode) findModule.getDataChildByName("netconf-state");
        ContainerSchemaNode datastores =  (ContainerSchemaNode) netconf.getDataChildByName("datastores");
        ListSchemaNode datastore =  (ListSchemaNode) datastores.getDataChildByName("datastore");
        ContainerSchemaNode locks =  (ContainerSchemaNode) datastore.getDataChildByName("locks");
        ChoiceSchemaNode lockType =  (ChoiceSchemaNode)locks.getDataChildByName("lock-type");

        ChoiceCaseNode leafAugCase = lockType.getCaseNodeByName("leaf-aug-case");
        assertTrue(leafAugCase.isAugmenting());
        DataSchemaNode leafAug = leafAugCase.getDataChildByName("leaf-aug-case");
        assertFalse(leafAug.isAugmenting());
    }

}
