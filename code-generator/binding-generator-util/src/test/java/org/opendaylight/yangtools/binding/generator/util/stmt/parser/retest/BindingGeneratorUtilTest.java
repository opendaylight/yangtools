/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.stmt.parser.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil;
import com.google.common.base.Optional;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;
import org.opendaylight.yangtools.yang.model.util.Decimal64;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.ExtendedType.Builder;
import org.opendaylight.yangtools.yang.model.util.Int16;
import org.opendaylight.yangtools.yang.model.util.StringType;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;

public class BindingGeneratorUtilTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private static List<File> loadTestResources(String testFile) {
        final List<File> testModels = new ArrayList<File>();
        File listModelFile;
        try {
            listModelFile = new File(BindingGeneratorUtilTest.class.getResource(testFile).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to load sources from " + testFile);
        }
        testModels.add(listModelFile);
        return testModels;
    }

    /**
     * Tests methods:
     * <ul>
     * <li>moduleNamespaceToPackageName</li> - with revision
     * <li>packageNameForGeneratedType</li>
     * <ul>
     * <li>validateJavaPackage</li>
     * </ul>
     * <li>packageNameForTypeDefinition</li> <li>moduleNamespaceToPackageName</li>
     * - without revision </ul>
     * @throws ReactorException
     * @throws SourceException
     */
    @Test
    public void testBindingGeneratorUtilMethods() throws IOException, SourceException, ReactorException {
        List<File> testModels = loadTestResources("/module.yang");

        final Set<Module> modules = RetestUtils.parseYangSources(testModels).getModules();
        String packageName = "";
        Module module = null;
        for (Module m : modules) {
            module = m;
            break;
        }
        assertNotNull("Module can't be null", module);

        // test of the method moduleNamespaceToPackageName()
        packageName = BindingGeneratorUtil.moduleNamespaceToPackageName(module);
        assertEquals("Generated package name is incorrect.",
                "org.opendaylight.yang.gen.v1.urn.m.o.d.u.l.e.n.a.m.e.t.e.s.t._case._1digit.rev130910", packageName);

        // test of the method packageNameForGeneratedType()
        DataNodeIterator it = new DataNodeIterator(module);
        List<ContainerSchemaNode> schemaContainers = it.allContainers();
        String subPackageNameForDataNode = "";
        for (ContainerSchemaNode containerSchemaNode : schemaContainers) {
            if (containerSchemaNode.getQName().getLocalName().equals("cont-inner")) {
                subPackageNameForDataNode = BindingGeneratorUtil.packageNameForGeneratedType(packageName,
                        containerSchemaNode.getPath());
                break;
            }
        }
        assertEquals("The name of the subpackage is incorrect.",
                "org.opendaylight.yang.gen.v1.urn.m.o.d.u.l.e.n.a.m.e.t.e.s.t._case._1digit.rev130910.cont.outter",
                subPackageNameForDataNode);

        // test of the method packageNameForTypeDefinition
        Set<TypeDefinition<?>> typeDefinitions = module.getTypeDefinitions();
        String subPackageNameForTypeDefinition = "";
        TypeDefinition<?> firstTypeDef = null;

        for (TypeDefinition<?> tpDef : typeDefinitions) {
            if (tpDef.getQName().getLocalName().equals("tpdf")) {
                subPackageNameForTypeDefinition = BindingGeneratorUtil.packageNameForTypeDefinition(packageName, tpDef);
                firstTypeDef = tpDef;
                break;
            }
        }
        assertEquals("The name of the subpackage is incorrect.",
                "org.opendaylight.yang.gen.v1.urn.m.o.d.u.l.e.n.a.m.e.t.e.s.t._case._1digit.rev130910",
                subPackageNameForTypeDefinition);

        // test method getRestrictions
        Restrictions restriction = BindingGeneratorUtil.getRestrictions(firstTypeDef);
        assertNotNull(restriction);

        // test method computeDefaultSUID
        GeneratedTypeBuilder genTypeBuilder = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test", "TestType");
        genTypeBuilder.addMethod("testMethod");
        genTypeBuilder.addAnnotation("org.opendaylight.yangtools.test.annotation", "AnnotationTest");
        genTypeBuilder.addEnclosingTransferObject("testObject");
        genTypeBuilder.addProperty("newProp");
        GeneratedTypeBuilder genType = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test", "Type2");
        genTypeBuilder.addImplementsType(genType);
        long computedSUID = BindingGeneratorUtil.computeDefaultSUID(genTypeBuilder);

        GeneratedTypeBuilder genTypeBuilder2 = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test2", "TestType2");
        long computedSUID2 = BindingGeneratorUtil.computeDefaultSUID(genTypeBuilder2);
        assertNotEquals(computedSUID, computedSUID2);

        // test of exception part of the method moduleNamespaceToPackageName()
        ModuleBuilder moduleBuilder = new ModuleBuilder("module-withut-revision", null);
        moduleBuilder.setSource("");
        Module moduleWithoutRevision = moduleBuilder.build();
        boolean passedSuccesfully = false;
        try {
            BindingGeneratorUtil.moduleNamespaceToPackageName(moduleWithoutRevision);
            passedSuccesfully = true;
        } catch (IllegalArgumentException e) {
        }
        assertFalse("Exception 'IllegalArgumentException' wasn't raised", passedSuccesfully);

    }

    /**
     * Test for the method
     * <ul>
     * <li>{@link BindingGeneratorUtil#packageNameForTypeDefinition()
     * packageNameForTypeDefinition()}</li>
     * </ul>
     */
    @Test
    public void testPackageNameForTypeDefinitionNullBasePackageName() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Base Package Name cannot be NULL!");
        BindingGeneratorUtil.packageNameForTypeDefinition(null, null);
    }

    /**
     * Test for the method
     * <ul>
     * <li>{@link BindingGeneratorUtil#packageNameForTypeDefinition()
     * packageNameForTypeDefinition()}</li>
     * </ul>
     */
    @Test
    public void testPackageNameForTypeDefinitionNullTypeDefinition() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Type Definition reference cannot be NULL!");
        BindingGeneratorUtil.packageNameForTypeDefinition("test.package", null);
    }

    /**
     * Test for the method
     * <ul>
     * <li>{@link BindingGeneratorUtil#packageNameForGeneratedType()
     * packageNameForGeneratedType()}</li>
     * </ul>
     */
    @Test
    public void testPackageNameForGeneratedTypeNullBasePackageName() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Base Package Name cannot be NULL!");
        BindingGeneratorUtil.packageNameForGeneratedType(null, null);
    }

    /**
     * Test for the method
     * <ul>
     * <li>{@link BindingGeneratorUtil#packageNameForGeneratedType()
     * packageNameForGeneratedType()}</li>
     * </ul>
     */
    @Test
    public void testPackageNameForGeneratedTypeNullSchemaPath() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Schema Path cannot be NULL!");
        BindingGeneratorUtil.packageNameForGeneratedType("test.package", null);
    }

    /**
     * Test for the method
     * <ul>
     * <li>{@link BindingGeneratorUtil#parseToClassName()
     * parseToClassName()}</li>
     * </ul>
     */
    @Test
    public void testParseToClassNameNullValue() {
        String className = BindingGeneratorUtil.parseToClassName("test-class-name");
        assertEquals("TestClassName", className);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Name can not be null");
        className = BindingGeneratorUtil.parseToClassName(null);
    }

    /**
     * Test for the method
     * <ul>
     * <li>{@link BindingGeneratorUtil#parseToClassName()
     * parseToClassName()}</li>
     * </ul>
     */
    @Test
    public void testParseToClassNameEmptyValue() {
        String className = BindingGeneratorUtil.parseToClassName("test-class-name");
        assertEquals("TestClassName", className);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Name can not be empty");
        className = BindingGeneratorUtil.parseToClassName("");
    }

    /**
     * Test for the method
     * <ul>
     * <li>{@link BindingGeneratorUtil#validateParameterName()
     * validateParameterName()}</li>
     * <ul>
     */
    @Test
    public void testValidateParameterName() {
        assertNull("Return value is incorrect.", BindingGeneratorUtil.resolveJavaReservedWordEquivalency(null));
        assertEquals("Return value is incorrect.", "whatever",
                BindingGeneratorUtil.resolveJavaReservedWordEquivalency("whatever"));
        assertEquals("Return value is incorrect.", "_case",
                BindingGeneratorUtil.resolveJavaReservedWordEquivalency("case"));
    }

    /**
     * Tests the methods:
     * <ul>
     * <li>parseToClassName</li>
     * <ul>
     * <li>parseToCamelCase</li>
     * <ul>
     * <li>replaceWithCamelCase</li>
     * </ul>
     * </ul> <li>parseToValidParamName</li>
     * <ul>
     * <li>parseToCamelCase</li>
     * <ul>
     * <li>replaceWithCamelCase</li>
     * </ul>
     * </ul>
     * <ul>
     */
    @Test
    public void testParsingMethods() {
        // parseToClassName method testing
        assertEquals("Class name has incorrect format", "SomeTestingClassName",
                BindingMapping.getClassName("  some-testing_class name   "));
        assertEquals("Class name has incorrect format", "_0SomeTestingClassName",
                BindingMapping.getClassName("  0 some-testing_class name   "));

        // parseToValidParamName
        assertEquals("Parameter name has incorrect format", "someTestingParameterName",
                BindingGeneratorUtil.parseToValidParamName("  some-testing_parameter   name   "));
        assertEquals("Parameter name has incorrect format", "_0someTestingParameterName",
                BindingGeneratorUtil.parseToValidParamName("  0some-testing_parameter   name   "));
    }

    @Test
    public void computeDefaultSUIDTest() {
        GeneratedTypeBuilderImpl generatedTypeBuilder = new GeneratedTypeBuilderImpl("my.package", "MyName");

        MethodSignatureBuilder method = generatedTypeBuilder.addMethod("myMethodName");
        method.setAccessModifier(AccessModifier.PUBLIC);
        generatedTypeBuilder.addProperty("myProperty");
        generatedTypeBuilder.addImplementsType(Types.typeForClass(Serializable.class));

        assertEquals(6788238694991761868L, BindingGeneratorUtil.computeDefaultSUID(generatedTypeBuilder));

    }

    @Test
    public void getRestrictionsTest() {

        Optional<String> absent = Optional.absent();

        Builder extTypeBuilder = ExtendedType.builder(new QName(URI.create("namespace"), "localName"),
                Int16.getInstance(), absent, absent, SchemaPath.create(true, QName.create("/root")));

        ArrayList<LengthConstraint> lenght = new ArrayList<LengthConstraint>();
        ArrayList<RangeConstraint> range = new ArrayList<RangeConstraint>();
        ArrayList<PatternConstraint> pattern = new ArrayList<PatternConstraint>();

        lenght.add(BaseConstraints.newLengthConstraint(1, 2, absent, absent));
        range.add(BaseConstraints.newRangeConstraint(1, 2, absent, absent));
        pattern.add(BaseConstraints.newPatternConstraint(".*", absent, absent));

        extTypeBuilder.lengths(lenght);
        extTypeBuilder.ranges(range);
        extTypeBuilder.patterns(pattern);

        Restrictions restrictions = BindingGeneratorUtil.getRestrictions(extTypeBuilder.build());

        assertNotNull(restrictions);

        assertEquals(1, restrictions.getLengthConstraints().size());
        assertEquals(1, restrictions.getRangeConstraints().size());
        assertEquals(1, restrictions.getPatternConstraints().size());

        assertFalse(restrictions.isEmpty());
        assertTrue(restrictions.getLengthConstraints().contains(
                BaseConstraints.newLengthConstraint(1, 2, absent, absent)));
        assertTrue(restrictions.getRangeConstraints()
                .contains(BaseConstraints.newRangeConstraint(1, 2, absent, absent)));
        assertTrue(restrictions.getPatternConstraints().contains(
                BaseConstraints.newPatternConstraint(".*", absent, absent)));
    }

    @Test
    public void getEmptyRestrictionsTest() {

        Optional<String> absent = Optional.absent();

        Builder extTypeBuilder = ExtendedType.builder(new QName(URI.create("namespace"), "localName"),
                StringType.getInstance(), absent, absent, SchemaPath.create(true, QName.create("/root")));

        Restrictions restrictions = BindingGeneratorUtil.getRestrictions(extTypeBuilder.build());

        assertNotNull(restrictions);
        assertTrue(restrictions.isEmpty());

    }

    @Test
    public void getDefaultIntegerRestrictionsTest() {

        Optional<String> absent = Optional.absent();

        Builder extTypeBuilder = ExtendedType.builder(new QName(URI.create("namespace"), "localName"),
                Int16.getInstance(), absent, absent, SchemaPath.create(true, QName.create("/root")));

        ExtendedType extType = extTypeBuilder.build();
        Restrictions restrictions = BindingGeneratorUtil.getRestrictions(extType);

        assertNotNull(restrictions);
        assertFalse(restrictions.isEmpty());
        assertEquals(((IntegerTypeDefinition) extType.getBaseType()).getRangeConstraints(),
                restrictions.getRangeConstraints());
        assertTrue(restrictions.getLengthConstraints().isEmpty());
        assertTrue(restrictions.getPatternConstraints().isEmpty());

    }

    @Test
    public void getDefaultUnsignedIntegerRestrictionsTest() {

        Optional<String> absent = Optional.absent();

        Builder extTypeBuilder = ExtendedType.builder(new QName(URI.create("namespace"), "localName"),
                Uint16.getInstance(), absent, absent, SchemaPath.create(true, QName.create("/root")));

        ExtendedType extType = extTypeBuilder.build();
        Restrictions restrictions = BindingGeneratorUtil.getRestrictions(extType);

        assertNotNull(restrictions);
        assertFalse(restrictions.isEmpty());
        assertEquals(((UnsignedIntegerTypeDefinition) extType.getBaseType()).getRangeConstraints(),
                restrictions.getRangeConstraints());
        assertTrue(restrictions.getLengthConstraints().isEmpty());
        assertTrue(restrictions.getPatternConstraints().isEmpty());
    }

    @Test
    public void getDefaultDecimalRestrictionsTest() {

        Optional<String> absent = Optional.absent();
        SchemaPath path = SchemaPath.create(true, QName.create("/root"));

        Builder extTypeBuilder = ExtendedType.builder(new QName(URI.create("namespace"), "localName"),
                Decimal64.create(path, 10), absent, absent, path);

        ExtendedType extType = extTypeBuilder.build();
        Restrictions restrictions = BindingGeneratorUtil.getRestrictions(extType);

        assertNotNull(restrictions);
        assertFalse(restrictions.isEmpty());
        assertEquals(((DecimalTypeDefinition) extType.getBaseType()).getRangeConstraints(),
                restrictions.getRangeConstraints());
        assertTrue(restrictions.getLengthConstraints().isEmpty());
        assertTrue(restrictions.getPatternConstraints().isEmpty());

    }

}
