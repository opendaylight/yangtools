/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;

import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.cleanUp;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.testCompilation;

import java.io.File;
import org.junit.Test;

/**
 * Test if generated classes from yang file is compilable, generated javadoc comments contains
 * symbols as javadoc comment tag, which caused of compilation problem.
 */
public class EncodingInJavaDocTest extends BaseCompilationTest {

    @Test
    public void testAugmentToUsesInAugment() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("encoding-javadoc");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("encoding-javadoc");
        generateTestSources("/compilation/encoding-javadoc", sourcesOutputDir);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);
        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

}
