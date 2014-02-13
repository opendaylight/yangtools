/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.sal.binding.generator.impl.SupportTestUtil.containsInterface;
import static org.opendaylight.yangtools.sal.binding.generator.impl.SupportTestUtil.containsMethods;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class UsesTest {

    private static List<File> loadTestResources(String testFile) {
        try {
        final List<File> testModels = new ArrayList<File>();
        final File listModelFile = new File(UsesTest.class.getResource(testFile).toURI());
        testModels.add(listModelFile);
        return testModels;
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void usesInGroupingDependenciesTest() {
        List<File> testModels = loadTestResources("/uses-of-grouping/uses-of-grouping-dependencies.yang");
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);
        GeneratedType groupingU = null;
        GeneratedType groupingX = null;
        GeneratedType groupingV = null;

        int groupingUCounter = 0;
        int groupingXCounter = 0;
        int groupingVCounter = 0;

        for (Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                GeneratedType genType = (GeneratedType) type;
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
        List<File> testModels = loadTestResources("/uses-of-grouping/uses-of-grouping-case.yang");
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        GeneratedType groupingCaseTest = null;
        int groupingCaseTestCounter = 0;
        GeneratedType caseC = null;
        int caseCCounter = 0;
        for (Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                GeneratedType genType = (GeneratedType) type;
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
        assertTrue("Case C shouldn't contain any method.", caseC.getMethodDefinitions().isEmpty());

        assertEquals("Number of method in GroupingCaseTest is incorrect", 1, groupingCaseTest.getMethodDefinitions()
                .size());
        containsMethods(groupingCaseTest.getMethodDefinitions(), new NameTypePattern("getLeafGroupingCaseTest1",
                "String"));
    }

    @Test
    public void usesInContainerTest() {
        List<File> testModels = loadTestResources("/uses-of-grouping/uses-of-grouping-container.yang");
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        int containerTestCount = 0;
        int groupingContainerTestCounter = 0;
        GeneratedType containerTest = null;
        GeneratedType groupingContainerTest = null;

        for (Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                GeneratedType genType = (GeneratedType) type;
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

        assertEquals("Number of method in GroupingContainerTestis incorrect", 2, groupingContainerTest
                .getMethodDefinitions().size());
        assertEquals("Number of method in ContainerTest is incorrect", 1, containerTest.getMethodDefinitions().size());

        containsMethods(groupingContainerTest.getMethodDefinitions(), new NameTypePattern(
                "getLeafGroupingContainerTest1", "String"), new NameTypePattern("getLeafGroupingContainerTest2",
                "Short"));

        containsMethods(containerTest.getMethodDefinitions(), new NameTypePattern("getContainerLeafTest", "String"));
    }

    @Test
    public void usesInGroupingTest() {
        List<File> testModels = loadTestResources("/uses-of-grouping/uses-of-grouping-grouping.yang");
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        int groupingTestCount = 0;
        int groupingGroupingTestCounter = 0;
        GeneratedType groupingTest = null;
        GeneratedType groupingGroupingTest = null;

        for (Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                GeneratedType genType = (GeneratedType) type;
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

        assertEquals("Number of method in GroupingGroupingTest is incorrect", 1, groupingGroupingTest
                .getMethodDefinitions().size());
        assertEquals("Number of method in GroupingTest is incorrect", 1, groupingTest.getMethodDefinitions().size());

        containsMethods(groupingGroupingTest.getMethodDefinitions(), new NameTypePattern("getLeafGroupingGrouping",
                "String"));

        containsMethods(groupingTest.getMethodDefinitions(), new NameTypePattern("getLeafGroupingTest", "Byte"));
    }

    @Test
    public void usesInListTest() {
        List<File> testModels = loadTestResources("/uses-of-grouping/uses-of-grouping-list.yang");
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        int listTestCounter = 0;
        int groupingListTestCounter = 0;
        int containerGroupingListTestCounter = 0;
        int listGroupingListTestCounter = 0;
        GeneratedType listTest = null;
        GeneratedType groupingListTest = null;
        GeneratedType containerGroupingListTest = null;
        GeneratedType listGroupingListTest = null;

        for (Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                GeneratedType genType = (GeneratedType) type;
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

        assertEquals("Number of method in GroupingListTest is incorrect", 4, groupingListTest.getMethodDefinitions()
                .size());
        assertEquals("Number of method in ListTest is incorrect", 1, listTest.getMethodDefinitions().size());
        assertEquals("Number of method in ContainerGroupingListTest is incorrect", 1, containerGroupingListTest
                .getMethodDefinitions().size());
        assertEquals("Number of method in ListGroupingListTest is incorrect", 1, listGroupingListTest
                .getMethodDefinitions().size());

        containsMethods(groupingListTest.getMethodDefinitions(), new NameTypePattern("getContainerGroupingListTest",
                "ContainerGroupingListTest"), new NameTypePattern("getLeafGroupingListTest", "String"),
                new NameTypePattern("getLeaffllistGroupingListTest", "List<String>"), new NameTypePattern(
                        "getListGroupingListTest", "List<ListGroupingListTest>"));
        containsMethods(listTest.getMethodDefinitions(), new NameTypePattern("getListLeafTest", "String"));
        containsMethods(containerGroupingListTest.getMethodDefinitions(), new NameTypePattern(
                "getLeafContainerGroupingListTest", "Short"));
        containsMethods(listGroupingListTest.getMethodDefinitions(), new NameTypePattern("getLeafListGroupingListTest",
                "Integer"));
    }

    @Test
    public void usesInModulTest() {
        List<File> testModels = loadTestResources("/uses-of-grouping/uses-of-grouping-modul.yang");
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        int groupingModulTestCounter = 0;
        int groupingUsesModulDataCounter = 0;
        GeneratedType groupingModulTest = null;
        GeneratedType groupingUsesModulData = null;

        for (Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                GeneratedType genType = (GeneratedType) type;
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

        assertEquals("Number of method in GroupingUsesModulData is incorrect", 0, groupingUsesModulData
                .getMethodDefinitions().size());
        assertEquals("Number of method in GroupingModulTest is incorrect", 2, groupingModulTest.getMethodDefinitions()
                .size());

        containsMethods(groupingModulTest.getMethodDefinitions(), new NameTypePattern("getLeafGroupingModulTest",
                "String"), new NameTypePattern("getLeafGroupingModulTest2", "Short"));
    }

    @Test
    public void usesInRpcTest() {
        List<File> testModels = loadTestResources("/uses-of-grouping/uses-of-grouping-rpc.yang");
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

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

        for (Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                GeneratedType genType = (GeneratedType) type;
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

        assertNotNull("Generated type for RPC test input wasn't generated", rpcTestInput);
        assertEquals("RpcTestInput interface - incorrect number of occurences", 1, rpcTestInputCounter);
        assertEquals("RpcTestInput isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718", rpcTestInput.getPackageName());

        assertNotNull("Generated type for RPC test output wasn't generated", rpcTestOutput);
        assertEquals("RpcTestOutput interface - incorrect number of occurences", 1, rpcTestOutputCounter);
        assertEquals("RpcTestOutput isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718", rpcTestOutput.getPackageName());

        assertNotNull("Generated type for grouping-rpc-input-test wasn't generated", groupingRpcInputTest);
        assertEquals("RpcTestOutput interface - incorrect number of occurences", 1, groupingRpcInputTestCounter);
        assertEquals("GroupingRpcInputTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718", groupingRpcInputTest.getPackageName());

        assertNotNull("Generated type for grouping-rpc-output-test wasn't generated", groupingRpcOutputTest);
        assertEquals("RpcTestOutput interface - incorrect number of occurences", 1, groupingRpcOutputTestCounter);
        assertEquals("GroupingRpcOutputTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718", groupingRpcOutputTest.getPackageName());

        assertNotNull("Generated type for container-grouping-rpc-input-test wasn't generated",
                containerGroupingRpcInputTest);
        assertEquals("ContainerGroupingRpcInputTest interface - incorrect number of occurences", 1,
                containerGroupingRpcInputTestCounter);
        assertEquals("ContainerGroupingRpcInputTest isn't in correct package",
                "org.opendaylight.yang.gen.v1.urn.grouping.uses.rpc.rev130718.grouping.rpc.input.test",
                containerGroupingRpcInputTest.getPackageName());

        containsInterface("GroupingRpcInputTest", rpcTestInput);
        containsInterface("GroupingRpcOutputTest", rpcTestOutput);

        assertEquals("Number of method in RpcTestInput is incorrect", 0, rpcTestInput.getMethodDefinitions().size());
        assertEquals("Number of method in RpcTestOutput is incorrect", 0, rpcTestOutput.getMethodDefinitions().size());
        assertEquals("Number of method in GroupingRpcInputTest is incorrect", 2, groupingRpcInputTest
                .getMethodDefinitions().size());
        assertEquals("Number of method in GroupingRpcOutputTest is incorrect", 1, groupingRpcOutputTest
                .getMethodDefinitions().size());
        assertEquals("Number of method in ContainerGroupingRpcInputTest is incorrect", 1, containerGroupingRpcInputTest
                .getMethodDefinitions().size());

        containsMethods(groupingRpcInputTest.getMethodDefinitions(), new NameTypePattern(
                "getContainerGroupingRpcInputTest", "ContainerGroupingRpcInputTest"), new NameTypePattern(
                "getLeaflistGroupingRpcInputTest", "List<Short>"));
        containsMethods(groupingRpcOutputTest.getMethodDefinitions(), new NameTypePattern(
                "getLeafGroupingRpcOutputTest", "Byte"));
        containsMethods(containerGroupingRpcInputTest.getMethodDefinitions(), new NameTypePattern(
                "getLeafContainerGroupingRpcInputTest", "String"));
    }

    @Test
    public void usesInAugmentTest() {
        List<File> testModels = loadTestResources("/uses-of-grouping/uses-of-grouping-augment.yang");
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        GeneratedType containerAugment1 = null;
        GeneratedType groupingAugmentTest = null;
        int containerAugment1Counter = 0;
        int groupingAugmentTestCounter = 0;

        for (Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                GeneratedType genType = (GeneratedType) type;
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

        assertEquals("Number of method in GroupingCaseTest is incorrect", 0, containerAugment1.getMethodDefinitions()
                .size());

        assertEquals("Number of method in ContainerAugment1 is incorrect", 0, containerAugment1.getMethodDefinitions()
                .size());
        assertEquals("Number of method in GroupingCaseTest is incorrect", 1, groupingAugmentTest.getMethodDefinitions()
                .size());

        containsMethods(groupingAugmentTest.getMethodDefinitions(), new NameTypePattern("getLeafGroupingAugmentTest",
                "String"));
    }

    @Test
    public void usesInNotification() {
        List<File> testModels = loadTestResources("/uses-of-grouping/uses-of-grouping-notification.yang");
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        GeneratedType notificationTest = null;
        GeneratedType groupingNotificationTest = null;
        GeneratedType containerGroupingNotificationTest = null;
        int notificationTestCounter = 0;
        int groupingNotificationTestCounter = 0;
        int containerGroupingNotificationTestCounter = 0;

        for (Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                GeneratedType genType = (GeneratedType) type;
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

        assertEquals("Number of method in NotificationTest is incorrect", 1, notificationTest.getMethodDefinitions()
                .size());
        assertEquals("Number of method in GroupingNotificationTest is incorrect", 2, groupingNotificationTest
                .getMethodDefinitions().size());
        assertEquals("Number of method in ContainerGroupingNotificationTest is incorrect", 1,
                containerGroupingNotificationTest.getMethodDefinitions().size());

        containsMethods(notificationTest.getMethodDefinitions(), new NameTypePattern("getLeafNotificationTest",
                "String"));
        containsMethods(groupingNotificationTest.getMethodDefinitions(), new NameTypePattern(
                "getContainerGroupingNotificationTest", "ContainerGroupingNotificationTest"), new NameTypePattern(
                "getLeaffllistGroupingNotificationTest", "List<String>"));
        containsMethods(containerGroupingNotificationTest.getMethodDefinitions(), new NameTypePattern(
                "getLeafContainerGroupingNotificationTest", "Long"));
    }

}
