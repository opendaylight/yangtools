/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.BASE_SVC_PATH;

import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Bug5151 involves adding <code>{@literal @}return</code> annotations to accessor methods.
 */
class Bug5151Test extends BaseCompilationTest {
    private static final String BUG_ID = "bug5151";
    private static final Path SVC_PATH = BASE_SVC_PATH.resolve(Path.of("urn", "test", "foo", "rev160706"));

    @Test
    void test() throws Exception {
        // Xtend code generation uses the "line.separator" system property to generate proper line endings
        // in templates, leading to test failures running on Windows-type OS.
        assumeFalse(System.getProperty("os.name").toLowerCase().startsWith("win"));
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput(BUG_ID);
        final var compiledOutputDir = CompilationTestUtils.compilerOutput(BUG_ID);
        generateTestSources(File.separator + "compilation" + File.separator + BUG_ID,
            sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final var generatedFiles = FileSearchUtil.getFiles(sourcesOutputDir);
        assertEquals(14, generatedFiles.size());

        final var fooContainerFile = generatedFiles.get("FooContainer.java");
        assertNotNull(fooContainerFile);
        FileSearchUtil.assertFileContains(fooContainerFile,
            "@return {@code String} fooInContainer, or {@code null} if it is not present");

        final var fooDataFile = generatedFiles.get("FooData.java");
        assertNotNull(fooDataFile);
        FileSearchUtil.assertFileContains(fooDataFile,
            "FooContainer} fooContainer, or {@code null} if it is not present");

        CompilationTestUtils.assertFilesCount(sourcesOutputDir.resolve(SVC_PATH), 1);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
