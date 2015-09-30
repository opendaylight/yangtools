/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

/**
 * Test suite for increasing of test coverage of ModuleBuilder implementation.
 *
 * @see org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder
 *
 * @author Lukas Sedlak &lt;lsedlak@cisco.com&gt;
 *
 * @deprecated Pre-Beryllium implementation, scheduled for removal.
 */
@Deprecated
public class ModuleBuilderTest {

    private final static String TEST_MODULE_NAMESPACE = "urn:opendaylight.foo";
    private final static String TEST_MODULE_REVISION = "2014-09-22";
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
            @Override
            public String getModuleName() {
                return null;
            }

            @Override
            public int getLine() {
                return 0;
            }

            @Override
            public Builder getParent() {
                return null;
            }

            @Override
            public void setParent(final Builder parent) {

            }

            @Override
            public void addUnknownNodeBuilder(final UnknownSchemaNodeBuilder unknownNode) {

            }

            @Override
            public List<UnknownSchemaNodeBuilder> getUnknownNodes() {
                return null;
            }

            @Override
            public Object build() {
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
        moduleBuilder.addGrouping(22, testGroup2, groupPath2);
    }

    @Test
    public void testAddTwoGroupingIntoDataNodeContainer() {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));
        final QName testGroup1 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-group");
        final SchemaPath groupPath1 = SchemaPath.create(true, containerQName, testGroup1);

        final QName testGroup2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-group2");
        final SchemaPath groupPath2 = SchemaPath.create(true, containerQName, testGroup2);

        GroupingBuilder grouping1 = moduleBuilder.addGrouping(12, testGroup1, groupPath1);
        GroupingBuilder grouping2 = moduleBuilder.addGrouping(22, testGroup2, groupPath2);

        assertNotNull(grouping1);
        assertNotNull(grouping2);
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
        moduleBuilder.addGrouping(22, testGroup2, groupPath2);
    }

    @Test
    public void testAddTwoGroupingsIntoRpcDefinition() {
        final QName rpcDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "rpc-definition");
        SchemaPath rpcPath = SchemaPath.create(true, rpcDefName);
        moduleBuilder.enterNode(new RpcDefinitionBuilder(MODULE_NAME, 10, rpcDefName, rpcPath));
        final QName testGroup1 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-group");
        final SchemaPath groupPath1 = SchemaPath.create(true, rpcDefName, testGroup1);

        final QName testGroup2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-group2");
        final SchemaPath groupPath2 = SchemaPath.create(true, rpcDefName, testGroup2);

        GroupingBuilder grouping1 = moduleBuilder.addGrouping(12, testGroup1, groupPath1);
        GroupingBuilder grouping2 = moduleBuilder.addGrouping(22, testGroup2, groupPath2);

        assertNotNull(grouping1);
        assertNotNull(grouping2);
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
    public void testAddTwoFeatures() throws Exception {
        final QName testFeature = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-feature");
        SchemaPath featurePath = SchemaPath.create(true, testFeature);

        final FeatureBuilder feature1 = moduleBuilder.addFeature(23, testFeature, featurePath);

        final QName testFeature2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-feature2");
        SchemaPath featurePath2 = SchemaPath.create(true, testFeature2);

        final FeatureBuilder feature2 = moduleBuilder.addFeature(23, testFeature2, featurePath2);

        assertNotNull(feature1);
        assertNotNull(feature2);
    }

    @Test(expected = YangParseException.class)
    public void testAddCaseWhenParentIsNull() throws Exception {
        moduleBuilder.exitNode();
        addTestCase();
    }

    private void addTestCase() {
        final QName testCase = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-case");
        SchemaPath testCasePath = SchemaPath.create(true, testCase);

        moduleBuilder.addCase(72, testCase, testCasePath);
    }

    @Test(expected = YangParseException.class)
    public void testAddCaseWhenParentIsModule() throws Exception {
        addTestCase();
    }

    @Test(expected = YangParseException.class)
    public void testAddCaseWhenParentIsNotChoiceNorAugmentation() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));
        addTestCase();
    }

    @Test(expected = YangParseException.class)
    public void testAddDuplicateTypedefIntoContainer() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        tryToAddDuplicateTypedef();
    }

    private void tryToAddDuplicateTypedef() {
        final QName testTypedef1 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-type-definition");
        SchemaPath testTypedefPath1 = SchemaPath.create(true, testTypedef1);

        final QName testTypedef2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-type-definition");
        SchemaPath testTypedefPath2 = SchemaPath.create(true, testTypedef2);

        moduleBuilder.addTypedef(23, testTypedef1, testTypedefPath1);
        moduleBuilder.addTypedef(44, testTypedef2, testTypedefPath2);
    }

    @Test(expected = YangParseException.class)
    public void testAddDuplicateTypedefIntoRpcDefinition() throws Exception {
        final QName rpcQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-rpc");
        SchemaPath rpcPath = SchemaPath.create(true, rpcQName);
        moduleBuilder.enterNode(new RpcDefinitionBuilder(MODULE_NAME, 10, rpcQName, rpcPath));

        tryToAddDuplicateTypedef();
    }

    @Test(expected = YangParseException.class)
    public void testAddTypedefIntoNonValidStatement() throws Exception {
        final QName leafName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-leaf");
        SchemaPath leafPath = SchemaPath.create(true, leafName);
        moduleBuilder.enterNode(new LeafSchemaNodeBuilder(MODULE_NAME, 10, leafName, leafPath));

        final QName testRpc = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-type-definition");
        SchemaPath testRpcPath = SchemaPath.create(true, testRpc);

        moduleBuilder.addTypedef(23, testRpc, testRpcPath);
    }

    @Test
    public void testAddTwoTypedefsIntoContainer() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        tryToAddTwoTypedefs();
    }

    private void tryToAddTwoTypedefs() {
        final QName testTypedef1 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-type-definition");
        SchemaPath testTypedefPath1 = SchemaPath.create(true, testTypedef1);

        final QName testTypedef2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-type-definition2");
        SchemaPath testTypedefPath2 = SchemaPath.create(true, testTypedef2);

        final TypeDefinitionBuilderImpl typedef1 = moduleBuilder.addTypedef(23, testTypedef1, testTypedefPath1);
        final TypeDefinitionBuilderImpl typedef2 = moduleBuilder.addTypedef(44, testTypedef2, testTypedefPath2);

        assertNotNull(typedef1);
        assertNotNull(typedef2);
    }

    @Test
    public void testAddTwoTypedefsIntoRpcDefinition() throws Exception {
        final QName rpcQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-rpc");
        SchemaPath rpcPath = SchemaPath.create(true, rpcQName);
        moduleBuilder.enterNode(new RpcDefinitionBuilder(MODULE_NAME, 10, rpcQName, rpcPath));

        tryToAddTwoTypedefs();
    }

    @Test(expected = YangParseException.class)
    public void testSetType() throws Exception {
        final Uint16 uint = Uint16.getInstance();
        moduleBuilder.setType(uint);
    }

    @Test(expected = YangParseException.class)
    public void testAddUnionTypeWhenParentIsNull() throws Exception {
        moduleBuilder.exitNode();

        tryToAddUnionType();
    }

    private void tryToAddUnionType() {
        final QName unionQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "union-type-def");
        moduleBuilder.addUnionType(25, QNameModule.create(unionQName.getNamespace(), unionQName.getRevision()));
    }

    @Test(expected = YangParseException.class)
    public void testAddUnionTypeWhenParentIsNotTypeAwareBuilder() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        tryToAddUnionType();
    }

    @Test(expected = YangParseException.class)
    public void addIdentityrefTypeWhenParentIsNull() throws Exception {
        moduleBuilder.exitNode();
        tryToAddIdentityrefType();
    }

    private void tryToAddIdentityrefType() {
        final QName identityrefQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "identityref-type-def");
        SchemaPath identityrefPath = SchemaPath.create(true, identityrefQName);
        moduleBuilder.addIdentityrefType(25, identityrefPath, "simplebase");
    }

    @Test(expected = YangParseException.class)
    public void addIdentityrefTypeWhenParentIsNotTypeAwareBuilder() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        tryToAddIdentityrefType();
    }

    @Test(expected = YangParseException.class)
    public void testAddDeviationWhereParentIsNotModule() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        final QName deviationQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-deviation-name");
        SchemaPath deviationPath = SchemaPath.create(true, deviationQName);

        moduleBuilder.addDeviation(78, deviationPath);
    }

    @Test(expected = YangParseException.class)
    public void testAddIdentityWhereParentIsNotModule() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        tryToAddIdentity();
    }

    private void tryToAddIdentity() {
        final QName identityQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "test-identity-name");
        SchemaPath identityPath = SchemaPath.create(true, identityQName);

        moduleBuilder.addIdentity(identityQName, 78, identityPath);
    }

    @Test(expected = YangParseException.class)
    public void testAddDuplicateIdentity() throws Exception {
        tryToAddIdentity();
        tryToAddIdentity();
    }

    @Test
    public void testAddUnknownSchemaNodeDirectlyToParent() throws Exception {
        final QName unknownQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "my-extension-use");
        SchemaPath unknownPath = SchemaPath.create(true, unknownQName);
        assertTrue(moduleBuilder.getUnknownNodes().isEmpty());
        moduleBuilder.addUnknownSchemaNode(72, unknownQName, unknownPath);
        assertFalse(moduleBuilder.getUnknownNodes().isEmpty());
    }

    @Test
    public void testAddUnknownSchemaNodeIntoDataNodeContainer() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        final QName unknownQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "my-extension-use");
        SchemaPath unknownPath = SchemaPath.create(true, unknownQName);
        assertTrue(moduleBuilder.getAllUnknownNodes().isEmpty());
        moduleBuilder.addUnknownSchemaNode(72, unknownQName, unknownPath);
        assertFalse(moduleBuilder.getAllUnknownNodes().isEmpty());
    }

    @Test(expected = YangParseException.class)
    public void testAddUnknownSchemaNodeIntoNonSchemaNode() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-non-schema-node");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new NonSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        final QName unknownQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "my-extension-use");
        SchemaPath unknownPath = SchemaPath.create(true, unknownQName);
        assertTrue(moduleBuilder.getUnknownNodes().isEmpty());
        moduleBuilder.addUnknownSchemaNode(72, unknownQName, unknownPath);
        assertFalse(moduleBuilder.getUnknownNodes().isEmpty());
    }

    @Test(expected = YangParseException.class)
    public void testAddContainerWithSameNameAsRpcDefintion() throws Exception {
        final QName rpcDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "message");
        SchemaPath rpcPath = SchemaPath.create(true, rpcDefName);
        moduleBuilder.addRpc(70, rpcDefName, rpcPath);
        tryToAddContainer();
    }

    private void tryToAddContainer() {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "message");
        final SchemaPath containerPath = SchemaPath.create(true, containerQName);

        moduleBuilder.addContainerNode(22, containerQName, containerPath);
    }

    @Test(expected = YangParseException.class)
    public void testAddContainerWithSameNameAsNotificationDefintion() throws Exception {
        final QName notificationName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "message");
        SchemaPath notificationPath = SchemaPath.create(true, notificationName);
        moduleBuilder.addNotification(70, notificationName, notificationPath);
        tryToAddContainer();
    }

    @Test
    public void testAddContainerWithAlreadyAddedRpcDefintion() throws Exception {
        final QName rpcDefName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "send-message");
        SchemaPath rpcPath = SchemaPath.create(true, rpcDefName);
        moduleBuilder.addRpc(70, rpcDefName, rpcPath);
        tryToAddContainer();

        DataSchemaNodeBuilder addedContainer = moduleBuilder.getDataChildByName("message");
        assertNotNull(addedContainer);
    }

    @Test
    public void testAddContainerWithAlreadyAddedNotificationDefintion() throws Exception {
        final QName notificationName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "notify-me");
        SchemaPath notificationPath = SchemaPath.create(true, notificationName);
        moduleBuilder.addNotification(70, notificationName, notificationPath);
        tryToAddContainer();

        DataSchemaNodeBuilder addedContainer = moduleBuilder.getDataChildByName("message");
        assertNotNull(addedContainer);
    }

    @Test(expected = YangParseException.class)
    public void testAddDuplicateSchemaNodeIntoSubnodeOfModule() throws Exception {
        final QName containerQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        moduleBuilder.enterNode(new ContainerSchemaNodeBuilder(MODULE_NAME, 10, containerQName, containerPath));

        final QName leafQName1 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "simple-leaf");
        SchemaPath leafPath1 = SchemaPath.create(true, containerQName, leafQName1);

        final QName leafQName2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "simple-leaf");
        SchemaPath leafPath2 = SchemaPath.create(true, containerQName, leafQName2);

        moduleBuilder.addLeafNode(12, leafQName1, leafPath1);
        moduleBuilder.addLeafNode(20, leafQName2, leafPath2);
    }

    @Test(expected = YangParseException.class)
    public void testAddDuplicateCaseIntoChoiceCaseNode() throws Exception {
        //FIXME: move this into ChoiceBuilderTest !!!
        final QName choiceQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "top-level-container");
        SchemaPath choicePath = SchemaPath.create(true, choiceQName);

        moduleBuilder.enterNode(new ChoiceBuilder(MODULE_NAME, 10, choiceQName, choicePath));

        final QName caseQName1 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "simple-case");
        SchemaPath casePath1 = SchemaPath.create(true, choiceQName, caseQName1);

        final QName caseQName2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "simple-case");
        SchemaPath casePath2 = SchemaPath.create(true, choiceQName, caseQName2);

        moduleBuilder.addCase(12, caseQName1, casePath1);
        moduleBuilder.addCase(20, caseQName2, casePath2);
    }

    @Test(expected = YangParseException.class)
    public void testAddChoiceWithSameNameAsCase() throws Exception {
        final QName choiceQName = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "my-choice");
        SchemaPath choicePath = SchemaPath.create(true, choiceQName);

        final ChoiceBuilder choiceBuilder = moduleBuilder.addChoice(8, choiceQName, choicePath);
        moduleBuilder.enterNode(choiceBuilder);

        final QName caseQName1 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "simple-case");
        SchemaPath casePath1 = SchemaPath.create(true, choiceQName, caseQName1);

        final QName caseQName2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "simple-case");
        SchemaPath casePath2 = SchemaPath.create(true, choiceQName, caseQName2);

        moduleBuilder.addCase(12, caseQName1, casePath1);
        moduleBuilder.addChoice(20, caseQName2, casePath2);
    }

    @Test(expected = YangParseException.class)
    public void testAddChildToSubnodeForNonDataNodeContainerAndNonChoiceNode() {
        final QName leafQName1 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "simple-leaf");
        SchemaPath leafPath1 = SchemaPath.create(true, leafQName1);

        final LeafSchemaNodeBuilder leafBuilder = moduleBuilder.addLeafNode(10, leafQName1, leafPath1);
        moduleBuilder.enterNode(leafBuilder);

        final QName leafQName2 = QName.create(TEST_MODULE_NAMESPACE, TEST_MODULE_REVISION, "simple-leaf");
        SchemaPath leafPath2 = SchemaPath.create(true, leafQName1, leafQName2);

        moduleBuilder.addLeafNode(12, leafQName2, leafPath2);
    }

    private static class NonSchemaNodeBuilder implements Builder {

        String moduleName;
        final int line;
        final QName name;
        final SchemaPath path;

        public NonSchemaNodeBuilder(final String moduleName, final int line, final QName name, final SchemaPath path) {
            this.moduleName = moduleName;
            this.line = line;
            this.name = name;
            this.path = path;
        }

        @Override
        public String getModuleName() {
            return moduleName;
        }

        @Override
        public int getLine() {
            return line;
        }

        @Override
        public Builder getParent() {
            return null;
        }

        @Override
        public void setParent(final Builder parent) {

        }

        @Override
        public void addUnknownNodeBuilder(final UnknownSchemaNodeBuilder unknownNode) {

        }

        @Override
        public List<UnknownSchemaNodeBuilder> getUnknownNodes() {
            return null;
        }

        @Override
        public Object build() {
            return null;
        }
    }
}