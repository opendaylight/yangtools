/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.BASE_PKG;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.COMPILER_OUTPUT_PATH;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.FS;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.GENERATOR_OUTPUT_PATH;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.NS_TEST;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.assertFilesCount;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.assertImplementsIfc;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.cleanUp;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.getSourceFiles;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.testCompilation;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Test correct code generation.
 *
 */
public class NestedGroupingCompilationTest extends BaseCompilationTest {

    @Test
    public void testListGeneration() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "nested-grouping");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "nested-grouping");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        generateTestSources("/compilation/nested-grouping", sourcesOutputDir);

        // Test if all sources are generated
        File parent = new File(sourcesOutputDir, NS_TEST);
        File foo = new File(parent, "Foo.java");
        File fooBuilder = new File(parent, "FooBuilder.java");
        File testData = new File(parent, "TestData.java");
        File fooDir = new File(parent, "foo");
        assertTrue(foo.exists());
        assertTrue(fooBuilder.exists());
        assertTrue(testData.exists());
        assertTrue(fooDir.exists());
        assertFilesCount(parent, 4);

        parent = new File(parent, "foo");
        File bar = new File(parent, "Bar.java");
        assertTrue(bar.exists());
        assertFilesCount(parent, 1);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> fooClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.Foo", true, loader);
        Class<?> barClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.foo.Bar", true, loader);

        // Test generated 'foo'
        assertTrue(fooClass.isInterface());
        assertImplementsIfc(fooClass, barClass);

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private void generateTestSources(final String resourceDirPath, final File sourcesOutputDir) throws Exception {
        final List<File> sourceFiles = getSourceFiles(resourceDirPath);
        final SchemaContext context = RetestUtils.parseYangSources(sourceFiles);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(ImmutableSet.copyOf(types));
        generator.generateToFile(sourcesOutputDir);
    }

}
