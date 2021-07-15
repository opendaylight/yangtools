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
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
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

    private static LeafListSchemaNode provideLeafListNodeFromTopLevelContainer(final Module module,
            final String containerName, final String leafListNodeName) {
        final QName containerNode = QName.create(module.getQNameModule(), containerName);
        final DataSchemaNode rootNode = module.findDataChildByName(containerNode).get();
        assertTrue(rootNode instanceof DataNodeContainer);

        final DataNodeContainer rootContainer = (DataNodeContainer) rootNode;
        final QName leafListNode = QName.create(module.getQNameModule(), leafListNodeName);
        final DataSchemaNode node = rootContainer.findDataChildByName(leafListNode).get();
        assertTrue(node instanceof LeafListSchemaNode);
        return (LeafListSchemaNode) node;
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
    public void javaTypeForSchemaDefinitionEmptyStringPatternTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(SCHEMA_CONTEXT);

        final Module testTypeProvider = resolveModule("test-type-provider");
        final TypeDefinition<?> emptyPatternString = resolveTypeDefinitionFromModule(testTypeProvider,
            "empty-pattern-string");

        assertNotNull(emptyPatternString);
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(emptyPatternString);

        Type result = provider.javaTypeForSchemaDefinitionType(emptyPatternString, emptyPatternString, restrictions);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);

        result = provider.generatedTypeForExtendedDefinitionType(emptyPatternString, emptyPatternString);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);
    }

    private static TypeDefinition<?> resolveTypeDefinitionFromModule(final Module module, final String typedefName) {
        TypeDefinition<?> result = null;
        for (final TypeDefinition<?> typedef : module.getTypeDefinitions()) {
            if (typedef.getQName().getLocalName().equals(typedefName)) {
                result = typedef;
            }
        }
        return result;
    }

    // FIXME: Remove @Ignore annotation once the bug https://bugs.opendaylight.org/show_bug.cgi?id=1862 is fixed
    @Ignore
    @Test
    public void bug1862RestrictedTypedefTransformationTest() {
        final AbstractTypeProvider provider = new CodegenTypeProvider(SCHEMA_CONTEXT);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(TEST_TYPE_PROVIDER, "foo",
            "bug-1862-restricted-typedef");

        final TypeDefinition<?> leafType = leaf.getType();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(leafType);
        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf, restrictions);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);
        //TODO: complete test after bug 1862 is fixed
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
    public void javaTypeForSchemaDefinitionLeafrefToEnumTypeTest() {
        final AbstractTypeProvider provider = new CodegenTypeProvider(SCHEMA_CONTEXT);

        setReferencedTypeForTypeProvider(provider);

        final Module module = resolveModule("test-type-provider-b");

        final QName leafNode = QName.create(module.getQNameModule(), "enum");
        final DataSchemaNode enumNode = module.findDataChildByName(leafNode).get();
        assertTrue(enumNode instanceof LeafSchemaNode);
        final LeafSchemaNode leaf = (LeafSchemaNode) enumNode;
        final TypeDefinition<?> leafType = leaf.getType();

        final Type leafrefResolvedType1 = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(leafrefResolvedType1);

        final QName leafListNode = QName.create(module.getQNameModule(), "enums");
        final DataSchemaNode enumListNode = module.findDataChildByName(leafListNode).get();
        assertTrue(enumListNode instanceof LeafListSchemaNode);
        final LeafListSchemaNode leafList = (LeafListSchemaNode) enumListNode;
        final TypeDefinition<?> leafListType = leafList.getType();

        final Type leafrefResolvedType2 = provider.javaTypeForSchemaDefinitionType(leafListType, leafList);
        assertNotNull(leafrefResolvedType2);
        assertTrue(leafrefResolvedType2 instanceof ParameterizedType);
    }

    private static void setReferencedTypeForTypeProvider(final AbstractTypeProvider provider) {
        final LeafSchemaNode enumLeafNode = provideLeafNodeFromTopLevelContainer(TEST_TYPE_PROVIDER, "foo",
            "resolve-direct-use-of-enum");
        final TypeDefinition<?> enumLeafTypedef = enumLeafNode.getType();
        provider.putReferencedType(enumLeafNode.getPath(),
            Type.of(provider.javaTypeForSchemaDefinitionType(enumLeafTypedef, enumLeafNode)));

        final LeafListSchemaNode enumListNode = provideLeafListNodeFromTopLevelContainer(TEST_TYPE_PROVIDER,
            "foo", "list-of-enums");
        final TypeDefinition<?> enumLeafListTypedef = enumListNode.getType();
        provider.putReferencedType(enumListNode.getPath(),
            Type.of(provider.javaTypeForSchemaDefinitionType(enumLeafListTypedef, enumListNode)));
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

    @Test(expected = IllegalArgumentException.class)
    public void javaTypeForSchemaDefinitionInvalidLeafrefPathTest() {
        final AbstractTypeProvider provider = new CodegenTypeProvider(SCHEMA_CONTEXT);
        final Module module = resolveModule("test-type-provider-b");

        final QName leafrefNode = QName.create(module.getQNameModule(), "unreslovable-leafref");
        final DataSchemaNode condLeaf = module.findDataChildByName(leafrefNode).get();
        assertTrue(condLeaf instanceof LeafSchemaNode);
        final LeafSchemaNode leaf = (LeafSchemaNode) condLeaf;
        final TypeDefinition<?> leafType = leaf.getType();

        provider.javaTypeForSchemaDefinitionType(leafType, leaf);
    }

    @Test
    public void javaTypeForSchemaDefinitionIdentityrefExtTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(SCHEMA_CONTEXT);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(TEST_TYPE_PROVIDER, "foo", "crypto");
        final TypeDefinition<?> leafType = leaf.getType();

        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof ParameterizedType);
    }

    @Test
    public void javaTypeForSchemaDefinitionForExtUnionWithSimpleTypesTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(SCHEMA_CONTEXT);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(TEST_TYPE_PROVIDER, "use-of-unions",
            "simple-int-types-union");
        final TypeDefinition<?> leafType = leaf.getType();

        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);
        assertEquals("YangUnion", result.getName());
        //TODO: write additional asserts to compare whole GeneratedTrasnferObject against yang union definition
    }

    @Test
    public void javaTypeForSchemaDefinitionForExtComplexUnionWithInnerUnionTypesTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(SCHEMA_CONTEXT);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(TEST_TYPE_PROVIDER, "use-of-unions",
            "complex-union");
        final TypeDefinition<?> leafType = leaf.getType();

        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);
        assertEquals("ComplexUnion", result.getName());
        //TODO: write additional asserts to compare whole GeneratedTrasnferObject against yang union definition
    }

    @Test
    public void javaTypeForSchemaDefinitionForExtUnionWithInnerUnionAndSimpleTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(SCHEMA_CONTEXT);
        final LeafSchemaNode leaf = provideLeafNodeFromTopLevelContainer(TEST_TYPE_PROVIDER, "use-of-unions",
            "complex-string-int-union");
        final TypeDefinition<?> leafType = leaf.getType();

        final Type result = provider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(result);
        assertTrue(result instanceof GeneratedTransferObject);
        assertEquals("ComplexStringIntUnion", result.getName());
        //TODO: write additional asserts to compare whole GeneratedTrasnferObject against yang union definition
    }

    @Test
    public void provideGeneratedTOBuilderForUnionTypeDefWithInnerUnionTypesTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(SCHEMA_CONTEXT);

        final TypeDefinition<?> unionTypeDef = resolveTypeDefinitionFromModule(TEST_TYPE_PROVIDER, "complex-union");

        assertNotNull(unionTypeDef);
        assertTrue(unionTypeDef.getBaseType() instanceof UnionTypeDefinition);
        GeneratedTOBuilder unionTypeBuilder = provider.provideGeneratedTOBuilderForUnionTypeDef(
            JavaTypeName.create("test.package.name", BindingMapping.getClassName(unionTypeDef.getQName())),
            (UnionTypeDefinition)unionTypeDef.getBaseType(), unionTypeDef);

        assertNotNull(unionTypeBuilder);

        GeneratedTransferObject unionType = unionTypeBuilder.build();
        assertEquals("ComplexUnion", unionType.getName());
    }

    @Test
    public void provideGeneratedTOBuilderForUnionTypeDefWithInnerUnionAndSimpleTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(SCHEMA_CONTEXT);

        final TypeDefinition<?> unionTypeDef = resolveTypeDefinitionFromModule(TEST_TYPE_PROVIDER,
            "complex-string-int-union");

        assertNotNull(unionTypeDef);
        assertTrue(unionTypeDef.getBaseType() instanceof UnionTypeDefinition);
        final GeneratedTOBuilder unionTypeBuilder = provider.provideGeneratedTOBuilderForUnionTypeDef(
            JavaTypeName.create("test.package.name", BindingMapping.getClassName(unionTypeDef.getQName())),
            (UnionTypeDefinition)unionTypeDef.getBaseType(), unionTypeDef);

        assertNotNull(unionTypeBuilder);

        final GeneratedTransferObject unionType = unionTypeBuilder.build();
        assertEquals("ComplexStringIntUnion", unionType.getName());
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeTest() {
        final AbstractTypeProvider provider = new CodegenTypeProvider(SCHEMA_CONTEXT);

        final Module baseYangTypes = resolveModule("base-yang-types");

        Type yangBoolean = null;
        Type yangEmpty = null;
        Type yangEnumeration = null;
        Type yangInt8 = null;
        Type yangInt8Restricted = null;
        Type yangInt16 = null;
        Type yangInt32 = null;
        Type yangInt64 = null;
        Type yangString = null;
        Type yangDecimal = null;
        Type yangUint8 = null;
        Type yangUint16 = null;
        Type yangUint32 = null;
        Type yangUint64 = null;
        Type yangUnion = null;
        Type yangBinary = null;
        Type yangBits = null;
        Type yangInstanceIdentifier = null;

        for (final TypeDefinition<?> typedef : baseYangTypes.getTypeDefinitions()) {
            final Type type = provider.generatedTypeForExtendedDefinitionType(typedef, typedef);
            if (type instanceof GeneratedTransferObject) {
                if (type.getName().equals("YangBoolean")) {
                    yangBoolean = type;
                } else if (type.getName().equals("YangEmpty")) {
                    yangEmpty = type;
                } else if (type.getName().equals("YangInt8")) {
                    yangInt8 = type;
                } else if (type.getName().equals("YangInt8Restricted")) {
                    yangInt8Restricted = type;
                } else if (type.getName().equals("YangInt16")) {
                    yangInt16 = type;
                } else if (type.getName().equals("YangInt32")) {
                    yangInt32 = type;
                } else if (type.getName().equals("YangInt64")) {
                    yangInt64 = type;
                } else if (type.getName().equals("YangString")) {
                    yangString = type;
                } else if (type.getName().equals("YangDecimal64")) {
                    yangDecimal = type;
                } else if (type.getName().equals("YangUint8")) {
                    yangUint8 = type;
                } else if (type.getName().equals("YangUint16")) {
                    yangUint16 = type;
                } else if (type.getName().equals("YangUint32")) {
                    yangUint32 = type;
                } else if (type.getName().equals("YangUint64")) {
                    yangUint64 = type;
                } else if (type.getName().equals("YangUnion")) {
                    yangUnion = type;
                } else if (type.getName().equals("YangBinary")) {
                    yangBinary = type;
                } else if (type.getName().equals("YangInstanceIdentifier")) {
                    yangInstanceIdentifier = type;
                } else if (type.getName().equals("YangBits")) {
                    yangBits = type;
                }
            } else if (type instanceof Enumeration) {
                if (type.getName().equals("YangEnumeration")) {
                    yangEnumeration = type;
                }
            }
        }

        assertNotNull(yangBoolean);
        assertNotNull(yangEmpty);
        assertNotNull(yangEnumeration);
        assertNotNull(yangInt8);
        assertNotNull(yangInt8Restricted);
        assertNotNull(yangInt16);
        assertNotNull(yangInt32);
        assertNotNull(yangInt64);
        assertNotNull(yangString);
        assertNotNull(yangDecimal);
        assertNotNull(yangUint8);
        assertNotNull(yangUint16);
        assertNotNull(yangUint32);
        assertNotNull(yangUint64);
        assertNotNull(yangUnion);
        assertNotNull(yangBinary);
        assertNotNull(yangBits);
        assertNotNull(yangInstanceIdentifier);
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeWithInnerExtendedTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(SCHEMA_CONTEXT);

        final Module baseYangTypes = resolveModule("test-type-provider");
        final TypeDefinition<?> extYangInt8Typedef = resolveTypeDefinitionFromModule(baseYangTypes,
            "extended-yang-int8");
        assertNotNull(extYangInt8Typedef);
        final Type extType = provider.generatedTypeForExtendedDefinitionType(extYangInt8Typedef, extYangInt8Typedef);
        assertNotNull(extType);
        assertTrue(extType instanceof GeneratedTransferObject);
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeWithLeafrefBaseTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(SCHEMA_CONTEXT);

        final Module baseYangTypes = resolveModule("test-type-provider");
        final TypeDefinition<?> barItemLeafrefId = resolveTypeDefinitionFromModule(baseYangTypes,
            "bar-item-leafref-id");
        assertNotNull(barItemLeafrefId);
        final Type extType = provider.generatedTypeForExtendedDefinitionType(barItemLeafrefId, barItemLeafrefId);
        assertEquals(null, extType);
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeWithIdentityrefBaseTypeTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(SCHEMA_CONTEXT);

        final Module baseYangTypes = resolveModule("test-type-provider");

        final TypeDefinition<?> aesIdentityrefType = resolveTypeDefinitionFromModule(baseYangTypes,
            "aes-identityref-type");

        assertNotNull(aesIdentityrefType);
        final Type extType = provider.generatedTypeForExtendedDefinitionType(aesIdentityrefType, aesIdentityrefType);
        assertEquals(null, extType);
    }

    @Test(expected = NullPointerException.class)
    public void provideGeneratedTOBuilderForBitsTypeDefinitionWithNullTypedefTest() {
        final AbstractTypeProvider provider = new RuntimeTypeProvider(SCHEMA_CONTEXT);
        provider.provideGeneratedTOBuilderForBitsTypeDefinition(JavaTypeName.create("foo", "foo"), null, "foo");
    }

    @Test
    public void addUnitsToGenTOTest() {
        final GeneratedTOBuilder builder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("test.package", "TestBuilder"));

        CodegenTypeProvider.addUnitsToGenTO(builder, null);
        GeneratedTransferObject genTO = builder.build();
        assertTrue(genTO.getConstantDefinitions().isEmpty());

        CodegenTypeProvider.addUnitsToGenTO(builder, "");
        genTO = builder.build();
        assertTrue(genTO.getConstantDefinitions().isEmpty());

        CodegenTypeProvider.addUnitsToGenTO(builder, "125");
        genTO = builder.build();
        assertTrue(!genTO.getConstantDefinitions().isEmpty());
        assertEquals(1, genTO.getConstantDefinitions().size());
        assertEquals("_UNITS", genTO.getConstantDefinitions().get(0).getName());
        assertEquals(genTO.getConstantDefinitions().get(0).getValue(), "\"125\"");
    }
}
