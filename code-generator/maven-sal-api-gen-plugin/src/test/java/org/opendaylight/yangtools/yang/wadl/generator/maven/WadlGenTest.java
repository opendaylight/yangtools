package org.opendaylight.yangtools.yang.wadl.generator.maven;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.opendaylight.yangtools.yang2sources.spi.CodeGenerator;

public class WadlGenTest {
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
        final List<File> sourceFiles = getSourceFiles("/wadl-gen");
        final Set<Module> modulesToBuild = parser.parseYangModels(sourceFiles);
        final SchemaContext context = parser.resolveSchemaContext(modulesToBuild);
        final CodeGenerator generator = new WadlGenerator();
        Collection<File> generatedWadlFiles = generator.generateSources(context, GENERATOR_OUTPUT_DIR, modulesToBuild);
        assertEquals(3, generatedWadlFiles.size());
    }

    private static List<File> getSourceFiles(String path) throws FileNotFoundException {
        final String resPath = WadlGenTest.class.getResource(path).getPath();
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

    private static void deleteTestDir(File file) {
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
