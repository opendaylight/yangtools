/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TipProducingDataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefDataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefValidatation;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTreeCandidateValidatorTest {

    private static SchemaContext context;
    private static Module valModule;
    private static QNameModule valModuleQname;
    private static LeafRefContext rootLeafRefContext;
    public static TipProducingDataTree inMemoryDataTree;

    private static QName odl;
    private static QName project;
    private static QName name;
    private static QName desc;
    private static QName lead;
    private static QName owner;
    private static QName odlContributor;
    private static QName contributor;
    private static QName odlProjectName;
    private static QName odlProjectDesc;
    private static QName login;
    private static QName contributorName;
    private static QName l1;
    private static QName l2;
    private static QName con1;
    private static QName ch1;
    private static QName ch2;
    private static QName leafrefInChoice;
    private static QName listInChoice;

    private static QName leafrefInChoiceToChoice;
    private static QName con3;
    private static QName list3InChoice;
    private static QName l3;
    private static QName choiceInCon3;

    private static QName listInChoiceKey;
    private static QName k;

    private static QName leafrefLeafList;

    private static final Logger LOG = LoggerFactory.getLogger(DataTreeCandidateValidatorTest.class);
    private static final String NEW_LINE = System.getProperty("line.separator");

    static {
        BasicConfigurator.configure();
    }

    @BeforeClass
    public static void init() throws FileNotFoundException, ReactorException,
            URISyntaxException {
        initSchemaContext();

        initLeafRefContext();

        initQnames();

        initDataTree();

    }

    private static void initSchemaContext() {
        context = YangParserTestUtils.parseYangResourceDirectory("/leafref-validation");

        final Set<Module> modules = context.getModules();
        for (final Module module : modules) {
            if (module.getName().equals("leafref-validation")) {
                valModule = module;
            }
        }

        valModuleQname = valModule.getQNameModule();
    }

    private static void initLeafRefContext() {
        rootLeafRefContext = LeafRefContext.create(context);
    }

    private static void initQnames() {
        odl = QName.create(valModuleQname, "odl-project");
        project = QName.create(valModuleQname, "project");
        name = QName.create(valModuleQname, "name");
        desc = QName.create(valModuleQname, "desc");
        lead = QName.create(valModuleQname, "project-lead");
        owner = QName.create(valModuleQname, "project-owner");

        odlContributor = QName.create(valModuleQname, "odl-contributor");
        contributor = QName.create(valModuleQname, "contributor");
        odlProjectName = QName.create(valModuleQname, "odl-project-name");
        login = QName.create(valModuleQname, "login");
        contributorName = QName.create(valModuleQname, "contributor-name");

        con1 = QName.create(valModuleQname, "con1");
        l1 = QName.create(valModuleQname, "l1");
        l2 = QName.create(valModuleQname, "l2");
        odlProjectDesc = QName.create(valModuleQname, "odl-project-desc");

        ch1 = QName.create(valModuleQname, "ch1");
        ch2 = QName.create(valModuleQname, "ch2");
        leafrefInChoice = QName.create(valModuleQname, "leafref-in-choice");
        listInChoice = QName.create(valModuleQname, "list-in-choice");

        leafrefInChoiceToChoice = QName.create(valModuleQname,
                "leafref-in-choice-to-choice");
        con3 = QName.create(valModuleQname, "con3");
        list3InChoice = QName.create(valModuleQname, "list3-in-choice");
        l3 = QName.create(valModuleQname, "l3");
        choiceInCon3 = QName.create(valModuleQname, "choice-in-con3");

        listInChoiceKey = QName.create(valModuleQname, "list-in-choice-key");
        k = QName.create(valModuleQname, "k");

        leafrefLeafList = QName.create(valModuleQname, "leafref-leaf-list");

    }

    private static void initDataTree() {
        inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL);
        inMemoryDataTree.setSchemaContext(context);

        final DataTreeModification initialDataTreeModification = inMemoryDataTree
                .takeSnapshot().newModification();

        final ContainerSchemaNode odlProjContSchemaNode = (ContainerSchemaNode) valModule
                .getDataChildByName(odl);

        final ContainerNode odlProjectContainer = createOdlContainer(odlProjContSchemaNode);

        final YangInstanceIdentifier path = YangInstanceIdentifier.of(odl);
        initialDataTreeModification.write(path, odlProjectContainer);
        initialDataTreeModification.ready();

        final DataTreeCandidate writeContributorsCandidate = inMemoryDataTree
                .prepare(initialDataTreeModification);
        inMemoryDataTree.commit(writeContributorsCandidate);

    }

    @Test
    public void dataTreeCanditateValidationTest() {
        write();

        write2();

        delete();

        writeContributors();

        writeMapEntry();

        writeIntoMapEntry();
    }

    private static void writeContributors() {

        final ContainerSchemaNode contributorContSchemaNode = (ContainerSchemaNode) valModule
                .getDataChildByName(odlContributor);

        final ContainerNode contributorContainer = createBasicContributorContainer(contributorContSchemaNode);

        final YangInstanceIdentifier contributorPath = YangInstanceIdentifier
                .of(odlContributor);
        final DataTreeModification writeModification = inMemoryDataTree
                .takeSnapshot().newModification();
        writeModification.write(contributorPath, contributorContainer);
        writeModification.ready();

        final DataTreeCandidate writeContributorsCandidate = inMemoryDataTree
                .prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before write of contributors: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        boolean exception = false;
        try {
            LeafRefValidatation.validate(writeContributorsCandidate,
                    rootLeafRefContext);
        } catch (final LeafRefDataValidationFailedException e) {
            LOG.debug("All validation errors:" + NEW_LINE + e.getMessage());
            assertEquals(3, e.getValidationsErrorsCount());
            exception = true;
        }

        inMemoryDataTree.commit(writeContributorsCandidate);

        LOG.debug("*************************");
        LOG.debug("After write of contributors: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        assertTrue(exception);

    }

    private static void writeIntoMapEntry() {

        final Map<QName, Object> keys = new HashMap<>();
        keys.put(name, "New Project");
        final NodeIdentifierWithPredicates mapEntryPath = new NodeIdentifierWithPredicates(
                project, keys);

        final YangInstanceIdentifier leaderPath = YangInstanceIdentifier
                .of(odl).node(project).node(mapEntryPath).node(lead);

        final LeafNode<String> leader = ImmutableNodes.leafNode(lead,
                "Updated leader");

        final DataTreeModification writeModification = inMemoryDataTree
                .takeSnapshot().newModification();
        writeModification.write(leaderPath, leader);
        writeModification.ready();

        final DataTreeCandidate writeContributorsCandidate = inMemoryDataTree
                .prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before write into map entry (update of leader name): ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        boolean exception = false;
        try {
            LeafRefValidatation.validate(writeContributorsCandidate,
                    rootLeafRefContext);
        } catch (final LeafRefDataValidationFailedException e) {
            LOG.debug("All validation errors:" + NEW_LINE + e.getMessage());
            assertEquals(1, e.getValidationsErrorsCount());
            exception = true;
        }

        inMemoryDataTree.commit(writeContributorsCandidate);

        LOG.debug("*************************");
        LOG.debug("After write into map entry (update of leader name): ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        assertTrue(exception);

    }

    private static void writeMapEntry() {

        final Map<QName, Object> keys = new HashMap<>();
        keys.put(name, "New Project");
        final NodeIdentifierWithPredicates mapEntryPath = new NodeIdentifierWithPredicates(
                project, keys);

        final YangInstanceIdentifier newOdlProjectMapEntryPath = YangInstanceIdentifier
                .of(odl).node(project).node(mapEntryPath);

        final ContainerSchemaNode odlProjContSchemaNode = (ContainerSchemaNode) valModule
                .getDataChildByName(odl);
        final ListSchemaNode projListSchemaNode = (ListSchemaNode) odlProjContSchemaNode
                .getDataChildByName(project);
        final MapEntryNode newProjectMapEntry = createProjectListEntry(
                "New Project", "New Project description ...",
                "Leader of New Project", "Owner of New Project",
                projListSchemaNode);

        final DataTreeModification writeModification = inMemoryDataTree
                .takeSnapshot().newModification();
        writeModification.write(newOdlProjectMapEntryPath, newProjectMapEntry);
        writeModification.ready();

        final DataTreeCandidate writeContributorsCandidate = inMemoryDataTree
                .prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before map entry write: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        boolean exception = false;
        try {
            LeafRefValidatation.validate(writeContributorsCandidate,
                    rootLeafRefContext);
        } catch (final LeafRefDataValidationFailedException e) {
            LOG.debug("All validation errors:" + NEW_LINE + e.getMessage());
            assertEquals(2, e.getValidationsErrorsCount());
            exception = true;
        }

        inMemoryDataTree.commit(writeContributorsCandidate);

        LOG.debug("*************************");
        LOG.debug("After map entry write: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        assertTrue(exception);

    }

    private static void write() {

        final ContainerSchemaNode contributorContSchemaNode = (ContainerSchemaNode) valModule
                .getDataChildByName(odlContributor);

        final ContainerNode contributorContainer = createContributorContainer(contributorContSchemaNode);

        final YangInstanceIdentifier contributorPath = YangInstanceIdentifier
                .of(odlContributor);
        final DataTreeModification writeModification = inMemoryDataTree
                .takeSnapshot().newModification();
        writeModification.write(contributorPath, contributorContainer);

        writeModification.write(YangInstanceIdentifier.of(l1),
                ImmutableNodes.leafNode(l1, "Leafref l1 under the root"));
        writeModification
                .write(YangInstanceIdentifier.of(l2), ImmutableNodes.leafNode(
                        l2, "Leafref target l2 under the root"));

        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = inMemoryDataTree
                .prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before write: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        boolean exception = false;
        try {
            LeafRefValidatation.validate(writeContributorsCandidate,
                    rootLeafRefContext);
        } catch (final LeafRefDataValidationFailedException e) {
            LOG.debug("All validation errors:" + NEW_LINE + e.getMessage());
            assertEquals(12, e.getValidationsErrorsCount());
            exception = true;
        }

        inMemoryDataTree.commit(writeContributorsCandidate);

        LOG.debug("*************************");
        LOG.debug("After write: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        assertTrue(exception);
    }

    private static void write2() {

        final ContainerSchemaNode odlCon = (ContainerSchemaNode) valModule
                .getDataChildByName(odl);
        final ContainerSchemaNode con1Con = (ContainerSchemaNode) odlCon
                .getDataChildByName(con1);
        final LeafNode<String> l1Leaf = ImmutableNodes.leafNode(l1, "l1 value");
        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> containerBuilder = Builders
                .containerBuilder(con1Con);
        containerBuilder.addChild(l1Leaf);
        final ContainerNode con1Node = containerBuilder.build();

        final YangInstanceIdentifier con1Path = YangInstanceIdentifier.of(odl)
                .node(con1);
        final DataTreeModification writeModification = inMemoryDataTree
                .takeSnapshot().newModification();
        writeModification.write(con1Path, con1Node);

        final ChoiceNode choiceNode = createChoiceNode();
        final YangInstanceIdentifier choicePath = YangInstanceIdentifier
                .of(odl).node(ch1);
        writeModification.write(choicePath, choiceNode);

        final ContainerNode con3Node = createCon3Node();
        final YangInstanceIdentifier con3Path = YangInstanceIdentifier.of(odl)
                .node(con3);
        writeModification.write(con3Path, con3Node);

        final LeafSetNode<?> leafListNode = createLeafRefLeafListNode();
        final YangInstanceIdentifier leafListPath = YangInstanceIdentifier.of(
                odl).node(leafrefLeafList);
        writeModification.write(leafListPath, leafListNode);
        writeModification.ready();

        final DataTreeCandidate writeContributorsCandidate = inMemoryDataTree
                .prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before write2: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        boolean exception = false;
        try {
            LeafRefValidatation.validate(writeContributorsCandidate,
                    rootLeafRefContext);
        } catch (final LeafRefDataValidationFailedException e) {
            LOG.debug("All validation errors:" + NEW_LINE + e.getMessage());
            assertEquals(6, e.getValidationsErrorsCount());
            exception = true;
        }

        assertTrue(exception);

        inMemoryDataTree.commit(writeContributorsCandidate);

        LOG.debug("*************************");
        LOG.debug("After write2: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

    }

    private static LeafSetNode<?> createLeafRefLeafListNode() {

        final ListNodeBuilder<Object, LeafSetEntryNode<Object>> leafSetBuilder = Builders
                .leafSetBuilder();
        leafSetBuilder.withNodeIdentifier(new NodeIdentifier(leafrefLeafList));

        leafSetBuilder.addChild(createLeafSetEntry(leafrefLeafList, "k1"));
        leafSetBuilder.addChild(createLeafSetEntry(leafrefLeafList, "k2"));
        leafSetBuilder.addChild(createLeafSetEntry(leafrefLeafList, "k3"));

        return leafSetBuilder.build();
    }

    private static ContainerNode createCon3Node() {

        final CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder = Builders
                .mapBuilder();
        mapBuilder.withNodeIdentifier(new NodeIdentifier(list3InChoice));

        mapBuilder.addChild(createList3Entry("k1", "val1", "valA", "valX"));
        mapBuilder.addChild(createList3Entry("k2", "val2", "valB", "valY"));

        final DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> choiceBuilder = Builders
                .choiceBuilder();
        choiceBuilder.withNodeIdentifier(new NodeIdentifier(choiceInCon3));

        choiceBuilder.addChild(mapBuilder.build());

        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> containerBuilder = Builders
                .containerBuilder();
        containerBuilder.withNodeIdentifier(new NodeIdentifier(con3));

        containerBuilder.addChild(choiceBuilder.build());

        return containerBuilder.build();
    }

    private static MapEntryNode createList3Entry(final String kVal,
            final String l3Val1, final String l3Val2, final String l3Val3) {
        final DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder = Builders
                .mapEntryBuilder();
        mapEntryBuilder.withNodeIdentifier(new NodeIdentifierWithPredicates(
                list3InChoice, k, kVal));

        final ListNodeBuilder<Object, LeafSetEntryNode<Object>> leafSetBuilder = Builders
                .leafSetBuilder();
        leafSetBuilder.withNodeIdentifier(new NodeIdentifier(l3));

        leafSetBuilder.addChild(createLeafSetEntry(l3, l3Val1));
        leafSetBuilder.addChild(createLeafSetEntry(l3, l3Val2));
        leafSetBuilder.addChild(createLeafSetEntry(l3, l3Val3));

        mapEntryBuilder.addChild(ImmutableNodes.leafNode(k, kVal));
        mapEntryBuilder.addChild(leafSetBuilder.build());

        return mapEntryBuilder.build();
    }

    private static LeafSetEntryNode<Object> createLeafSetEntry(
            final QName qname, final String val) {
        final NormalizedNodeAttrBuilder<NodeWithValue, Object, LeafSetEntryNode<Object>> leafSetEntryBuilder = Builders
                .leafSetEntryBuilder();
        leafSetEntryBuilder.withNodeIdentifier(new NodeWithValue<>(qname, val));
        leafSetEntryBuilder.withValue(val);
        return leafSetEntryBuilder.build();
    }

    private static ChoiceNode createChoiceNode() {

        final CollectionNodeBuilder<MapEntryNode, MapNode> listInChoiceBuilder = Builders
                .mapBuilder();
        listInChoiceBuilder
                .withNodeIdentifier(new NodeIdentifier(listInChoice));

        listInChoiceBuilder.addChild(createListInChoiceEntry("key1",
                "leafref-in-choice value", "val1"));
        listInChoiceBuilder.addChild(createListInChoiceEntry("key2",
                "l1 value", "val2"));
        listInChoiceBuilder.addChild(createListInChoiceEntry("key3",
                "l1 value", "val3"));

        final DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> choice2Builder = Builders
                .choiceBuilder();
        choice2Builder.withNodeIdentifier(new NodeIdentifier(ch2));

        choice2Builder.addChild(listInChoiceBuilder.build());

        final DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> choiceBuilder = Builders
                .choiceBuilder();
        choiceBuilder.withNodeIdentifier(new NodeIdentifier(ch1));
        choiceBuilder.addChild(choice2Builder.build());

        return choiceBuilder.build();
    }

    private static MapEntryNode createListInChoiceEntry(final String keyVal,
            final String leafrefInChoiceVal,
            final String leafrefInChoiceToChoiceVal) {

        final DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder = Builders
                .mapEntryBuilder();

        mapEntryBuilder.withNodeIdentifier(new NodeIdentifierWithPredicates(
                listInChoice, listInChoiceKey, keyVal));

        mapEntryBuilder.addChild(ImmutableNodes.leafNode(listInChoiceKey,
                keyVal));
        mapEntryBuilder.addChild(ImmutableNodes.leafNode(leafrefInChoice,
                leafrefInChoiceVal));
        mapEntryBuilder.addChild(ImmutableNodes.leafNode(
                leafrefInChoiceToChoice, leafrefInChoiceToChoiceVal));

        return mapEntryBuilder.build();
    }

    private static void delete() {

        final YangInstanceIdentifier contributorPath = YangInstanceIdentifier
                .of(odlContributor);
        final DataTreeModification delete = inMemoryDataTree.takeSnapshot()
                .newModification();
        delete.delete(contributorPath);
        delete.ready();

        final DataTreeCandidate deleteContributorsCanditate = inMemoryDataTree
                .prepare(delete);

        LOG.debug("*************************");
        LOG.debug("Before delete: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        boolean exception = false;
        try {
            LeafRefValidatation.validate(deleteContributorsCanditate,
                    rootLeafRefContext);
        } catch (final LeafRefDataValidationFailedException e) {
            LOG.debug("All validation errors:" + NEW_LINE + e.getMessage());
            assertEquals(6, e.getValidationsErrorsCount());
            exception = true;
        }

        assertTrue(exception);

        inMemoryDataTree.commit(deleteContributorsCanditate);

        LOG.debug("*************************");
        LOG.debug("After delete: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

    }

    private static ContainerNode createContributorContainer(
            final ContainerSchemaNode contributorContSchemaNode) {

        final ListSchemaNode contributorListSchemaNode = (ListSchemaNode) contributorContSchemaNode
                .getDataChildByName(contributor);

        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> contributorContainerBldr = Builders
                .containerBuilder(contributorContSchemaNode);

        final MapNode contributorMap = createContributorList(contributorListSchemaNode);
        contributorContainerBldr.addChild(contributorMap);

        final ContainerNode contributorContainer = contributorContainerBldr
                .build();

        return contributorContainer;

    }

    private static MapNode createContributorList(
            final ListSchemaNode contributorListSchemaNode) {

        final CollectionNodeBuilder<MapEntryNode, MapNode> contributorMapBldr = Builders
                .mapBuilder(contributorListSchemaNode);

        final MapEntryNode contributorMapEntry1 = createContributorListEntry(
                "Leader of Yangtools", "Yangtools Leader name", "Yangtools",
                "Yangtools description ...", contributorListSchemaNode);
        final MapEntryNode contributorMapEntry2 = createContributorListEntry(
                "Leader of MD-SAL", "MD-SAL Leader name", "MD-SAL",
                "MD-SAL description ...", contributorListSchemaNode);
        final MapEntryNode contributorMapEntry3 = createContributorListEntry(
                "Leader of Controller", "Controller Leader name", "Controller",
                "Controller description ...", contributorListSchemaNode);

        final MapEntryNode contributorMapEntry4 = createContributorListEntry(
                "jdoe", "John Doe", "MD-SAL", "Yangtools description ...",
                contributorListSchemaNode);

        final MapEntryNode contributorMapEntry5 = createContributorListEntry(
                "foo", "foo name", "Controller", "MD-SAL description ...",
                contributorListSchemaNode);

        final MapEntryNode contributorMapEntry6 = createContributorListEntry(
                "bar", "bar name", "Yangtools", "Controller description ...",
                contributorListSchemaNode);

        final MapEntryNode contributorMapEntry7 = createContributorListEntry(
                "baz", "baz name", "Unknown Project",
                "Unknown Project description ...", contributorListSchemaNode);

        final MapEntryNode contributorMapEntry8 = createContributorListEntry(
                "pk", "pk name", "Unknown Project 2",
                "Controller description ...", contributorListSchemaNode);

        contributorMapBldr.addChild(contributorMapEntry1);
        contributorMapBldr.addChild(contributorMapEntry2);
        contributorMapBldr.addChild(contributorMapEntry3);
        contributorMapBldr.addChild(contributorMapEntry4);
        contributorMapBldr.addChild(contributorMapEntry5);
        contributorMapBldr.addChild(contributorMapEntry6);
        contributorMapBldr.addChild(contributorMapEntry7);
        contributorMapBldr.addChild(contributorMapEntry8);

        final MapNode contributorMap = contributorMapBldr.build();

        return contributorMap;

    }

    private static MapEntryNode createContributorListEntry(
            final String loginVal, final String contributorNameVal,
            final String odlProjectNameVal, final String odlProjectDescVal,
            final ListSchemaNode contributorListSchemaNode) {

        final LeafNode<String> loginLeaf = ImmutableNodes.leafNode(login,
                loginVal);
        final LeafNode<String> contributorNameLeaf = ImmutableNodes.leafNode(
                contributorName, contributorNameVal);
        final LeafNode<String> odlProjectNameLeafRef = ImmutableNodes.leafNode(
                odlProjectName, odlProjectNameVal);
        final LeafNode<String> odlProjectDescLeafRef = ImmutableNodes.leafNode(
                odlProjectDesc, odlProjectDescVal);

        final DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> contributorMapEntryBldr = Builders
                .mapEntryBuilder(contributorListSchemaNode);

        contributorMapEntryBldr.addChild(loginLeaf);
        contributorMapEntryBldr.addChild(contributorNameLeaf);
        contributorMapEntryBldr.addChild(odlProjectNameLeafRef);
        contributorMapEntryBldr.addChild(odlProjectDescLeafRef);

        final MapEntryNode contributorMapEntry = contributorMapEntryBldr
                .build();

        return contributorMapEntry;
    }

    private static ContainerNode createOdlContainer(
            final ContainerSchemaNode container) {

        final ListSchemaNode projListSchemaNode = (ListSchemaNode) container
                .getDataChildByName(project);

        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> odlProjectContainerBldr = Builders
                .containerBuilder(container);

        final MapNode projectMap = createProjectList(projListSchemaNode);
        odlProjectContainerBldr.addChild(projectMap);

        final ContainerNode odlProjectContainer = odlProjectContainerBldr
                .build();

        return odlProjectContainer;
    }

    private static MapNode createProjectList(
            final ListSchemaNode projListSchemaNode) {

        final CollectionNodeBuilder<MapEntryNode, MapNode> projectMapBldr = Builders
                .mapBuilder(projListSchemaNode);

        final MapEntryNode projMapEntry1 = createProjectListEntry("Yangtools",
                "Yangtools description ...", "Leader of Yangtools",
                "Owner of Yangtools", projListSchemaNode);
        final MapEntryNode projMapEntry2 = createProjectListEntry("MD-SAL",
                "MD-SAL description ...", "Leader of MD-SAL",
                "Owner of MD-SAL", projListSchemaNode);
        final MapEntryNode projMapEntry3 = createProjectListEntry("Controller",
                "Controller description ...", "Leader of Controller",
                "Owner of Controller", projListSchemaNode);

        projectMapBldr.addChild(projMapEntry1);
        projectMapBldr.addChild(projMapEntry2);
        projectMapBldr.addChild(projMapEntry3);

        final MapNode projectMap = projectMapBldr.build();

        return projectMap;
    }

    private static MapEntryNode createProjectListEntry(final String nameVal,
            final String descVal, final String leadVal, final String ownerVal,
            final ListSchemaNode projListSchemaNode) {

        final LeafNode<String> nameLeaf = ImmutableNodes
                .leafNode(name, nameVal);
        final LeafNode<String> descLeaf = ImmutableNodes
                .leafNode(desc, descVal);
        final LeafNode<String> leadLeafRef = ImmutableNodes.leafNode(lead,
                leadVal);
        final LeafNode<String> ownerLeafRef = ImmutableNodes.leafNode(owner,
                ownerVal);

        final DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> projMapEntryBldr = Builders
                .mapEntryBuilder(projListSchemaNode);

        projMapEntryBldr.addChild(nameLeaf);
        projMapEntryBldr.addChild(descLeaf);
        projMapEntryBldr.addChild(leadLeafRef);
        projMapEntryBldr.addChild(ownerLeafRef);
        final MapEntryNode projMapEntry = projMapEntryBldr.build();

        return projMapEntry;
    }

    private static ContainerNode createBasicContributorContainer(
            final ContainerSchemaNode contributorContSchemaNode) {

        final ListSchemaNode contributorListSchemaNode = (ListSchemaNode) contributorContSchemaNode
                .getDataChildByName(contributor);

        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> contributorContainerBldr = Builders
                .containerBuilder(contributorContSchemaNode);

        final MapNode contributorMap = createBasicContributorList(contributorListSchemaNode);
        contributorContainerBldr.addChild(contributorMap);

        final ContainerNode contributorContainer = contributorContainerBldr
                .build();

        return contributorContainer;

    }

    private static MapNode createBasicContributorList(
            final ListSchemaNode contributorListSchemaNode) {

        final CollectionNodeBuilder<MapEntryNode, MapNode> contributorMapBldr = Builders
                .mapBuilder(contributorListSchemaNode);

        final MapEntryNode contributorMapEntry1 = createContributorListEntry(
                "Leader of Yangtools", "Yangtools Leader name", "Yangtools",
                "Yangtools description ...", contributorListSchemaNode);
        final MapEntryNode contributorMapEntry2 = createContributorListEntry(
                "Leader of MD-SAL", "MD-SAL Leader name", "MD-SAL",
                "MD-SAL description ...", contributorListSchemaNode);
        final MapEntryNode contributorMapEntry3 = createContributorListEntry(
                "Leader of Controller", "Controller Leader name", "Controller",
                "Controller description ...", contributorListSchemaNode);

        contributorMapBldr.addChild(contributorMapEntry1);
        contributorMapBldr.addChild(contributorMapEntry2);
        contributorMapBldr.addChild(contributorMapEntry3);

        final MapNode contributorMap = contributorMapBldr.build();

        return contributorMap;
    }
}