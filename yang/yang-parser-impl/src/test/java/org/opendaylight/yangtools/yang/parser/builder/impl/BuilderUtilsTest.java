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
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

import com.google.common.base.Optional;

/**
 * Test suite for increasing of test coverage of BuilderUtils implementation.
 *
 * @see org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
public class BuilderUtilsTest {

    private final static String MASTER_MODULE_NAMESPACE = "urn:opendaylight.master-model";
    private final static String MODULES_REVISION = "2014-10-06";
    private final static String MASTER_MODULE = "master-module";
    private final static String DEPENDENT_MODULE_NAMESPACE = "urn:opendaylight.secondary-model";
    private final static String DEPENDENT_MODULE = "depend-module";
    private final static String MASTER_MODULE_PATH = "test/module/path/master-module@" + MODULES_REVISION + ".yang";
    private final static String DEPENDENT_MODULE_PATH = "test/module/path/depend-module@" + MODULES_REVISION + ".yang";
    private final static String DEPENDENT_MODULE_PREFIX = "dep";

    private final static DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-mm-dd");

    private ModuleBuilder masterModule;
    private ModuleBuilder dependentModule;

    @Before
    public void setUp() throws Exception {
        masterModule = new ModuleBuilder(MASTER_MODULE, MASTER_MODULE_PATH);
        dependentModule = new ModuleBuilder(DEPENDENT_MODULE, DEPENDENT_MODULE_PATH);

        final Date moduleRevision = SIMPLE_DATE_FORMAT.parse(MODULES_REVISION);
        masterModule.setRevision(moduleRevision);
        dependentModule.setRevision(moduleRevision);

        masterModule.setNamespace(URI.create(MASTER_MODULE_NAMESPACE));
        dependentModule.setNamespace(URI.create(DEPENDENT_MODULE_NAMESPACE));

        masterModule.setNamespace(URI.create(MASTER_MODULE_NAMESPACE));
        dependentModule.setNamespace(URI.create(DEPENDENT_MODULE_NAMESPACE));

        masterModule.setPrefix("mod");
        dependentModule.setPrefix("mod");
        masterModule.addModuleImport(dependentModule.getModuleName(), dependentModule.getRevision(), DEPENDENT_MODULE_PREFIX);
    }

    @Test
    public void testFindModuleFromBuildersWithNullPrefix() throws Exception {
        final Map<String, NavigableMap<Date, ModuleBuilder>> testModules = initModuleBuildersForTest();

        ModuleBuilder result = BuilderUtils.findModuleFromBuilders(testModules, masterModule, null, 12);
        assertEquals(masterModule, result);

        result = BuilderUtils.findModuleFromBuilders(testModules, masterModule, masterModule.getPrefix(), 12);
        assertEquals(masterModule, result);

        result = BuilderUtils.findModuleFromBuilders(testModules, masterModule, DEPENDENT_MODULE_PREFIX, 12);
        assertEquals(dependentModule, result);
    }

    private Map<String, NavigableMap<Date, ModuleBuilder>> initModuleBuildersForTest() throws Exception {
        final Map<String, NavigableMap<Date, ModuleBuilder>> modules = new HashMap<>();
        final String module3Name = "Module3";

        ModuleBuilder module3 = new ModuleBuilder(module3Name, "test/module/path/module3.yang");
        final Date moduleRevision = SIMPLE_DATE_FORMAT.parse(MODULES_REVISION);

        module3.setRevision(moduleRevision);
        module3.setNamespace(URI.create("urn:opendaylight.ternary-model"));
        module3.setPrefix("mod");

        dependentModule.addModuleImport(module3.getModuleName(), module3.getRevision(), "ter");

        final NavigableMap<Date, ModuleBuilder> module1Map = new TreeMap<>();
        module1Map.put(masterModule.getRevision(), masterModule);

        final NavigableMap<Date, ModuleBuilder> module2Map = new TreeMap<>();
        module2Map.put(dependentModule.getRevision(), dependentModule);

        final NavigableMap<Date, ModuleBuilder> module3Map = new TreeMap<>();
        module3Map.put(module3.getRevision(), module3);

        modules.put(masterModule.getName(), module1Map);
        modules.put(dependentModule.getName(), module2Map);
        modules.put(module3Name, module3Map);

        return modules;
    }

    @Test(expected = YangParseException.class)
    public void testFindModuleFromBuildersWithNoImportedModule() throws Exception {
        final Map<String, NavigableMap<Date, ModuleBuilder>> testModules = initModuleBuildersForTest();

        BuilderUtils.findModuleFromBuilders(testModules, masterModule, "eth", 12);
    }

    @Test(expected = YangParseException.class)
    public void testFindModuleFromContextWithDependentModuleImportEqualsToNull() {
        final SchemaContext mockContext = mock(SchemaContext.class);

        BuilderUtils.findModuleFromContext(mockContext, masterModule, "inalid-prefix", 14);
    }

    @Test(expected = YangParseException.class)
    public void testFindModuleFromContextWhereModuleIsNotPresent() {
        final SchemaContext mockContext = mock(SchemaContext.class);

        BuilderUtils.findModuleFromContext(mockContext, masterModule, DEPENDENT_MODULE_PREFIX, 14);
    }

    @Test(expected = YangParseException.class)
    public void testFillAugmentTargetForInvalidTarget() {
        AugmentationSchemaBuilder augBuilder = mock(AugmentationSchemaBuilder.class);
        Builder invalidTarget = mock(Builder.class);
        BuilderUtils.fillAugmentTarget(augBuilder, invalidTarget);
    }

    @Test(expected = YangParseException.class)
    public void testFillAugmentTargetWithChoiceBuilderContainingInvalidUsesNodeBuilder() {
        final QName usesQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "test-grouping");
        final UsesNodeBuilder usesNodeBuilder = new UsesNodeBuilderImpl(masterModule.getModuleName(), 10, SchemaPath.create(true, usesQName));

        final QName augTargetQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "target");
        final AugmentationSchemaBuilder augBuilder = new AugmentationSchemaBuilderImpl(masterModule.getModuleName(), 12,
            "/target", SchemaPath.create(true, augTargetQName), 1);

        augBuilder.setParent(usesNodeBuilder);

        final QName choiceQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "test-choice");
        final ChoiceBuilder choiceBuilder = new ChoiceBuilder(masterModule.getModuleName(), 14, choiceQName,
            SchemaPath.create(true, choiceQName));

        augBuilder.addUsesNode(usesNodeBuilder);
        BuilderUtils.fillAugmentTarget(augBuilder, choiceBuilder);
    }

    @Test
    public void testFillAugmentTargetSetNodeAddedByUses() {
        final QName augTargetQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "target");
        final AugmentationSchemaBuilder augBuilder = new AugmentationSchemaBuilderImpl(masterModule.getModuleName(), 12,
            "/target", SchemaPath.create(true, augTargetQName), 1);

        final String containerLocalName = "top-level-container";
        final QName containerQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), containerLocalName);
        SchemaPath containerPath = SchemaPath.create(true, containerQName);
        augBuilder.addChildNode(
            new ContainerSchemaNodeBuilder(masterModule.getModuleName(), 10, containerQName, containerPath));

        final QName choiceQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "test-choice");
        final ChoiceBuilder choiceBuilder = new ChoiceBuilder(masterModule.getModuleName(), 14, choiceQName,
            SchemaPath.create(true, choiceQName));

        BuilderUtils.fillAugmentTarget(augBuilder, choiceBuilder);

        ChoiceCaseBuilder result = choiceBuilder.getCaseNodeByName(containerLocalName);
        assertNotNull(result);
    }

    @Test
    public void testFindUnknownNodeInDataNodeContainer() {
        final String parentLocalName = "parent";
        final QName containerQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), parentLocalName);
        SchemaPath containerPath = SchemaPath.create(true, containerQName);

        final ContainerSchemaNodeBuilder containerBuilder = new ContainerSchemaNodeBuilder(masterModule.getModuleName(), 10,
            containerQName, containerPath);

        final String unknownLocalName = "unknown-ext-use";
        final QName unknownNode = QName.create(masterModule.getNamespace(), masterModule.getRevision(), unknownLocalName);
        SchemaPath unknownNodePath = SchemaPath.create(true, containerQName, unknownNode);
        UnknownSchemaNodeBuilderImpl unknownNodeBuilder = new UnknownSchemaNodeBuilderImpl(masterModule.getModuleName(),
            22, unknownNode, unknownNodePath);
        containerBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        List<QName> path = new ArrayList<>(2);
        path.add(unknownNode);

        SchemaNodeBuilder result = BuilderUtils.findSchemaNode(path, containerBuilder);
        assertNotNull(result);
        assertEquals(unknownNodeBuilder, result);

        path.add(QName.create(masterModule.getNamespace(), masterModule.getRevision(), "foo"));
        assertNull(BuilderUtils.findSchemaNode(path, containerBuilder));

        final QName choiceQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "select");
        SchemaPath choicePath = SchemaPath.create(true, choiceQName);

        final ChoiceBuilder choiceBuilder = new ChoiceBuilder(masterModule.getModuleName(), 33, choiceQName, choicePath);

        final QName caseQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "simple-case");
        SchemaPath casePath = SchemaPath.create(true, choiceQName, caseQName);

        ChoiceCaseBuilder caseBuilder = new ChoiceCaseBuilder(masterModule.getModuleName(), 35, caseQName, casePath);
        choiceBuilder.addCase(caseBuilder);

        choiceBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        path.clear();
        path.add(caseQName);
        result = BuilderUtils.findSchemaNode(path, choiceBuilder);
        assertNotNull(result);
        assertTrue(result instanceof ChoiceCaseBuilder);
        assertEquals(caseBuilder, result);

        path.clear();
        path.add(unknownNode);
        result = BuilderUtils.findSchemaNode(path, choiceBuilder);
        assertNotNull(result);
        assertEquals(unknownNodeBuilder, result);

        path.add(QName.create(masterModule.getNamespace(), masterModule.getRevision(), "foo"));
        result = BuilderUtils.findSchemaNode(path, choiceBuilder);
        assertNull(result);

        final QName rpcQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), parentLocalName);
        SchemaPath rpcPath = SchemaPath.create(true, rpcQName);
        final RpcDefinitionBuilder rpcBuilder = new RpcDefinitionBuilder(masterModule.getModuleName(), 45, rpcQName, rpcPath);

        final QName inputQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "input");
        final SchemaPath inputPath = SchemaPath.create(true, rpcQName, inputQName);

        final QName outputQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "output");
        final SchemaPath outputPath = SchemaPath.create(true, rpcQName, outputQName);

        final ContainerSchemaNodeBuilder inputBuilder = new ContainerSchemaNodeBuilder(masterModule.getModuleName(), 46,
            inputQName, inputPath);
        final ContainerSchemaNodeBuilder outputBuilder = new ContainerSchemaNodeBuilder(masterModule.getModuleName(), 76,
            outputQName, outputPath);

        rpcBuilder.setInput(inputBuilder);
        rpcBuilder.setOutput(outputBuilder);
        rpcBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        path.clear();
        path.add(inputQName);
        result = BuilderUtils.findSchemaNode(path, rpcBuilder);
        assertNotNull(result);
        assertEquals(inputBuilder, result);

        path.clear();
        path.add(outputQName);
        result = BuilderUtils.findSchemaNode(path, rpcBuilder);
        assertNotNull(result);
        assertEquals(outputBuilder, result);

        path.clear();
        path.add(unknownNode);
        result = BuilderUtils.findSchemaNode(path, rpcBuilder);
        assertNotNull(result);
        assertEquals(unknownNodeBuilder, result);

        final QName leafQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "simple-leaf");
        SchemaPath leafPath = SchemaPath.create(true, leafQName);

        final LeafSchemaNodeBuilder leafBuilder = new LeafSchemaNodeBuilder(masterModule.getModuleName(), 10, leafQName, leafPath);
        leafBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        path.clear();
        path.add(unknownNode);
        result = BuilderUtils.findSchemaNode(path, leafBuilder);
        assertNotNull(result);
        assertEquals(unknownNodeBuilder, result);
    }

    @Test
    public void testFindUnknownSchemaNodeInModule() {
        final String parentLocalName = "parent";
        final QName containerQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), parentLocalName);
        SchemaPath containerPath = SchemaPath.create(true, containerQName);

        final ContainerSchemaNodeBuilder containerBuilder = new ContainerSchemaNodeBuilder(masterModule.getModuleName(), 10,
            containerQName, containerPath);

        final String unknownLocalName = "unknown-ext-use";
        final QName unknownNode = QName.create(masterModule.getNamespace(), masterModule.getRevision(), unknownLocalName);
        final SchemaPath unknownNodePath = SchemaPath.create(true, containerQName, unknownNode);
        UnknownSchemaNodeBuilderImpl unknownNodeBuilder = new UnknownSchemaNodeBuilderImpl(masterModule.getModuleName(),
            22, unknownNode, unknownNodePath);
        containerBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        masterModule.addChildNode(containerBuilder);

        Optional<SchemaNodeBuilder> result = BuilderUtils.findSchemaNodeInModule(unknownNodePath, masterModule);

        assertTrue(result.isPresent());
        assertEquals(result.get(), unknownNodeBuilder);

        final QName invalidUnknownNode = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "invalid-ext-use");
        final SchemaPath invalidPath = SchemaPath.create(true, containerQName, invalidUnknownNode);

        result = BuilderUtils.findSchemaNodeInModule(invalidPath, masterModule);
        assertFalse(result.isPresent());
    }

    @Test
    public void testFindContainerInRPC() {

        final QName rpcQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "send-message");
        SchemaPath rpcPath = SchemaPath.create(true, rpcQName);
        final RpcDefinitionBuilder rpcBuilder = masterModule.addRpc(45, rpcQName, rpcPath);

        final QName inputQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "input");
        final SchemaPath inputPath = SchemaPath.create(true, rpcQName, inputQName);

        final QName outputQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "output");
        final SchemaPath outputPath = SchemaPath.create(true, rpcQName, outputQName);

        Optional<SchemaNodeBuilder> requestResult = BuilderUtils.findSchemaNodeInModule(inputPath, masterModule);
        assertTrue(requestResult.isPresent());

        Optional<SchemaNodeBuilder> responseResult = BuilderUtils.findSchemaNodeInModule(outputPath, masterModule);
        assertTrue(responseResult.isPresent());

        final ContainerSchemaNodeBuilder inputBuilder = new ContainerSchemaNodeBuilder(masterModule.getModuleName(), 46,
            inputQName, inputPath);
        final ContainerSchemaNodeBuilder outputBuilder = new ContainerSchemaNodeBuilder(masterModule.getModuleName(), 76,
            outputQName, outputPath);

        final QName request = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "request");
        final SchemaPath requestPath = SchemaPath.create(true, rpcQName, inputQName, request);

        final QName response = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "response");
        final SchemaPath responsePath = SchemaPath.create(true, rpcQName, outputQName, response);

        final ContainerSchemaNodeBuilder requestBuilder = new ContainerSchemaNodeBuilder(masterModule.getModuleName(), 50,
            request, requestPath);
        final ContainerSchemaNodeBuilder responseBuilder = new ContainerSchemaNodeBuilder(masterModule.getModuleName(), 80,
            response, responsePath);

        inputBuilder.addChildNode(requestBuilder);
        outputBuilder.addChildNode(responseBuilder);

        rpcBuilder.setInput(inputBuilder);
        rpcBuilder.setOutput(outputBuilder);

        requestResult = BuilderUtils.findSchemaNodeInModule(requestPath, masterModule);
        assertTrue(requestResult.isPresent());

        responseResult = BuilderUtils.findSchemaNodeInModule(responsePath, masterModule);
        assertTrue(responseResult.isPresent());

        final SchemaPath invalidPath = SchemaPath.create(true, rpcQName, response);
        Optional<SchemaNodeBuilder> invalidResult = BuilderUtils.findSchemaNodeInModule(invalidPath, masterModule);
        assertFalse(invalidResult.isPresent());
    }

    @Test
    public void testFindIdentity() {
        assertNull(BuilderUtils.findIdentity(Collections.<IdentitySchemaNodeBuilder>emptySet(), "test-identity"));
    }

    @Test
    public void testGetModuleByPrefix() {
        assertEquals(BuilderUtils.getModuleByPrefix(masterModule, null), masterModule);
        assertEquals(BuilderUtils.getModuleByPrefix(masterModule, ""), masterModule);
        assertEquals(BuilderUtils.getModuleByPrefix(masterModule, masterModule.getPrefix()), masterModule);
    }

    @Test
    public void testFindModule() {
        final Map<URI, NavigableMap<Date, ModuleBuilder>> modules = new HashMap<>(1);
        final NavigableMap<Date, ModuleBuilder> masterModuleMap = new TreeMap<>();
        masterModuleMap.put(masterModule.getRevision(), masterModule);
        modules.put(masterModule.getNamespace(), masterModuleMap);

        assertNull(BuilderUtils.findModule(QName.create("test-urn:namespace", "2014-10-08", "impossible-module"), modules));

        assertEquals(BuilderUtils.findModule(QName.create(masterModule.getNamespace(), null, masterModule.getName()), modules),
            masterModule);
    }

    @Test
    public void testFindBaseIdentity() {
        assertNull(BuilderUtils.findBaseIdentity(masterModule, "prefix:ident", 27));

        final QName identity = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "ident");
        masterModule.addIdentity(identity, 22, SchemaPath.create(true, identity));

        final IdentitySchemaNodeBuilder result = BuilderUtils
            .findBaseIdentity(masterModule, masterModule.getPrefix() + ":ident", 22);
        assertNotNull(result);
        assertEquals(result.getQName(), identity);
    }

    @Test
    public void testWrapChildNode() {
        final QName leafQName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "parent-leaf");
        final SchemaPath leafPath = SchemaPath.create(true, leafQName);
        final LeafListSchemaNodeBuilder leafListBuilder = new LeafListSchemaNodeBuilder(masterModule.getModuleName(),
            27, leafQName, leafPath);
        leafListBuilder.setType(Uint16.getInstance());

        final LeafListSchemaNode leafList = leafListBuilder.build();

        DataSchemaNodeBuilder wrapedLeafList = BuilderUtils
            .wrapChildNode(masterModule.getModuleName(), 72, leafList, leafPath, leafQName);

        assertNotNull(wrapedLeafList);
        assertEquals(wrapedLeafList.getQName(), leafList.getQName());
    }

    @Test(expected = YangParseException.class)
    public void wrapChildNodeForUnknownNode() {
        final QName qName = QName.create(masterModule.getNamespace(), masterModule.getRevision(), "name");
        final SchemaPath path = SchemaPath.create(true, qName);

        BuilderUtils
            .wrapChildNode(masterModule.getModuleName(), 72, new NotExistingDataSchemaNodeImpl(), path, qName);
    }

    private static class NotExistingDataSchemaNodeImpl implements DataSchemaNode {

        @Override public boolean isAugmenting() {
            return false;
        }

        @Override public boolean isAddedByUses() {
            return false;
        }

        @Override public boolean isConfiguration() {
            return false;
        }

        @Override public ConstraintDefinition getConstraints() {
            return null;
        }

        @Override public QName getQName() {
            return null;
        }

        @Override public SchemaPath getPath() {
            return null;
        }

        @Override public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return null;
        }

        @Override public String getDescription() {
            return null;
        }

        @Override public String getReference() {
            return null;
        }

        @Override public Status getStatus() {
            return null;
        }
    }
}
