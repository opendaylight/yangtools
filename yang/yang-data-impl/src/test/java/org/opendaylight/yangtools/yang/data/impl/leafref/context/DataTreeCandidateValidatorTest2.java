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
import java.util.Set;
import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
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
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTreeCandidateValidatorTest2 {

    private static SchemaContext context;
    private static Module mainModule;
    private static QNameModule rootModuleQname;
    private static LeafRefContext rootLeafRefContext;
    public static TipProducingDataTree inMemoryDataTree;

    private static QName chips;
    private static QName chip;
    private static QName devType;
    private static QName chipDesc;

    private static QName devices;
    private static QName device;
    private static QName typeChoice;
    private static QName typeText;
    private static QName devDesc;
    private static QName sn;
    private static QName defaultIp;

    private static QName deviceTypeStr;
    private static QName deviceType;
    private static QName type;
    private static QName desc;

    private static final Logger LOG = LoggerFactory.getLogger(DataTreeCandidateValidatorTest2.class);
    private static final String NEW_LINE = System.getProperty("line.separator");

    static {
        BasicConfigurator.configure();
    }

    @BeforeClass
    public static void init() throws FileNotFoundException, ReactorException, URISyntaxException {

        initSchemaContext();
        initLeafRefContext();
        initQnames();
        initDataTree();
    }

    @Test
    public void dataTreeCanditateValidationTest2() {

        writeDevices();
    }

    private static void writeDevices() {

        final ContainerSchemaNode devicesContSchemaNode = (ContainerSchemaNode) mainModule.getDataChildByName(devices);

        final ContainerNode devicesContainer = createDevicesContainer(devicesContSchemaNode);

        final YangInstanceIdentifier devicesPath = YangInstanceIdentifier.of(devices);
        final DataTreeModification writeModification = inMemoryDataTree.takeSnapshot().newModification();
        writeModification.write(devicesPath, devicesContainer);

        writeModification.ready();
        final DataTreeCandidate writeDevicesCandidate = inMemoryDataTree.prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before writeDevices: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());

        boolean exception = false;
        try {
            LeafRefValidatation.validate(writeDevicesCandidate, rootLeafRefContext);
        } catch (final LeafRefDataValidationFailedException e) {
            LOG.debug("All validation errors:" + NEW_LINE + e.getMessage());

            assertEquals(4, e.getValidationsErrorsCount());
            exception = true;
        }

        assertTrue(exception);

        inMemoryDataTree.commit(writeDevicesCandidate);

        LOG.debug("*************************");
        LOG.debug("After write: ");
        LOG.debug("*************************");
        LOG.debug(inMemoryDataTree.toString());
    }

    private static void initQnames() {

        chips = QName.create(rootModuleQname, "chips");
        chip = QName.create(rootModuleQname, "chip");
        devType = QName.create(rootModuleQname, "dev_type");
        chipDesc = QName.create(rootModuleQname, "chip_desc");

        devices = QName.create(rootModuleQname, "devices");
        device = QName.create(rootModuleQname, "device");
        typeText = QName.create(rootModuleQname, "type_text");
        devDesc = QName.create(rootModuleQname, "dev_desc");
        sn = QName.create(rootModuleQname, "sn");
        defaultIp = QName.create(rootModuleQname, "default_ip");

        deviceTypeStr = QName.create(rootModuleQname, "device_types");
        deviceType = QName.create(rootModuleQname, "device_type");
        type = QName.create(rootModuleQname, "type");
        desc = QName.create(rootModuleQname, "desc");
    }

    private static void initSchemaContext() {
        context = YangParserTestUtils.parseYangResourceDirectory("/leafref-validation");

        final Set<Module> modules = context.getModules();
        for (final Module module : modules) {
            if (module.getName().equals("leafref-validation2")) {
                mainModule = module;
            }
        }

        rootModuleQname = mainModule.getQNameModule();
    }

    private static void initDataTree() {

        inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL);
        inMemoryDataTree.setSchemaContext(context);

        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();

        final ContainerSchemaNode chipsListContSchemaNode = (ContainerSchemaNode) mainModule.getDataChildByName(chips);
        final ContainerNode chipsContainer = createChipsContainer(chipsListContSchemaNode);
        final YangInstanceIdentifier path1 = YangInstanceIdentifier.of(chips);
        initialDataTreeModification.write(path1, chipsContainer);

        final ContainerSchemaNode devTypesListContSchemaNode = (ContainerSchemaNode) mainModule
                .getDataChildByName(deviceTypeStr);
        final ContainerNode deviceTypesContainer = createDevTypeStrContainer(devTypesListContSchemaNode);
        final YangInstanceIdentifier path2 = YangInstanceIdentifier.of(deviceTypeStr);
        initialDataTreeModification.write(path2, deviceTypesContainer);

        initialDataTreeModification.ready();
        final DataTreeCandidate writeChipsCandidate = inMemoryDataTree.prepare(initialDataTreeModification);

        inMemoryDataTree.commit(writeChipsCandidate);

        System.out.println(inMemoryDataTree.toString());
    }

    private static void initLeafRefContext() {
        rootLeafRefContext = LeafRefContext.create(context);
    }

    private static ContainerNode createDevTypeStrContainer(final ContainerSchemaNode container) {

        final ListSchemaNode devTypeListSchemaNode = (ListSchemaNode) container.getDataChildByName(deviceType);

        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> devTypeContainerBldr = Builders
                .containerBuilder(container);

        final MapNode devTypeMap = createDevTypeList(devTypeListSchemaNode);
        devTypeContainerBldr.addChild(devTypeMap);

        return devTypeContainerBldr.build();
    }

    private static MapNode createDevTypeList(final ListSchemaNode devTypeListSchemaNode) {

        final CollectionNodeBuilder<MapEntryNode, MapNode> devTypeMapBldr = Builders.mapBuilder(devTypeListSchemaNode);

        devTypeMapBldr.addChild(createDevTypeListEntry("dev_type_1", "typedesc1", devTypeListSchemaNode));
        devTypeMapBldr.addChild(createDevTypeListEntry("dev_type_2", "typedesc2", devTypeListSchemaNode));
        devTypeMapBldr.addChild(createDevTypeListEntry("dev_type_3", "typedesc3", devTypeListSchemaNode));

        return devTypeMapBldr.build();
    }

    private static MapEntryNode createDevTypeListEntry(final String typeVal, final String descVal,
            final ListSchemaNode devTypeListSchemaNode) {

        final LeafNode<String> typeLeaf = ImmutableNodes.leafNode(type, typeVal);
        final LeafNode<String> descLeaf = ImmutableNodes.leafNode(desc, descVal);

        final DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> devTypeMapEntryBldr = Builders
                .mapEntryBuilder(devTypeListSchemaNode);

        devTypeMapEntryBldr.addChild(typeLeaf);
        devTypeMapEntryBldr.addChild(descLeaf);

        return devTypeMapEntryBldr.build();
    }

    private static ContainerNode createChipsContainer(final ContainerSchemaNode container) {

        final ListSchemaNode chipsListSchemaNode = (ListSchemaNode) container.getDataChildByName(chip);

        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> chipsContainerBldr = Builders
                .containerBuilder(container);

        final MapNode chipsMap = createChipsList(chipsListSchemaNode);
        chipsContainerBldr.addChild(chipsMap);

        return chipsContainerBldr.build();
    }

    private static MapNode createChipsList(final ListSchemaNode chipsListSchemaNode) {

        final CollectionNodeBuilder<MapEntryNode, MapNode> chipsMapBldr = Builders.mapBuilder(chipsListSchemaNode);

        chipsMapBldr.addChild(createChipsListEntry("dev_type_1", "desc1", chipsListSchemaNode));
        chipsMapBldr.addChild(createChipsListEntry("dev_type_2", "desc2", chipsListSchemaNode));

        return chipsMapBldr.build();
    }

    private static MapEntryNode createChipsListEntry(final String devTypeVal, final String chipDescVal,
            final ListSchemaNode chipsListSchemaNode) {

        final LeafNode<String> devTypeLeaf = ImmutableNodes.leafNode(devType, devTypeVal);
        final LeafNode<String> chipDescLeaf = ImmutableNodes.leafNode(chipDesc, chipDescVal);

        final DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> chipsMapEntryBldr = Builders
                .mapEntryBuilder(chipsListSchemaNode);

        chipsMapEntryBldr.addChild(devTypeLeaf);
        chipsMapEntryBldr.addChild(chipDescLeaf);

        return chipsMapEntryBldr.build();
    }

    private static ContainerNode createDevicesContainer(final ContainerSchemaNode container) {

        final ListSchemaNode devicesListSchemaNode = (ListSchemaNode) container.getDataChildByName(device);

        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> devicesContainerBldr = Builders
                .containerBuilder(container);

        final MapNode devicesMap = createDeviceList(devicesListSchemaNode);
        devicesContainerBldr.addChild(devicesMap);

        return devicesContainerBldr.build();
    }

    private static MapNode createDeviceList(final ListSchemaNode deviceListSchemaNode) {

        final CollectionNodeBuilder<MapEntryNode, MapNode> devicesMapBldr = Builders.mapBuilder(deviceListSchemaNode);

        devicesMapBldr.addChild(createDeviceListEntry("dev_type_1", "typedesc1", 123456, "192.168.0.1",
                deviceListSchemaNode));
        devicesMapBldr.addChild(createDeviceListEntry("dev_type_2", "typedesc2", 123457, "192.168.0.1",
                deviceListSchemaNode));
        devicesMapBldr.addChild(createDeviceListEntry("dev_type_2", "typedesc3", 123457, "192.168.0.1",
                deviceListSchemaNode));
        devicesMapBldr.addChild(createDeviceListEntry("dev_type_1", "typedesc2", 123458, "192.168.0.1",
                deviceListSchemaNode));
        devicesMapBldr
                .addChild(createDeviceListEntry("unknown", "unknown", 123457, "192.168.0.1", deviceListSchemaNode));

        return devicesMapBldr.build();
    }

    private static MapEntryNode createDeviceListEntry(final String typeTextVal, final String descVal, final int snVal,
            final String defaultIpVal, final ListSchemaNode devicesListSchemaNode) {

        final LeafNode<String> typeTextLeaf = ImmutableNodes.leafNode(typeText, typeTextVal);
        final LeafNode<String> descLeaf = ImmutableNodes.leafNode(devDesc, descVal);
        final LeafNode<Integer> snValLeaf = ImmutableNodes.leafNode(sn, snVal);
        final LeafNode<String> defaultIpLeaf = ImmutableNodes.leafNode(defaultIp, defaultIpVal);

        final DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> devicesMapEntryBldr = Builders
                .mapEntryBuilder(devicesListSchemaNode);

        devicesMapEntryBldr.addChild(typeTextLeaf);
        devicesMapEntryBldr.addChild(descLeaf);
        devicesMapEntryBldr.addChild(snValLeaf);
        devicesMapEntryBldr.addChild(defaultIpLeaf);

        return devicesMapEntryBldr.build();
    }
}