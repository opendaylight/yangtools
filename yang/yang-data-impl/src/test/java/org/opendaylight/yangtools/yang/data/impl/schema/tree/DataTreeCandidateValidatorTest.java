/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.*;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.validation.LeafRefDataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.validation.DataTreeCandidateValidator;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.leafrefcontext.api.LeafRefContext;
import org.opendaylight.yangtools.leafrefcontext.builder.LeafRefContextTreeBuilder;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTree;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.junit.Test;

public class DataTreeCandidateValidatorTest {

    private static SchemaContext context;
    private static Module valModule;
    private static QNameModule valModuleQname;
    private static LeafRefContext rootLeafRefContext;
    public static InMemoryDataTree inMemoryDataTree;

    private static QName odl;
    private static QName project;
    private static QName name;
    private static QName desc;
    private static QName lead;
    private static QName owner;
    private static QName odlContributor;
    private static QName contributor;
    private static QName odlProjectName;
    private static QName login;
    private static QName contributorName;
    private static QName l1;
    private static QName l2;
    private static QName con1;

    private static final Logger LOG = LoggerFactory.getLogger("");
    private static final String NEW_LINE = System.getProperty("line.separator");

    static {
        BasicConfigurator.configure();
    }

    @BeforeClass
    public static void init() throws URISyntaxException, IOException,
            YangSyntaxErrorException {
        initSchemaContext();

        initLeafRefContext();

        initQnames();

        initDataTree();

    }

    private static void initSchemaContext() throws URISyntaxException,
            IOException, YangSyntaxErrorException {
        File resourceFile = new File(DataTreeCandidateValidatorTest.class
                .getResource("/leafref-validation/leafref-validation.yang")
                .toURI());
        File resourceDir = resourceFile.getParentFile();

        YangParserImpl parser = YangParserImpl.getInstance();
        context = parser.parseFile(resourceFile, resourceDir);

        Set<Module> modules = context.getModules();
        for (Module module : modules) {
            if (module.getName().equals("leafref-validation")) {
                valModule = module;
            }
        }

        valModuleQname = valModule.getQNameModule();
    }

    private static void initLeafRefContext() throws IOException,
            YangSyntaxErrorException {
        LeafRefContextTreeBuilder leafRefContextTreeBuilder = new LeafRefContextTreeBuilder(
                context);

        rootLeafRefContext = leafRefContextTreeBuilder
                .buildLeafRefContextTree();
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
    }

    private static void initDataTree() {
        inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(context);

        DataTreeModification initialDataTreeModification = inMemoryDataTree
                .takeSnapshot().newModification();

        ContainerSchemaNode odlProjContSchemaNode = (ContainerSchemaNode) valModule
                .getDataChildByName(odl);

        ContainerNode odlProjectContainer = createOdlContainer(odlProjContSchemaNode);

        YangInstanceIdentifier path = YangInstanceIdentifier.of(odl);
        initialDataTreeModification.write(path, odlProjectContainer);

        DataTreeCandidate writeContributorsCandidate = inMemoryDataTree
                .prepare(initialDataTreeModification);
        inMemoryDataTree.commit(writeContributorsCandidate);

    }

    @Test
    public void dataTreeCanditateValidationTest()
            throws LeafRefDataValidationFailedException {
        write();

        write2();

        delete();

    }

    private void write() {

        ContainerSchemaNode contributorContSchemaNode = (ContainerSchemaNode) valModule
                .getDataChildByName(odlContributor);

        ContainerNode contributorContainer = createContributorContainer(contributorContSchemaNode);

        YangInstanceIdentifier contributorPath = YangInstanceIdentifier
                .of(odlContributor);
        DataTreeModification writeModification = inMemoryDataTree
                .takeSnapshot().newModification();
        writeModification.write(contributorPath, contributorContainer);

        writeModification.write(YangInstanceIdentifier.of(l1),
                ImmutableNodes.leafNode(l1, "Leafref l1 under the root"));
        writeModification
                .write(YangInstanceIdentifier.of(l2), ImmutableNodes.leafNode(
                        l2, "Leafref target l2 under the root"));

        DataTreeCandidate writeContributorsCandidate = inMemoryDataTree
                .prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before write: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        boolean exception = false;
        try {
            new DataTreeCandidateValidator().validateLeafRefs(
                    writeContributorsCandidate, rootLeafRefContext);
        } catch (LeafRefDataValidationFailedException e) {
            LOG.debug("All validation errors:" + NEW_LINE + e.getMessage());
            assertEquals(5, e.getValidationsErrorsCount());
            exception = true;
        }

        assertTrue(exception);

        inMemoryDataTree.commit(writeContributorsCandidate);

        LOG.debug("*************************");
        LOG.debug("After write: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

    }

    private void write2() {

        ContainerSchemaNode odlCon = (ContainerSchemaNode) valModule
                .getDataChildByName(odl);
        ContainerSchemaNode con1Con = (ContainerSchemaNode) odlCon
                .getDataChildByName(con1);

        LeafNode<String> l1Leaf = ImmutableNodes.leafNode(l1, "l1 value");

        DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> containerBuilder = Builders
                .containerBuilder(con1Con);
        containerBuilder.addChild(l1Leaf);

        ContainerNode con1Node = containerBuilder.build();

        YangInstanceIdentifier con1Path = YangInstanceIdentifier.of(odl).node(
                con1);
        DataTreeModification writeModification = inMemoryDataTree
                .takeSnapshot().newModification();
        writeModification.write(con1Path, con1Node);

        DataTreeCandidate writeContributorsCandidate = inMemoryDataTree
                .prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before write2: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        boolean exception = false;
        try {
            new DataTreeCandidateValidator().validateLeafRefs(
                    writeContributorsCandidate, rootLeafRefContext);
        } catch (LeafRefDataValidationFailedException e) {
            LOG.debug("All validation errors:" + NEW_LINE + e.getMessage());
            exception = true;
        }

        assertFalse(exception);

        inMemoryDataTree.commit(writeContributorsCandidate);

        LOG.debug("*************************");
        LOG.debug("After write2: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

    }

    private void delete() {

        YangInstanceIdentifier contributorPath = YangInstanceIdentifier
                .of(odlContributor);
        InMemoryDataTreeModification delete = inMemoryDataTree.takeSnapshot()
                .newModification();
        delete.delete(contributorPath);

        DataTreeCandidate deleteContributorsCanditate = inMemoryDataTree
                .prepare(delete);

        LOG.debug("*************************");
        LOG.debug("Before delete: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        boolean exception = false;
        try {
            new DataTreeCandidateValidator().validateLeafRefs(
                    deleteContributorsCanditate, rootLeafRefContext);
        } catch (LeafRefDataValidationFailedException e) {
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
            ContainerSchemaNode contributorContSchemaNode) {

        ListSchemaNode contributorListSchemaNode = (ListSchemaNode) contributorContSchemaNode
                .getDataChildByName(contributor);

        DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> contributorContainerBldr = Builders
                .containerBuilder(contributorContSchemaNode);

        MapNode contributorMap = createContributorList(contributorListSchemaNode);
        contributorContainerBldr.addChild(contributorMap);

        ContainerNode contributorContainer = contributorContainerBldr.build();

        return contributorContainer;

    }

    private static MapNode createContributorList(
            ListSchemaNode contributorListSchemaNode) {

        CollectionNodeBuilder<MapEntryNode, MapNode> contributorMapBldr = Builders
                .mapBuilder(contributorListSchemaNode);

        MapEntryNode contributorMapEntry1 = createContributorListEntry(
                "Leader of Yangtools", "Yangtools Leader name", "Yangtools",
                contributorListSchemaNode);
        MapEntryNode contributorMapEntry2 = createContributorListEntry(
                "Leader of MD-SAL", "MD-SAL Leader name", "MD-SAL",
                contributorListSchemaNode);
        MapEntryNode contributorMapEntry3 = createContributorListEntry(
                "Leader of Controller", "Controller Leader name", "Controller",
                contributorListSchemaNode);

        MapEntryNode contributorMapEntry4 = createContributorListEntry("jdoe",
                "John Doe", "Yangtools", contributorListSchemaNode);

        MapEntryNode contributorMapEntry5 = createContributorListEntry("foo",
                "foo name", "MD-SAL", contributorListSchemaNode);

        MapEntryNode contributorMapEntry6 = createContributorListEntry("bar",
                "bar name", "Controller", contributorListSchemaNode);

        contributorMapBldr.addChild(contributorMapEntry1);
        contributorMapBldr.addChild(contributorMapEntry2);
        contributorMapBldr.addChild(contributorMapEntry3);
        contributorMapBldr.addChild(contributorMapEntry4);
        contributorMapBldr.addChild(contributorMapEntry5);
        contributorMapBldr.addChild(contributorMapEntry6);

        MapNode contributorMap = contributorMapBldr.build();

        return contributorMap;

    }

    private static MapEntryNode createContributorListEntry(String loginVal,
            String contributorNameVal, String odlProjectNameVal,
            ListSchemaNode contributorListSchemaNode) {

        LeafNode<String> loginLeaf = ImmutableNodes.leafNode(login, loginVal);
        LeafNode<String> contributorNameLeaf = ImmutableNodes.leafNode(
                contributorName, contributorNameVal);
        LeafNode<String> odlProjectNameLeafRef = ImmutableNodes.leafNode(
                odlProjectName, odlProjectNameVal);

        DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> contributorMapEntryBldr = Builders
                .mapEntryBuilder(contributorListSchemaNode);

        contributorMapEntryBldr.addChild(loginLeaf);
        contributorMapEntryBldr.addChild(contributorNameLeaf);
        contributorMapEntryBldr.addChild(odlProjectNameLeafRef);

        MapEntryNode contributorMapEntry = contributorMapEntryBldr.build();

        return contributorMapEntry;
    }

    private static ContainerNode createOdlContainer(
            ContainerSchemaNode container) {

        ListSchemaNode projListSchemaNode = (ListSchemaNode) container
                .getDataChildByName(project);

        DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> odlProjectContainerBldr = Builders
                .containerBuilder(container);

        MapNode projectMap = createProjectList(projListSchemaNode);
        odlProjectContainerBldr.addChild(projectMap);

        ContainerNode odlProjectContainer = odlProjectContainerBldr.build();

        return odlProjectContainer;
    }

    private static MapNode createProjectList(ListSchemaNode projListSchemaNode) {

        CollectionNodeBuilder<MapEntryNode, MapNode> projectMapBldr = Builders
                .mapBuilder(projListSchemaNode);

        MapEntryNode projMapEntry1 = createProjectListEntry("Yangtools",
                "Yangtools description ...", "Leader of Yangtools",
                "Owner of Yangtools", projListSchemaNode);
        MapEntryNode projMapEntry2 = createProjectListEntry("MD-SAL",
                "MD-SAL description ...", "Leader of MD-SAL",
                "Owner of MD-SAL", projListSchemaNode);
        MapEntryNode projMapEntry3 = createProjectListEntry("Controller",
                "Controller description ...", "Leader of Controller",
                "Owner of Controller", projListSchemaNode);

        projectMapBldr.addChild(projMapEntry1);
        projectMapBldr.addChild(projMapEntry2);
        projectMapBldr.addChild(projMapEntry3);

        MapNode projectMap = projectMapBldr.build();

        return projectMap;
    }

    private static MapEntryNode createProjectListEntry(String nameVal,
            String descVal, String leadVal, String ownerVal,
            ListSchemaNode projListSchemaNode) {

        LeafNode<String> nameLeaf = ImmutableNodes.leafNode(name, nameVal);
        LeafNode<String> descLeaf = ImmutableNodes.leafNode(desc, descVal);
        LeafNode<String> leadLeafRef = ImmutableNodes.leafNode(lead, leadVal);
        LeafNode<String> ownerLeafRef = ImmutableNodes
                .leafNode(owner, ownerVal);

        DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> projMapEntryBldr = Builders
                .mapEntryBuilder(projListSchemaNode);

        projMapEntryBldr.addChild(nameLeaf);
        projMapEntryBldr.addChild(descLeaf);
        projMapEntryBldr.addChild(leadLeafRef);
        projMapEntryBldr.addChild(ownerLeafRef);
        MapEntryNode projMapEntry = projMapEntryBldr.build();

        return projMapEntry;
    }

}
