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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.BASE_PKG;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.NS_BAR;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.NS_BAZ;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.NS_FOO;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.NS_SVC_BAR;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.NS_SVC_BAZ;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.NS_SVC_FOO;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.assertFilesCount;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.assertImplementsIfc;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.cleanUp;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.testCompilation;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Test;

public class CascadeUsesCompilationTest extends BaseCompilationTest {

    @Test
    public void testCascadeUsesCompilation() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("cascade-uses");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("cascade-uses");
        generateTestSources("/compilation/cascade-uses", sourcesOutputDir);

        // Test if all sources are generated from module foo
        File parent = new File(sourcesOutputDir, NS_FOO);
        assertTrue(new File(parent, "FooData.java").exists());
        assertTrue(new File(parent, "FooGr1.java").exists());
        assertTrue(new File(parent, "Nodes.java").exists());
        assertTrue(new File(parent, "NodesBuilder.java").exists());
        assertFilesCount(parent, 5);
        File svcParent = new File(sourcesOutputDir, NS_SVC_FOO);
        assertFilesCount(svcParent, 1);

        // Test if all sources are generated from module bar
        parent = new File(sourcesOutputDir, NS_BAR);
        assertTrue(new File(parent, "BarData.java").exists());
        assertTrue(new File(parent, "BarGr1.java").exists());
        assertTrue(new File(parent, "BarGr2.java").exists());
        assertFilesCount(parent, 3);
        svcParent = new File(sourcesOutputDir, NS_SVC_BAR);
        assertFilesCount(svcParent, 1);

        // Test if all sources are generated from module baz
        parent = new File(sourcesOutputDir, NS_BAZ);
        assertTrue(new File(parent, "BazData.java").exists());
        assertTrue(new File(parent, "BazGr1.java").exists());
        assertFilesCount(parent, 2);
        svcParent = new File(sourcesOutputDir, NS_SVC_BAZ);
        assertFilesCount(svcParent, 1);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        final var loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
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
