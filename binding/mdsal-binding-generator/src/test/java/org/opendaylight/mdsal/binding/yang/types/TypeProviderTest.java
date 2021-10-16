/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Range;
import java.util.List;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.generator.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Test suite for testing public methods in TypeProviderImpl class.
 *
 * @see org.opendaylight.mdsal.binding.yang.types.AbstractTypeProvider
 * @author Lukas Sedlak &lt;lsedlak@cisco.com&gt;
 */
// FIXME: rewrite tests without TypeProvider interface
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class TypeProviderTest {
    static EffectiveModelContext SCHEMA_CONTEXT;
    static Module TEST_TYPE_PROVIDER;

    @Mock
    public SchemaPath schemaPath;

    @Mock
    public SchemaNode schemaNode;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResources(TypeProviderTest.class,
            "/base-yang-types.yang", "/test-type-provider-b.yang", "/test-type-provider.yang");
        TEST_TYPE_PROVIDER = resolveModule("test-type-provider");
    }

    @AfterClass
    public static void afterClass() {
        TEST_TYPE_PROVIDER = null;
        SCHEMA_CONTEXT = null;
    }

    private static Module resolveModule(final String moduleName) {
        return SCHEMA_CONTEXT.findModules(moduleName).iterator().next();
    }

    private static LeafSchemaNode provideLeafNodeFromTopLevelContainer(final Module module, final String containerName,
            final String leafNodeName) {
        final QName containerNode = QName.create(module.getQNameModule(), containerName);
        final DataSchemaNode rootNode = module.findDataChildByName(containerNode).get();
        assertTrue(rootNode instanceof DataNodeContainer);

        final QName leafNode = QName.create(module.getQNameModule(), leafNodeName);
        final DataNodeContainer rootContainer = (DataNodeContainer) rootNode;
        final DataSchemaNode node = rootContainer.findDataChildByName(leafNode).get();
        assertTrue(node instanceof LeafSchemaNode);
        return (LeafSchemaNode) node;
    }

    @Test
    public void javaTypeForSchemaDefinitionExtTypeTest() {
        final AbstractTypeProvider provider = new CodegenTypeProvider(SCHEMA_CONTEXT);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(TEST_TYPE_PROVIDER, "foo",
            "yang-int8-type");

        final TypeDefinition<?> leafType = leaf.getType();
        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);

        final GeneratedTransferObject genTO = (GeneratedTransferObject) result;
        assertEquals("base-yang-types", genTO.getModuleName());
        assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914",
            genTO.getPackageName());
        assertEquals("YangInt8", genTO.getName());
        assertEquals(1, genTO.getProperties().size());
    }

    @Test
    public void javaTypeForSchemaDefinitionRestrictedExtTypeTest() {
        final AbstractTypeProvider provider = new CodegenTypeProvider(SCHEMA_CONTEXT);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(TEST_TYPE_PROVIDER, "foo",
            "restricted-int8-type");

        final TypeDefinition<?> leafType = leaf.getType();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(leafType);

        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf, restrictions);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);

        final GeneratedTransferObject genTO = (GeneratedTransferObject) result;
        assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914",
            genTO.getPackageName());
        assertEquals("YangInt8Restricted", genTO.getName());
        assertEquals(1, genTO.getProperties().size());
        final Optional<? extends RangeConstraint<?>> rangeConstraints = genTO.getRestrictions().getRangeConstraint();

        assertTrue(rangeConstraints.isPresent());
        final Range<?> constraint = rangeConstraints.get().getAllowedRanges().asRanges().iterator().next();
        assertEquals((byte) 1, constraint.lowerEndpoint());
        assertEquals((byte) 100, constraint.upperEndpoint());
    }

    @Test
    public void javaTypeForSchemaDefinitionEnumExtTypeTest() {
        final AbstractTypeProvider provider = new CodegenTypeProvider(SCHEMA_CONTEXT);
        LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(TEST_TYPE_PROVIDER, "foo", "resolve-enum-leaf");
        TypeDefinition<?> leafType = leaf.getType();
        Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof Enumeration);

        final Enumeration enumType = (Enumeration) result;
        final List<Enumeration.Pair> enumValues = enumType.getValues();
        assertTrue(!enumValues.isEmpty());
        assertEquals("a", enumValues.get(0).getName());
        assertEquals("b", enumValues.get(1).getName());
        assertEquals("A", enumValues.get(0).getMappedName());
        assertEquals("B", enumValues.get(1).getMappedName());

        leaf = provideLeafNodeFromTopLevelContainer(TEST_TYPE_PROVIDER, "foo", "resolve-direct-use-of-enum");
        leafType = leaf.getType();
        result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof ConcreteType);

        assertEquals("java.lang", result.getPackageName());
        assertEquals("Enum", result.getName());
    }

    @Test
    public void javaTypeForSchemaDefinitionLeafrefExtTypeTest() {
        final AbstractTypeProvider provider = new CodegenTypeProvider(SCHEMA_CONTEXT);
        LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(TEST_TYPE_PROVIDER, "bar", "leafref-value");
        TypeDefinition<?> leafType = leaf.getType();
        final Type leafrefResolvedType1 = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(leafrefResolvedType1);
        assertTrue(leafrefResolvedType1 instanceof GeneratedTransferObject);

        final Module module = resolveModule("test-type-provider-b");
        final QName leafNode = QName.create(module.getQNameModule(), "id");
        final DataSchemaNode rootNode = module.findDataChildByName(leafNode).get();
        assertNotNull("leaf id is not present in root of module " + module.getName(), rootNode);
        assertTrue(rootNode instanceof LeafSchemaNode);
        leaf = (LeafSchemaNode) rootNode;
        leafType = leaf.getType();

        final Type leafrefResolvedType2 = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(leafrefResolvedType2);
        assertTrue(leafrefResolvedType2 instanceof GeneratedTransferObject);
    }

    @Test
    public void javaTypeForSchemaDefinitionConditionalLeafrefTest() {
        final AbstractTypeProvider provider = new CodegenTypeProvider(SCHEMA_CONTEXT);
        final Module module = resolveModule("test-type-provider-b");

        final QName leafrefNode = QName.create(module.getQNameModule(), "conditional-leafref");
        final DataSchemaNode condLeaf = module.findDataChildByName(leafrefNode).get();
        assertTrue(condLeaf instanceof LeafSchemaNode);
        final LeafSchemaNode leaf = (LeafSchemaNode) condLeaf;
        final TypeDefinition<?> leafType = leaf.getType();

        final Type resultType = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(resultType);
        assertTrue(resultType instanceof ConcreteType);
        assertEquals("java.lang", resultType.getPackageName());
        assertEquals("Object", resultType.getName());
    }
}
