/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.BASE_SVC_PATH;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.FS;

import java.io.File;
import java.util.Map;
import org.junit.Test;

/**
 * Bug5151 involves adding <code>{@literal @}return</code> annotations to accessor methods.
 */
public class Bug5151Test extends BaseCompilationTest {

    private static final String BUG_ID = "bug5151";
    private static final String SVC_PATH = BASE_SVC_PATH + FS + "urn" + FS + "test" + FS + "foo" + FS + "rev160706";

    @Test
    public void test() throws Exception {
        // Xtend code generation uses the "line.separator" system property to generate proper line endings
        // in templates, leading to test failures running on Windows-type OS.
        assumeFalse(System.getProperty("os.name").toLowerCase().startsWith("win"));
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput(BUG_ID);
        final File compiledOutputDir = CompilationTestUtils.compilerOutput(BUG_ID);
        generateTestSources(FS + "compilation" + FS + BUG_ID,
            sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final Map<String, File> generatedFiles = FileSearchUtil.getFiles(sourcesOutputDir);
        assertEquals(14, generatedFiles.size());

        final File fooContainerFile = generatedFiles.get("FooContainer.java");
        assertNotNull(fooContainerFile);
        FileSearchUtil.assertFileContains(fooContainerFile,
            "@return {@code String} fooInContainer, or {@code null} if it is not present");

        final File fooDataFile = generatedFiles.get("FooData.java");
        assertNotNull(fooDataFile);
        FileSearchUtil.assertFileContains(fooDataFile,
            "FooContainer} fooContainer, or {@code null} if it is not present");

        final File svcParent = new File(sourcesOutputDir, SVC_PATH);
        CompilationTestUtils.assertFilesCount(svcParent, 1);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
