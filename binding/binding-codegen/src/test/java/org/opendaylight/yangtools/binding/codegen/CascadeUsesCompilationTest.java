/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.BASE_PKG;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.NS_BAR;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.NS_BAZ;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.NS_FOO;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.NS_SVC_BAR;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.NS_SVC_BAZ;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.NS_SVC_FOO;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.assertFilesCount;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.assertImplementsIfc;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.assertRegularFile;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.cleanUp;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.testCompilation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.jupiter.api.Test;

class CascadeUsesCompilationTest extends BaseCompilationTest {
    @Test
    void testCascadeUsesCompilation() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("cascade-uses");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("cascade-uses");
        generateTestSources("/compilation/cascade-uses", sourcesOutputDir);

        // Test if all sources are generated from module foo
        var parent = sourcesOutputDir.resolve(NS_FOO);
        assertRegularFile(parent, "FooData.java");
        assertRegularFile(parent, "FooGr1.java");
        assertRegularFile(parent, "Nodes.java");
        assertRegularFile(parent, "NodesBuilder.java");
        assertFilesCount(parent, 5);
        var svcParent = sourcesOutputDir.resolve(NS_SVC_FOO);
        assertFilesCount(svcParent, 1);

        // Test if all sources are generated from module bar
        parent = sourcesOutputDir.resolve(NS_BAR);
        assertRegularFile(parent, "BarData.java");
        assertRegularFile(parent, "BarGr1.java");
        assertRegularFile(parent, "BarGr2.java");
        assertFilesCount(parent, 3);
        svcParent = sourcesOutputDir.resolve(NS_SVC_BAR);
        assertFilesCount(svcParent, 1);

        // Test if all sources are generated from module baz
        parent = sourcesOutputDir.resolve(NS_BAZ);
        assertRegularFile(parent, "BazData.java");
        assertRegularFile(parent, "BazGr1.java");
        assertFilesCount(parent, 2);
        svcParent = sourcesOutputDir.resolve(NS_SVC_BAZ);
        assertFilesCount(svcParent, 1);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        final var loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() });
        final var nodesClass = Class.forName(BASE_PKG + ".urn.opendaylight.foo.rev131008.Nodes", true, loader);
        final var nodesBuilderClass = Class.forName(BASE_PKG + ".urn.opendaylight.foo.rev131008.NodesBuilder", true,
                loader);
        final var fooGr1Class = Class.forName(BASE_PKG + ".urn.opendaylight.foo.rev131008.FooGr1", true, loader);
        final var barGr2Class = Class.forName(BASE_PKG + ".urn.opendaylight.bar.rev131008.BarGr2", true, loader);
        final var barGr1Class = Class.forName(BASE_PKG + ".urn.opendaylight.bar.rev131008.BarGr1", true, loader);
        final var bazGr1Class = Class.forName(BASE_PKG + ".urn.opendaylight.baz.rev131008.BazGr1", true, loader);

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
        assertEquals(1, fieldsFromMethod.getParameterCount());

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
