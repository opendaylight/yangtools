/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

/**
 * Test suite for increasing of test coverage of ModuleBuilder implementation.
 *
 * @see org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
public class ModuleBuilderTest {

    private final static String TEST_MODULE_NAMESPACE = "urn:opendaylight.foo";
    private final static String TEST_MODULE_REVISION = "2013-07-03";
    private final static String MODULE_NAME = "TestModule";
    private static final String TEST_MODULE_PATH = "/test/module/path";

    private ModuleBuilder moduleBuilder;

    @Before
    public void setUp() throws Exception {
        moduleBuilder = new ModuleBuilder(MODULE_NAME , TEST_MODULE_PATH);
    }

    @Test
    public void testGetSetModuleBuilderMethods() throws Exception {
        assertEquals("module", moduleBuilder.getStatementName());
        assertEquals(TEST_MODULE_PATH, moduleBuilder.getModuleSourcePath());
        assertNotNull(moduleBuilder.getAllLists());
        final URI namespace = URI.create(TEST_MODULE_NAMESPACE + "?" + TEST_MODULE_REVISION);
        moduleBuilder.setNamespace(namespace);
        assertEquals(namespace, moduleBuilder.getNamespace());
        moduleBuilder.setBelongsTo("TEST_SUBMODULE");
        assertEquals("TEST_SUBMODULE", moduleBuilder.getBelongsTo());
        final Date revision = new SimpleDateFormat("yyyy-dd-mm").parse(TEST_MODULE_REVISION);
        moduleBuilder.setRevision(revision);
        assertEquals(revision, moduleBuilder.getRevision());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckPrefixFailOnPrefixNull() {
        moduleBuilder.addModuleImport("TEST_MODULE", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckPrefixFailOnPrefixStringIsEmpty() {
        moduleBuilder.addModuleImport("TEST_MODULE", null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckPrefixFailOnPrefixEqualsModuleBuilderPrefix() {
        moduleBuilder.setPrefix("prefix");
        moduleBuilder.addModuleImport("TEST_MODULE", null, "prefix");
    }

    @Test
    public void testGetActualNodeNullResult() {
        moduleBuilder.exitNode();
        assertNull(moduleBuilder.getActualNode());
    }

    @Test(expected = YangParseException.class)
    public void testSetParent() throws Exception {
        moduleBuilder.setParent(new Builder() {
            @Override public String getModuleName() {
                return null;
            }

            @Override public void setModuleName(String moduleName) {

            }

            @Override public int getLine() {
                return 0;
            }

            @Override public Builder getParent() {
                return null;
            }

            @Override public void setParent(Builder parent) {

            }

            @Override public void addUnknownNodeBuilder(UnknownSchemaNodeBuilder unknownNode) {

            }

            @Override public List<UnknownSchemaNodeBuilder> getUnknownNodes() {
                return null;
            }

            @Override public Object build() {
                return null;
            }
        });
    }

    @Test(expected = YangParseException.class)
    public void testAddExtensionWhereParentNodeIsNotModuleBuilder() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));
        final QName extQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-ext");
        SchemaPath extPath = SchemaPath.create(true, containerQName, extQName);
        moduleBuilder.addExtension(extQName, 12, extPath);
    }

    @Test(expected = YangParseException.class)
    public void testAddExtensionWhereNameOfExtensionCollides() throws Exception {
        final QName extQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-ext");
        SchemaPath extPath = SchemaPath.create(true, extQName);
        moduleBuilder.addExtension(extQName, 12, extPath);

        final QName extQName2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-ext");
        moduleBuilder.addExtension(extQName2, 22, extPath);
    }

    @Test(expected = YangParseException.class)
    public void testAddDuplicateGroupingIntoModule() {
        final QName testGroup1 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-group");
        final SchemaPath groupPath1 = SchemaPath.create(true, testGroup1);

        final QName testGroup2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-group");
        final SchemaPath groupPath2 = SchemaPath.create(true, testGroup2);

        moduleBuilder.addGrouping(12, testGroup1, groupPath1);
        moduleBuilder.addGrouping(12, testGroup2, groupPath2);
    }

    @Test(expected = YangParseException.class)
    public void testAddDuplicateGroupingIntoDataNodeContainer() {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));
        final QName testGroup1 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-group");
        final SchemaPath groupPath1 = SchemaPath.create(true, containerQName, testGroup1);

        final QName testGroup2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-group");
        final SchemaPath groupPath2 = SchemaPath.create(true, containerQName, testGroup2);

        moduleBuilder.addGrouping(12, testGroup1, groupPath1);
        moduleBuilder.addGrouping(12, testGroup2, groupPath2);
    }

    @Test(expected = YangParseException.class)
    public void testAddDuplicateGroupingIntoRpcDefinition() {
        final QName rpcDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "rpc-definition");
        SchemaPath rpcPath = SchemaPath.create(true, rpcDefName);
        moduleBuilder.enterNode(new RpcDefinitionBuilder(MODULE_NAME, 10, rpcDefName, rpcPath));
        final QName testGroup1 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-group");
        final SchemaPath groupPath1 = SchemaPath.create(true, rpcDefName, testGroup1);

        final QName testGroup2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-group");
        final SchemaPath groupPath2 = SchemaPath.create(true, rpcDefName, testGroup2);

        moduleBuilder.addGrouping(12, testGroup1, groupPath1);
        moduleBuilder.addGrouping(12, testGroup2, groupPath2);
    }

    @Test(expected = YangParseException.class)
    public void testAddGroupingIntoNonValidStatementDefinition() {
        final QName leafListDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-leaflist");
        SchemaPath leaflistPath = SchemaPath.create(true, leafListDefName);
        moduleBuilder.enterNode(new LeafListSchemaNodeBuilder(MODULE_NAME, 10, leafListDefName, leaflistPath));
        final QName testGroup = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-group");
        final SchemaPath groupPath = SchemaPath.create(true, leafListDefName, testGroup);

        moduleBuilder.addGrouping(12, testGroup, groupPath);
    }

    @Test(expected = YangParseException.class)
    public void testInvalidAugmentPathTargetString() {
        final QName targetSchemaName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "fictive-target-container");
        moduleBuilder.addAugment(12, "something/something", SchemaPath.create(true, targetSchemaName), 0);
    }

    @Test(expected = YangParseException.class)
    public void testInvalidAugmentPathTargetStringInUsesNode() {
        final QName usesNode = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "fictive-uses-node");
        SchemaPath targetPath = SchemaPath.create(true, usesNode);
        moduleBuilder.enterNode(new UsesNodeBuilderImpl(MODULE_NAME, 10, targetPath));
        final QName targetSchemaName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "fictive-target-container");
        moduleBuilder.addAugment(12, "/something/something", SchemaPath.create(true, targetSchemaName), 0);
    }

    @Test(expected = YangParseException.class)
    public void testInvalidAugmentDefinition() {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        final QName targetSchemaName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "fictive-target-container");
        moduleBuilder.addAugment(12, "/something/something", SchemaPath.create(true, targetSchemaName), 0);
    }

    @Test(expected = YangParseException.class)
    public void testInvalidUsesNodeUse() {
        final QName leafListDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-leaflist");
        SchemaPath leaflistPath = SchemaPath.create(true, leafListDefName);
        moduleBuilder.enterNode(new LeafListSchemaNodeBuilder(MODULE_NAME, 10, leafListDefName, leaflistPath));

        final QName testGroup = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-group");
        final SchemaPath groupPath = SchemaPath.create(true, testGroup);

        moduleBuilder.addUsesNode(17, groupPath);
    }

    @Test(expected = YangParseException.class)
    public void testAddRefineToNonUsesNodeBuilder() throws Exception {
        final QName leafListDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-leaflist");
        SchemaPath leaflistPath = SchemaPath.create(true, leafListDefName);
        moduleBuilder.enterNode(new LeafListSchemaNodeBuilder(MODULE_NAME, 10, leafListDefName, leaflistPath));

        moduleBuilder.addRefine(new RefineHolderImpl(MODULE_NAME, 12, "testRefineName"));
    }

    @Test(expected = YangParseException.class)
    public void testAddRpcToNonModuleBuilder() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        final QName rpcDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "rpc-definition");
        SchemaPath rpcPath = SchemaPath.create(true, rpcDefName);

        moduleBuilder.addRpc(17, rpcDefName, rpcPath);
    }

    @Test(expected = YangParseException.class)
    public void testAddRpcWithSameNameAsAlreadyExistingDataNodeContainer() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "element-foo");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.addContainerNode(10, containerQName, containerPath);

        final QName rpcDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "element-foo");
        SchemaPath rpcPath = SchemaPath.create(true, rpcDefName);
        moduleBuilder.addRpc(17, rpcDefName, rpcPath);
    }

    @Test(expected = YangParseException.class)
    public void testAddDuplicateRpc() throws Exception {
        final QName rpcDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "rpc-definition");
        SchemaPath rpcPath = SchemaPath.create(true, rpcDefName);
        moduleBuilder.addRpc(17, rpcDefName, rpcPath);

        final QName rpcDefName2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "rpc-definition");
        SchemaPath rpcPath2 = SchemaPath.create(true, rpcDefName2);
        moduleBuilder.addRpc(23, rpcDefName2, rpcPath2);
    }

    @Test(expected = YangParseException.class)
    public void testAddRpcWithSameNameAsAlreadyDefinedNotification() throws Exception {
        final QName rpcDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "rpc-definition");
        SchemaPath rpcPath = SchemaPath.create(true, rpcDefName);
        moduleBuilder.addNotification(17, rpcDefName, rpcPath);

        final QName rpcDefName2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "rpc-definition");
        SchemaPath rpcPath2 = SchemaPath.create(true, rpcDefName2);
        moduleBuilder.addRpc(23, rpcDefName2, rpcPath2);
    }

    @Test(expected = YangParseException.class)
    public void testAddRpcInputForInvalidParent() throws Exception {
        final QName rpcDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "rpc-definition");
        final QName rpcInput = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "input");
        SchemaPath rpcInputPath = SchemaPath.create(true, rpcDefName, rpcInput);

        moduleBuilder.addRpcInput(23, rpcInput, rpcInputPath);
    }

    @Test(expected = YangParseException.class)
    public void testAddRpcOutputForInvalidParent() throws Exception {
        final QName rpcDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "rpc-definition");
        final QName rpcOutput = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "output");
        SchemaPath rpcOutputPath = SchemaPath.create(true, rpcDefName, rpcOutput);

        moduleBuilder.addRpcOutput(rpcOutputPath, rpcOutput, 23);
    }

    @Test(expected = YangParseException.class)
    public void testAddNotificationForInvalidParent() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        final QName notificationName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "notify-this");
        SchemaPath notificationPath = SchemaPath.create(true, notificationName);

        moduleBuilder.addNotification(12, notificationName, notificationPath);
    }

    @Test(expected = YangParseException.class)
    public void testAddNotificationWithSameNameAsAlreadyExistingDataNodeContainer() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "notify");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.addContainerNode(10, containerQName, containerPath);

        final QName notificationName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "notify");
        SchemaPath notificationPath = SchemaPath.create(true, notificationName);

        moduleBuilder.addNotification(12, notificationName, notificationPath);
    }

    @Test(expected = YangParseException.class)
    public void testAddDuplicateNotification() throws Exception {
        final QName notificationName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "notify-this");
        SchemaPath notificationPath = SchemaPath.create(true, notificationName);

        moduleBuilder.addNotification(12, notificationName, notificationPath);

        final QName notificationName2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "notify-this");
        SchemaPath notificationPath2 = SchemaPath.create(true, notificationName2);

        moduleBuilder.addNotification(17, notificationName2, notificationPath2);
    }

    @Test(expected = YangParseException.class)
    public void testAddNotificationWithSameNameAsAlreadyDefinedRpc() throws Exception {
        final QName rpcDefName2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "notify-this");
        SchemaPath rpcPath2 = SchemaPath.create(true, rpcDefName2);
        moduleBuilder.addRpc(17, rpcDefName2, rpcPath2);

        final QName rpcDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "notify-this");
        SchemaPath rpcPath = SchemaPath.create(true, rpcDefName);
        moduleBuilder.addNotification(23, rpcDefName, rpcPath);
    }

    @Test(expected = YangParseException.class)
    public void testAddFeatureIntoNonModuleBuilder() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        final QName testFeature = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-feature");
        SchemaPath featurePath = SchemaPath.create(true, testFeature);

        moduleBuilder.addFeature(23, testFeature, featurePath);
    }

    @Test(expected = YangParseException.class)
    public void testAddDuplicateFeature() throws Exception {
        final QName testFeature = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-feature");
        SchemaPath featurePath = SchemaPath.create(true, testFeature);

        moduleBuilder.addFeature(23, testFeature, featurePath);

        final QName testFeature2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-feature");
        SchemaPath featurePath2 = SchemaPath.create(true, testFeature2);

        moduleBuilder.addFeature(23, testFeature2, featurePath2);
    }

    @Test
    public void testAddCase() throws Exception {

    }

    @Test
    public void testAddTypedef() throws Exception {

    }

    @Test(expected = YangParseException.class)
    public void testSetType() throws Exception {
        final Uint16 uint = Uint16.getInstance();
        moduleBuilder.setType(uint);
    }

    @Test
    public void testAddUnionType() throws Exception {

    }

    @Test
    public void testAddDeviation() throws Exception {

    }

    @Test
    public void testAddIdentity() throws Exception {

    }

    @Test
    public void testAddUnknownNodeBuilder() throws Exception {

    }

    @Test
    public void testAddUnknownSchemaNode() throws Exception {

    }

    @Test
    public void testToString() throws Exception {

    }

    @Test
    public void testSetSource() throws Exception {

    }
}