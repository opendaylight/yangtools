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

import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTreeCandidateValidatorTest2 {

    private static EffectiveModelContext context;
    private static Module mainModule;
    private static QNameModule rootModuleQname;
    private static LeafRefContext rootLeafRefContext;
    public static DataTree inMemoryDataTree;

    private static QName chips;
    private static QName chip;
    private static QName devType;
    private static QName chipDesc;

    private static QName devices;
    private static QName device;
    private static QName typeText;
    private static QName devDesc;
    private static QName sn;
    private static QName defaultIp;

    private static QName deviceTypeStr;
    private static QName deviceType;
    private static QName type;
    private static QName desc;

    private static final Logger LOG = LoggerFactory.getLogger(DataTreeCandidateValidatorTest2.class);

    @BeforeAll
    static void init() throws DataValidationFailedException {
        context = YangParserTestUtils.parseYangResourceDirectory("/leafref-validation");

        for (final var module : context.getModules()) {
            if (module.getName().equals("leafref-validation2")) {
                mainModule = module;
            }
        }

        rootModuleQname = mainModule.getQNameModule();
        rootLeafRefContext = LeafRefContext.create(context);

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

        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, context);
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(YangInstanceIdentifier.of(chips), ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(chips))
            .addChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(chip))
                .addChild(createChipsListEntry("dev_type_1", "desc1"))
                .addChild(createChipsListEntry("dev_type_2", "desc2"))
                .build())
            .build());

        initialDataTreeModification.write(YangInstanceIdentifier.of(deviceTypeStr), ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(deviceTypeStr))
            .addChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(deviceType))
                .addChild(createDevTypeListEntry("dev_type_1", "typedesc1"))
                .addChild(createDevTypeListEntry("dev_type_2", "typedesc2"))
                .addChild(createDevTypeListEntry("dev_type_3", "typedesc3"))
                .build())
            .build());

        initialDataTreeModification.ready();
        final var writeChipsCandidate = inMemoryDataTree.prepare(initialDataTreeModification);

        inMemoryDataTree.commit(writeChipsCandidate);
        LOG.debug("{}", inMemoryDataTree);
    }

    @AfterAll
    static void cleanup() {
        inMemoryDataTree = null;
        rootLeafRefContext = null;
        mainModule = null;
        context = null;
    }

    @Test
    void dataTreeCanditateValidationTest2() throws DataValidationFailedException {
        writeDevices();
    }

    private static void writeDevices() throws DataValidationFailedException {
        final var writeModification = inMemoryDataTree.takeSnapshot().newModification();
        writeModification.write(YangInstanceIdentifier.of(devices), ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(devices))
            .addChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(device))
                .addChild(createDeviceListEntry("dev_type_1", "typedesc1", 123456, "192.168.0.1"))
                .addChild(createDeviceListEntry("dev_type_2", "typedesc2", 123457, "192.168.0.1"))
                .addChild(createDeviceListEntry("dev_type_2", "typedesc3", 123457, "192.168.0.1"))
                .addChild(createDeviceListEntry("dev_type_1", "typedesc2", 123458, "192.168.0.1"))
                .addChild(createDeviceListEntry("unknown", "unknown", 123457, "192.168.0.1"))
                .build())
            .build());

        writeModification.ready();
        final var writeDevicesCandidate = inMemoryDataTree.prepare(writeModification);

        LOG.debug("*************************");
        LOG.debug("Before writeDevices: ");
        LOG.debug("*************************");
        LOG.debug("{}", inMemoryDataTree);

        final var ex = assertThrows(LeafRefDataValidationFailedException.class,
            () -> LeafRefValidation.validate(writeDevicesCandidate, rootLeafRefContext));
        assertEquals(4, ex.getValidationsErrorsCount());

        inMemoryDataTree.commit(writeDevicesCandidate);

        LOG.debug("*************************");
        LOG.debug("After write: ");
        LOG.debug("*************************");
        LOG.debug("{}", inMemoryDataTree);
    }

    private static MapEntryNode createDevTypeListEntry(final String typeVal, final String descVal) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(deviceType, type, typeVal))
            .addChild(ImmutableNodes.leafNode(type, typeVal))
            .addChild(ImmutableNodes.leafNode(desc, descVal))
            .build();
    }

    private static MapEntryNode createChipsListEntry(final String devTypeVal, final String chipDescVal) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(chip, devType, devTypeVal))
            .addChild(ImmutableNodes.leafNode(devType, devTypeVal))
            .addChild(ImmutableNodes.leafNode(chipDesc, chipDescVal))
            .build();
    }

    private static MapEntryNode createDeviceListEntry(final String typeTextVal, final String descVal, final int snVal,
            final String defaultIpVal) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(device, Map.of(typeText, typeTextVal, sn, snVal)))
            .addChild(ImmutableNodes.leafNode(typeText, typeTextVal))
            .addChild(ImmutableNodes.leafNode(devDesc, descVal))
            .addChild(ImmutableNodes.leafNode(sn, snVal))
            .addChild(ImmutableNodes.leafNode(defaultIp, defaultIpVal))
            .build();
    }
}
