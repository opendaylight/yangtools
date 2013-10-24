/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.Before;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public abstract class BaseCompilationTest {
    public static final String FS = File.separator;
    protected static final String BASE_PKG = "org.opendaylight.yang.gen.v1";

    protected static final String TEST_PATH = "target" + FS + "test";
    protected static final File TEST_DIR = new File(TEST_PATH);

    protected static final String GENERATOR_OUTPUT_PATH = TEST_PATH + FS + "src";
    protected static final File GENERATOR_OUTPUT_DIR = new File(GENERATOR_OUTPUT_PATH);
    protected static final String COMPILER_OUTPUT_PATH = TEST_PATH + FS + "bin";
    protected static final File COMPILER_OUTPUT_DIR = new File(COMPILER_OUTPUT_PATH);

    protected static final String BASE_PATH = "org" + FS + "opendaylight" + FS + "yang" + FS + "gen" + FS + "v1";
    protected static final String NS_TEST = BASE_PATH + FS + "urn" + FS + "opendaylight" + FS + "test" + FS
            + "rev131008";
    protected static final String NS_FOO = BASE_PATH + FS + "urn" + FS + "opendaylight" + FS + "foo" + FS + "rev131008";
    protected static final String NS_BAR = BASE_PATH + FS + "urn" + FS + "opendaylight" + FS + "bar" + FS + "rev131008";
    protected static final String NS_BAZ = BASE_PATH + FS + "urn" + FS + "opendaylight" + FS + "baz" + FS + "rev131008";

    protected YangParserImpl parser;
    protected BindingGenerator bindingGenerator;

    @BeforeClass
    public static void createTestDirs() {
        if (TEST_DIR.exists()) {
            deleteTestDir(TEST_DIR);
        }
        assertTrue(GENERATOR_OUTPUT_DIR.mkdirs());
        assertTrue(COMPILER_OUTPUT_DIR.mkdirs());
    }

    @Before
    public void init() {
        parser = new YangParserImpl();
        bindingGenerator = new BindingGeneratorImpl();
    }

    /**
     * Method to clean resources. It is manually called at the end of each test
     * instead of marking it with @After annotation to prevent removing
     * generated code if test fails.
     */
    protected void cleanUp(File... resourceDirs) {
        for (File resourceDir : resourceDirs) {
            if (resourceDir.exists()) {
                deleteTestDir(resourceDir);
            }
        }
    }

    protected static void testImplementIfc(Class<?> classToTest, Class<?> ifcClass) throws ClassNotFoundException {
        Class<?>[] interfaces = classToTest.getInterfaces();
        List<Class<?>> ifcsList = Arrays.asList(interfaces);
        if (!ifcsList.contains(ifcClass)) {
            throw new AssertionError(classToTest + " should implement " + ifcClass);
        }
    }

    protected static void testAugmentation(Class<?> classToTest, String paramClass) {
        final String augmentationIfc = "interface org.opendaylight.yangtools.yang.binding.Augmentation";
        ParameterizedType augmentation = null;
        for (java.lang.reflect.Type ifc : classToTest.getGenericInterfaces()) {
            if (ifc instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) ifc;
                if (augmentationIfc.equals(pt.getRawType().toString())) {
                    augmentation = pt;
                }
            }
        }
        assertNotNull(augmentation);

        java.lang.reflect.Type[] typeArguments = augmentation.getActualTypeArguments();
        assertEquals(1, typeArguments.length);
        assertEquals("interface " + paramClass, typeArguments[0].toString());
    }

    /**
     * Test if source code is compilable.
     * 
     * @param sourcesOutputDir
     *            directory containing source files
     * @param compiledOutputDir
     *            compiler output directory
     */
    protected static void testCompilation(File sourcesOutputDir, File compiledOutputDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        List<File> filesList = getJavaFiles(sourcesOutputDir);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(filesList);
        Iterable<String> options = Arrays.asList("-d", compiledOutputDir.getAbsolutePath());
        boolean compiled = compiler.getTask(null, null, null, options, null, compilationUnits).call();
        assertTrue(compiled);
    }

    /**
     * Search recursively given directory for *.java files.
     * 
     * @param directory
     *            directory to search
     * @return List of java files found
     */
    private static List<File> getJavaFiles(File directory) {
        List<File> result = new ArrayList<>();
        File[] filesToRead = directory.listFiles();
        if (filesToRead != null) {
            for (File file : filesToRead) {
                if (file.isDirectory()) {
                    result.addAll(getJavaFiles(file));
                } else {
                    String absPath = file.getAbsolutePath();
                    if (absPath.endsWith(".java")) {
                        result.add(file);
                    }
                }
            }
        }
        return result;
    }

    protected static List<File> getSourceFiles(String path) throws FileNotFoundException {
        final String resPath = BaseCompilationTest.class.getResource(path).getPath();
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
