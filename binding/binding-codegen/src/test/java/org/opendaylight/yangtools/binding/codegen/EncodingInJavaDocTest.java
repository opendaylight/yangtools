/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.cleanUp;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.testCompilation;

import org.junit.jupiter.api.Test;

/**
 * Test if generated classes from yang file is compilable, generated javadoc comments contains
 * symbols as javadoc comment tag, which caused of compilation problem.
 */
class EncodingInJavaDocTest extends BaseCompilationTest {
    @Test
    void testAugmentToUsesInAugment() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("encoding-javadoc");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("encoding-javadoc");
        generateTestSources("/compilation/encoding-javadoc", sourcesOutputDir);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);
        cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
