/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.*;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.*;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Test correct code generation.
 *
 */
public class TypedefCompilationTest extends BaseCompilationTest {
    private static final String VAL = "_value";
    private static final String UNITS = "_UNITS";

    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "typedef");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "typedef");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        final List<File> sourceFiles = getSourceFiles("/compilation/typedef");
        final Set<Module> modulesToBuild = parser.parseYangModels(sourceFiles);
        final SchemaContext context = parser.resolveSchemaContext(modulesToBuild);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(new HashSet<>(types));
        generator.generateToFile(sourcesOutputDir);

        File parent = new File(sourcesOutputDir, NS_FOO);
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
        assertFilesCount(parent, 16);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        String pkg = BASE_PKG + ".urn.opendaylight.foo.rev131008";
        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> int32Ext1Class = Class.forName(pkg + ".Int32Ext1", true, loader);
        Class<?> int32Ext2Class = Class.forName(pkg + ".Int32Ext2", true, loader);
        Class<?> myDecimalTypeClass = Class.forName(pkg + ".MyDecimalType", true, loader);
        Class<?> stringExt1Class = Class.forName(pkg + ".StringExt1", true, loader);
        Class<?> stringExt2Class = Class.forName(pkg + ".StringExt2", true, loader);
        Class<?> stringExt3Class = Class.forName(pkg + ".StringExt3", true, loader);
        Class<?> unionExt1Class = Class.forName(pkg + ".UnionExt1", true, loader);
        Class<?> unionExt2Class = Class.forName(pkg + ".UnionExt2", true, loader);
        Class<?> unionExt3Class = Class.forName(pkg + ".UnionExt3", true, loader);
        Class<?> unionExt4Class = Class.forName(pkg + ".UnionExt4", true, loader);

        // typedef int32-ext1
        assertFalse(int32Ext1Class.isInterface());
        assertContainsField(int32Ext1Class, VAL, Integer.class);
        assertEquals(1, int32Ext1Class.getDeclaredFields().length);
        assertContainsConstructor(int32Ext1Class, Integer.class);
        assertContainsConstructor(int32Ext1Class, int32Ext1Class);
        assertEquals(2, int32Ext1Class.getConstructors().length);
        assertContainsDefaultMethods(int32Ext1Class);

        // typedef int32-ext2
        assertFalse(int32Ext2Class.isInterface());
        assertContainsFieldWithValue(int32Ext2Class, UNITS, String.class, "mile", Integer.class);
        assertEquals(1, int32Ext2Class.getDeclaredFields().length);
        assertContainsConstructor(int32Ext2Class, Integer.class);
        assertContainsConstructor(int32Ext2Class, int32Ext2Class);
        assertContainsConstructor(int32Ext2Class, int32Ext1Class);
        assertEquals(3, int32Ext2Class.getDeclaredConstructors().length);
        assertContainsMethod(int32Ext2Class, String.class, "toString");
        assertEquals(1, int32Ext2Class.getDeclaredMethods().length);

        // typedef string-ext1
        assertFalse(stringExt1Class.isInterface());
        assertContainsField(stringExt1Class, VAL, String.class);
        assertContainsField(stringExt1Class, "patterns", List.class);
        assertContainsField(stringExt1Class, "PATTERN_CONSTANTS", List.class);
        assertContainsMethod(stringExt1Class, String.class, "getValue");
        assertEquals(3, stringExt1Class.getDeclaredFields().length);
        assertContainsConstructor(stringExt1Class, String.class);
        assertContainsConstructor(stringExt1Class, stringExt1Class);
        assertEquals(2, stringExt1Class.getDeclaredConstructors().length);
        assertContainsGetLength(stringExt1Class);
        assertContainsDefaultMethods(stringExt1Class);

        // typedef string-ext2
        assertFalse(stringExt2Class.isInterface());
        assertEquals(0, stringExt2Class.getDeclaredFields().length);
        assertContainsConstructor(stringExt2Class, String.class);
        assertContainsConstructor(stringExt2Class, stringExt2Class);
        assertContainsConstructor(stringExt2Class, stringExt1Class);
        assertEquals(3, stringExt2Class.getDeclaredConstructors().length);
        assertContainsGetLength(stringExt2Class);
        assertEquals(1, stringExt2Class.getDeclaredMethods().length);

        // typedef string-ext3
        assertFalse(stringExt3Class.isInterface());
        assertEquals(0, stringExt3Class.getDeclaredFields().length);
        assertContainsConstructor(stringExt3Class, String.class);
        assertContainsConstructor(stringExt3Class, stringExt3Class);
        assertContainsConstructor(stringExt3Class, stringExt2Class);
        assertEquals(3, stringExt3Class.getDeclaredConstructors().length);
        assertEquals(0, stringExt3Class.getDeclaredMethods().length);

        // typedef my-decimal-type
        assertFalse(myDecimalTypeClass.isInterface());
        assertContainsField(myDecimalTypeClass, VAL, BigDecimal.class);
        assertEquals(1, myDecimalTypeClass.getDeclaredFields().length);
        assertContainsMethod(myDecimalTypeClass, BigDecimal.class, "getValue");
        assertContainsConstructor(myDecimalTypeClass, BigDecimal.class);
        assertContainsConstructor(myDecimalTypeClass, myDecimalTypeClass);
        assertEquals(2, myDecimalTypeClass.getDeclaredConstructors().length);
        assertContainsDefaultMethods(myDecimalTypeClass);

        // typedef union-ext1
        assertFalse(unionExt1Class.isInterface());
        assertContainsField(unionExt1Class, "_int16", Short.class);
        assertContainsField(unionExt1Class, "_int32", Integer.class);
        assertEquals(2, unionExt1Class.getDeclaredFields().length);
        assertContainsMethod(unionExt1Class, Short.class, "getInt16");
        assertContainsMethod(unionExt1Class, Integer.class, "getInt32");
        assertContainsConstructor(unionExt1Class, Short.class);
        assertContainsConstructor(unionExt1Class, Integer.class);
        assertContainsConstructor(unionExt1Class, unionExt1Class);
        assertEquals(3, unionExt1Class.getDeclaredConstructors().length);
        assertContainsDefaultMethods(unionExt1Class);

        // typedef union-ext2
        assertFalse(unionExt2Class.isInterface());
        assertEquals(0, unionExt2Class.getDeclaredFields().length);
        assertEquals(0, unionExt2Class.getDeclaredMethods().length);
        assertContainsConstructor(unionExt2Class, Short.class);
        assertContainsConstructor(unionExt2Class, Integer.class);
        assertContainsConstructor(unionExt2Class, unionExt2Class);
        assertContainsConstructor(unionExt2Class, unionExt1Class);
        assertEquals(4, unionExt2Class.getDeclaredConstructors().length);

        // typedef union-ext3
        assertFalse(unionExt3Class.isInterface());
        assertContainsField(unionExt3Class, "_string", String.class);
        assertContainsField(unionExt3Class, "_unionExt2", unionExt2Class);
        assertContainsFieldWithValue(unionExt3Class, UNITS, String.class, "object id", String.class);
        assertEquals(3, unionExt3Class.getDeclaredFields().length);
        assertContainsMethod(unionExt3Class, String.class, "getString");
        assertContainsMethod(unionExt3Class, unionExt2Class, "getUnionExt2");
        assertContainsConstructor(unionExt3Class, String.class);
        assertContainsConstructor(unionExt3Class, unionExt2Class);
        assertContainsConstructor(unionExt3Class, unionExt3Class);
        assertEquals(3, unionExt3Class.getDeclaredConstructors().length);
        assertContainsDefaultMethods(unionExt3Class);

        // typedef union-ext4
        assertFalse(unionExt4Class.isInterface());
        assertContainsField(unionExt4Class, "_unionExt3", unionExt3Class);
        assertContainsField(unionExt4Class, "_int32Ext2", int32Ext2Class);
        assertContainsField(unionExt4Class, "_empty", Boolean.class);
        assertContainsField(unionExt4Class, "_myDecimalType", myDecimalTypeClass);
        assertEquals(4, unionExt4Class.getDeclaredFields().length);
        assertContainsMethod(unionExt4Class, unionExt3Class, "getUnionExt3");
        assertContainsMethod(unionExt4Class, int32Ext2Class, "getInt32Ext2");
        assertContainsMethod(unionExt4Class, Boolean.class, "isEmpty");
        assertContainsMethod(unionExt4Class, myDecimalTypeClass, "getMyDecimalType");
        assertContainsConstructor(unionExt4Class, unionExt3Class);
        assertContainsConstructor(unionExt4Class, int32Ext2Class);
        assertContainsConstructor(unionExt4Class, Boolean.class);
        assertContainsConstructor(unionExt4Class, myDecimalTypeClass);
        assertContainsConstructor(unionExt4Class, unionExt4Class);
        assertEquals(5, unionExt4Class.getDeclaredConstructors().length);
        assertContainsDefaultMethods(unionExt4Class);

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

}
