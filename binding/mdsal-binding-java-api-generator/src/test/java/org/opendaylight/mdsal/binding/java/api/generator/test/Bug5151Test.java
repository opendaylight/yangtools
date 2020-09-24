/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Map;
import org.junit.Test;

/**
 * Bug5151 involves adding <code>{@literal @}return</code> annotations to accessor methods.
 */
public class Bug5151Test extends BaseCompilationTest {

    private static final String BUG_ID = "bug5151";

    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput(BUG_ID);
        final File compiledOutputDir = CompilationTestUtils.compilerOutput(BUG_ID);
        generateTestSources(CompilationTestUtils.FS + "compilation" + CompilationTestUtils.FS + BUG_ID,
            sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final Map<String, File> generatedFiles = FileSearchUtil.getFiles(sourcesOutputDir);
        assertEquals(13, generatedFiles.size());

        final File fooContainerFile = generatedFiles.get("FooContainer.java");
        assertNotNull(fooContainerFile);
        assertTrue(FileSearchUtil.findInFile(fooContainerFile,
                "@return <code>java.lang.String</code> <code>fooInContainer</code>, "
                        + "or <code>null</code> if not present"));

        final File fooDataFile = generatedFiles.get("FooData.java");
        assertNotNull(fooDataFile);
        assertTrue(FileSearchUtil.findInFile(fooDataFile,
            "FooContainer</code> <code>fooContainer</code>, or <code>null</code> if not present"));

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
