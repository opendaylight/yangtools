/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Range;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Test correct code generation.
 */
public class TypedefCompilationTest extends BaseCompilationTest {
    private static final String VAL = "_value";
    private static final String GET_VAL = "getValue";
    private static final String UNITS = "_UNITS";

    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("typedef");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("typedef");
        generateTestSources("/compilation/typedef", sourcesOutputDir);

        final File parent = new File(sourcesOutputDir, CompilationTestUtils.NS_FOO);
        assertTrue(new File(parent, "BitsExt.java").exists());
        assertTrue(new File(parent, "Int32Ext0.java").exists());
        assertTrue(new File(parent, "Int32Ext1.java").exists());
        assertTrue(new File(parent, "Int32Ext2.java").exists());
        assertTrue(new File(parent, "MyDecimalType.java").exists());
        assertTrue(new File(parent, "StringExt1.java").exists());
        assertTrue(new File(parent, "StringExt2.java").exists());
        assertTrue(new File(parent, "StringExt3.java").exists());
        assertTrue(new File(parent, "UnionExt1.java").exists());
        assertTrue(new File(parent, "UnionExt2.java").exists());
        assertTrue(new File(parent, "UnionExt3.java").exists());
        assertTrue(new File(parent, "UnionExt4.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 31);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        String pkg = CompilationTestUtils.BASE_PKG + ".urn.opendaylight.foo.rev131008";
        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        final Class<?> bitsExtClass = Class.forName(pkg + ".BitsExt", true, loader);
        final Class<?> int32Ext1Class = Class.forName(pkg + ".Int32Ext1", true, loader);
        final Class<?> int32Ext2Class = Class.forName(pkg + ".Int32Ext2", true, loader);
        final Class<?> myDecimalTypeClass = Class.forName(pkg + ".MyDecimalType", true, loader);
        final Class<?> myDecimalType2Class = Class.forName(pkg + ".MyDecimalType2", true, loader);
        final Class<?> stringExt1Class = Class.forName(pkg + ".StringExt1", true, loader);
        final Class<?> stringExt2Class = Class.forName(pkg + ".StringExt2", true, loader);
        final Class<?> stringExt3Class = Class.forName(pkg + ".StringExt3", true, loader);
        final Class<?> unionExt1Class = Class.forName(pkg + ".UnionExt1", true, loader);
        final Class<?> unionExt2Class = Class.forName(pkg + ".UnionExt2", true, loader);
        final Class<?> unionExt3Class = Class.forName(pkg + ".UnionExt3", true, loader);
        final Class<?> unionExt4Class = Class.forName(pkg + ".UnionExt4", true, loader);

        // typedef bits-ext
        assertFalse(bitsExtClass.isInterface());
        CompilationTestUtils.assertContainsField(bitsExtClass, "_pc", boolean.class);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_bpc", boolean.class);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_dpc", boolean.class);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_lbpc", boolean.class);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_spc", boolean.class);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_sfmof", boolean.class);
        CompilationTestUtils.assertContainsField(bitsExtClass, "_sfapc", boolean.class);
        CompilationTestUtils.assertContainsFieldWithValue(bitsExtClass, "serialVersionUID", Long.TYPE,
            -2922917845344851623L, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class,
            boolean.class, boolean.class);

        assertEquals(9, bitsExtClass.getDeclaredFields().length);
        CompilationTestUtils.assertContainsConstructor(bitsExtClass, bitsExtClass);
        assertEquals(2, bitsExtClass.getConstructors().length);
        Method defInst = CompilationTestUtils.assertContainsMethod(bitsExtClass, bitsExtClass, "getDefaultInstance",
            String.class);
        CompilationTestUtils.assertContainsDefaultMethods(bitsExtClass);
        assertEquals(13, bitsExtClass.getDeclaredMethods().length);

        Constructor<?> expectedConstructor = CompilationTestUtils.assertContainsConstructor(bitsExtClass, boolean.class,
            boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class);
        Object obj = expectedConstructor.newInstance(false, false, false, false, false, true, false);
        assertEquals(obj, defInst.invoke(true, "sfmof"));

        // typedef int32-ext1
        assertFalse(int32Ext1Class.isInterface());
        CompilationTestUtils.assertContainsField(int32Ext1Class, VAL, Integer.class);
        CompilationTestUtils.assertContainsFieldWithValue(int32Ext1Class, "serialVersionUID", Long.TYPE,
            5351634010010233292L, Integer.class);
        assertEquals(2, int32Ext1Class.getDeclaredFields().length);

        expectedConstructor = CompilationTestUtils.assertContainsConstructor(int32Ext1Class, Integer.class);
        CompilationTestUtils.assertContainsConstructor(int32Ext1Class, int32Ext1Class);
        assertEquals(2, int32Ext1Class.getConstructors().length);
        CompilationTestUtils.assertContainsDefaultMethods(int32Ext1Class);
        CompilationTestUtils.assertContainsMethod(int32Ext1Class, Integer.class, GET_VAL);
        defInst = CompilationTestUtils.assertContainsMethod(int32Ext1Class, int32Ext1Class, "getDefaultInstance",
            String.class);
        assertEquals(7, int32Ext1Class.getDeclaredMethods().length);

        List<Range<Integer>> rangeConstraints = new ArrayList<>();
        rangeConstraints.add(Range.closed(2, 2147483647));
        Object arg = 1;
        String expectedMsg = String.format("Invalid range: %s, expected: %s.", arg, rangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance(159);
        assertEquals(obj, defInst.invoke(null, "159"));

        // typedef int32-ext2
        assertFalse(int32Ext2Class.isInterface());
        CompilationTestUtils.assertContainsFieldWithValue(int32Ext2Class, UNITS, String.class, "mile", Integer.class);
        CompilationTestUtils.assertContainsFieldWithValue(int32Ext2Class, "serialVersionUID", Long.TYPE,
            317831889060130988L, Integer.class);
        assertEquals(2, int32Ext2Class.getDeclaredFields().length);
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(int32Ext2Class, Integer.class);
        CompilationTestUtils.assertContainsConstructor(int32Ext2Class, int32Ext2Class);
        CompilationTestUtils.assertContainsConstructor(int32Ext2Class, int32Ext1Class);
        assertEquals(3, int32Ext2Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsMethod(int32Ext2Class, String.class, "toString");
        defInst = CompilationTestUtils.assertContainsMethod(int32Ext2Class, int32Ext2Class, "getDefaultInstance",
            String.class);
        assertEquals(3, int32Ext2Class.getDeclaredMethods().length);

        rangeConstraints.clear();
        rangeConstraints.add(Range.closed(3, 9));
        rangeConstraints.add(Range.closed(11, 2147483647));
        arg = Integer.valueOf("10");
        expectedMsg = String.format("Invalid range: %s, expected: %s.", arg, rangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance(2147483647);
        assertEquals(obj, defInst.invoke(null, "2147483647"));

        // typedef string-ext1
        assertFalse(stringExt1Class.isInterface());
        CompilationTestUtils.assertContainsField(stringExt1Class, VAL, String.class);
        CompilationTestUtils.assertContainsField(stringExt1Class, "patterns", Pattern.class);
        CompilationTestUtils.assertContainsField(stringExt1Class, "PATTERN_CONSTANTS", List.class);
        CompilationTestUtils.assertContainsFieldWithValue(stringExt1Class, "serialVersionUID", Long.TYPE,
            6943827552297110991L, String.class);
        assertEquals(5, stringExt1Class.getDeclaredFields().length);
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(stringExt1Class, String.class);
        CompilationTestUtils.assertContainsConstructor(stringExt1Class, stringExt1Class);
        assertEquals(2, stringExt1Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsMethod(stringExt1Class, String.class, GET_VAL);
        defInst = CompilationTestUtils.assertContainsMethod(stringExt1Class, stringExt1Class, "getDefaultInstance",
            String.class);
        CompilationTestUtils.assertContainsDefaultMethods(stringExt1Class);
        assertEquals(7, stringExt1Class.getDeclaredMethods().length);

        List<Range<Integer>> lengthConstraints = new ArrayList<>();
        lengthConstraints.add(Range.closed(5, 11));
        arg = "abcd";
        expectedMsg = String.format("Invalid length: %s, expected: %s.", arg, lengthConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);

        obj = expectedConstructor.newInstance("abcde");
        assertEquals(obj, defInst.invoke(null, "abcde"));

        // typedef string-ext2
        assertFalse(stringExt2Class.isInterface());
        CompilationTestUtils.assertContainsFieldWithValue(stringExt2Class, "serialVersionUID", Long.TYPE,
            8100233177432072092L, String.class);
        assertEquals(1, stringExt2Class.getDeclaredFields().length);
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(stringExt2Class, String.class);
        CompilationTestUtils.assertContainsConstructor(stringExt2Class, stringExt2Class);
        CompilationTestUtils.assertContainsConstructor(stringExt2Class, stringExt1Class);
        assertEquals(3, stringExt2Class.getDeclaredConstructors().length);
        defInst = CompilationTestUtils.assertContainsMethod(stringExt2Class, stringExt2Class, "getDefaultInstance",
            String.class);
        assertEquals(2, stringExt2Class.getDeclaredMethods().length);

        lengthConstraints.clear();
        lengthConstraints.add(Range.closed(6, 10));
        arg = "abcde";
        expectedMsg = String.format("Invalid length: %s, expected: %s.", arg, lengthConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance("abcdef");
        assertEquals(obj, defInst.invoke(null, "abcdef"));

        // typedef string-ext3
        assertFalse(stringExt3Class.isInterface());
        CompilationTestUtils.assertContainsFieldWithValue(stringExt3Class, "serialVersionUID", Long.TYPE,
            -2751063130555484180L, String.class);
        assertEquals(4, stringExt3Class.getDeclaredFields().length);
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(stringExt3Class, String.class);
        CompilationTestUtils.assertContainsConstructor(stringExt3Class, stringExt3Class);
        CompilationTestUtils.assertContainsConstructor(stringExt3Class, stringExt2Class);
        assertEquals(3, stringExt3Class.getDeclaredConstructors().length);
        defInst = CompilationTestUtils.assertContainsMethod(stringExt3Class, stringExt3Class, "getDefaultInstance",
            String.class);
        assertEquals(1, stringExt3Class.getDeclaredMethods().length);

        obj = expectedConstructor.newInstance("bbbbbb");
        assertEquals(obj, defInst.invoke(null, "bbbbbb"));

        // typedef my-decimal-type
        assertFalse(myDecimalTypeClass.isInterface());
        CompilationTestUtils.assertContainsField(myDecimalTypeClass, VAL, Decimal64.class);
        CompilationTestUtils.assertContainsFieldWithValue(myDecimalTypeClass, "serialVersionUID", Long.TYPE,
            3143735729419861095L, Decimal64.class);
        assertEquals(3, myDecimalTypeClass.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(myDecimalTypeClass, Decimal64.class, "getValue");
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(myDecimalTypeClass, Decimal64.class);
        CompilationTestUtils.assertContainsConstructor(myDecimalTypeClass, myDecimalTypeClass);
        assertEquals(2, myDecimalTypeClass.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsMethod(myDecimalTypeClass, Decimal64.class, GET_VAL);
        CompilationTestUtils.assertContainsDefaultMethods(myDecimalTypeClass);
        defInst = CompilationTestUtils.assertContainsMethod(myDecimalTypeClass, myDecimalTypeClass,
            "getDefaultInstance", String.class);
        assertEquals(7, myDecimalTypeClass.getDeclaredMethods().length);

        List<Range<Decimal64>> decimalRangeConstraints = new ArrayList<>();
        decimalRangeConstraints.add(Range.closed(Decimal64.valueOf("1.5"), Decimal64.valueOf("5.5")));
        arg = Decimal64.valueOf("1.4");
        expectedMsg = String.format("Invalid range: %s, expected: %s.", arg, decimalRangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance(Decimal64.valueOf("3.14"));
        assertEquals(obj, defInst.invoke(null, "3.14"));

        // typedef my-decimal-type2
        assertFalse(myDecimalType2Class.isInterface());
        CompilationTestUtils.assertContainsField(myDecimalType2Class, VAL, Decimal64.class);
        CompilationTestUtils.assertContainsFieldWithValue(myDecimalType2Class, "serialVersionUID", Long.TYPE,
            -672265764962082714L, Decimal64.class);
        assertEquals(3, myDecimalType2Class.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(myDecimalType2Class, Decimal64.class, "getValue");
        expectedConstructor = CompilationTestUtils.assertContainsConstructor(myDecimalType2Class, Decimal64.class);
        CompilationTestUtils.assertContainsConstructor(myDecimalType2Class, myDecimalType2Class);
        assertEquals(2, myDecimalType2Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsMethod(myDecimalType2Class, Decimal64.class, GET_VAL);
        CompilationTestUtils.assertContainsDefaultMethods(myDecimalType2Class);
        defInst = CompilationTestUtils.assertContainsMethod(myDecimalType2Class, myDecimalType2Class,
            "getDefaultInstance", String.class);
        assertEquals(7, myDecimalType2Class.getDeclaredMethods().length);

        List<Range<Decimal64>> decimal2RangeConstraints = new ArrayList<>();
        decimal2RangeConstraints.add(Range.closed(Decimal64.valueOf("0.0"), Decimal64.valueOf("1.0")));
        arg = Decimal64.valueOf("1.4");
        expectedMsg = String.format("Invalid range: %s, expected: %s.", arg, decimal2RangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(expectedConstructor, expectedMsg, arg);
        obj = expectedConstructor.newInstance(Decimal64.valueOf("0.14"));
        assertEquals(obj, defInst.invoke(null, "0.14"));

        // typedef union-ext1
        assertFalse(unionExt1Class.isInterface());
        CompilationTestUtils.assertContainsField(unionExt1Class, "_int16", Short.class);
        CompilationTestUtils.assertContainsField(unionExt1Class, "_int32", Integer.class);
        CompilationTestUtils.assertContainsFieldWithValue(unionExt1Class, "serialVersionUID", Long.TYPE,
            -6955858981055390623L, new Class<?>[] { Short.class }, Short.valueOf("1"));
        assertEquals(3, unionExt1Class.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(unionExt1Class, Short.class, "getInt16");
        CompilationTestUtils.assertContainsMethod(unionExt1Class, Integer.class, "getInt32");
        CompilationTestUtils.assertContainsConstructor(unionExt1Class, Short.class);
        CompilationTestUtils.assertContainsConstructor(unionExt1Class, Integer.class);
        CompilationTestUtils.assertContainsConstructor(unionExt1Class, unionExt1Class);
        assertEquals(3, unionExt1Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsDefaultMethods(unionExt1Class);

        // typedef union-ext2
        assertFalse(unionExt2Class.isInterface());
        CompilationTestUtils.assertContainsFieldWithValue(unionExt2Class, "serialVersionUID", Long.TYPE,
            -8833407459073585206L, new Class<?>[] { Short.class }, Short.valueOf("1"));
        assertEquals(1, unionExt2Class.getDeclaredFields().length);
        assertEquals(1, unionExt2Class.getDeclaredMethods().length);
        CompilationTestUtils.assertContainsConstructor(unionExt2Class, Short.class);
        CompilationTestUtils.assertContainsConstructor(unionExt2Class, Integer.class);
        CompilationTestUtils.assertContainsConstructor(unionExt2Class, unionExt2Class);
        CompilationTestUtils.assertContainsConstructor(unionExt2Class, unionExt1Class);
        assertEquals(4, unionExt2Class.getDeclaredConstructors().length);

        // typedef union-ext3
        assertFalse(unionExt3Class.isInterface());
        CompilationTestUtils.assertContainsField(unionExt3Class, "_string", String.class);
        CompilationTestUtils.assertContainsField(unionExt3Class, "_unionExt2", unionExt2Class);
        CompilationTestUtils.assertContainsFieldWithValue(unionExt3Class, UNITS, String.class, "object id",
            new Class<?>[] { String.class }, "");
        CompilationTestUtils.assertContainsFieldWithValue(unionExt3Class, "serialVersionUID", Long.TYPE,
            -1558836942803815106L, new Class<?>[] { String.class }, "");
        assertEquals(4, unionExt3Class.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(unionExt3Class, String.class, "getString");
        CompilationTestUtils.assertContainsMethod(unionExt3Class, unionExt2Class, "getUnionExt2");
        CompilationTestUtils.assertContainsConstructor(unionExt3Class, String.class);
        CompilationTestUtils.assertContainsConstructor(unionExt3Class, unionExt2Class);
        CompilationTestUtils.assertContainsConstructor(unionExt3Class, unionExt3Class);
        assertEquals(3, unionExt3Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsDefaultMethods(unionExt3Class);

        // typedef union-ext4
        assertFalse(unionExt4Class.isInterface());
        CompilationTestUtils.assertContainsField(unionExt4Class, "_unionExt3", unionExt3Class);
        CompilationTestUtils.assertContainsField(unionExt4Class, "_int32Ext2", int32Ext2Class);
        CompilationTestUtils.assertContainsField(unionExt4Class, "_empty", Empty.class);
        CompilationTestUtils.assertContainsField(unionExt4Class, "_myDecimalType", myDecimalTypeClass);
        CompilationTestUtils.assertContainsFieldWithValue(unionExt4Class, "serialVersionUID", Long.TYPE,
            8089656970520476667L, new Class<?>[] { Boolean.class }, false);
        assertEquals(5, unionExt4Class.getDeclaredFields().length);
        CompilationTestUtils.assertContainsMethod(unionExt4Class, unionExt3Class, "getUnionExt3");
        CompilationTestUtils.assertContainsMethod(unionExt4Class, int32Ext2Class, "getInt32Ext2");
        CompilationTestUtils.assertContainsMethod(unionExt4Class, Empty.class, "getEmpty");
        CompilationTestUtils.assertContainsMethod(unionExt4Class, myDecimalTypeClass, "getMyDecimalType");
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, unionExt3Class);
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, int32Ext2Class);
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, Empty.class);
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, myDecimalTypeClass);
        CompilationTestUtils.assertContainsConstructor(unionExt4Class, unionExt4Class);
        assertEquals(5, unionExt4Class.getDeclaredConstructors().length);
        CompilationTestUtils.assertContainsDefaultMethods(unionExt4Class);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
