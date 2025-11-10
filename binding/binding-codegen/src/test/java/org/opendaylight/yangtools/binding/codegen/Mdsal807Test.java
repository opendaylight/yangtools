/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Mdsal807Test extends BaseCompilationTest {
    private Path sourcesOutputDir;
    private Path compiledOutputDir;

    @BeforeEach
    void before() {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal807");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal807");
    }

    @AfterEach
    void after() throws Exception {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testBitsTypedef() throws Exception {
        generateTestSources("/compilation/mdsal807", sourcesOutputDir);
        final var pmDataType = FileSearchUtil.getFiles(sourcesOutputDir).get("TableConfig.java");
        assertNotNull(pmDataType);

        FileSearchUtil.assertFileContainsConsecutiveLines(pmDataType, Files.readString(pmDataType),
            "    public static TableConfig getDefaultInstance(final String defaultValue) {",
            "        var properties = List.of(\"oFPTCDEPRECATEDMASK\"",
            "        );",
            "        if (!properties.contains(defaultValue)) {",
            "            throw new IllegalArgumentException(\"invalid default parameter\");",
            "        }",
            "        int i = 0;",
            "        return new TableConfig(",
            "        properties.get(i++).equals(defaultValue) ? true : false",
            "        );",
            "    }"
        );
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
    }
}
