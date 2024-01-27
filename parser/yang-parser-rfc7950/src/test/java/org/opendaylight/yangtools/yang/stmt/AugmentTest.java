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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;

class AugmentTest extends AbstractYangTest {
    private static final QNameModule FOO = QNameModule.of("urn:opendaylight.foo", "2013-10-13");
    private static final QNameModule BAR = QNameModule.of("urn:opendaylight.bar", "2013-10-14");
    private static final QNameModule BAZ = QNameModule.of("urn:opendaylight.baz", "2013-10-15");

    private static final QName Q0 = QName.create(BAR, "interfaces");
    private static final QName Q1 = QName.create(BAR, "ifEntry");
    private static final QName Q2 = QName.create(BAZ, "augment-holder");

    private static EffectiveModelContext AUGMENT_IN_AUGMENT;

    @BeforeAll
    static void beforeClass() throws Exception {
        AUGMENT_IN_AUGMENT = assertEffectiveModelDir("/augment-test/augment-in-augment");
    }

    @Test
    void testAugmentParsing() {
        // foo.yang
        final Module module1 = AUGMENT_IN_AUGMENT.findModules("foo").iterator().next();
        var augmentations = module1.getAugmentations();
        assertEquals(1, augmentations.size());
        final AugmentationSchemaNode augment = augmentations.iterator().next();
        assertNotNull(augment);

        assertEquals(Absolute.of(Q0, Q1, Q2), augment.getTargetPath());

        final var augmentChildren = augment.getChildNodes();
        assertEquals(4, augmentChildren.size());
        for (final DataSchemaNode dsn : augmentChildren) {
            checkIsAugmenting(dsn, false);
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
        assertEquals(QName.create(FOO, "ds0ChannelNumber"), ds0ChannelNumber.getQName());
        assertFalse(ds0ChannelNumber.isAugmenting());
        // type of leaf ds0ChannelNumber
        assertEquals(TypeDefinitions.STRING, ds0ChannelNumber.getType().getQName());

        // leaf interface-id
        assertEquals(QName.create(FOO, "interface-id"), interfaceId.getQName());
        assertFalse(interfaceId.isAugmenting());

        // container schemas
        assertEquals(QName.create(FOO, "schemas"), schemas.getQName());
        assertFalse(schemas.isAugmenting());

        // choice odl
        assertEquals(QName.create(FOO, "odl"), odl.getQName());
        assertFalse(odl.isAugmenting());

        // baz.yang
        final Module module3 = AUGMENT_IN_AUGMENT.findModules("baz").iterator().next();
        augmentations = module3.getAugmentations();
        assertEquals(3, augmentations.size());
        AugmentationSchemaNode augment1 = null;
        AugmentationSchemaNode augment2 = null;
        AugmentationSchemaNode augment3 = null;
        for (final AugmentationSchemaNode as : augmentations) {
            if (as.getWhenCondition().isEmpty()) {
                augment3 = as;
            } else if ("br:ifType='ds0'".equals(as.getWhenCondition().orElseThrow().toString())) {
                augment1 = as;
            } else if ("br:ifType='ds2'".equals(as.getWhenCondition().orElseThrow().toString())) {
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
    void testAugmentResolving() {
        final Module module2 = AUGMENT_IN_AUGMENT.findModules("bar").iterator().next();
        final ContainerSchemaNode interfaces = (ContainerSchemaNode) module2.getDataChildByName(QName.create(
            module2.getQNameModule(), "interfaces"));
        final ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName(QName.create(
            module2.getQNameModule(), "ifEntry"));

        // baz.yang
        // augment "/br:interfaces/br:ifEntry" {
        final ContainerSchemaNode augmentHolder = (ContainerSchemaNode) ifEntry.getDataChildByName(QName.create(BAZ,
            "augment-holder"));
        checkIsAugmenting(augmentHolder, true);
        assertEquals(Q2, augmentHolder.getQName());

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
        assertEquals(QName.create(FOO, "ds0ChannelNumber"), ds0ChannelNumber.getQName());

        // leaf interface-id
        assertEquals(QName.create(FOO, "interface-id"), interfaceId.getQName());

        // container schemas
        assertEquals(QName.create(FOO, "schemas"), schemas.getQName());

        // choice odl
        assertEquals(QName.create(FOO, "odl"), odl.getQName());
    }

    @Test
    void testAugmentedChoice() {
        final Module module2 = AUGMENT_IN_AUGMENT.findModules("bar").iterator().next();
        final ContainerSchemaNode interfaces = (ContainerSchemaNode) module2.getDataChildByName(QName.create(
            module2.getQNameModule(), "interfaces"));
        final ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName(QName.create(
            module2.getQNameModule(), "ifEntry"));
        final ContainerSchemaNode augmentedHolder = (ContainerSchemaNode) ifEntry.getDataChildByName(QName.create(
            BAZ, "augment-holder"));
        checkIsAugmenting(augmentedHolder, true);

        // foo.yang
        // augment "/br:interfaces/br:ifEntry/bz:augment-holder"
        final ChoiceSchemaNode odl = (ChoiceSchemaNode) augmentedHolder.getDataChildByName(QName.create(FOO, "odl"));
        assertNotNull(odl);
        final var cases = odl.getCases();
        assertEquals(4, cases.size());

        CaseSchemaNode id = null;
        CaseSchemaNode node1 = null;
        CaseSchemaNode node2 = null;
        CaseSchemaNode node3 = null;

        for (final CaseSchemaNode ccn : cases) {
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

        // case id
        assertEquals(QName.create(FOO, "id"), id.getQName());
        final var idChildren = id.getChildNodes();
        assertEquals(1, idChildren.size());

        // case node1
        assertEquals(QName.create(FOO, "node1"), node1.getQName());
        final var node1Children = node1.getChildNodes();
        assertTrue(node1Children.isEmpty());

        // case node2
        assertEquals(QName.create(FOO, "node2"), node2.getQName());
        final var node2Children = node2.getChildNodes();
        assertTrue(node2Children.isEmpty());

        // case node3
        assertEquals(QName.create(FOO, "node3"), node3.getQName());
        final var node3Children = node3.getChildNodes();
        assertEquals(1, node3Children.size());

        // test cases
        // case id child
        final LeafSchemaNode caseIdChild = (LeafSchemaNode) idChildren.iterator().next();
        assertNotNull(caseIdChild);
        assertEquals(QName.create(FOO, "id"), caseIdChild.getQName());

        // case node3 child
        final ContainerSchemaNode caseNode3Child = (ContainerSchemaNode) node3Children.iterator().next();
        assertNotNull(caseNode3Child);
        assertEquals(QName.create(FOO, "node3"), caseNode3Child.getQName());
    }

    @Test
    void testAugmentRpc() throws Exception {
        final EffectiveModelContext context = assertEffectiveModelDir("/augment-test/rpc");
        final XMLNamespace NS_BAR = XMLNamespace.of("urn:opendaylight:bar");
        final XMLNamespace NS_FOO = XMLNamespace.of("urn:opendaylight:foo");
        final Revision revision = Revision.of("2013-10-11");
        final Module bar = context.findModules("bar").iterator().next();
        final var rpcs = bar.getRpcs();
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
        final InputSchemaNode input = submit.getInput();
        final QName inputQName = QName.create(NS_BAR, revision, "input");
        assertEquals(inputQName, input.getQName());
        final ChoiceSchemaNode arguments = (ChoiceSchemaNode) input.getDataChildByName(QName.create(NS_BAR, revision,
            "arguments"));
        final QName argumentsQName = QName.create(NS_BAR, revision, "arguments");
        assertEquals(argumentsQName, arguments.getQName());
        assertFalse(arguments.isAugmenting());
        final var cases = arguments.getCases();
        assertEquals(3, cases.size());

        CaseSchemaNode attach = null;
        CaseSchemaNode create = null;
        CaseSchemaNode destroy = null;
        for (final CaseSchemaNode child : cases) {
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

        // case attach
        assertEquals(QName.create(NS_FOO, revision, "attach"), attach.getQName());
        final var attachChildren = attach.getChildNodes();
        assertEquals(1, attachChildren.size());

        // case create
        assertEquals(QName.create(NS_FOO, revision, "create"), create.getQName());
        final var createChildren = create.getChildNodes();
        assertEquals(1, createChildren.size());

        // case attach
        assertEquals(QName.create(NS_FOO, revision, "destroy"), destroy.getQName());
        final var destroyChildren = destroy.getChildNodes();
        assertEquals(1, destroyChildren.size());
    }

    @Test
    void testAugmentInUsesResolving() throws Exception {
        final EffectiveModelContext context = assertEffectiveModelDir("/augment-test/augment-in-uses");
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
        final var augments = node.getAvailableAugmentations();
        assertEquals(1, augments.size());
        assertEquals(1, node.getChildNodes().size());
        final LeafSchemaNode id = (LeafSchemaNode) node.getDataChildByName(QName.create(test.getQNameModule(), "id"));
        assertTrue(id.isAugmenting());
    }

    /**
     * Test if node has augmenting flag set to expected value. In case this is  DataNodeContainer/ChoiceNode, check its
     * child nodes/case nodes too.
     *
     * @param node node to check
     * @param expected expected value
     */
    private static void checkIsAugmenting(final DataSchemaNode node, final boolean expected) {
        assertEquals(expected, node.isAugmenting());
        if (node instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) node).getChildNodes()) {
                checkIsAugmenting(child, expected);
            }
        } else if (node instanceof ChoiceSchemaNode) {
            for (CaseSchemaNode caseNode : ((ChoiceSchemaNode) node).getCases()) {
                checkIsAugmenting(caseNode, expected);
            }
        }
    }
}
