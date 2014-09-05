/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil;
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.*;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * Test suite for testing public methods in TypeProviderImpl class
 *
 * @see org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
@RunWith(JUnit4.class)
public class TypeProviderTest {

    private SchemaContext schemaContext;
    private Set<Module> schemaModules;
    private Module testTypeProviderModule;

    @Mock
    private SchemaPath schemaPath;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        schemaContext = TypeProviderModel.createTestContext();
        assertNotNull(schemaContext);
        schemaModules = schemaContext.getModules();
        assertNotNull(schemaModules);
        testTypeProviderModule = resolveModule(TypeProviderModel.TEST_TYPE_PROVIDER_MODULE_NAME);
        assertNotNull(testTypeProviderModule);
    }

    private Module resolveModule(final String moduleName) {
        assertNotNull(moduleName);
        for (Module m : schemaModules) {
            if (moduleName.equals(m.getName())) {
                return m;
            }
        }
        assertTrue("Unable to resolve module " + moduleName + ". No module present within Schema Context!" , false);
        return null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void typeProviderInstanceWithNullSchemaContextTest() {
        final TypeProvider provider = new TypeProviderImpl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void putReferencedTypeWithNullParamsTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);

        ((TypeProviderImpl) provider).putReferencedType(null, null);
        ((TypeProviderImpl) provider).putReferencedType(schemaPath, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void javaTypeForSchemaDefinitionTypeNullTypedefTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);

        provider.javaTypeForSchemaDefinitionType(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void javaTypeForSchemaDefinitionTypeTypedefNullQNameTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);

        TestIntegerTypeDefinition testTypedef = new TestIntegerTypeDefinition();
        provider.javaTypeForSchemaDefinitionType(testTypedef, null, null);
    }

    private LeafSchemaNode provideLeafNodeFromTopLevelContainer(final Module module, final String containerName, final String leafNodeName) {
        final DataSchemaNode rootNode = module.getDataChildByName(containerName);
        assertNotNull("Container foo is not present in root of module "+ module.getName(), rootNode);
        assertTrue(rootNode instanceof DataNodeContainer);

        final DataNodeContainer rootContainer = (DataNodeContainer) rootNode;
        final DataSchemaNode node = rootContainer.getDataChildByName(leafNodeName);
        assertNotNull(node);
        assertTrue(node instanceof LeafSchemaNode);
        return (LeafSchemaNode) node;
    }

    @Test
    public void javaTypeForSchemaDefinitionExtTypeTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(testTypeProviderModule, "foo", "yang-int8-type");

        final TypeDefinition<?> leafType = leaf.getType();
        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);

        final GeneratedTransferObject genTO = (GeneratedTransferObject) result;
        assertEquals("base-yang-types", genTO.getModuleName());
        assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914", genTO.getPackageName());
        assertEquals("YangInt8", genTO.getName());
        assertTrue(genTO.getProperties().size() == 1);
    }

    @Test
    public void javaTypeForSchemaDefinitionRestrictedExtTypeTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(testTypeProviderModule, "foo", "restricted-int8-type");

        final TypeDefinition<?> leafType = leaf.getType();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(leafType);

        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf, restrictions);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);

        final GeneratedTransferObject genTO = (GeneratedTransferObject) result;
        assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914", genTO.getPackageName());
        assertEquals("YangInt8Restricted", genTO.getName());
        assertTrue(genTO.getProperties().size() == 1);
        final List<RangeConstraint> rangeConstraints = genTO.getRestrictions().getRangeConstraints();

        assertTrue(!rangeConstraints.isEmpty());
        final RangeConstraint constraint = rangeConstraints.get(0);
        assertEquals(BigInteger.ONE, constraint.getMin());
        assertEquals(BigInteger.valueOf(100), constraint.getMax());
    }

    /**
     * FIXME: Remove @Ignore annotation once the bug https://bugs.opendaylight.org/show_bug.cgi?id=1862 is fixed
     */
    @Ignore
    @Test
    public void bug1862RestrictedTypedefTransformationTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(testTypeProviderModule, "foo", "bug-1862-restricted-typedef");

        final TypeDefinition<?> leafType = leaf.getType();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(leafType);
        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf, restrictions);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);
        //TODO: complete test after bug 1862 is fixed
    }

    @Test
    public void javaTypeForSchemaDefinitionEnumExtTypeTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);
        LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(testTypeProviderModule, "foo", "resolve-enum-leaf");
        TypeDefinition<?> leafType = leaf.getType();
        Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof Enumeration);

        final Enumeration enumType = (Enumeration) result;
        final List<Enumeration.Pair> enumValues = enumType.getValues();
        assertTrue(!enumValues.isEmpty());
        assertEquals("A", enumValues.get(0).getName());
        assertEquals("B", enumValues.get(1).getName());

        leaf = provideLeafNodeFromTopLevelContainer(testTypeProviderModule, "foo", "resolve-direct-use-of-enum");
        leafType = leaf.getType();
        result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof ConcreteType);

        assertEquals("java.lang", result.getPackageName());
        assertEquals("Enum", result.getName());
    }

    @Test
    public void javaTypeForSchemaDefinitionLeafrefExtTypeTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);
        LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(testTypeProviderModule, "bar", "leafref-value");
        TypeDefinition<?> leafType = leaf.getType();
        final Type leafrefResolvedType1 = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(leafrefResolvedType1);
        assertTrue(leafrefResolvedType1 instanceof GeneratedTransferObject);

        final Module module = resolveModule("test-type-provider-b");
        final DataSchemaNode rootNode = module.getDataChildByName("id");
        assertNotNull("leaf id is not present in root of module "+ module.getName(), rootNode);
        assertTrue(rootNode instanceof LeafSchemaNode);
        leaf = (LeafSchemaNode) rootNode;
        leafType = leaf.getType();

        final Type leafrefResolvedType2 = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(leafrefResolvedType2);
        assertTrue(leafrefResolvedType2 instanceof GeneratedTransferObject);
    }

    @Test
    public void javaTypeForSchemaDefinitionLeafrefToEnumTypeTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);

        setReferencedTypeForTypeProvider(provider);

        final Module module = resolveModule("test-type-provider-b");

        final DataSchemaNode enumNode = module.getDataChildByName("enum");
        assertNotNull("leaf enum is not present in root of module "+ module.getName(), enumNode);
        assertTrue(enumNode instanceof LeafSchemaNode);
        LeafSchemaNode leaf = (LeafSchemaNode) enumNode;
        final TypeDefinition<?> leafType = leaf.getType();

        final Type leafrefResolvedType1 = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(leafrefResolvedType1);
        assertTrue(leafrefResolvedType1 instanceof ReferencedTypeImpl);

        final DataSchemaNode enumListNode = module.getDataChildByName("enums");
        assertNotNull("leaf-list enums is not present in root of module "+ module.getName(), enumNode);
        assertTrue(enumListNode instanceof LeafListSchemaNode);
        LeafListSchemaNode leafList = (LeafListSchemaNode) enumListNode;
        TypeDefinition<?> leafListType = leafList.getType();

        final Type leafrefResolvedType2 = provider.javaTypeForSchemaDefinitionType(leafListType, leafList);
        assertNotNull(leafrefResolvedType2);
        assertTrue(leafrefResolvedType2 instanceof ReferencedTypeImpl);
    }

    private void setReferencedTypeForTypeProvider(TypeProvider provider) {
        final LeafSchemaNode enumLeafNode = provideLeafNodeFromTopLevelContainer(testTypeProviderModule, "foo",
            "resolve-direct-use-of-enum");
        final TypeDefinition<?> enumLeafTypedef = enumLeafNode.getType();
        Type enumType = provider.javaTypeForSchemaDefinitionType(enumLeafTypedef, enumLeafNode);

        final Type refType = new ReferencedTypeImpl(enumType.getPackageName(), enumType.getName());
        ((TypeProviderImpl) provider).putReferencedType(enumLeafNode.getPath(), refType);
    }

    @Test
    public void javaTypeForSchemaDefinitionConditionalLeafrefTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);
        final Module module = resolveModule("test-type-provider-b");

        final DataSchemaNode condLeaf = module.getDataChildByName("conditional-leafref");
        assertNotNull("leaf conditional-leafref is not present in root of module "+ module.getName(), condLeaf);
        assertTrue(condLeaf instanceof LeafSchemaNode);
        LeafSchemaNode leaf = (LeafSchemaNode) condLeaf;
        final TypeDefinition<?> leafType = leaf.getType();

        final Type resultType = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(resultType);
        assertTrue(resultType instanceof ConcreteType);
        assertEquals("java.lang", resultType.getPackageName());
        assertEquals("Object", resultType.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void javaTypeForSchemaDefinitionInvalidLeafrefPathTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);
        final Module module = resolveModule("test-type-provider-b");

        final DataSchemaNode condLeaf = module.getDataChildByName("unreslovable-leafref");
        assertNotNull("leaf unreslovable-leafref is not present in root of module "+ module.getName(), condLeaf);
        assertTrue(condLeaf instanceof LeafSchemaNode);
        LeafSchemaNode leaf = (LeafSchemaNode) condLeaf;
        final TypeDefinition<?> leafType = leaf.getType();

        provider.javaTypeForSchemaDefinitionType(leafType, leaf);
    }

    @Test
    public void javaTypeForSchemaDefinitionIdentityrefExtTypeTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(testTypeProviderModule, "foo", "crypto");
        final TypeDefinition<?> leafType = leaf.getType();

        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof ParameterizedType);
    }

    @Test
    public void javaTypeForSchemaDefinitionUnionExtTypeTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(testTypeProviderModule, "foo", "resolve-union-type");
        final TypeDefinition<?> leafType = leaf.getType();

        //TODO: finish test
    }

    @Test
    public void getConstructorPropertyNameTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);

        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(testTypeProviderModule, "foo", "yang-int8-type");
        final TypeDefinition<?> leafType = leaf.getType();

        final String ctorPropertyName = provider.getConstructorPropertyName(leafType);
        assertEquals("value", ctorPropertyName);

        final String emptyStringName = provider.getConstructorPropertyName(leaf);
        assertTrue(emptyStringName.isEmpty());
    }

    @Test
    public void getParamNameFromTypeTest() {
        final TypeProvider provider = new TypeProviderImpl(schemaContext);

        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(testTypeProviderModule, "foo", "yang-int8-type");
        final TypeDefinition<?> leafType = leaf.getType();

        final String paramName = provider.getParamNameFromType(leafType);
        assertEquals("yangInt8", paramName);
    }
}
