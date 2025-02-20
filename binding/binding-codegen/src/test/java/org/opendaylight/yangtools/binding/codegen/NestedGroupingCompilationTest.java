/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.BASE_PKG;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.NS_SVC_TEST;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.NS_TEST;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.assertFilesCount;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.assertImplementsIfc;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.assertRegularFile;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.cleanUp;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.testCompilation;

import java.net.URL;
import java.net.URLClassLoader;
import org.junit.jupiter.api.Test;

class NestedGroupingCompilationTest extends BaseCompilationTest {
    @Test
    void testListGeneration() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("nested-grouping");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("nested-grouping");
        generateTestSources("/compilation/nested-grouping", sourcesOutputDir);

        // Test if all sources are generated
        var parent = sourcesOutputDir.resolve(NS_TEST);
        assertRegularFile(parent, "Foo.java");
        assertRegularFile(parent, "FooBuilder.java");
        assertRegularFile(parent, "TestData.java");
        assertFilesCount(parent, 4);
        final var svcParent = sourcesOutputDir.resolve(NS_SVC_TEST);
        assertFilesCount(svcParent, 1);

        parent = parent.resolve("foo");
        assertRegularFile(parent, "Bar.java");
        assertFilesCount(parent, 1);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() });
        Class<?> fooClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.Foo", true, loader);
        Class<?> barClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.foo.Bar", true, loader);

        // Test generated 'foo'
        assertTrue(fooClass.isInterface());
        assertImplementsIfc(fooClass, barClass);

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
