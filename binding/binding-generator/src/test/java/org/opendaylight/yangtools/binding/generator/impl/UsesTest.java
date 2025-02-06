/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.containsInterface;
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.containsMethods;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class UsesTest {
    @Test
    void usesInGroupingDependenciesTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/uses-of-grouping/uses-of-grouping-dependencies.yang"));
        GeneratedType groupingU = null;
        GeneratedType groupingX = null;
        GeneratedType groupingV = null;

        int groupingUCounter = 0;
        int groupingXCounter = 0;
        int groupingVCounter = 0;

        for (var genType : genTypes) {
            if (!(genType instanceof GeneratedTransferObject)) {
                if (genType.getName().equals("GroupingU")) {
                    groupingU = genType;
                    groupingUCounter++;
                } else if (genType.getName().equals("GroupingV")) {
                    groupingV = genType;
                    groupingVCounter++;
                } else if (genType.getName().equals("GroupingX")) {
                    groupingX = genType;
                    groupingXCounter++;
                }
            }
        }

        assertNotNull(groupingU, "Generated type for grouping-U wasn't generated.");
        assertEquals(1, groupingUCounter, "GroupingU interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.dependencies.rev130718", groupingU.getPackageName(),
            "GroupingU is in wrong package.");

        assertNotNull(groupingV, "Generated type for grouping-V wasn't generated.");
        assertEquals(1, groupingVCounter, "GroupingV interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.dependencies.rev130718", groupingV.getPackageName(),
            "GroupingV is in wrong package.");

        assertNotNull(groupingX, "Generated type for grouping-X wasn't generated.");
        assertEquals(1, groupingXCounter, "GroupingX interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.dependencies.rev130718", groupingX.getPackageName(),
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

        GeneratedType groupingCaseTest = null;
        int groupingCaseTestCounter = 0;
        GeneratedType caseC = null;
        int caseCCounter = 0;
        for (var genType : genTypes) {
            if (!(genType instanceof GeneratedTransferObject)) {
                if (genType.getName().equals("C")) {
                    caseC = genType;
                    caseCCounter++;
                } else if (genType.getName().equals("GroupingCaseTest")) {
                    groupingCaseTest = genType;
                    groupingCaseTestCounter++;
                }
            }
        }

        assertNotNull(caseC, "Generated type for case C wasn't generated.");
        assertEquals(1, caseCCounter, "Case C interface generated more than one time.");
        assertEquals(
            "org.opendaylight.yang.gen.v1.urn.grouping.uses._case.rev130718.container.with.choicetest.choice.test",
            caseC.getPackageName(),
            "Case C is in wrong package.");

        assertNotNull(groupingCaseTest, "Generated type for grouping-case-test wasn't generated.");
        assertEquals(1, groupingCaseTestCounter, "GroupingCaseTest interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses._case.rev130718",
            groupingCaseTest.getPackageName(), "GroupingCaseTest is in wrong package.");

        containsInterface("GroupingCaseTest", caseC);
        // FIXME: split this into getter/default/static asserts
        assertEquals(4, caseC.getMethodDefinitions().size());

        assertEquals(2, groupingCaseTest.getMethodDefinitions().size(),
            "Number of method in GroupingCaseTest is incorrect");
        containsMethods(groupingCaseTest.getMethodDefinitions(),
            new NameTypePattern("getLeafGroupingCaseTest1", "String"),
            new NameTypePattern("requireLeafGroupingCaseTest1", "String"));
    }

    @Test
    void usesInContainerTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-container.yang"));

        int containerTestCount = 0;
        int groupingContainerTestCounter = 0;
        GeneratedType containerTest = null;
        GeneratedType groupingContainerTest = null;

        for (var genType : genTypes) {
            if (!(genType instanceof GeneratedTransferObject)) {
                if (genType.getName().equals("GroupingContainerTest")) {
                    groupingContainerTest = genType;
                    groupingContainerTestCounter++;
                } else if (genType.getName().equals("ContainerTest")) {
                    containerTest = genType;
                    containerTestCount++;
                }
            }
        }

        assertNotNull(groupingContainerTest, "Generated type for grouping-container-test wasn't generated");
        assertEquals(1, groupingContainerTestCounter,
            "GroupingContainerTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.container.rev130718",
            groupingContainerTest.getPackageName(), "GroupingContainerTest isn't in correct package");

        assertNotNull(containerTest, "Generated type for container-test wasn't generated");
        assertEquals(1, containerTestCount, "ContainerTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.container.rev130718",
            containerTest.getPackageName(), "ContainerTest isn't in correct package");

        containsInterface("GroupingContainerTest", containerTest);

        assertEquals(4, groupingContainerTest.getMethodDefinitions().size(),
            "Number of method in GroupingContainerTestis incorrect");
        // FIXME: split this into getter/default/static asserts
        assertEquals(6, containerTest.getMethodDefinitions().size());

        containsMethods(groupingContainerTest.getMethodDefinitions(),
            new NameTypePattern("getLeafGroupingContainerTest1", "String"),
            new NameTypePattern("requireLeafGroupingContainerTest1", "String"),
            new NameTypePattern("getLeafGroupingContainerTest2", "Uint8"),
            new NameTypePattern("requireLeafGroupingContainerTest2", "Uint8"));

        containsMethods(containerTest.getMethodDefinitions(),
            new NameTypePattern("getContainerLeafTest", "String"),
            new NameTypePattern("requireContainerLeafTest", "String"));
    }

    @Test
    void usesInGroupingTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-grouping.yang"));

        int groupingTestCount = 0;
        int groupingGroupingTestCounter = 0;
        GeneratedType groupingTest = null;
        GeneratedType groupingGroupingTest = null;

        for (var genType : genTypes) {
            if (!(genType instanceof GeneratedTransferObject)) {
                if (genType.getName().equals("GroupingGroupingTest")) {
                    groupingGroupingTest = genType;
                    groupingGroupingTestCounter++;
                } else if (genType.getName().equals("GroupingTest")) {
                    groupingTest = genType;
                    groupingTestCount++;
                }
            }
        }

        assertNotNull(groupingGroupingTest, "Generated type for grouping-grouping-test wasn't generated");
        assertEquals(1, groupingGroupingTestCounter, "GroupingGroupingTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.grouping.rev130718",
            groupingGroupingTest.getPackageName(), "GroupingGroupingTest isn't in correct package");

        assertNotNull(groupingTest, "Generated type for grouping-test wasn't generated");
        assertEquals(1, groupingTestCount, "GroupingTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.grouping.rev130718", groupingTest.getPackageName(),
            "GroupingTest isn't in correct package");

        containsInterface("GroupingGroupingTest", groupingTest);

        assertEquals(2, groupingGroupingTest.getMethodDefinitions().size(),
            "Number of method in GroupingGroupingTest is incorrect");
        assertEquals(2, groupingTest.getMethodDefinitions().size(), "Number of method in GroupingTest is incorrect");

        containsMethods(groupingGroupingTest.getMethodDefinitions(),
            new NameTypePattern("getLeafGroupingGrouping", "String"),
            new NameTypePattern("requireLeafGroupingGrouping", "String"));

        containsMethods(groupingTest.getMethodDefinitions(),
            new NameTypePattern("getLeafGroupingTest", "Byte"),
            new NameTypePattern("requireLeafGroupingTest", "Byte"));
    }

    @Test
    void usesInListTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-list.yang"));

        int listTestCounter = 0;
        int groupingListTestCounter = 0;
        int containerGroupingListTestCounter = 0;
        int listGroupingListTestCounter = 0;
        GeneratedType listTest = null;
        GeneratedType groupingListTest = null;
        GeneratedType containerGroupingListTest = null;
        GeneratedType listGroupingListTest = null;

        for (var genType : genTypes) {
            if (!(genType instanceof GeneratedTransferObject)) {
                if (genType.getName().equals("GroupingListTest")) {
                    groupingListTest = genType;
                    groupingListTestCounter++;
                } else if (genType.getName().equals("ListTest")) {
                    listTest = genType;
                    listTestCounter++;
                } else if (genType.getName().equals("ContainerGroupingListTest")) {
                    containerGroupingListTest = genType;
                    containerGroupingListTestCounter++;
                } else if (genType.getName().equals("ListGroupingListTest")) {
                    listGroupingListTest = genType;
                    listGroupingListTestCounter++;
                }
            }
        }

        assertNotNull(groupingListTest, "Generated type for grouping-list-test wasn't generated");
        assertEquals(1, groupingListTestCounter, "GroupingListTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.list.rev130718", groupingListTest.getPackageName(),
            "GroupingListTest isn't in correct package");

        assertNotNull(listTest, "Generated type for list-test wasn't generated");
        assertEquals(1, listTestCounter, "ListTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.list.rev130718", listTest.getPackageName(),
            "ListTest isn't in correct package");

        assertNotNull(containerGroupingListTest, "Generated type for container-grouping-list-test wasn't generated");
        assertEquals(1, containerGroupingListTestCounter,
            "ContainerGroupingListTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.list.rev130718.grouping.list.test",
            containerGroupingListTest.getPackageName(), "ContainerGroupingListTest isn't in correct package");

        assertNotNull(listGroupingListTest, "Generated type for list-grouping-list-test wasn't generated");
        assertEquals(1, listGroupingListTestCounter, "ListGroupingListTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.list.rev130718.grouping.list.test",
            listGroupingListTest.getPackageName(), "ListGroupingListTest isn't in correct package");

        containsInterface("GroupingListTest", listTest);

        assertEquals(8, groupingListTest.getMethodDefinitions().size(),
            "Number of method in GroupingListTest is incorrect");
        // FIXME: split this into getter/default/static asserts
        assertEquals(6, listTest.getMethodDefinitions().size());
        // FIXME: split this into getter/default/static asserts
        assertEquals(6, containerGroupingListTest.getMethodDefinitions().size());
        // FIXME: split this into getter/default/static asserts
        assertEquals(6, listGroupingListTest.getMethodDefinitions().size());

        containsMethods(groupingListTest.getMethodDefinitions(),
            new NameTypePattern("getContainerGroupingListTest", "ContainerGroupingListTest"),
            new NameTypePattern("getLeafGroupingListTest", "String"),
            new NameTypePattern("requireLeafGroupingListTest", "String"),
            new NameTypePattern("getLeaffllistGroupingListTest", "Set<String>"),
            new NameTypePattern("requireLeaffllistGroupingListTest", "Set<String>"),
            new NameTypePattern("getListGroupingListTest", "List<ListGroupingListTest>"));
        containsMethods(listTest.getMethodDefinitions(), new NameTypePattern("getListLeafTest", "String"));
        containsMethods(containerGroupingListTest.getMethodDefinitions(), new NameTypePattern(
                "getLeafContainerGroupingListTest", "Uint8"));
        containsMethods(listGroupingListTest.getMethodDefinitions(), new NameTypePattern("getLeafListGroupingListTest",
                "Integer"));
    }

    @Test
    void usesInModulTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-modul.yang"));

        int groupingModulTestCounter = 0;
        int groupingUsesModulDataCounter = 0;
        GeneratedType groupingModulTest = null;
        GeneratedType groupingUsesModulData = null;

        for (var genType : genTypes) {
            if (!(genType instanceof GeneratedTransferObject)) {
                if (genType.getName().equals("GroupingModulTest")) {
                    groupingModulTest = genType;
                    groupingModulTestCounter++;
                } else if (genType.getName().equals("GroupingUsesModulData")) {
                    groupingUsesModulData = genType;
                    groupingUsesModulDataCounter++;
                }
            }
        }

        assertNotNull(groupingModulTest, "Generated type for grouping-list-test wasn't generated");
        assertEquals(1, groupingModulTestCounter, "GroupingModulTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.modul.rev130718",
            groupingModulTest.getPackageName(), "GroupingModulTest isn't in correct package");

        assertNotNull(groupingUsesModulData, "Generated type for modul wasn't generated");
        assertEquals(1, groupingUsesModulDataCounter,
            "GroupingUsesModulData interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.modul.rev130718",
            groupingUsesModulData.getPackageName(), "GroupingUsesModulData isn't in correct package");

        containsInterface("GroupingModulTest", groupingUsesModulData);

        assertEquals(1, groupingUsesModulData.getMethodDefinitions().size(),
            "Number of method in GroupingUsesModulData is incorrect");
        assertEquals(4, groupingModulTest.getMethodDefinitions().size(),
            "Number of method in GroupingModulTest is incorrect");

        containsMethods(groupingModulTest.getMethodDefinitions(),
            new NameTypePattern("getLeafGroupingModulTest", "String"),
            new NameTypePattern("requireLeafGroupingModulTest", "String"),
            new NameTypePattern("getLeafGroupingModulTest2", "Uint8"),
            new NameTypePattern("requireLeafGroupingModulTest2", "Uint8"));
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
        GeneratedType rpcTestInput = null;
        GeneratedType rpcTestOutput = null;
        GeneratedType groupingRpcInputTest = null;
        GeneratedType groupingRpcOutputTest = null;
        GeneratedType containerGroupingRpcInputTest = null;

        for (var genType : genTypes) {
            if (!(genType instanceof GeneratedTransferObject)) {
                if (genType.getName().equals("RpcTestInput")) {
                    rpcTestInput = genType;
                    rpcTestInputCounter++;
                } else if (genType.getName().equals("RpcTestOutput")) {
                    rpcTestOutput = genType;
                    rpcTestOutputCounter++;
                } else if (genType.getName().equals("GroupingRpcInputTest")) {
                    groupingRpcInputTest = genType;
                    groupingRpcInputTestCounter++;
                } else if (genType.getName().equals("GroupingRpcOutputTest")) {
                    groupingRpcOutputTest = genType;
                    groupingRpcOutputTestCounter++;
                } else if (genType.getName().equals("ContainerGroupingRpcInputTest")) {
                    containerGroupingRpcInputTest = genType;
                    containerGroupingRpcInputTestCounter++;
                }

            }
        }

        assertNotNull(rpcTestInput, "Generated type for RPC test input was not generated");
        assertEquals(1, rpcTestInputCounter, "RpcTestInput interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718", rpcTestInput.getPackageName(),
            "RpcTestInput is not in correct package");

        assertNotNull(rpcTestOutput, "Generated type for RPC test output was not generated");
        assertEquals(1, rpcTestOutputCounter, "RpcTestOutput interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718", rpcTestOutput.getPackageName(),
            "RpcTestOutput is not in correct package");

        assertNotNull(groupingRpcInputTest, "Generated type for grouping-rpc-input-test was not generated");
        assertEquals(1, groupingRpcInputTestCounter, "RpcTestOutput interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718",
            groupingRpcInputTest.getPackageName(), "GroupingRpcInputTest isn't in correct package");

        assertNotNull(groupingRpcOutputTest, "Generated type for grouping-rpc-output-test was not generated");
        assertEquals(1, groupingRpcOutputTestCounter, "RpcTestOutput interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718",
            groupingRpcOutputTest.getPackageName(), "GroupingRpcOutputTest isn't in correct package");

        assertNotNull(containerGroupingRpcInputTest,
            "Generated type for container-grouping-rpc-input-test wasn't generated");
        assertEquals(1, containerGroupingRpcInputTestCounter,
            "ContainerGroupingRpcInputTest interface - incorrect number of occurences");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718.grouping.rpc.input.test",
            containerGroupingRpcInputTest.getPackageName(), "ContainerGroupingRpcInputTest is not in correct package");

        containsInterface("GroupingRpcInputTest", rpcTestInput);
        containsInterface("GroupingRpcOutputTest", rpcTestOutput);

        // FIXME: split this into getter/default/static asserts
        assertEquals(4, rpcTestInput.getMethodDefinitions().size());
        // FIXME: split this into getter/default/static asserts
        assertEquals(4, rpcTestOutput.getMethodDefinitions().size());
        assertEquals(4, groupingRpcInputTest.getMethodDefinitions().size(),
            "Number of method in GroupingRpcInputTest is incorrect");
        assertEquals(2, groupingRpcOutputTest.getMethodDefinitions().size(),
            "Number of method in GroupingRpcOutputTest is incorrect");
        // FIXME: split this into getter/default/static asserts
        assertEquals(6, containerGroupingRpcInputTest.getMethodDefinitions().size());

        containsMethods(groupingRpcInputTest.getMethodDefinitions(),
            new NameTypePattern("getContainerGroupingRpcInputTest", "ContainerGroupingRpcInputTest"),
            new NameTypePattern("getLeaflistGroupingRpcInputTest", "List<Uint8>"),
            new NameTypePattern("requireLeaflistGroupingRpcInputTest", "List<Uint8>"));
        containsMethods(groupingRpcOutputTest.getMethodDefinitions(),
            new NameTypePattern("getLeafGroupingRpcOutputTest", "Byte"));
        containsMethods(containerGroupingRpcInputTest.getMethodDefinitions(),
            new NameTypePattern("getLeafContainerGroupingRpcInputTest", "String"));
    }

    @Test
    void usesInAugmentTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/uses-of-grouping/uses-of-grouping-augment.yang"));

        GeneratedType containerAugment1 = null;
        GeneratedType groupingAugmentTest = null;
        int containerAugment1Counter = 0;
        int groupingAugmentTestCounter = 0;

        for (var genType : genTypes) {
            if (!(genType instanceof GeneratedTransferObject)) {
                if (genType.getName().equals("ContainerAugment1")) {
                    containerAugment1 = genType;
                    containerAugment1Counter++;
                } else if (genType.getName().equals("GroupingAugmentTest")) {
                    groupingAugmentTest = genType;
                    groupingAugmentTestCounter++;
                }
            }
        }

        assertNotNull(containerAugment1, "Generated type for augment /container-augment wasn't generated.");
        assertEquals(1, containerAugment1Counter, "ContainerAugment1 interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.augment.rev130718",
            containerAugment1.getPackageName(), "ContainerAugment1 is in wrong package.");

        assertNotNull(groupingAugmentTest, "Generated type for grouping-augment-test wasn't generated.");
        assertEquals(1, groupingAugmentTestCounter, "GroupingAugmentTest interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.augment.rev130718",
            groupingAugmentTest.getPackageName(), "groupingAugmentTest is in wrong package.");

        containsInterface("GroupingAugmentTest", containerAugment1);

        // FIXME: split this into getter/default/static asserts
        assertEquals(4, containerAugment1.getMethodDefinitions().size());
        // FIXME: split this into getter/default/static asserts
        assertEquals(4, containerAugment1.getMethodDefinitions().size());
        assertEquals(2, groupingAugmentTest.getMethodDefinitions().size(),
            "Number of method in GroupingCaseTest is incorrect");

        containsMethods(groupingAugmentTest.getMethodDefinitions(),
            new NameTypePattern("getLeafGroupingAugmentTest", "String"),
            new NameTypePattern("requireLeafGroupingAugmentTest", "String"));
    }

    @Test
    void usesInNotification() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/uses-of-grouping/uses-of-grouping-notification.yang"));

        GeneratedType notificationTest = null;
        GeneratedType groupingNotificationTest = null;
        GeneratedType containerGroupingNotificationTest = null;
        int notificationTestCounter = 0;
        int groupingNotificationTestCounter = 0;
        int containerGroupingNotificationTestCounter = 0;

        for (var type : genTypes) {
            if (!(type instanceof GeneratedTransferObject)) {
                if (type.getName().equals("NotificationTest")) {
                    notificationTest = type;
                    notificationTestCounter++;
                } else if (type.getName().equals("GroupingNotificationTest")) {
                    groupingNotificationTest = type;
                    groupingNotificationTestCounter++;
                } else if (type.getName().equals("ContainerGroupingNotificationTest")) {
                    containerGroupingNotificationTest = type;
                    containerGroupingNotificationTestCounter++;
                }
            }
        }

        assertNotNull(notificationTest, "Generated type for notification-test wasn't generated.");
        assertEquals(1, notificationTestCounter, "NotificationTest interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.notification.rev130718",
            notificationTest.getPackageName(), "NotificationTest is in wrong package.");

        assertNotNull(groupingNotificationTest, "Generated type for grouping-notification-test wasn't generated.");
        assertEquals(1, groupingNotificationTestCounter,
            "GroupingNotificationTest interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.notification.rev130718",
            groupingNotificationTest.getPackageName(), "groupingNotificationTest is in wrong package.");

        assertNotNull(containerGroupingNotificationTest,
            "Generated type for container-grouping-notification-test wasn't generated.");
        assertEquals(1, containerGroupingNotificationTestCounter,
            "ContainerGroupingNotificationTest interface generated more than one time.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.grouping.uses.notification.rev130718.grouping.notification.test",
            containerGroupingNotificationTest.getPackageName(),
            "ContainerGroupingNotificationTest is in wrong package.");

        containsInterface("GroupingNotificationTest", notificationTest);

        // FIXME: split this into getter/default/static asserts
        assertEquals(6, notificationTest.getMethodDefinitions().size());
        assertEquals(4, groupingNotificationTest.getMethodDefinitions().size(),
            "Number of method in GroupingNotificationTest is incorrect");
        // FIXME: split this into getter/default/static asserts
        assertEquals(6, containerGroupingNotificationTest.getMethodDefinitions().size());

        containsMethods(notificationTest.getMethodDefinitions(),
            new NameTypePattern("getLeafNotificationTest",  "String"));
        containsMethods(groupingNotificationTest.getMethodDefinitions(),
            new NameTypePattern("getContainerGroupingNotificationTest", "ContainerGroupingNotificationTest"),
            new NameTypePattern("getLeaffllistGroupingNotificationTest", "Set<String>"),
            new NameTypePattern("requireLeaffllistGroupingNotificationTest", "Set<String>"));
        containsMethods(containerGroupingNotificationTest.getMethodDefinitions(),
            new NameTypePattern("getLeafContainerGroupingNotificationTest", "Uint32"),
            new NameTypePattern("requireLeafContainerGroupingNotificationTest", "Uint32"));
    }
}
