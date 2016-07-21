/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Bug5151 involves adding <code>{@literal @}return</code> annotations to accessor methods.
 */
public class Bug5151Test extends BaseCompilationTest {

    private static final String BUG_ID = "bug5151";

    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = new File(CompilationTestUtils.GENERATOR_OUTPUT_PATH + CompilationTestUtils.FS + BUG_ID);
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(CompilationTestUtils.COMPILER_OUTPUT_PATH + CompilationTestUtils.FS + BUG_ID);
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        generateTestSources(CompilationTestUtils.FS + "compilation" + CompilationTestUtils.FS + BUG_ID, sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final Map<String, File> generatedFiles = getFiles(sourcesOutputDir);
        assertEquals(3, generatedFiles.size());

        final File fooContainerFile = generatedFiles.get("FooContainer.java");
        assertNotNull(fooContainerFile);
        assertTrue(findInFile(fooContainerFile,
                "@return <code>java.lang.String</code> <code>fooInContainer</code>, "
                        + "or <code>null</code> if not present"));

        final File fooDataFile = generatedFiles.get("FooData.java");
        assertNotNull(fooDataFile);
        assertTrue(findInFile(fooDataFile, "FooContainer</code> <code>fooContainer</code>, or <code>null</code> if not present"));

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private static boolean findInFile(final File file, final String searchText) throws FileNotFoundException {
        final Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            final String nextLine = scanner.nextLine();
            if (nextLine.contains(searchText)) {
                return true;
            }
        }
        return false;
    }

    private void generateTestSources(final String resourceDirPath, final File sourcesOutputDir) throws Exception {
        final List<File> sourceFiles = CompilationTestUtils.getSourceFiles(resourceDirPath);
        final SchemaContext context = TestUtils.parseYangSources(sourceFiles);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(ImmutableSet.copyOf(types));
        generator.generateToFile(sourcesOutputDir);
    }

    private static Map<String, File> getFiles(final File path) {
        return getFiles(path, Maps.newHashMap());
    }

    private static Map<String, File> getFiles(final File path, final Map<String, File> files) {
        final File [] dirFiles = path.listFiles();
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                return getFiles(file, files);
            } else {
                files.put(file.getName(), file);
            }
        }
        return files;
    }
}
