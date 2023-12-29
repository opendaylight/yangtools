/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTreeCandidateValidatorTest {

    private static EffectiveModelContext context;
    private static Module valModule;
    private static QNameModule valModuleQname;
    private static LeafRefContext rootLeafRefContext;
    public static DataTree inMemoryDataTree;

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

    @BeforeAll
    static void init() throws DataValidationFailedException {
        context = YangParserTestUtils.parseYangResourceDirectory("/leafref-validation");

        for (final var module : context.getModules()) {
            if (module.getName().equals("leafref-validation")) {
                valModule = module;
            }
        }

        valModuleQname = valModule.getQNameModule();
        rootLeafRefContext = LeafRefContext.create(context);

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

        leafrefInChoiceToChoice = QName.create(valModuleQname, "leafref-in-choice-to-choice");
        con3 = QName.create(valModuleQname, "con3");
        list3InChoice = QName.create(valModuleQname, "list3-in-choice");
        l3 = QName.create(valModuleQname, "l3");
        choiceInCon3 = QName.create(valModuleQname, "choice-in-con3");

        listInChoiceKey = QName.create(valModuleQname, "list-in-choice-key");
        k = QName.create(valModuleQname, "k");

        leafrefLeafList = QName.create(valModuleQname, "leafref-leaf-list");

        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, context);

        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();

        final var odlProjectContainer = createOdlContainer();

        final var path = YangInstanceIdentifier.of(odl);
        initialDataTreeModification.write(path, odlProjectContainer);
        initialDataTreeModification.ready();

        final var writeContributorsCandidate = inMemoryDataTree
                .prepare(initialDataTreeModification);
        inMemoryDataTree.commit(writeContributorsCandidate);
    }

    @AfterAll
    static void cleanup() {
        inMemoryDataTree = null;
        rootLeafRefContext = null;
        valModule = null;
        context = null;
    }

    @Test
    void dataTreeCanditateValidationTest() throws DataValidationFailedException {
        write();

        write2();

        delete();

        writeContributors();

        writeMapEntry();

        writeIntoMapEntry();
    }

    private static void writeContributors() throws DataValidationFailedException {

        final var contributorContainer = createBasicContributorContainer();

        final var contributorPath = YangInstanceIdentifier.of(odlContributor);
        final var writeModification = inMemoryDataTree.takeSnapshot().newModification();
        writeModification.write(contributorPath, contributorContainer);
        writeModification.ready();

        final var writeContributorsCandidate = inMemoryDataTree
                .prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before write of contributors: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        final var ex = assertThrows(LeafRefDataValidationFailedException.class,
            () -> LeafRefValidation.validate(writeContributorsCandidate, rootLeafRefContext));
        assertEquals(3, ex.getValidationsErrorsCount());

        inMemoryDataTree.commit(writeContributorsCandidate);

        LOG.debug("*************************");
        LOG.debug("After write of contributors: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());
    }

    private static void writeIntoMapEntry() throws DataValidationFailedException {
        final var keys = new HashMap<QName, Object>();
        keys.put(name, "New Project");
        final var mapEntryPath = NodeIdentifierWithPredicates.of(project, keys);

        final var leaderPath = YangInstanceIdentifier.of(odl).node(project).node(mapEntryPath)
            .node(lead);

        final var leader = ImmutableNodes.leafNode(lead, "Updated leader");

        final var writeModification = inMemoryDataTree.takeSnapshot().newModification();
        writeModification.write(leaderPath, leader);
        writeModification.ready();

        final var writeContributorsCandidate = inMemoryDataTree.prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before write into map entry (update of leader name): ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        final var ex = assertThrows(LeafRefDataValidationFailedException.class,
            () -> LeafRefValidation.validate(writeContributorsCandidate, rootLeafRefContext));
        assertEquals(1, ex.getValidationsErrorsCount());

        inMemoryDataTree.commit(writeContributorsCandidate);

        LOG.debug("*************************");
        LOG.debug("After write into map entry (update of leader name): ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());
    }

    private static void writeMapEntry() throws DataValidationFailedException {
        final var mapEntryPath = NodeIdentifierWithPredicates.of(project, name, "New Project");

        final var newProjectMapEntry = createProjectListEntry(
                "New Project", "New Project description ...",
                "Leader of New Project", "Owner of New Project");

        final var writeModification = inMemoryDataTree.takeSnapshot().newModification();
        writeModification.write(YangInstanceIdentifier.of(odl).node(project).node(mapEntryPath), newProjectMapEntry);
        writeModification.ready();

        final var writeContributorsCandidate = inMemoryDataTree.prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before map entry write: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        final var ex = assertThrows(LeafRefDataValidationFailedException.class,
            () -> LeafRefValidation.validate(writeContributorsCandidate, rootLeafRefContext));
        assertEquals(2, ex.getValidationsErrorsCount());

        inMemoryDataTree.commit(writeContributorsCandidate);

        LOG.debug("*************************");
        LOG.debug("After map entry write: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());
    }

    private static void write() throws DataValidationFailedException {
        final var contributorContainer = createContributorContainer(
            (ContainerSchemaNode) valModule.getDataChildByName(odlContributor));

        final var contributorPath = YangInstanceIdentifier.of(odlContributor);
        final var writeModification = inMemoryDataTree.takeSnapshot().newModification();
        writeModification.write(contributorPath, contributorContainer);

        writeModification.write(YangInstanceIdentifier.of(l1),
            ImmutableNodes.leafNode(l1, "Leafref l1 under the root"));
        writeModification.write(YangInstanceIdentifier.of(l2),
            ImmutableNodes.leafNode(l2, "Leafref target l2 under the root"));

        writeModification.ready();
        final var writeContributorsCandidate = inMemoryDataTree.prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before write: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        final var ex = assertThrows(LeafRefDataValidationFailedException.class,
            () -> LeafRefValidation.validate(writeContributorsCandidate, rootLeafRefContext));
        assertEquals(12, ex.getValidationsErrorsCount());

        inMemoryDataTree.commit(writeContributorsCandidate);

        LOG.debug("*************************");
        LOG.debug("After write: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());
    }

    private static void write2() throws DataValidationFailedException {
        final var writeModification = inMemoryDataTree.takeSnapshot().newModification();
        writeModification.write(YangInstanceIdentifier.of(odl).node(con1), Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(con1))
            .addChild(ImmutableNodes.leafNode(l1, "l1 value"))
            .build());
        writeModification.write(YangInstanceIdentifier.of(odl).node(ch1), createChoiceNode());
        writeModification.write(YangInstanceIdentifier.of(odl).node(con3), createCon3Node());
        writeModification.write(YangInstanceIdentifier.of(odl).node(leafrefLeafList), createLeafRefLeafListNode());
        writeModification.ready();

        final var writeContributorsCandidate = inMemoryDataTree.prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before write2: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        final var ex = assertThrows(LeafRefDataValidationFailedException.class,
            () -> LeafRefValidation.validate(writeContributorsCandidate, rootLeafRefContext));
        assertEquals(6, ex.getValidationsErrorsCount());

        inMemoryDataTree.commit(writeContributorsCandidate);

        LOG.debug("*************************");
        LOG.debug("After write2: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());
    }

    private static @NonNull LeafSetNode<Object> createLeafRefLeafListNode() {
        return Builders.leafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(leafrefLeafList))
            .addChild(createLeafSetEntry(leafrefLeafList, "k1"))
            .addChild(createLeafSetEntry(leafrefLeafList, "k2"))
            .addChild(createLeafSetEntry(leafrefLeafList, "k3"))
            .build();
    }

    private static ContainerNode createCon3Node() {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(con3))
            .addChild(Builders.choiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(choiceInCon3))
                .addChild(Builders.mapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(list3InChoice))
                    .addChild(createList3Entry("k1", "val1", "valA", "valX"))
                    .addChild(createList3Entry("k2", "val2", "valB", "valY"))
                    .build())
                .build())
            .build();
    }

    private static MapEntryNode createList3Entry(final String keyVal,final String l3Val1, final String l3Val2,
            final String l3Val3) {
        return Builders.mapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(list3InChoice, k, keyVal))
            .addChild(ImmutableNodes.leafNode(k, keyVal))
            .addChild(Builders.leafSetBuilder()
                .withNodeIdentifier(new NodeIdentifier(l3))
                .addChild(createLeafSetEntry(l3, l3Val1))
                .addChild(createLeafSetEntry(l3, l3Val2))
                .addChild(createLeafSetEntry(l3, l3Val3))
                .build())
            .build();
    }

    private static LeafSetEntryNode<Object> createLeafSetEntry(final QName qname, final String val) {
        return Builders.leafSetEntryBuilder()
            .withNodeIdentifier(new NodeWithValue<>(qname, val))
            .withValue(val)
            .build();
    }

    private static ChoiceNode createChoiceNode() {
        return Builders.choiceBuilder()
            .withNodeIdentifier(new NodeIdentifier(ch1))
            .addChild(Builders.choiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(ch2))
                .addChild(Builders.mapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(listInChoice))
                    .addChild(createListInChoiceEntry("key1", "leafref-in-choice value", "val1"))
                    .addChild(createListInChoiceEntry("key2", "l1 value", "val2"))
                    .addChild(createListInChoiceEntry("key3", "l1 value", "val3"))
                    .build())
                .build())
            .build();
    }

    private static MapEntryNode createListInChoiceEntry(final String keyVal, final String leafrefInChoiceVal,
            final String leafrefInChoiceToChoiceVal) {
        return Builders.mapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(listInChoice, listInChoiceKey, keyVal))
            .addChild(ImmutableNodes.leafNode(listInChoiceKey, keyVal))
            .addChild(ImmutableNodes.leafNode(leafrefInChoice, leafrefInChoiceVal))
            .addChild(ImmutableNodes.leafNode(leafrefInChoiceToChoice, leafrefInChoiceToChoiceVal))
            .build();
    }

    private static void delete() throws DataValidationFailedException {
        final var delete = inMemoryDataTree.takeSnapshot().newModification();
        delete.delete(YangInstanceIdentifier.of(odlContributor));
        delete.ready();

        final var deleteContributorsCanditate = inMemoryDataTree.prepare(delete);

        LOG.debug("*************************");
        LOG.debug("Before delete: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());


        final var ex = assertThrows(LeafRefDataValidationFailedException.class,
            () -> LeafRefValidation.validate(deleteContributorsCanditate, rootLeafRefContext));
        assertEquals(6, ex.getValidationsErrorsCount());

        inMemoryDataTree.commit(deleteContributorsCanditate);

        LOG.debug("*************************");
        LOG.debug("After delete: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());
    }

    private static ContainerNode createContributorContainer(final ContainerSchemaNode contributorCont) {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(odlContributor))
            .addChild(createContributorList((ListSchemaNode) contributorCont.getDataChildByName(contributor)))
            .build();

    }

    private static SystemMapNode createContributorList(final ListSchemaNode contributorListSchemaNode) {
        return Builders.mapBuilder()
            .withNodeIdentifier(new NodeIdentifier(contributor))
            .addChild(createContributorListEntry("Leader of Yangtools", "Yangtools Leader name", "Yangtools",
                "Yangtools description ..."))
            .addChild(createContributorListEntry("Leader of MD-SAL", "MD-SAL Leader name", "MD-SAL",
                "MD-SAL description ..."))
            .addChild(createContributorListEntry("Leader of Controller", "Controller Leader name", "Controller",
                "Controller description ..."))
            .addChild(createContributorListEntry("jdoe", "John Doe", "MD-SAL", "Yangtools description ..."))
            .addChild(createContributorListEntry("foo", "foo name", "Controller", "MD-SAL description ..."))
            .addChild(createContributorListEntry("bar", "bar name", "Yangtools", "Controller description ..."))
            .addChild(createContributorListEntry("baz", "baz name", "Unknown Project",
                "Unknown Project description ..."))
            .addChild(createContributorListEntry("pk", "pk name", "Unknown Project 2", "Controller description ..."))
            .build();
    }

    private static MapEntryNode createContributorListEntry(final String loginVal, final String contributorNameVal,
            final String odlProjectNameVal, final String odlProjectDescVal) {
        return Builders.mapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(contributor, login, loginVal))
            .addChild(ImmutableNodes.leafNode(login, loginVal))
            .addChild(ImmutableNodes.leafNode(contributorName, contributorNameVal))
            .addChild(ImmutableNodes.leafNode(odlProjectName, odlProjectNameVal))
            .addChild(ImmutableNodes.leafNode(odlProjectDesc, odlProjectDescVal))
            .build();
    }

    private static ContainerNode createOdlContainer() {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(odl))
            .addChild(createProjectList())
            .build();
    }

    private static SystemMapNode createProjectList() {
        return Builders.mapBuilder()
            .withNodeIdentifier(new NodeIdentifier(project))
            .addChild(createProjectListEntry("Yangtools", "Yangtools description ...", "Leader of Yangtools",
                "Owner of Yangtools"))
            .addChild(createProjectListEntry("MD-SAL", "MD-SAL description ...", "Leader of MD-SAL", "Owner of MD-SAL"))
            .addChild(createProjectListEntry("Controller", "Controller description ...", "Leader of Controller",
            "Owner of Controller")).build();
    }

    private static MapEntryNode createProjectListEntry(final String nameVal, final String descVal, final String leadVal,
            final String ownerVal) {
        return Builders.mapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(project, name, nameVal))
            .addChild(ImmutableNodes.leafNode(name, nameVal))
            .addChild(ImmutableNodes.leafNode(desc, descVal))
            .addChild(ImmutableNodes.leafNode(lead, leadVal))
            .addChild(ImmutableNodes.leafNode(owner, ownerVal))
            .build();
    }

    private static ContainerNode createBasicContributorContainer() {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(odlContributor))
            .addChild(createBasicContributorList())
            .build();
    }

    private static SystemMapNode createBasicContributorList() {
        return Builders.mapBuilder()
            .withNodeIdentifier(new NodeIdentifier(contributor))
            .addChild(createContributorListEntry("Leader of Yangtools", "Yangtools Leader name", "Yangtools",
                "Yangtools description ..."))
            .addChild(createContributorListEntry("Leader of MD-SAL", "MD-SAL Leader name", "MD-SAL",
                "MD-SAL description ..."))
            .addChild(createContributorListEntry("Leader of Controller", "Controller Leader name", "Controller",
                "Controller description ...")).build();
    }
}
