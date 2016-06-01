/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Test correct code generation.
 *
 */
public class TypedefCompilationTest extends BaseCompilationTest {
    private static final String VAL = "_value";
    private static final String GET_VAL = "getValue";
    private static final String UNITS = "_UNITS";

    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = new File(CompilationTestUtils.GENERATOR_OUTPUT_PATH + CompilationTestUtils.FS + "typedef");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(CompilationTestUtils.COMPILER_OUTPUT_PATH + CompilationTestUtils.FS + "typedef");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        final List<File> sourceFiles = CompilationTestUtils.getSourceFiles("/compilation/typedef");
        final SchemaContext context = TestUtils.parseYangSources(sourceFiles);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(ImmutableSet.copyOf(types));
        generator.generateToFile(sourcesOutputDir);

        File parent = new File(sourcesOutputDir, CompilationTestUtils.NS_FOO);
        File bitsExt = new File(parent, "BitsExt.java");
        File int32Ext0 = new File(parent, "Int32Ext0.java");
        File int32Ext1 = new File(parent, "Int32Ext1.java");
        File int32Ext2 = new File(parent, "Int32Ext2.java");
        File myDecimalType = new File(parent, "MyDecimalType.java");
        File stringExt1 = new File(parent, "StringExt1.java");
        File stringExt2 = new File(parent, "StringExt2.java");
        File stringExt3 = new File(parent, "StringExt3.java");
        File unionExt1 = new File(parent, "UnionExt1.java");
        File unionExt2 = new File(parent, "UnionExt2.java");
        File unionExt3 = new File(parent, "UnionExt3.java");
        File unionExt4 = new File(parent, "UnionExt4.java");
        assertTrue(bitsExt.exists());
        assertTrue(int32Ext0.exists());
        assertTrue(int32Ext1.exists());
        assertTrue(int32Ext2.exists());
        assertTrue(myDecimalType.exists());
        assertTrue(stringExt1.exists());
        assertTrue(stringExt2.exists());
        assertTrue(stringExt3.exists());
        assertTrue(unionExt1.exists());
        assertTrue(unionExt2.exists());
        assertTrue(unionExt3.exists());
        assertTrue(unionExt4.exists());
        CompilationTestUtils.assertFilesCount(parent, 33);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        String pkg = CompilationTestUtils.BASE_PKG + ".urn.opendaylight.foo.rev131008";
        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> bitsExtClass = Class.forName(pkg + ".BitsExt", true, loader);
        Class<?> int32Ext1Class = Class.forName(pkg + ".Int32Ext1", true, loader);
        Class<?> int32Ext2Class = Class.forName(pkg + ".Int32Ext2", true, loader);
        Class<?> myDecimalTypeClass = Class.forName(pkg + ".MyDecimalType", true, loader);
        Class<?> myDecimalType2Class = Class.forName(pkg + ".MyDecimalType2", true, loader);
        Class<?> stringExt1Class = Class.forName(pkg + ".StringExt1", true, loader);
        Class<?> stringExt2Class = Class.forName(pkg + ".StringExt2", true, loader);
        Class<?> stringExt3Class = Class.forName(pkg + ".StringExt3", true, loader);
        Class<?> unionExt1Class = Class.forName(pkg + ".UnionExt1", true, loader);
        Class<?> unionExt2Class = Class.forName(pkg + ".UnionExt2", true, loader);
        Class<?> unionExt3Class = Class.forName(pkg + ".UnionExt3", true, loader);
        Class<?> unionExt4Class = Class.forName(pkg + ".UnionExt4", true, loader);

        // typedef bits-ext
        assertFalse(bitsExtClass.isInterface());
        CompilationTestUtils.assertContainsField(bitsExtClass, "_pc", Boolean.class);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_bpc", Boolean.class);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_dpc", Boolean.class);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_lbpc", Boolean.class);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_spc", Boolean.class);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_sfmof", Boolean.class);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_sfapc", Boolean.class);
        CompilationTestUtils.assertContainsFieldWithValue(bitsExtClass, "serialVersionUID", Long.TYPE, -2922917845344851623L, Boolean.class,
                Boolean.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class);

        // assertEquals(8, bitsExtClass.getDeclaredFields());
        Constructor<?> expectedConstructor = CompilationTestUtils.assertContainsConstructor(bitsExtClass, Boolean.class, Boolean.class,
                Boolean.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class);
        CompilationTestUtils.assertContainsConstructor(bitsExtClass, bitsExtClass);
        assertEquals(2, bitsExtClass.getConstructors().length);
        Method defInst = CompilationTestUtils.assertContainsMethod(bitsExtClass, bitsExtClass, "getDefaultInstance", String.class);
        CompilationTestUtils.assertContainsDefaultMethods(bitsExtClass);
        // assertEquals(11, bitsExtClass.getDeclaredMethods().length);

        Object obj = expectedConstructor.newInstance(null, null, null, null, null, new Boolean("true"), null);
        assertEquals(obj, defInst.invoke(null, "sfmof"));

        // typedef int32-ext1
        assertFalse(int32Ext1Class.isInterface());
        CompilationTestUtils.assertContainsField(int32Ext1Class, VAL, Integer.class);
        CompilationTestUtils.assertContainsFieldWithValue(int32Ext1Class, "serialVersionUID", Long.TYPE, 5351634010010233292L, Integer.class);
        // assertEquals(3, int32Ext1Class.getDeclaredFields().length);

        expectedConstructor = CompilationTestUtils.assertContainsConstructor(int32Ext1Class, Integer.class);
        CompilationTestUtils.assertContainsConstructor(int32Ext1Class, int32Ext1Class);
        assertEquals(2, int32Ext1Class.getConstructors().length);
        CompilationTestUtils.assertContainsDefaultMethods(int32Ext1Class);
        CompilationTestUtils.assertContainsMethod(int32Ext1Class, Integer.class, GET_VAL);
        defInst = CompilationTestUtils.assertContainsMethod(int32Ext1Class, int32Ext1Class, "getDefaultInstance", String.class);
        // assertEquals(6, int32Ext1Class.getDeclaredMethods().length);

        List<Range<Integer>> rangeConstraints = new ArrayList<>();
        rangeConstraints.add(Range.closed(new Integer("2"), new Integer("2147483647")));
        Object arg = new Integer("1");
        String expectedMsg = String.format("Invalid range: %s, expected: %s.", arg, rangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance(new Integer("159"));
        assertEquals(obj, defInst.invoke(null, "159"));

        // typedef int32-ext2
        assertFalse(int32Ext2Class.isInterface());
        CompilationTestUtils.assertContainsFieldWithValue(int32Ext2Class, UNITS, String.class, "mile", Integer.class);
        CompilationTestUtils.assertContainsFieldWithValue(int32Ext2Class, "serialVersionUID", Long.TYPE, 317831889060130988L, Integer.class);
        // assertEquals(3, int32Ext2Class.getDeclaredFields().length);
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(int32Ext2Class, Integer.class);
        CompilationTestUtils.assertContainsConstructor(int32Ext2Class, int32Ext2Class);
        CompilationTestUtils.assertContainsConstructor(int32Ext2Class, int32Ext1Class);
        assertEquals(3, int32Ext2Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsMethod(int32Ext2Class, String.class, "toString");
        defInst = CompilationTestUtils.assertContainsMethod(int32Ext2Class, int32Ext2Class, "getDefaultInstance", String.class);
        // assertEquals(3, int32Ext2Class.getDeclaredMethods().length);

        rangeConstraints.clear();
        rangeConstraints.add(Range.closed(new Integer("3"), new Integer("9")));
        rangeConstraints.add(Range.closed(new Integer("11"), new Integer("2147483647")));
        arg = new Integer("10");
        expectedMsg = String.format("Invalid range: %s, expected: %s.", arg, rangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance(new Integer("2147483647"));
        assertEquals(obj, defInst.invoke(null, "2147483647"));

        // typedef string-ext1
        assertFalse(stringExt1Class.isInterface());
        CompilationTestUtils.assertContainsField(stringExt1Class, VAL, String.class);
        CompilationTestUtils.assertContainsField(stringExt1Class, "patterns", Pattern[].class);
        CompilationTestUtils.assertContainsField(stringExt1Class, "PATTERN_CONSTANTS", List.class);
        CompilationTestUtils.assertContainsFieldWithValue(stringExt1Class, "serialVersionUID", Long.TYPE, 6943827552297110991L, String.class);
        // assertEquals(5, stringExt1Class.getDeclaredFields().length);
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(stringExt1Class, String.class);
        CompilationTestUtils.assertContainsConstructor(stringExt1Class, stringExt1Class);
        assertEquals(2, stringExt1Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsMethod(stringExt1Class, String.class, GET_VAL);
        defInst = CompilationTestUtils.assertContainsMethod(stringExt1Class, stringExt1Class, "getDefaultInstance", String.class);
        CompilationTestUtils.assertContainsDefaultMethods(stringExt1Class);
        // assertEquals(6, stringExt1Class.getDeclaredMethods().length);

        List<Range<Integer>> lengthConstraints = new ArrayList<>();
        lengthConstraints.add(Range.closed(5, 11));
        arg = "abcd";
        expectedMsg = String.format("Invalid length: %s, expected: %s.", arg, lengthConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);

        obj = expectedConstructor.newInstance("abcde");
        assertEquals(obj, defInst.invoke(null, "abcde"));

        // typedef string-ext2
        assertFalse(stringExt2Class.isInterface());
        CompilationTestUtils.assertContainsFieldWithValue(stringExt2Class, "serialVersionUID", Long.TYPE, 8100233177432072092L, String.class);
        // assertEquals(2, stringExt2Class.getDeclaredFields().length);
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(stringExt2Class, String.class);
        CompilationTestUtils.assertContainsConstructor(stringExt2Class, stringExt2Class);
        CompilationTestUtils.assertContainsConstructor(stringExt2Class, stringExt1Class);
        assertEquals(3, stringExt2Class.getDeclaredConstructors().length);
        defInst = CompilationTestUtils.assertContainsMethod(stringExt2Class, stringExt2Class, "getDefaultInstance", String.class);
        // assertEquals(2, stringExt2Class.getDeclaredMethods().length);

        lengthConstraints.clear();
        lengthConstraints.add(Range.closed(6, 10));
        arg = "abcde";
        expectedMsg = String.format("Invalid length: %s, expected: %s.", arg, lengthConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance("abcdef");
        assertEquals(obj, defInst.invoke(null, "abcdef"));

        // typedef string-ext3
        assertFalse(stringExt3Class.isInterface());
        CompilationTestUtils.assertContainsFieldWithValue(stringExt3Class, "serialVersionUID", Long.TYPE, -2751063130555484180L,
                String.class);
        // assertEquals(1, stringExt3Class.getDeclaredFields().length);
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(stringExt3Class, String.class);
        CompilationTestUtils.assertContainsConstructor(stringExt3Class, stringExt3Class);
        CompilationTestUtils.assertContainsConstructor(stringExt3Class, stringExt2Class);
        assertEquals(3, stringExt3Class.getDeclaredConstructors().length);
        defInst = CompilationTestUtils.assertContainsMethod(stringExt3Class, stringExt3Class, "getDefaultInstance", String.class);
        // assertEquals(1, stringExt3Class.getDeclaredMethods().length);

        obj = expectedConstructor.newInstance("bbbbbb");
        assertEquals(obj, defInst.invoke(null, "bbbbbb"));

        // typedef my-decimal-type
        assertFalse(myDecimalTypeClass.isInterface());
        CompilationTestUtils.assertContainsField(myDecimalTypeClass, VAL, BigDecimal.class);
        CompilationTestUtils.assertContainsFieldWithValue(myDecimalTypeClass, "serialVersionUID", Long.TYPE, 3143735729419861095L,
                BigDecimal.class);
        // assertEquals(3, myDecimalTypeClass.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(myDecimalTypeClass, BigDecimal.class, "getValue");
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(myDecimalTypeClass, BigDecimal.class);
        CompilationTestUtils.assertContainsConstructor(myDecimalTypeClass, myDecimalTypeClass);
        assertEquals(2, myDecimalTypeClass.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsMethod(myDecimalTypeClass, BigDecimal.class, GET_VAL);
        CompilationTestUtils.assertContainsDefaultMethods(myDecimalTypeClass);
        defInst = CompilationTestUtils.assertContainsMethod(myDecimalTypeClass, myDecimalTypeClass, "getDefaultInstance", String.class);
        // assertEquals(6, myDecimalTypeClass.getDeclaredMethods().length);

        List<Range<BigDecimal>> decimalRangeConstraints = new ArrayList<>();
        decimalRangeConstraints.add(Range.closed(new BigDecimal("1.5"), new BigDecimal("5.5")));
        arg = new BigDecimal("1.4");
        expectedMsg = String.format("Invalid range: %s, expected: %s.", arg, decimalRangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance(new BigDecimal("3.14"));
        assertEquals(obj, defInst.invoke(null, "3.14"));

        // typedef my-decimal-type2
        assertFalse(myDecimalType2Class.isInterface());
        CompilationTestUtils.assertContainsField(myDecimalType2Class, VAL, BigDecimal.class);
        CompilationTestUtils.assertContainsFieldWithValue(myDecimalType2Class, "serialVersionUID", Long.TYPE, -672265764962082714L, BigDecimal.class);
        // assertEquals(3, myDecimalType2Class.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(myDecimalType2Class, BigDecimal.class, "getValue");
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(myDecimalType2Class, BigDecimal.class);
        CompilationTestUtils.assertContainsConstructor(myDecimalType2Class, myDecimalType2Class);
        assertEquals(2, myDecimalType2Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsMethod(myDecimalType2Class, BigDecimal.class, GET_VAL);
        CompilationTestUtils.assertContainsDefaultMethods(myDecimalType2Class);
        defInst = CompilationTestUtils.assertContainsMethod(myDecimalType2Class, myDecimalType2Class, "getDefaultInstance", String.class);
        // assertEquals(6, myDecimalType2Class.getDeclaredMethods().length);

        List<Range<BigDecimal>> decimal2RangeConstraints = new ArrayList<>();
        decimal2RangeConstraints.add(Range.closed(new BigDecimal("0"), new BigDecimal("1")));
        arg = new BigDecimal("1.4");
        expectedMsg = String.format("Invalid range: %s, expected: %s.", arg, decimal2RangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance(new BigDecimal("0.14"));
        assertEquals(obj, defInst.invoke(null, "0.14"));

        // typedef union-ext1
        assertFalse(unionExt1Class.isInterface());
        CompilationTestUtils.assertContainsField(unionExt1Class, "_int16", Short.class);
        CompilationTestUtils.assertContainsField(unionExt1Class, "_int32", Integer.class);
        CompilationTestUtils.assertContainsFieldWithValue(unionExt1Class, "serialVersionUID", Long.TYPE, -5610530488718168882L,
                new Class<?>[] { Short.class }, Short.valueOf("1"));
        // assertEquals(4, unionExt1Class.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(unionExt1Class, Short.class, "getInt16");
        CompilationTestUtils.assertContainsMethod(unionExt1Class, Integer.class, "getInt32");
        CompilationTestUtils.assertContainsConstructor(unionExt1Class, Short.class);
        CompilationTestUtils.assertContainsConstructor(unionExt1Class, Integer.class);
        CompilationTestUtils.assertContainsConstructor(unionExt1Class, unionExt1Class);
        assertEquals(4, unionExt1Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsDefaultMethods(unionExt1Class);

        // typedef union-ext2
        assertFalse(unionExt2Class.isInterface());
        CompilationTestUtils.assertContainsFieldWithValue(unionExt2Class, "serialVersionUID", Long.TYPE, -8833407459073585206L,
                new Class<?>[] { Short.class }, Short.valueOf("1"));
        // assertEquals(1, unionExt2Class.getDeclaredFields().length);
        // assertEquals(0, unionExt2Class.getDeclaredMethods().length);
        CompilationTestUtils.assertContainsConstructor(unionExt2Class, Short.class);
        CompilationTestUtils.assertContainsConstructor(unionExt2Class, Integer.class);
        CompilationTestUtils.assertContainsConstructor(unionExt2Class, unionExt2Class);
        CompilationTestUtils.assertContainsConstructor(unionExt2Class, unionExt1Class);
        assertEquals(5, unionExt2Class.getDeclaredConstructors().length);

        // typedef union-ext3
        assertFalse(unionExt3Class.isInterface());
        CompilationTestUtils.assertContainsField(unionExt3Class, "_string", String.class);
        CompilationTestUtils.assertContainsField(unionExt3Class, "_unionExt2", unionExt2Class);
        CompilationTestUtils.assertContainsFieldWithValue(unionExt3Class, UNITS, String.class, "object id", new Class<?>[] { String.class },
                "");
        CompilationTestUtils.assertContainsFieldWithValue(unionExt3Class, "serialVersionUID", Long.TYPE, 4347887914884631036L,
                new Class<?>[] { String.class }, "");
        // assertEquals(5, unionExt3Class.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(unionExt3Class, String.class, "getString");
        CompilationTestUtils.assertContainsMethod(unionExt3Class, unionExt2Class, "getUnionExt2");
        CompilationTestUtils.assertContainsConstructor(unionExt3Class, String.class);
        CompilationTestUtils.assertContainsConstructor(unionExt3Class, unionExt2Class);
        CompilationTestUtils.assertContainsConstructor(unionExt3Class, unionExt3Class);
        assertEquals(4, unionExt3Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsDefaultMethods(unionExt3Class);

        // typedef union-ext4
        assertFalse(unionExt4Class.isInterface());
        CompilationTestUtils.assertContainsField(unionExt4Class, "_unionExt3", unionExt3Class);
        CompilationTestUtils.assertContainsField(unionExt4Class, "_int32Ext2", int32Ext2Class);
        CompilationTestUtils.assertContainsField(unionExt4Class, "_empty", Boolean.class);
        CompilationTestUtils.assertContainsField(unionExt4Class, "_myDecimalType", myDecimalTypeClass);
        CompilationTestUtils.assertContainsFieldWithValue(unionExt4Class, "serialVersionUID", Long.TYPE, 4299836385615211130L,
                new Class<?>[] { Boolean.class }, false);
        // assertEquals(6, unionExt4Class.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(unionExt4Class, unionExt3Class, "getUnionExt3");
        CompilationTestUtils.assertContainsMethod(unionExt4Class, int32Ext2Class, "getInt32Ext2");
        CompilationTestUtils.assertContainsMethod(unionExt4Class, Boolean.class, "isEmpty");
        CompilationTestUtils.assertContainsMethod(unionExt4Class, myDecimalTypeClass, "getMyDecimalType");
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, unionExt3Class);
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, int32Ext2Class);
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, Boolean.class);
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, myDecimalTypeClass);
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, unionExt4Class);
        assertEquals(6, unionExt4Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsDefaultMethods(unionExt4Class);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
