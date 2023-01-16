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

public class Mdsal807Test extends BaseCompilationTest {
    private File sourcesOutputDir;
    private File compiledOutputDir;

    @Before
    public void before() throws IOException, URISyntaxException {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal807");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal807");
    }

    @After
    public void after() {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testBitsTypedef() throws IOException, URISyntaxException {
        generateTestSources("/compilation/mdsal807", sourcesOutputDir);
        final var pmDataType = FileSearchUtil.getFiles(sourcesOutputDir).get("TableConfig.java");
        assertNotNull(pmDataType);

        FileSearchUtil.assertFileContainsConsecutiveLines(pmDataType, Files.readString(pmDataType.toPath()),
            "    public static TableConfig getDefaultInstance(final String defaultValue) {",
            "        List<String> properties = Lists.newArrayList(\"oFPTCDEPRECATEDMASK\"",
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
