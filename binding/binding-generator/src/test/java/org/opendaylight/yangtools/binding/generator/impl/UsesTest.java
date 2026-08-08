/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.containsInterface;
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.containsMethods;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.AugmentationArchetype;
import org.opendaylight.yangtools.binding.model.CaseObjectArchetype;
import org.opendaylight.yangtools.binding.model.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.ItemObjectArchetype;
import org.opendaylight.yangtools.binding.model.NotificationArchetype;
import org.opendaylight.yangtools.binding.model.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.RpcOutputArchetype;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class UsesTest {
    @Test
    void usesInGroupingDependenciesTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/uses-of-grouping/uses-of-grouping-dependencies.yang"));
        GroupingArchetype groupingU = null;
        GroupingArchetype groupingX = null;
        GroupingArchetype groupingV = null;

        int groupingUCounter = 0;
        int groupingXCounter = 0;
        int groupingVCounter = 0;

        for (var genType : genTypes) {
            if (genType instanceof GroupingArchetype archetype) {
                switch (archetype.simpleName()) {
                    case "GroupingU" -> {
                        groupingU = archetype;
                        groupingUCounter++;
                    }
                    case "GroupingV" -> {
                        groupingV = archetype;
                        groupingVCounter++;
                    }
                    case "GroupingX" -> {
                        groupingX = archetype;
                        groupingXCounter++;
                    }
                    default -> {
                        // no-op
                    }
                }
            }
        }

        assertNotNull(groupingU, "Generated type for grouping-U wasn't generated.");
        assertEquals(1, groupingUCounter, "GroupingU interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.dependencies.rev130718", groupingU.packageName(),
            "GroupingU is in wrong package.");

        assertNotNull(groupingV, "Generated type for grouping-V wasn't generated.");
        assertEquals(1, groupingVCounter, "GroupingV interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.dependencies.rev130718", groupingV.packageName(),
            "GroupingV is in wrong package.");

        assertNotNull(groupingX, "Generated type for grouping-X wasn't generated.");
        assertEquals(1, groupingXCounter, "GroupingX interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.dependencies.rev130718", groupingX.packageName(),
            "GroupingX is in wrong package.");

        containsInterface("GroupingV", groupingU);
        containsInterface("GroupingX", groupingU);
        containsInterface("GroupingZ", groupingV);
        containsInterface("GroupingZZ", groupingV);
        containsInterface("GroupingY", groupingX);
    }

    @Test
    void usesInCaseTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-case.yang"));

        GroupingArchetype groupingCaseTest = null;
        int groupingCaseTestCounter = 0;
        CaseObjectArchetype caseC = null;
        int caseCCounter = 0;
        for (var genType : genTypes) {
            switch (genType.simpleName()) {
                case "C" -> {
                    caseC = assertInstanceOf(CaseObjectArchetype.class, genType);
                    caseCCounter++;
                }
                case "GroupingCaseTest" -> {
                    groupingCaseTest = assertInstanceOf(GroupingArchetype.class, genType);
                    groupingCaseTestCounter++;
                }
                default -> {
                    // no-op
                }
            }
        }

        assertNotNull(caseC, "Generated type for case C wasn't generated.");
        assertEquals(1, caseCCounter, "Case C interface generated more than one time.");
        assertEquals(
            "org.opendaylight.yang.gen.v1.urn.grouping.uses._case.rev130718.container.with.choicetest.choice.test",
            caseC.packageName(),
            "Case C is in wrong package.");

        assertNotNull(groupingCaseTest, "Generated type for grouping-case-test wasn't generated.");
        assertEquals(1, groupingCaseTestCounter, "GroupingCaseTest interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses._case.rev130718",
            groupingCaseTest.packageName(), "GroupingCaseTest is in wrong package.");

        containsInterface("GroupingCaseTest", caseC);
        assertEquals(List.of(), caseC.getters());

        assertEquals(1, groupingCaseTest.getters().size(), "Number of method in GroupingCaseTest is incorrect");
        containsMethods(groupingCaseTest.getters(), new NameTypePattern("getLeafGroupingCaseTest1", "String"));
    }

    @Test
    void usesInContainerTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-container.yang"));

        int containerTestCount = 0;
        int groupingContainerTestCounter = 0;
        ContainerObjectArchetype containerTest = null;
        GroupingArchetype groupingContainerTest = null;

        for (var genType : genTypes) {
            switch (genType.simpleName()) {
                case "GroupingContainerTest" -> {
                    groupingContainerTest = assertInstanceOf(GroupingArchetype.class, genType);
                    groupingContainerTestCounter++;
                }
                case "ContainerTest" -> {
                    containerTest = assertInstanceOf(ContainerObjectArchetype.class, genType);
                    containerTestCount++;
                }
                default -> {
                    // no-op
                }
            }
        }

        assertNotNull(groupingContainerTest, "Generated type for grouping-container-test wasn't generated");
        assertEquals(1, groupingContainerTestCounter,
            "GroupingContainerTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.container.rev130718",
            groupingContainerTest.packageName(), "GroupingContainerTest isn't in correct package");

        assertNotNull(containerTest, "Generated type for container-test wasn't generated");
        assertEquals(1, containerTestCount, "ContainerTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.container.rev130718",
            containerTest.packageName(), "ContainerTest isn't in correct package");

        containsInterface("GroupingContainerTest", containerTest);

        assertEquals(2, groupingContainerTest.getters().size());
        containsMethods(groupingContainerTest.getters(),
            new NameTypePattern("getLeafGroupingContainerTest1", "String"),
            new NameTypePattern("getLeafGroupingContainerTest2", "Uint8"));

        assertEquals(1, containerTest.getters().size());
        containsMethods(containerTest.getters(), new NameTypePattern("getContainerLeafTest", "String"));
    }

    @Test
    void usesInGroupingTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-grouping.yang"));

        int groupingTestCount = 0;
        int groupingGroupingTestCounter = 0;
        GroupingArchetype groupingTest = null;
        GroupingArchetype groupingGroupingTest = null;

        for (var genType : genTypes) {
            if (genType instanceof GroupingArchetype archetype) {
                if (archetype.simpleName().equals("GroupingGroupingTest")) {
                    groupingGroupingTest = archetype;
                    groupingGroupingTestCounter++;
                } else if (archetype.simpleName().equals("GroupingTest")) {
                    groupingTest = archetype;
                    groupingTestCount++;
                }
            }
        }

        assertNotNull(groupingGroupingTest, "Generated type for grouping-grouping-test wasn't generated");
        assertEquals(1, groupingGroupingTestCounter, "GroupingGroupingTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.grouping.rev130718",
            groupingGroupingTest.packageName(), "GroupingGroupingTest isn't in correct package");

        assertNotNull(groupingTest, "Generated type for grouping-test wasn't generated");
        assertEquals(1, groupingTestCount, "GroupingTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.grouping.rev130718", groupingTest.packageName(),
            "GroupingTest isn't in correct package");

        containsInterface("GroupingGroupingTest", groupingTest);

        assertEquals(1, groupingGroupingTest.getters().size(),
            "Number of method in GroupingGroupingTest is incorrect");
        containsMethods(groupingGroupingTest.getters(), new NameTypePattern("getLeafGroupingGrouping", "String"));

        assertEquals(1, groupingTest.getters().size(), "Number of method in GroupingTest is incorrect");
        containsMethods(groupingTest.getters(), new NameTypePattern("getLeafGroupingTest", "Byte"));
    }

    @Test
    void usesInListTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-list.yang"));

        int listTestCounter = 0;
        int groupingListTestCounter = 0;
        int containerGroupingListTestCounter = 0;
        int listGroupingListTestCounter = 0;
        ItemObjectArchetype listTest = null;
        GroupingArchetype groupingListTest = null;
        ContainerObjectArchetype containerGroupingListTest = null;
        ItemObjectArchetype listGroupingListTest = null;

        for (var genType : genTypes) {
            switch (genType.simpleName()) {
                case "GroupingListTest" -> {
                    groupingListTest = assertInstanceOf(GroupingArchetype.class, genType);
                    groupingListTestCounter++;
                }
                case "ListTest" -> {
                    listTest = assertInstanceOf(ItemObjectArchetype.class, genType);
                    listTestCounter++;
                }
                case "ContainerGroupingListTest" -> {
                    containerGroupingListTest = assertInstanceOf(ContainerObjectArchetype.class, genType);
                    containerGroupingListTestCounter++;
                }
                case "ListGroupingListTest" -> {
                    listGroupingListTest = assertInstanceOf(ItemObjectArchetype.class, genType);
                    listGroupingListTestCounter++;
                }
                default -> {
                    // no-op
                }
            }
        }

        assertNotNull(groupingListTest, "Generated type for grouping-list-test wasn't generated");
        assertEquals(1, groupingListTestCounter, "GroupingListTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.list.rev130718", groupingListTest.packageName());

        assertNotNull(listTest, "Generated type for list-test wasn't generated");
        assertEquals(1, listTestCounter, "ListTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.list.rev130718", listTest.packageName(),
            "ListTest isn't in correct package");

        assertNotNull(containerGroupingListTest, "Generated type for container-grouping-list-test wasn't generated");
        assertEquals(1, containerGroupingListTestCounter,
            "ContainerGroupingListTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.list.rev130718.grouping.list.test",
            containerGroupingListTest.packageName(), "ContainerGroupingListTest isn't in correct package");

        assertNotNull(listGroupingListTest, "Generated type for list-grouping-list-test wasn't generated");
        assertEquals(1, listGroupingListTestCounter, "ListGroupingListTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.list.rev130718.grouping.list.test",
            listGroupingListTest.packageName(), "ListGroupingListTest isn't in correct package");

        containsInterface("GroupingListTest", listTest);

        assertEquals(4, groupingListTest.getters().size());
        containsMethods(groupingListTest.getters(),
            new NameTypePattern("getContainerGroupingListTest", "ContainerGroupingListTest"),
            new NameTypePattern("getLeafGroupingListTest", "String"),
            new NameTypePattern("getLeaffllistGroupingListTest", "Set<String>"),
            new NameTypePattern("getListGroupingListTest", "List<ListGroupingListTest>"));

        assertEquals(1, listTest.getters().size());
        containsMethods(listTest.getters(), new NameTypePattern("getListLeafTest", "String"));

        assertEquals(1, containerGroupingListTest.getters().size());
        containsMethods(containerGroupingListTest.getters(),
            new NameTypePattern("getLeafContainerGroupingListTest", "Uint8"));

        assertEquals(1, listGroupingListTest.getters().size());
        containsMethods(listGroupingListTest.getters(),
            new NameTypePattern("getLeafListGroupingListTest", "Integer"));
    }

    @Test
    void usesInModulTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-modul.yang"));

        int groupingModulTestCounter = 0;
        int groupingUsesModulDataCounter = 0;
        GroupingArchetype groupingModulTest = null;
        DataRootArchetype groupingUsesModulData = null;

        for (var genType : genTypes) {
            switch (genType.simpleName()) {
                case "GroupingModulTest" -> {
                    groupingModulTest = assertInstanceOf(GroupingArchetype.class, genType);
                    groupingModulTestCounter++;
                }
                case "GroupingUsesModulData" -> {
                    groupingUsesModulData = assertInstanceOf(DataRootArchetype.class, genType);
                    groupingUsesModulDataCounter++;
                }
                default -> {
                    // no-op
                }
            }
        }

        assertNotNull(groupingModulTest, "Generated type for grouping-list-test wasn't generated");
        assertEquals(1, groupingModulTestCounter, "GroupingModulTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.modul.rev130718",
            groupingModulTest.packageName(), "GroupingModulTest isn't in correct package");

        assertNotNull(groupingUsesModulData, "Generated type for modul wasn't generated");
        assertEquals(1, groupingUsesModulDataCounter,
            "GroupingUsesModulData interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.modul.rev130718",
            groupingUsesModulData.packageName(), "GroupingUsesModulData isn't in correct package");

        containsInterface("GroupingModulTest", groupingUsesModulData);

        assertEquals(List.of(), groupingUsesModulData.getters());
        assertEquals(2, groupingModulTest.getters().size(), "Number of method in GroupingModulTest is incorrect");

        containsMethods(groupingModulTest.getters(),
            new NameTypePattern("getLeafGroupingModulTest", "String"),
            new NameTypePattern("getLeafGroupingModulTest2", "Uint8"));
    }

    @Test
    void usesInRpcTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-rpc.yang"));

        int rpcTestInputCounter = 0;
        int rpcTestOutputCounter = 0;
        int groupingRpcInputTestCounter = 0;
        int groupingRpcOutputTestCounter = 0;
        int containerGroupingRpcInputTestCounter = 0;
        RpcInputArchetype rpcTestInput = null;
        RpcOutputArchetype rpcTestOutput = null;
        GroupingArchetype groupingRpcInputTest = null;
        GroupingArchetype groupingRpcOutputTest = null;
        ContainerObjectArchetype containerGroupingRpcInputTest = null;

        for (var genType : genTypes) {
            switch (genType.simpleName()) {
                case "RpcTestInput" -> {
                    rpcTestInput = assertInstanceOf(RpcInputArchetype.class, genType);
                    rpcTestInputCounter++;
                }
                case "RpcTestOutput" -> {
                    rpcTestOutput = assertInstanceOf(RpcOutputArchetype.class, genType);
                    rpcTestOutputCounter++;
                }
                case "GroupingRpcInputTest" -> {
                    groupingRpcInputTest = assertInstanceOf(GroupingArchetype.class, genType);
                    groupingRpcInputTestCounter++;
                }
                case "GroupingRpcOutputTest" -> {
                    groupingRpcOutputTest = assertInstanceOf(GroupingArchetype.class, genType);
                    groupingRpcOutputTestCounter++;
                }
                case "ContainerGroupingRpcInputTest" -> {
                    containerGroupingRpcInputTest = assertInstanceOf(ContainerObjectArchetype.class, genType);
                    containerGroupingRpcInputTestCounter++;
                }
                default -> {
                    // no-op
                }
            }
        }

        assertNotNull(rpcTestInput, "Generated type for RPC test input was not generated");
        assertEquals(1, rpcTestInputCounter, "RpcTestInput interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718", rpcTestInput.packageName(),
            "RpcTestInput is not in correct package");

        assertNotNull(rpcTestOutput, "Generated type for RPC test output was not generated");
        assertEquals(1, rpcTestOutputCounter, "RpcTestOutput interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718", rpcTestOutput.packageName(),
            "RpcTestOutput is not in correct package");

        assertNotNull(groupingRpcInputTest, "Generated type for grouping-rpc-input-test was not generated");
        assertEquals(1, groupingRpcInputTestCounter, "RpcTestOutput interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718",
            groupingRpcInputTest.packageName(), "GroupingRpcInputTest isn't in correct package");

        assertNotNull(groupingRpcOutputTest, "Generated type for grouping-rpc-output-test was not generated");
        assertEquals(1, groupingRpcOutputTestCounter, "RpcTestOutput interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718",
            groupingRpcOutputTest.packageName(), "GroupingRpcOutputTest isn't in correct package");

        assertNotNull(containerGroupingRpcInputTest,
            "Generated type for container-grouping-rpc-input-test wasn't generated");
        assertEquals(1, containerGroupingRpcInputTestCounter,
            "ContainerGroupingRpcInputTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718.grouping.rpc.input.test",
            containerGroupingRpcInputTest.packageName(), "ContainerGroupingRpcInputTest is not in correct package");

        containsInterface("GroupingRpcInputTest", rpcTestInput);
        containsInterface("GroupingRpcOutputTest", rpcTestOutput);

        assertEquals(List.of(), rpcTestInput.getters());
        assertEquals(List.of(), rpcTestOutput.getters());
        assertEquals(2, groupingRpcInputTest.getters().size(),
            "Number of method in GroupingRpcInputTest is incorrect");
        assertEquals(1, groupingRpcOutputTest.getters().size(),
            "Number of method in GroupingRpcOutputTest is incorrect");
        assertEquals(1, containerGroupingRpcInputTest.getters().size());

        containsMethods(groupingRpcInputTest.getters(),
            new NameTypePattern("getContainerGroupingRpcInputTest", "ContainerGroupingRpcInputTest"),
            new NameTypePattern("getLeaflistGroupingRpcInputTest", "List<Uint8>"));
        containsMethods(groupingRpcOutputTest.getters(), new NameTypePattern("getLeafGroupingRpcOutputTest", "Byte"));
        containsMethods(containerGroupingRpcInputTest.getters(),
            new NameTypePattern("getLeafContainerGroupingRpcInputTest", "String"));
    }

    @Test
    void usesInAugmentTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/uses-of-grouping/uses-of-grouping-augment.yang"));

        AugmentationArchetype containerAugment1 = null;
        GroupingArchetype groupingAugmentTest = null;
        int containerAugment1Counter = 0;
        int groupingAugmentTestCounter = 0;

        for (var genType : genTypes) {
            switch (genType.simpleName()) {
                case "ContainerAugment1" -> {
                    containerAugment1 = assertInstanceOf(AugmentationArchetype.class, genType);
                    containerAugment1Counter++;
                }
                case "GroupingAugmentTest" -> {
                    groupingAugmentTest = assertInstanceOf(GroupingArchetype.class, genType);
                    groupingAugmentTestCounter++;
                }
                default -> {
                    // no-op
                }
            }
        }

        assertNotNull(containerAugment1, "Generated type for augment /container-augment wasn't generated.");
        assertEquals(1, containerAugment1Counter, "ContainerAugment1 interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.augment.rev130718",
            containerAugment1.packageName(), "ContainerAugment1 is in wrong package.");

        assertNotNull(groupingAugmentTest, "Generated type for grouping-augment-test wasn't generated.");
        assertEquals(1, groupingAugmentTestCounter, "GroupingAugmentTest interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.augment.rev130718",
            groupingAugmentTest.packageName(), "groupingAugmentTest is in wrong package.");

        containsInterface("GroupingAugmentTest", containerAugment1);

        assertEquals(List.of(), containerAugment1.getters());
        assertEquals(List.of(), containerAugment1.getters());
        assertEquals(1, groupingAugmentTest.getters().size(), "Number of method in GroupingCaseTest is incorrect");

        containsMethods(groupingAugmentTest.getters(), new NameTypePattern("getLeafGroupingAugmentTest", "String"));
    }

    @Test
    void usesInNotification() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/uses-of-grouping/uses-of-grouping-notification.yang"));

        NotificationArchetype notificationTest = null;
        GroupingArchetype groupingNotificationTest = null;
        ContainerObjectArchetype containerGroupingNotificationTest = null;
        int notificationTestCounter = 0;
        int groupingNotificationTestCounter = 0;
        int containerGroupingNotificationTestCounter = 0;

        for (var type : genTypes) {
            switch (type.simpleName()) {
                case "NotificationTest" -> {
                    notificationTest = assertInstanceOf(NotificationArchetype.class, type);
                    notificationTestCounter++;
                }
                case "GroupingNotificationTest" -> {
                    groupingNotificationTest = assertInstanceOf(GroupingArchetype.class, type);
                    groupingNotificationTestCounter++;
                }
                case "ContainerGroupingNotificationTest" -> {
                    containerGroupingNotificationTest = assertInstanceOf(ContainerObjectArchetype.class, type);
                    containerGroupingNotificationTestCounter++;
                }
                default -> {
                    // no-op
                }
            }
        }

        assertNotNull(notificationTest, "Generated type for notification-test wasn't generated.");
        assertEquals(1, notificationTestCounter, "NotificationTest interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.notification.rev130718",
            notificationTest.packageName(), "NotificationTest is in wrong package.");

        assertNotNull(groupingNotificationTest, "Generated type for grouping-notification-test wasn't generated.");
        assertEquals(1, groupingNotificationTestCounter,
            "GroupingNotificationTest interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.notification.rev130718",
            groupingNotificationTest.packageName(), "groupingNotificationTest is in wrong package.");

        assertNotNull(containerGroupingNotificationTest,
            "Generated type for container-grouping-notification-test wasn't generated.");
        assertEquals(1, containerGroupingNotificationTestCounter,
            "ContainerGroupingNotificationTest interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.notification.rev130718.grouping.notification.test",
            containerGroupingNotificationTest.packageName(),
            "ContainerGroupingNotificationTest is in wrong package.");

        containsInterface("GroupingNotificationTest", notificationTest);

        assertEquals(1, notificationTest.getters().size());
        containsMethods(notificationTest.getters(),
            new NameTypePattern("getLeafNotificationTest",  "String"));

        assertEquals(2, groupingNotificationTest.getters().size(),
            "Number of method in GroupingNotificationTest is incorrect");
        containsMethods(groupingNotificationTest.getters(),
            new NameTypePattern("getContainerGroupingNotificationTest", "ContainerGroupingNotificationTest"),
            new NameTypePattern("getLeaffllistGroupingNotificationTest", "Set<String>"));

        assertEquals(1, containerGroupingNotificationTest.getters().size());
        containsMethods(containerGroupingNotificationTest.getters(),
            new NameTypePattern("getLeafContainerGroupingNotificationTest", "Uint32"));
    }
}
