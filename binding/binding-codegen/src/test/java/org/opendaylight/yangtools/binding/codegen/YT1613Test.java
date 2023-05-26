/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.BASE_PKG;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.testCompilation;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class YT1613Test extends BaseCompilationTest {
    private Path sourcesOutputDir;
    private Path compiledOutputDir;

    @BeforeEach
    void beforeEach() {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("yt1613");
        compiledOutputDir = CompilationTestUtils.compilerOutput("yt1613");
    }

    @AfterEach
    void afterEach() throws Exception {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void test() throws Exception {
        generateTestSources("/compilation/yt1613", sourcesOutputDir);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() });
        final Class<?> foo = Class.forName(BASE_PKG + ".yt1613.norev.Foo", true, loader);
        final Class<?> bar = Class.forName(BASE_PKG + ".yt1613.norev.Bar", true, loader);

        // case of not including range statement: range checkers should NOT generate
        assertEquals(0, Arrays.stream(foo.getDeclaredMethods())
                .filter(method -> method.getName().equals("check_valueRange")).count());
        // case of including range statement covering entire base typ: range checkers should NOT generate
        assertEquals(0, Arrays.stream(bar.getDeclaredMethods())
                .filter(method -> method.getName().equals("check_valueRange")).count());

        final Class<?> myType1 = Class.forName(BASE_PKG + ".yt1613.norev.MyType1", true, loader);
        final Class<?> myType2 = Class.forName(BASE_PKG + ".yt1613.norev.MyType2", true, loader);
        final Class<?> myType3 = Class.forName(BASE_PKG + ".yt1613.norev.MyType3", true, loader);

        // case of applying new range constraints: range checkers SHOULD generate
        assertEquals(1, Arrays.stream(myType1.getDeclaredMethods())
                .filter(method -> method.getName().equals("check_valueRange")).count());
        // case of including range statement covering entire base non-build-in typ: range checkers should NOT generate
        assertEquals(0, Arrays.stream(myType2.getDeclaredMethods())
                .filter(method -> method.getName().equals("check_valueRange")).count());
        // case of applying new range constraints on non-build-in type: range checkers SHOULD generate
        assertEquals(1, Arrays.stream(myType3.getDeclaredMethods())
                .filter(method -> method.getName().equals("check_valueRange")).count());
    }
}
