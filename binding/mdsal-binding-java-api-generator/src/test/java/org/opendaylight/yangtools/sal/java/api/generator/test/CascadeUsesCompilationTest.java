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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.BASE_PKG;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.COMPILER_OUTPUT_PATH;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.FS;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.GENERATOR_OUTPUT_PATH;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.NS_BAR;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.NS_BAZ;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.NS_FOO;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.assertFilesCount;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.assertImplementsIfc;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.cleanUp;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.getSourceFiles;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.testCompilation;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class CascadeUsesCompilationTest extends BaseCompilationTest {

    @Test
    public void testCascadeUsesCompilation() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "cascade-uses");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "cascade-uses");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        final List<File> sourceFiles = getSourceFiles("/compilation/cascade-uses");
        final SchemaContext context = TestUtils.parseYangSources(sourceFiles);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(ImmutableSet.copyOf(types));
        generator.generateToFile(sourcesOutputDir);

        // Test if all sources are generated from module foo
        File parent = new File(sourcesOutputDir, NS_FOO);
        assertFilesCount(parent, 5);
        File fooData = new File(parent, "FooData.java");
        File foo_gr1 = new File(parent, "FooGr1.java");
        File nodes = new File(parent, "Nodes.java");
        File nodesBuilder = new File(parent, "NodesBuilder.java");
        assertTrue(fooData.exists());
        assertTrue(foo_gr1.exists());
        assertTrue(nodes.exists());
        assertTrue(nodesBuilder.exists());

        // Test if all sources are generated from module bar
        parent = new File(sourcesOutputDir, NS_BAR);
        assertFilesCount(parent, 2);
        File barGr1 = new File(parent, "BarGr1.java");
        File barGr2 = new File(parent, "BarGr2.java");
        assertTrue(barGr1.exists());
        assertTrue(barGr2.exists());

        // Test if all sources are generated from module baz
        parent = new File(sourcesOutputDir, NS_BAZ);
        assertFilesCount(parent, 1);
        File bazGr1 = new File(parent, "BazGr1.java");
        assertTrue(bazGr1.exists());

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> nodesClass = Class.forName(BASE_PKG + ".urn.opendaylight.foo.rev131008.Nodes", true,
                loader);
        Class<?> nodesBuilderClass = Class.forName(BASE_PKG + ".urn.opendaylight.foo.rev131008.NodesBuilder", true,
                loader);
        Class<?> fooGr1Class = Class.forName(BASE_PKG + ".urn.opendaylight.foo.rev131008.FooGr1", true, loader);
        Class<?> barGr2Class = Class.forName(BASE_PKG + ".urn.opendaylight.bar.rev131008.BarGr2", true, loader);
        Class<?> barGr1Class = Class.forName(BASE_PKG + ".urn.opendaylight.bar.rev131008.BarGr1", true, loader);
        Class<?> bazGr1Class = Class.forName(BASE_PKG + ".urn.opendaylight.baz.rev131008.BazGr1", true, loader);

        // test generated interface from 'container nodes'
        assertImplementsIfc(nodesClass, fooGr1Class);
        assertImplementsIfc(nodesClass, barGr2Class);

        // test generated builder for 'container nodes'
        assertFalse(nodesBuilderClass.isInterface());
        Constructor<?>[] nodesBuilderConstructors = nodesBuilderClass.getConstructors();
        assertEquals(6, nodesBuilderConstructors.length);

        // test generation of builder constructors from uses in 'container nodes'
        Constructor<?> defaultConstructor = null;
        Constructor<?> usesFooGr1 = null;
        Constructor<?> usesBarGr2 = null;
        Constructor<?> usesBarGr1 = null;
        Constructor<?> usesBazGr1 = null;
        for (Constructor<?> c : nodesBuilderConstructors) {
            Class<?>[] params = c.getParameterTypes();
            if (params.length == 0) {
                defaultConstructor = c;
            } else {
                assertEquals(1, params.length);
                if (params[0].equals(fooGr1Class)) {
                    usesFooGr1 = c;
                } else if (params[0].equals(barGr2Class)) {
                    usesBarGr2 = c;
                } else if (params[0].equals(barGr1Class)) {
                    usesBarGr1 = c;
                } else if (params[0].equals(bazGr1Class)) {
                    usesBazGr1 = c;
                }
            }
        }
        assertNotNull(defaultConstructor);
        assertNotNull(usesFooGr1);
        assertNotNull(usesBarGr2);
        assertNotNull(usesBarGr1);
        assertNotNull(usesBazGr1);

        Method fieldsFromMethod = null;
        for (Method m : nodesBuilderClass.getDeclaredMethods()) {
            String methodName = m.getName();
            if ("fieldsFrom".equals(methodName)) {
                fieldsFromMethod = m;
            }
        }
        assertNotNull(fieldsFromMethod);
        assertEquals(1, fieldsFromMethod.getParameterTypes().length);

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

}
