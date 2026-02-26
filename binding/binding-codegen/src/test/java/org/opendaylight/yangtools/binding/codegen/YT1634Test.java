/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.BASE_PKG;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.testCompilation;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;

class YT1634Test extends BaseCompilationTest {
    private Path sourcesOutputDir;
    private Path compiledOutputDir;

    @BeforeEach
    void beforeEach() {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("yt1634");
        compiledOutputDir = CompilationTestUtils.compilerOutput("yt1634");
    }

    @AfterEach
    void afterEach() throws Exception {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void test() throws Exception {
        generateTestSources("/compilation/yt1634", sourcesOutputDir);
        testCompilation(sourcesOutputDir, compiledOutputDir);

        final var loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() });
        final var foo = Class.forName(BASE_PKG + ".yt1634.norev.Foo", true, loader);
        final var ctor = CompilationTestUtils.assertContainsConstructor(foo, Decimal64.class);
        final var badVal = Decimal64.valueOf(2, 0);

        final var ex = assertThrows(InvocationTargetException.class, () -> ctor.newInstance(badVal));
        final var cause = assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertEquals("Invalid 0.0 scale: 2, expected 5.", cause.getMessage());
    }

}
