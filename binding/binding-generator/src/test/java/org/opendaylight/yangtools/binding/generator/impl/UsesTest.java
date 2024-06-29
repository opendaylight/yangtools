/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.containsInterface;
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.containsMethods;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class UsesTest {
    @Test
    public void usesInGroupingDependenciesTest() {
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

        assertNotNull("Generated type for grouping-U wasn't generated.", groupingU);
        assertEquals("GroupingU interface generated more than one time.", 1, groupingUCounter);
        assertEquals("GroupingU is in wrong package.",
                "org.opendaylight.yang.gen.v1.urn.grouping.dependencies.rev130718", groupingU.getPackageName());

        assertNotNull("Generated type for grouping-V wasn't generated.", groupingV);
        assertEquals("GroupingV interface generated more than one time.", 1, groupingVCounter);
        assertEquals("GroupingV is in wrong package.",
                "org.opendaylight.yang.gen.v1.urn.grouping.dependencies.rev130718", groupingV.getPackageName());

        assertNotNull("Generated type for grouping-X wasn't generated.", groupingX);
        assertEquals("GroupingX interface generated more than one time.", 1, groupingXCounter);
        assertEquals("GroupingX is in wrong package.",
                "org.opendaylight.yang.gen.v1.urn.grouping.dependencies.rev130718", groupingX.getPackageName());

        containsInterface("GroupingV", groupingU);
        containsInterface("GroupingX", groupingU);
        containsInterface("GroupingZ", groupingV);
        containsInterface("GroupingZZ", groupingV);
        containsInterface("GroupingY", groupingX);
    }

    @Test
    public void usesInCaseTest() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-case.yang"));

        GeneratedType groupingCaseTest = null;
        int groupingCaseTestCounter = 0;
        GeneratedType caseC = null;
        int caseCCounter = 0;
        for (GeneratedType genType : genTypes) {
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

        assertNotNull("Generated type for case C wasn't generated.", caseC);
        assertEquals("Case C interface generated more than one time.", 1, caseCCounter);
        assertEquals(
                "Case C is in wrong package.",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses._case.rev130718.container.with.choicetest.choice.test",
                caseC.getPackageName());

        assertNotNull("Generated type for grouping-case-test wasn't generated.", groupingCaseTest);
        assertEquals("GroupingCaseTest interface generated more than one time.", 1, groupingCaseTestCounter);
        assertEquals("GroupingCaseTest is in wrong package.",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses._case.rev130718", groupingCaseTest.getPackageName());

        containsInterface("GroupingCaseTest", caseC);
        // FIXME: split this into getter/default/static asserts
        assertEquals(4, caseC.getMethodDefinitions().size());

        assertEquals("Number of method in GroupingCaseTest is incorrect", 3, groupingCaseTest.getMethodDefinitions()
                .size());
        containsMethods(groupingCaseTest.getMethodDefinitions(),
            new NameTypePattern("getLeafGroupingCaseTest1", "String"),
            new NameTypePattern("requireLeafGroupingCaseTest1", "String"));
    }

    @Test
    public void usesInContainerTest() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-container.yang"));

        int containerTestCount = 0;
        int groupingContainerTestCounter = 0;
        GeneratedType containerTest = null;
        GeneratedType groupingContainerTest = null;

        for (GeneratedType genType : genTypes) {
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

        assertNotNull("Generated type for grouping-container-test wasn't generated", groupingContainerTest);
        assertEquals("GroupingContainerTest interface - incorrect number of occurences", 1,
                groupingContainerTestCounter);
        assertEquals("GroupingContainerTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.container.rev130718",
                groupingContainerTest.getPackageName());

        assertNotNull("Generated type for container-test wasn't generated", containerTest);
        assertEquals("ContainerTest interface - incorrect number of occurences", 1, containerTestCount);
        assertEquals("ContainerTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.container.rev130718", containerTest.getPackageName());

        containsInterface("GroupingContainerTest", containerTest);

        assertEquals("Number of method in GroupingContainerTestis incorrect", 5, groupingContainerTest
                .getMethodDefinitions().size());
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
    public void usesInGroupingTest() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-grouping.yang"));

        int groupingTestCount = 0;
        int groupingGroupingTestCounter = 0;
        GeneratedType groupingTest = null;
        GeneratedType groupingGroupingTest = null;

        for (GeneratedType genType : genTypes) {
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

        assertNotNull("Generated type for grouping-grouping-test wasn't generated", groupingGroupingTest);
        assertEquals("GroupingGroupingTest interface - incorrect number of occurences", 1, groupingGroupingTestCounter);
        assertEquals("GroupingGroupingTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.grouping.rev130718",
                groupingGroupingTest.getPackageName());

        assertNotNull("Generated type for grouping-test wasn't generated", groupingTest);
        assertEquals("GroupingTest interface - incorrect number of occurences", 1, groupingTestCount);
        assertEquals("GroupingTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.grouping.rev130718", groupingTest.getPackageName());

        containsInterface("GroupingGroupingTest", groupingTest);

        assertEquals("Number of method in GroupingGroupingTest is incorrect", 3, groupingGroupingTest
                .getMethodDefinitions().size());
        assertEquals("Number of method in GroupingTest is incorrect", 3, groupingTest.getMethodDefinitions().size());

        containsMethods(groupingGroupingTest.getMethodDefinitions(),
            new NameTypePattern("getLeafGroupingGrouping", "String"),
            new NameTypePattern("requireLeafGroupingGrouping", "String"));

        containsMethods(groupingTest.getMethodDefinitions(),
            new NameTypePattern("getLeafGroupingTest", "Byte"),
            new NameTypePattern("requireLeafGroupingTest", "Byte"));
    }

    @Test
    public void usesInListTest() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-list.yang"));

        int listTestCounter = 0;
        int groupingListTestCounter = 0;
        int containerGroupingListTestCounter = 0;
        int listGroupingListTestCounter = 0;
        GeneratedType listTest = null;
        GeneratedType groupingListTest = null;
        GeneratedType containerGroupingListTest = null;
        GeneratedType listGroupingListTest = null;

        for (GeneratedType genType : genTypes) {
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

        assertNotNull("Generated type for grouping-list-test wasn't generated", groupingListTest);
        assertEquals("GroupingListTest interface - incorrect number of occurences", 1, groupingListTestCounter);
        assertEquals("GroupingListTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.list.rev130718", groupingListTest.getPackageName());

        assertNotNull("Generated type for list-test wasn't generated", listTest);
        assertEquals("ListTest interface - incorrect number of occurences", 1, listTestCounter);
        assertEquals("ListTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.list.rev130718", listTest.getPackageName());

        assertNotNull("Generated type for container-grouping-list-test wasn't generated", containerGroupingListTest);
        assertEquals("ContainerGroupingListTest interface - incorrect number of occurences", 1,
                containerGroupingListTestCounter);
        assertEquals("ContainerGroupingListTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.list.rev130718.grouping.list.test",
                containerGroupingListTest.getPackageName());

        assertNotNull("Generated type for list-grouping-list-test wasn't generated", listGroupingListTest);
        assertEquals("ListGroupingListTest interface - incorrect number of occurences", 1, listGroupingListTestCounter);
        assertEquals("ListGroupingListTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.list.rev130718.grouping.list.test",
                listGroupingListTest.getPackageName());

        containsInterface("GroupingListTest", listTest);

        assertEquals("Number of method in GroupingListTest is incorrect", 9, groupingListTest.getMethodDefinitions()
                .size());
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
    public void usesInModulTest() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/uses-of-grouping/uses-of-grouping-modul.yang"));

        int groupingModulTestCounter = 0;
        int groupingUsesModulDataCounter = 0;
        GeneratedType groupingModulTest = null;
        GeneratedType groupingUsesModulData = null;

        for (GeneratedType genType : genTypes) {
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

        assertNotNull("Generated type for grouping-list-test wasn't generated", groupingModulTest);
        assertEquals("GroupingModulTest interface - incorrect number of occurences", 1, groupingModulTestCounter);
        assertEquals("GroupingModulTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.modul.rev130718", groupingModulTest.getPackageName());

        assertNotNull("Generated type for modul wasn't generated", groupingUsesModulData);
        assertEquals("GroupingUsesModulData interface - incorrect number of occurences", 1,
                groupingUsesModulDataCounter);
        assertEquals("GroupingUsesModulData isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.modul.rev130718",
                groupingUsesModulData.getPackageName());

        containsInterface("GroupingModulTest", groupingUsesModulData);

        assertEquals("Number of method in GroupingUsesModulData is incorrect", 1, groupingUsesModulData
                .getMethodDefinitions().size());
        assertEquals("Number of method in GroupingModulTest is incorrect", 5, groupingModulTest.getMethodDefinitions()
                .size());

        containsMethods(groupingModulTest.getMethodDefinitions(),
            new NameTypePattern("getLeafGroupingModulTest", "String"),
            new NameTypePattern("requireLeafGroupingModulTest", "String"),
            new NameTypePattern("getLeafGroupingModulTest2", "Uint8"),
            new NameTypePattern("requireLeafGroupingModulTest2", "Uint8"));
    }

    @Test
    public void usesInRpcTest() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
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

        for (GeneratedType genType : genTypes) {
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

        assertNotNull("Generated type for RPC test input was not generated", rpcTestInput);
        assertEquals("RpcTestInput interface - incorrect number of occurences", 1, rpcTestInputCounter);
        assertEquals("RpcTestInput is not in correct package",
            "org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718", rpcTestInput.getPackageName());

        assertNotNull("Generated type for RPC test output was not generated", rpcTestOutput);
        assertEquals("RpcTestOutput interface - incorrect number of occurences", 1, rpcTestOutputCounter);
        assertEquals("RpcTestOutput is not in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718", rpcTestOutput.getPackageName());

        assertNotNull("Generated type for grouping-rpc-input-test was not generated", groupingRpcInputTest);
        assertEquals("RpcTestOutput interface - incorrect number of occurences", 1, groupingRpcInputTestCounter);
        assertEquals("GroupingRpcInputTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718", groupingRpcInputTest.getPackageName());

        assertNotNull("Generated type for grouping-rpc-output-test was not generated", groupingRpcOutputTest);
        assertEquals("RpcTestOutput interface - incorrect number of occurences", 1, groupingRpcOutputTestCounter);
        assertEquals("GroupingRpcOutputTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718", groupingRpcOutputTest.getPackageName());

        assertNotNull("Generated type for container-grouping-rpc-input-test wasn't generated",
                containerGroupingRpcInputTest);
        assertEquals("ContainerGroupingRpcInputTest interface - incorrect number of occurences", 1,
                containerGroupingRpcInputTestCounter);
        assertEquals("ContainerGroupingRpcInputTest is not in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718.grouping.rpc.input.test",
                containerGroupingRpcInputTest.getPackageName());

        containsInterface("GroupingRpcInputTest", rpcTestInput);
        containsInterface("GroupingRpcOutputTest", rpcTestOutput);

        // FIXME: split this into getter/default/static asserts
        assertEquals(4, rpcTestInput.getMethodDefinitions().size());
        // FIXME: split this into getter/default/static asserts
        assertEquals(4, rpcTestOutput.getMethodDefinitions().size());
        assertEquals("Number of method in GroupingRpcInputTest is incorrect", 5, groupingRpcInputTest
                .getMethodDefinitions().size());
        assertEquals("Number of method in GroupingRpcOutputTest is incorrect", 3, groupingRpcOutputTest
                .getMethodDefinitions().size());
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
    public void usesInAugmentTest() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/uses-of-grouping/uses-of-grouping-augment.yang"));

        GeneratedType containerAugment1 = null;
        GeneratedType groupingAugmentTest = null;
        int containerAugment1Counter = 0;
        int groupingAugmentTestCounter = 0;

        for (GeneratedType genType : genTypes) {
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

        assertNotNull("Generated type for augment /container-augment wasn't generated.", containerAugment1);
        assertEquals("ContainerAugment1 interface generated more than one time.", 1, containerAugment1Counter);
        assertEquals("ContainerAugment1 is in wrong package.",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.augment.rev130718", containerAugment1.getPackageName());

        assertNotNull("Generated type for grouping-augment-test wasn't generated.", groupingAugmentTest);
        assertEquals("GroupingAugmentTest interface generated more than one time.", 1, groupingAugmentTestCounter);
        assertEquals("groupingAugmentTest is in wrong package.",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.augment.rev130718",
                groupingAugmentTest.getPackageName());

        containsInterface("GroupingAugmentTest", containerAugment1);

        // FIXME: split this into getter/default/static asserts
        assertEquals(4, containerAugment1.getMethodDefinitions().size());
        // FIXME: split this into getter/default/static asserts
        assertEquals(4, containerAugment1.getMethodDefinitions().size());
        assertEquals("Number of method in GroupingCaseTest is incorrect", 3, groupingAugmentTest.getMethodDefinitions()
                .size());

        containsMethods(groupingAugmentTest.getMethodDefinitions(),
            new NameTypePattern("getLeafGroupingAugmentTest", "String"),
            new NameTypePattern("requireLeafGroupingAugmentTest", "String"));
    }

    @Test
    public void usesInNotification() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/uses-of-grouping/uses-of-grouping-notification.yang"));

        GeneratedType notificationTest = null;
        GeneratedType groupingNotificationTest = null;
        GeneratedType containerGroupingNotificationTest = null;
        int notificationTestCounter = 0;
        int groupingNotificationTestCounter = 0;
        int containerGroupingNotificationTestCounter = 0;

        for (GeneratedType type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                GeneratedType genType = type;
                if (genType.getName().equals("NotificationTest")) {
                    notificationTest = genType;
                    notificationTestCounter++;
                } else if (genType.getName().equals("GroupingNotificationTest")) {
                    groupingNotificationTest = genType;
                    groupingNotificationTestCounter++;
                } else if (genType.getName().equals("ContainerGroupingNotificationTest")) {
                    containerGroupingNotificationTest = genType;
                    containerGroupingNotificationTestCounter++;
                }
            }
        }

        assertNotNull("Generated type for notification-test wasn't generated.", notificationTest);
        assertEquals("NotificationTest interface generated more than one time.", 1, notificationTestCounter);
        assertEquals("NotificationTest is in wrong package.",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.notification.rev130718",
                notificationTest.getPackageName());

        assertNotNull("Generated type for grouping-notification-test wasn't generated.", groupingNotificationTest);
        assertEquals("GroupingNotificationTest interface generated more than one time.", 1,
                groupingNotificationTestCounter);
        assertEquals("groupingNotificationTest is in wrong package.",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.notification.rev130718",
                groupingNotificationTest.getPackageName());

        assertNotNull("Generated type for container-grouping-notification-test wasn't generated.",
                containerGroupingNotificationTest);
        assertEquals("ContainerGroupingNotificationTest interface generated more than one time.", 1,
                containerGroupingNotificationTestCounter);
        assertEquals("ContainerGroupingNotificationTest is in wrong package.",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.notification.rev130718.grouping.notification.test",
                containerGroupingNotificationTest.getPackageName());

        containsInterface("GroupingNotificationTest", notificationTest);

        // FIXME: split this into getter/default/static asserts
        assertEquals(6, notificationTest.getMethodDefinitions().size());
        assertEquals("Number of method in GroupingNotificationTest is incorrect", 5, groupingNotificationTest
                .getMethodDefinitions().size());
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
