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

public class Mdsal738Test extends BaseCompilationTest {
    private Path sourcesOutputDir;
    private Path compiledOutputDir;

    @BeforeEach
    void before() {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal738");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal738");
    }

    @AfterEach
    void after() throws Exception {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testUnionOfDecimal64() throws Exception {
        generateTestSources("/compilation/mdsal738", sourcesOutputDir);
        final var pmDataType = FileSearchUtil.getFiles(sourcesOutputDir).get("PmDataType.java");
        assertNotNull(pmDataType);

        final var content = Files.readString(pmDataType);
        FileSearchUtil.assertFileContainsConsecutiveLines(pmDataType, content,
            "    public String stringValue() {",
            "        if (_uint64 != null) {",
            "            return _uint64.toCanonicalString();",
            "        }",
            "        if (_int64 != null) {",
            "            return _int64.toString();",
            "        }",
            "        if (_decimal64 != null) {",
            "            return _decimal64.toCanonicalString();",
            "        }",
            "        throw new IllegalStateException(\"No value assigned\");",
            "    }");

        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
    }
}
