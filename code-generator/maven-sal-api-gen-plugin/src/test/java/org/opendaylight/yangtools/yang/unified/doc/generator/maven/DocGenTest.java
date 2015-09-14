/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.unified.doc.generator.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;

public class DocGenTest {
    public static final String FS = File.separator;
    private static final String TEST_PATH = "target" + FS + "test" + FS + "site";
    private static final File GENERATOR_OUTPUT_DIR = new File(TEST_PATH);
    private YangParserImpl parser;

    @Before
    public void init() {
        if (GENERATOR_OUTPUT_DIR.exists()) {
            deleteTestDir(GENERATOR_OUTPUT_DIR);
        }
        assertTrue(GENERATOR_OUTPUT_DIR.mkdirs());
        parser = new YangParserImpl();
    }

    @After
    public void cleanUp() {
        if (GENERATOR_OUTPUT_DIR.exists()) {
            deleteTestDir(GENERATOR_OUTPUT_DIR);
        }
    }

    @Test
    public void testListGeneration() throws Exception {
        final List<File> sourceFiles = getSourceFiles("/doc-gen");
        final SchemaContext context = parser.parseFiles(sourceFiles);
        final BasicCodeGenerator generator = new DocumentationGeneratorImpl();
        Collection<File> generatedFiles = generator.generateSources(context, GENERATOR_OUTPUT_DIR, context.getModules());
        assertEquals(4, generatedFiles.size());
    }

    private static List<File> getSourceFiles(final String path) throws Exception {
        final URI resPath = DocGenTest.class.getResource(path).toURI();
        final File sourcesDir = new File(resPath);
        if (sourcesDir.exists()) {
            final List<File> sourceFiles = new ArrayList<>();
            final File[] fileArray = sourcesDir.listFiles();
            if (fileArray == null) {
                throw new IllegalArgumentException("Unable to locate files in " + sourcesDir);
            }
            sourceFiles.addAll(Arrays.asList(fileArray));
            return sourceFiles;
        } else {
            throw new FileNotFoundException("Testing files were not found(" + sourcesDir.getName() + ")");
        }
    }

    private static void deleteTestDir(final File file) {
        if (file.isDirectory()) {
            File[] filesToDelete = file.listFiles();
            if (filesToDelete != null) {
                for (File f : filesToDelete) {
                    deleteTestDir(f);
                }
            }
        }
        if (!file.delete()) {
            throw new RuntimeException("Failed to clean up after test");
        }
    }

}
