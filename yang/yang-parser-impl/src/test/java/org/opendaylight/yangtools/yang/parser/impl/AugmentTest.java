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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

import com.google.common.collect.Lists;

public class AugmentTest {

    private final URI types1NS = URI.create("urn:simple.nodes.test");
    private final URI types2NS = URI.create("urn:simple.types.test");
    private final URI types3NS = URI.create("urn:custom.nodes.test");
    private Date types1Rev;
    private Date types2Rev;
    private Date types3Rev;
    private final String t1 = "n";
    private final String t2 = "t";
    private final String t3 = "c";
    private QName q0;
    private QName q1;
    private QName q2;

    private final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Set<Module> modules;

    @Before
    public void init() throws FileNotFoundException, ParseException {
        types1Rev = simpleDateFormat.parse("2013-02-27");
        types2Rev = simpleDateFormat.parse("2013-07-03");
        types3Rev = simpleDateFormat.parse("2013-02-27");

        q0 = new QName(types2NS, types2Rev, t2, "interfaces");
        q1 = new QName(types2NS, types2Rev, t2, "ifEntry");
        q2 = new QName(types3NS, types3Rev, t3, "augment-holder");

        modules = TestUtils.loadModules(getClass().getResource("/model").getPath());
        assertEquals(3, modules.size());
    }

    @Test
    public void testAugmentParsing() {
        SchemaPath expectedPath = null;
        QName[] qnames = null;

        // testfile1
        Module module1 = TestUtils.findModule(modules, "nodes");
        Set<AugmentationSchema> augmentations = module1.getAugmentations();
        assertEquals(1, augmentations.size());
        AugmentationSchema augment = augmentations.iterator().next();

        Set<DataSchemaNode> augmentChildren = augment.getChildNodes();
        assertEquals(5, augmentChildren.size());
        for (DataSchemaNode dsn : augmentChildren) {
            TestUtils.checkIsAugmenting(dsn, false);
        }

        LeafSchemaNode ds0ChannelNumber = (LeafSchemaNode) augment.getDataChildByName("ds0ChannelNumber");
        LeafSchemaNode interfaceId = (LeafSchemaNode) augment.getDataChildByName("interface-id");
        LeafSchemaNode myType = (LeafSchemaNode) augment.getDataChildByName("my-type");
        ContainerSchemaNode schemas = (ContainerSchemaNode) augment.getDataChildByName("schemas");
        ChoiceNode odl = (ChoiceNode) augment.getDataChildByName("odl");

        assertNotNull(ds0ChannelNumber);
        assertNotNull(interfaceId);
        assertNotNull(myType);
        assertNotNull(schemas);
        assertNotNull(odl);

        qnames = new QName[4];
        qnames[0] = q0;
        qnames[1] = q1;
        qnames[2] = q2;

        // leaf ds0ChannelNumber
        qnames[3] = new QName(types1NS, types1Rev, t1, "ds0ChannelNumber");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, ds0ChannelNumber.getPath());
        // type of leaf ds0ChannelNumber
        QName typeQName = BaseTypes.constructQName("string");
        List<QName> typePath = Collections.singletonList(typeQName);
        expectedPath = new SchemaPath(typePath, true);
        assertEquals(expectedPath, ds0ChannelNumber.getType().getPath());

        // leaf interface-id
        qnames[3] = new QName(types1NS, types1Rev, t1, "interface-id");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, interfaceId.getPath());

        // leaf my-type
        qnames[3] = new QName(types1NS, types1Rev, t1, "my-type");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, myType.getPath());

        // container schemas
        qnames[3] = new QName(types1NS, types1Rev, t1, "schemas");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, schemas.getPath());

        // choice odl
        qnames[3] = new QName(types1NS, types1Rev, t1, "odl");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, odl.getPath());

        // testfile3
        Module module3 = TestUtils.findModule(modules, "custom");
        augmentations = module3.getAugmentations();
        assertEquals(2, augmentations.size());
        AugmentationSchema augment1 = null;
        AugmentationSchema augment2 = null;
        for (AugmentationSchema as : augmentations) {
            if ("if:ifType='ds0'".equals(as.getWhenCondition().toString())) {
                augment1 = as;
            } else if ("if:ifType='ds2'".equals(as.getWhenCondition().toString())) {
                augment2 = as;
            }
        }
        assertNotNull(augment1);
        assertNotNull(augment2);

        assertEquals(1, augment1.getChildNodes().size());
        ContainerSchemaNode augmentHolder = (ContainerSchemaNode) augment1.getDataChildByName("augment-holder");
        assertNotNull(augmentHolder);

        assertEquals(1, augment2.getChildNodes().size());
        ContainerSchemaNode augmentHolder2 = (ContainerSchemaNode) augment2.getDataChildByName("augment-holder2");
        assertNotNull(augmentHolder2);
    }

    @Test
    public void testAugmentResolving() throws ParseException {
        SchemaPath expectedPath = null;
        QName[] qnames = null;

        Module module2 = TestUtils.findModule(modules, "types");
        ContainerSchemaNode interfaces = (ContainerSchemaNode) module2.getDataChildByName("interfaces");
        ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName("ifEntry");
        ContainerSchemaNode augmentedContainer = (ContainerSchemaNode) ifEntry.getDataChildByName("augment-holder");
        TestUtils.checkIsAugmenting(augmentedContainer, true);

        // testfile1.yang
        // augment "/data:interfaces/data:ifEntry/t3:augment-holder"
        LeafSchemaNode ds0ChannelNumber = (LeafSchemaNode) augmentedContainer.getDataChildByName("ds0ChannelNumber");
        LeafSchemaNode interfaceId = (LeafSchemaNode) augmentedContainer.getDataChildByName("interface-id");
        LeafSchemaNode myType = (LeafSchemaNode) augmentedContainer.getDataChildByName("my-type");
        ContainerSchemaNode schemas = (ContainerSchemaNode) augmentedContainer.getDataChildByName("schemas");
        ChoiceNode odl = (ChoiceNode) augmentedContainer.getDataChildByName("odl");

        assertNotNull(ds0ChannelNumber);
        assertNotNull(interfaceId);
        assertNotNull(myType);
        assertNotNull(schemas);
        assertNotNull(odl);

        qnames = new QName[4];
        qnames[0] = q0;
        qnames[1] = q1;
        qnames[2] = q2;

        // leaf ds0ChannelNumber
        qnames[3] = new QName(types1NS, types1Rev, t1, "ds0ChannelNumber");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, ds0ChannelNumber.getPath());

        // leaf interface-id
        qnames[3] = new QName(types1NS, types1Rev, t1, "interface-id");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, interfaceId.getPath());

        // leaf my-type
        qnames[3] = new QName(types1NS, types1Rev, t1, "my-type");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, myType.getPath());

        // container schemas
        qnames[3] = new QName(types1NS, types1Rev, t1, "schemas");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, schemas.getPath());

        // choice odl
        qnames[3] = new QName(types1NS, types1Rev, t1, "odl");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, odl.getPath());
    }

    @Test
    public void testAugmentChoice() throws ParseException {
        SchemaPath expectedPath = null;
        QName[] qnames = null;

        Module module2 = TestUtils.findModule(modules, "types");
        ContainerSchemaNode interfaces = (ContainerSchemaNode) module2.getDataChildByName("interfaces");
        ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName("ifEntry");
        ContainerSchemaNode augmentedContainer = (ContainerSchemaNode) ifEntry.getDataChildByName("augment-holder");
        TestUtils.checkIsAugmenting(augmentedContainer, true);

        // testfile1.yang
        // augment "/data:interfaces/data:ifEntry/t3:augment-holder"
        ChoiceNode odl = (ChoiceNode) augmentedContainer.getDataChildByName("odl");
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

        qnames = new QName[5];
        qnames[0] = q0;
        qnames[1] = q1;
        qnames[2] = q2;
        qnames[3] = new QName(types1NS, types1Rev, t1, "odl");

        // case id
        qnames[4] = new QName(types1NS, types1Rev, t1, "id");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, id.getPath());
        Set<DataSchemaNode> idChildren = id.getChildNodes();
        assertEquals(1, idChildren.size());

        // case node1
        qnames[4] = new QName(types1NS, types1Rev, t1, "node1");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, node1.getPath());
        Set<DataSchemaNode> node1Children = node1.getChildNodes();
        assertTrue(node1Children.isEmpty());

        // case node2
        qnames[4] = new QName(types1NS, types1Rev, t1, "node2");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, node2.getPath());
        Set<DataSchemaNode> node2Children = node2.getChildNodes();
        assertTrue(node2Children.isEmpty());

        // case node3
        qnames[4] = new QName(types1NS, types1Rev, t1, "node3");
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, node3.getPath());
        Set<DataSchemaNode> node3Children = node3.getChildNodes();
        assertEquals(1, node3Children.size());

        // test cases
        qnames = new QName[6];
        qnames[0] = q0;
        qnames[1] = q1;
        qnames[2] = q2;
        qnames[3] = new QName(types1NS, types1Rev, t1, "odl");

        // case id child
        qnames[4] = new QName(types1NS, types1Rev, t1, "id");
        qnames[5] = new QName(types1NS, types1Rev, t1, "id");
        LeafSchemaNode caseIdChild = (LeafSchemaNode) idChildren.iterator().next();
        assertNotNull(caseIdChild);
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, caseIdChild.getPath());

        // case node3 child
        qnames[4] = new QName(types1NS, types1Rev, t1, "node3");
        qnames[5] = new QName(types1NS, types1Rev, t1, "node3");
        ContainerSchemaNode caseNode3Child = (ContainerSchemaNode) node3Children.iterator().next();
        assertNotNull(caseNode3Child);
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, caseNode3Child.getPath());
    }

    @Test
    public void testAugmentRpc() throws Exception {
        modules = TestUtils.loadModules(getClass().getResource("/augment-test/rpc").getPath());
        final URI NS_BAR = URI.create("urn:opendaylight:bar");
        final URI NS_FOO = URI.create("urn:opendaylight:foo");
        final Date revision = simpleDateFormat.parse("2013-10-11");

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

        QName submitQName = new QName(NS_BAR, revision, "b", "submit");
        assertEquals(submitQName, submit.getQName());
        ContainerSchemaNode input = submit.getInput();
        QName inputQName = new QName(NS_BAR, revision, "b", "input");
        assertEquals(inputQName, input.getQName());
        ChoiceNode arguments = (ChoiceNode) input.getDataChildByName("arguments");
        QName argumentsQName = new QName(NS_BAR, revision, "b", "arguments");
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

        SchemaPath expectedPath = null;
        QName[] qnames = new QName[4];
        qnames[0] = submitQName;
        qnames[1] = inputQName;
        qnames[2] = argumentsQName;

        // case attach
        qnames[3] = new QName(NS_FOO, revision, "f", "attach");
        assertEquals(qnames[3], attach.getQName());
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, attach.getPath());
        Set<DataSchemaNode> attachChildren = attach.getChildNodes();
        assertEquals(1, attachChildren.size());

        // case create
        qnames[3] = new QName(NS_FOO, revision, "f", "create");
        assertEquals(qnames[3], create.getQName());
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, create.getPath());
        Set<DataSchemaNode> createChildren = create.getChildNodes();
        assertEquals(1, createChildren.size());

        // case attach
        qnames[3] = new QName(NS_FOO, revision, "f", "destroy");
        assertEquals(qnames[3], destroy.getQName());
        expectedPath = new SchemaPath(Lists.newArrayList(qnames), true);
        assertEquals(expectedPath, destroy.getPath());
        Set<DataSchemaNode> destroyChildren = destroy.getChildNodes();
        assertEquals(1, destroyChildren.size());

    }

}
