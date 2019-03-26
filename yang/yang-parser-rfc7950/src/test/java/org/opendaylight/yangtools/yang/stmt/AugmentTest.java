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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

public class AugmentTest {
    private static final QNameModule FOO = QNameModule.create(
        URI.create("urn:opendaylight.foo"), Revision.of("2013-10-13"));
    private static final QNameModule BAR = QNameModule.create(
        URI.create("urn:opendaylight.bar"), Revision.of("2013-10-14"));
    private static final QNameModule BAZ = QNameModule.create(
        URI.create("urn:opendaylight.baz"), Revision.of("2013-10-15"));

    private static final QName Q0 = QName.create(BAR, "interfaces");
    private static final QName Q1 = QName.create(BAR, "ifEntry");
    private static final QName Q2 = QName.create(BAZ, "augment-holder");

    @Test
    public void testAugmentParsing() throws Exception {
        final SchemaContext context = TestUtils.loadModules(getClass().getResource("/augment-test/augment-in-augment")
            .toURI());
        final List<QName> qnames = new ArrayList<>();
        qnames.add(Q0);
        qnames.add(Q1);
        qnames.add(Q2);

        // foo.yang
        final Module module1 = TestUtils.findModule(context, "foo").get();
        Set<AugmentationSchemaNode> augmentations = module1.getAugmentations();
        assertEquals(1, augmentations.size());
        final AugmentationSchemaNode augment = augmentations.iterator().next();
        assertNotNull(augment);

        SchemaPath expectedSchemaPath = SchemaPath.create(qnames, true);
        assertEquals(expectedSchemaPath, augment.getTargetPath());

        final Collection<DataSchemaNode> augmentChildren = augment.getChildNodes();
        assertEquals(4, augmentChildren.size());
        for (final DataSchemaNode dsn : augmentChildren) {
            TestUtils.checkIsAugmenting(dsn, false);
        }

        final LeafSchemaNode ds0ChannelNumber = (LeafSchemaNode) augment.getDataChildByName(QName.create(
                module1.getQNameModule(), "ds0ChannelNumber"));
        final LeafSchemaNode interfaceId = (LeafSchemaNode) augment.getDataChildByName(QName.create(
                module1.getQNameModule(), "interface-id"));
        final ContainerSchemaNode schemas = (ContainerSchemaNode) augment.getDataChildByName(QName.create(
                module1.getQNameModule(), "schemas"));
        final ChoiceSchemaNode odl = (ChoiceSchemaNode) augment.getDataChildByName(QName.create(
                module1.getQNameModule(), "odl"));

        assertNotNull(ds0ChannelNumber);
        assertNotNull(interfaceId);
        assertNotNull(schemas);
        assertNotNull(odl);

        // leaf ds0ChannelNumber
        QName qname = QName.create(FOO, "ds0ChannelNumber");
        qnames.add(qname);
        assertEquals(qname, ds0ChannelNumber.getQName());
        expectedSchemaPath = SchemaPath.create(qnames, true);
        assertEquals(expectedSchemaPath, ds0ChannelNumber.getPath());
        assertFalse(ds0ChannelNumber.isAugmenting());
        // type of leaf ds0ChannelNumber
        final List<QName> typePath = Collections.singletonList(BaseTypes.STRING_QNAME);
        expectedSchemaPath = SchemaPath.create(typePath, true);
        assertEquals(expectedSchemaPath, ds0ChannelNumber.getType().getPath());

        // leaf interface-id
        qname = QName.create(FOO, "interface-id");
        assertEquals(qname, interfaceId.getQName());
        qnames.set(3, qname);
        expectedSchemaPath = SchemaPath.create(qnames, true);
        assertEquals(expectedSchemaPath, interfaceId.getPath());
        assertFalse(interfaceId.isAugmenting());

        // container schemas
        qname = QName.create(FOO, "schemas");
        assertEquals(qname, schemas.getQName());
        qnames.set(3, qname);
        expectedSchemaPath = SchemaPath.create(qnames, true);
        assertEquals(expectedSchemaPath, schemas.getPath());
        assertFalse(schemas.isAugmenting());

        // choice odl
        qname = QName.create(FOO, "odl");
        assertEquals(qname, odl.getQName());
        qnames.set(3, qname);
        expectedSchemaPath = SchemaPath.create(qnames, true);
        assertEquals(expectedSchemaPath, odl.getPath());
        assertFalse(odl.isAugmenting());

        // baz.yang
        final Module module3 = TestUtils.findModule(context, "baz").get();
        augmentations = module3.getAugmentations();
        assertEquals(3, augmentations.size());
        AugmentationSchemaNode augment1 = null;
        AugmentationSchemaNode augment2 = null;
        AugmentationSchemaNode augment3 = null;
        for (final AugmentationSchemaNode as : augmentations) {
            if (!as.getWhenCondition().isPresent()) {
                augment3 = as;
            } else if ("if:ifType='ds0'".equals(as.getWhenCondition().get().getOriginalString())) {
                augment1 = as;
            } else if ("if:ifType='ds2'".equals(as.getWhenCondition().get().getOriginalString())) {
                augment2 = as;
            }
        }
        assertNotNull(augment1);
        assertNotNull(augment2);
        assertNotNull(augment3);

        assertEquals(1, augment1.getChildNodes().size());
        final ContainerSchemaNode augmentHolder = (ContainerSchemaNode) augment1.getDataChildByName(QName.create(
                module3.getQNameModule(), "augment-holder"));
        assertNotNull(augmentHolder);

        assertEquals(1, augment2.getChildNodes().size());
        final ContainerSchemaNode augmentHolder2 = (ContainerSchemaNode) augment2.getDataChildByName(QName.create(
                module3.getQNameModule(), "augment-holder2"));
        assertNotNull(augmentHolder2);

        assertEquals(1, augment3.getChildNodes().size());
        final CaseSchemaNode pause = (CaseSchemaNode) augment3.getDataChildByName(QName.create(
                module3.getQNameModule(), "pause"));
        assertNotNull(pause);
    }

    @Test
    public void testAugmentResolving() throws Exception {
        final SchemaContext context = TestUtils.loadModules(getClass().getResource("/augment-test/augment-in-augment")
            .toURI());
        final Module module2 = TestUtils.findModule(context, "bar").get();
        final ContainerSchemaNode interfaces = (ContainerSchemaNode) module2.getDataChildByName(QName.create(
                module2.getQNameModule(), "interfaces"));
        final ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName(QName.create(
                module2.getQNameModule(), "ifEntry"));

        final List<QName> qnames = new ArrayList<>();
        qnames.add(Q0);
        qnames.add(Q1);
        qnames.add(Q2);

        // baz.yang
        // augment "/br:interfaces/br:ifEntry" {
        final ContainerSchemaNode augmentHolder = (ContainerSchemaNode) ifEntry.getDataChildByName(QName.create(BAZ,
                "augment-holder"));
        TestUtils.checkIsAugmenting(augmentHolder, true);
        assertEquals(Q2, augmentHolder.getQName());
        assertEquals(SchemaPath.create(qnames, true), augmentHolder.getPath());

        // foo.yang
        // augment "/br:interfaces/br:ifEntry/bz:augment-holder"
        final LeafSchemaNode ds0ChannelNumber = (LeafSchemaNode) augmentHolder.getDataChildByName(QName.create(FOO,
                "ds0ChannelNumber"));
        final LeafSchemaNode interfaceId = (LeafSchemaNode) augmentHolder.getDataChildByName(QName.create(FOO,
                "interface-id"));
        final ContainerSchemaNode schemas = (ContainerSchemaNode) augmentHolder.getDataChildByName(QName.create(FOO,
                "schemas"));
        final ChoiceSchemaNode odl = (ChoiceSchemaNode) augmentHolder.getDataChildByName(QName.create(FOO, "odl"));

        assertNotNull(ds0ChannelNumber);
        assertNotNull(interfaceId);
        assertNotNull(schemas);
        assertNotNull(odl);

        // leaf ds0ChannelNumber
        QName qname = QName.create(FOO, "ds0ChannelNumber");
        assertEquals(qname, ds0ChannelNumber.getQName());
        qnames.add(qname);
        assertEquals(SchemaPath.create(qnames, true), ds0ChannelNumber.getPath());

        // leaf interface-id
        qname = QName.create(FOO, "interface-id");
        assertEquals(qname, interfaceId.getQName());
        qnames.set(3, qname);
        assertEquals(SchemaPath.create(qnames, true), interfaceId.getPath());

        // container schemas
        qname = QName.create(FOO, "schemas");
        assertEquals(qname, schemas.getQName());
        qnames.set(3, qname);
        assertEquals(SchemaPath.create(qnames, true), schemas.getPath());

        // choice odl
        qname = QName.create(FOO, "odl");
        assertEquals(qname, odl.getQName());
        qnames.set(3, qname);
        assertEquals(SchemaPath.create(qnames, true), odl.getPath());
    }

    @Test
    public void testAugmentedChoice() throws Exception {
        final SchemaContext context = TestUtils.loadModules(getClass().getResource("/augment-test/augment-in-augment")
            .toURI());
        final Module module2 = TestUtils.findModule(context, "bar").get();
        final ContainerSchemaNode interfaces = (ContainerSchemaNode) module2.getDataChildByName(QName.create(
                module2.getQNameModule(), "interfaces"));
        final ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName(QName.create(
                module2.getQNameModule(), "ifEntry"));
        final ContainerSchemaNode augmentedHolder = (ContainerSchemaNode) ifEntry.getDataChildByName(QName.create(
                BAZ, "augment-holder"));
        TestUtils.checkIsAugmenting(augmentedHolder, true);

        // foo.yang
        // augment "/br:interfaces/br:ifEntry/bz:augment-holder"
        final ChoiceSchemaNode odl = (ChoiceSchemaNode) augmentedHolder.getDataChildByName(QName.create(FOO, "odl"));
        assertNotNull(odl);
        final SortedMap<QName, CaseSchemaNode> cases = odl.getCases();
        assertEquals(4, cases.size());

        CaseSchemaNode id = null;
        CaseSchemaNode node1 = null;
        CaseSchemaNode node2 = null;
        CaseSchemaNode node3 = null;

        for (final CaseSchemaNode ccn : cases.values()) {
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

        final List<QName> qnames = new ArrayList<>();
        qnames.add(Q0);
        qnames.add(Q1);
        qnames.add(Q2);
        qnames.add(QName.create(FOO, "odl"));

        // case id
        QName qname = QName.create(FOO, "id");
        assertEquals(qname, id.getQName());
        qnames.add(qname);
        assertEquals(SchemaPath.create(qnames, true), id.getPath());
        final Collection<DataSchemaNode> idChildren = id.getChildNodes();
        assertEquals(1, idChildren.size());

        // case node1
        qname = QName.create(FOO, "node1");
        assertEquals(qname, node1.getQName());
        qnames.set(4, qname);
        assertEquals(SchemaPath.create(qnames, true), node1.getPath());
        final Collection<DataSchemaNode> node1Children = node1.getChildNodes();
        assertTrue(node1Children.isEmpty());

        // case node2
        qname = QName.create(FOO, "node2");
        assertEquals(qname, node2.getQName());
        qnames.set(4, qname);
        assertEquals(SchemaPath.create(qnames, true), node2.getPath());
        final Collection<DataSchemaNode> node2Children = node2.getChildNodes();
        assertTrue(node2Children.isEmpty());

        // case node3
        qname = QName.create(FOO, "node3");
        assertEquals(qname, node3.getQName());
        qnames.set(4, qname);
        assertEquals(SchemaPath.create(qnames, true), node3.getPath());
        final Collection<DataSchemaNode> node3Children = node3.getChildNodes();
        assertEquals(1, node3Children.size());

        // test cases
        qnames.clear();
        qnames.add(Q0);
        qnames.add(Q1);
        qnames.add(Q2);
        qnames.add(QName.create(FOO, "odl"));

        // case id child
        qnames.add(QName.create(FOO, "id"));
        qnames.add(QName.create(FOO, "id"));
        final LeafSchemaNode caseIdChild = (LeafSchemaNode) idChildren.iterator().next();
        assertNotNull(caseIdChild);
        assertEquals(SchemaPath.create(qnames, true), caseIdChild.getPath());

        // case node3 child
        qnames.set(4, QName.create(FOO, "node3"));
        qnames.set(5, QName.create(FOO, "node3"));
        final ContainerSchemaNode caseNode3Child = (ContainerSchemaNode) node3Children.iterator().next();
        assertNotNull(caseNode3Child);
        assertEquals(SchemaPath.create(qnames, true), caseNode3Child.getPath());
    }

    @Test
    public void testAugmentRpc() throws Exception {
        final SchemaContext context = TestUtils.loadModules(getClass().getResource("/augment-test/rpc").toURI());
        final URI NS_BAR = URI.create("urn:opendaylight:bar");
        final URI NS_FOO = URI.create("urn:opendaylight:foo");
        final Revision revision = Revision.of("2013-10-11");
        final Module bar = TestUtils.findModule(context, "bar").get();
        final Set<RpcDefinition> rpcs = bar.getRpcs();
        assertEquals(2, rpcs.size());

        RpcDefinition submit = null;
        for (final RpcDefinition rpc : rpcs) {
            if ("submit".equals(rpc.getQName().getLocalName())) {
                submit = rpc;
                break;
            }
        }
        assertNotNull(submit);

        final QName submitQName = QName.create(NS_BAR, revision, "submit");
        assertEquals(submitQName, submit.getQName());
        final ContainerSchemaNode input = submit.getInput();
        final QName inputQName = QName.create(NS_BAR, revision, "input");
        assertEquals(inputQName, input.getQName());
        final ChoiceSchemaNode arguments = (ChoiceSchemaNode) input.getDataChildByName(QName.create(NS_BAR, revision,
                "arguments"));
        final QName argumentsQName = QName.create(NS_BAR, revision, "arguments");
        assertEquals(argumentsQName, arguments.getQName());
        assertFalse(arguments.isAugmenting());
        final SortedMap<QName, CaseSchemaNode> cases = arguments.getCases();
        assertEquals(3, cases.size());

        CaseSchemaNode attach = null;
        CaseSchemaNode create = null;
        CaseSchemaNode destroy = null;
        for (final CaseSchemaNode child : cases.values()) {
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

        final QName[] qnames = new QName[4];
        qnames[0] = submitQName;
        qnames[1] = inputQName;
        qnames[2] = argumentsQName;

        // case attach
        qnames[3] = QName.create(NS_FOO, revision, "attach");
        assertEquals(qnames[3], attach.getQName());
        assertEquals(SchemaPath.create(true, qnames), attach.getPath());
        final Collection<DataSchemaNode> attachChildren = attach.getChildNodes();
        assertEquals(1, attachChildren.size());

        // case create
        qnames[3] = QName.create(NS_FOO, revision, "create");
        assertEquals(qnames[3], create.getQName());
        assertEquals(SchemaPath.create(true, qnames), create.getPath());
        final Collection<DataSchemaNode> createChildren = create.getChildNodes();
        assertEquals(1, createChildren.size());

        // case attach
        qnames[3] = QName.create(NS_FOO, revision, "destroy");
        assertEquals(qnames[3], destroy.getQName());
        assertEquals(SchemaPath.create(true, qnames), destroy.getPath());
        final Collection<DataSchemaNode> destroyChildren = destroy.getChildNodes();
        assertEquals(1, destroyChildren.size());
    }

    @Test
    public void testAugmentInUsesResolving() throws Exception {
        final SchemaContext context = TestUtils.loadModules(getClass().getResource("/augment-test/augment-in-uses")
            .toURI());
        assertEquals(1, context.getModules().size());

        final Module test = context.getModules().iterator().next();
        final DataNodeContainer links = (DataNodeContainer) test.getDataChildByName(QName.create(test.getQNameModule(),
                "links"));
        final DataNodeContainer link = (DataNodeContainer) links.getDataChildByName(QName.create(test.getQNameModule(),
                "link"));
        final DataNodeContainer nodes = (DataNodeContainer) link.getDataChildByName(QName.create(test.getQNameModule(),
                "nodes"));
        final ContainerSchemaNode node = (ContainerSchemaNode) nodes.getDataChildByName(QName.create(
                test.getQNameModule(), "node"));
        final Set<AugmentationSchemaNode> augments = node.getAvailableAugmentations();
        assertEquals(1, augments.size());
        assertEquals(1, node.getChildNodes().size());
        final LeafSchemaNode id = (LeafSchemaNode) node.getDataChildByName(QName.create(test.getQNameModule(), "id"));
        assertTrue(id.isAugmenting());
    }

}
