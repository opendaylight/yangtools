/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Mdsal738Test extends BaseCompilationTest {
    private File sourcesOutputDir;
    private File compiledOutputDir;

    @Before
    public void before() throws IOException, URISyntaxException {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal738");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal738");
    }

    @After
    public void after() {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testUnionOfDecimal64() throws IOException, URISyntaxException {
        generateTestSources("/compilation/mdsal738", sourcesOutputDir);
        final var pmDataType = FileSearchUtil.getFiles(sourcesOutputDir).get("PmDataType.java");
        assertNotNull(pmDataType);

        final var content = Files.readString(pmDataType.toPath());
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
