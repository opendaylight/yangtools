/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;

import static org.junit.Assert.assertTrue;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.BASE_PKG;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.NS_TEST;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.assertFilesCount;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.assertImplementsIfc;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.cleanUp;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.testCompilation;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Test;

/**
 * Test correct code generation.
 *
 */
public class NestedGroupingCompilationTest extends BaseCompilationTest {

    @Test
    public void testListGeneration() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("nested-grouping");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("nested-grouping");
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
        assertFilesCount(parent, 5);

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
}
