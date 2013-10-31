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

public class CompilationTestUtils {
    public static final String FS = File.separator;
    static final String BASE_PKG = "org.opendaylight.yang.gen.v1";

    static final String TEST_PATH = "target" + FS + "test";
    static final File TEST_DIR = new File(TEST_PATH);

    static final String GENERATOR_OUTPUT_PATH = TEST_PATH + FS + "src";
    static final File GENERATOR_OUTPUT_DIR = new File(GENERATOR_OUTPUT_PATH);
    static final String COMPILER_OUTPUT_PATH = TEST_PATH + FS + "bin";
    static final File COMPILER_OUTPUT_DIR = new File(COMPILER_OUTPUT_PATH);

    static final String BASE_PATH = "org" + FS + "opendaylight" + FS + "yang" + FS + "gen" + FS + "v1";
    static final String NS_TEST = BASE_PATH + FS + "urn" + FS + "opendaylight" + FS + "test" + FS
            + "rev131008";
    static final String NS_FOO = BASE_PATH + FS + "urn" + FS + "opendaylight" + FS + "foo" + FS + "rev131008";
    static final String NS_BAR = BASE_PATH + FS + "urn" + FS + "opendaylight" + FS + "bar" + FS + "rev131008";
    static final String NS_BAZ = BASE_PATH + FS + "urn" + FS + "opendaylight" + FS + "baz" + FS + "rev131008";

    /**
     * Method to clean resources. It is manually called at the end of each test
     * instead of marking it with @After annotation to prevent removing
     * generated code if test fails.
     */
    static void cleanUp(File... resourceDirs) {
        for (File resourceDir : resourceDirs) {
            if (resourceDir.exists()) {
                deleteTestDir(resourceDir);
            }
        }
    }

    /**
     * Test if generated source implements interface.
     *
     * @param classToTest
     *            source to test
     * @param ifcClass
     *            expected interface type
     */
    static void testImplementsIfc(Class<?> classToTest, Class<?> ifcClass) {
        Class<?>[] interfaces = classToTest.getInterfaces();
        List<Class<?>> ifcsList = Arrays.asList(interfaces);
        if (!ifcsList.contains(ifcClass)) {
            throw new AssertionError(classToTest + " should implement " + ifcClass);
        }
    }

    /**
     * Test if interface generated from augment extends Augmentation interface
     * with correct generic type.
     *
     * @param classToTest
     *            interface generated from augment
     * @param genericType
     *            fully qualified name of expected parameter type
     */
    static void testAugmentation(Class<?> classToTest, String genericType) {
        final String ifcToImplement = "interface org.opendaylight.yangtools.yang.binding.Augmentation";
        testImplementParameterizedIfc(classToTest, ifcToImplement, genericType);
    }

    static void testImplementParameterizedIfc(Class<?> classToTest, String ifcToImplement, String genericType) {
        ParameterizedType augmentation = null;
        for (java.lang.reflect.Type ifc : classToTest.getGenericInterfaces()) {
            if (ifc instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) ifc;
                if (ifcToImplement.equals(pt.getRawType().toString())) {
                    augmentation = pt;
                }
            }
        }
        assertNotNull(augmentation);

        java.lang.reflect.Type[] typeArguments = augmentation.getActualTypeArguments();
        assertEquals(1, typeArguments.length);
        assertEquals("interface " + genericType, typeArguments[0].toString());
    }

    /**
     * Test if source code is compilable.
     *
     * @param sourcesOutputDir
     *            directory containing source files
     * @param compiledOutputDir
     *            compiler output directory
     */
    static void testCompilation(File sourcesOutputDir, File compiledOutputDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        List<File> filesList = getJavaFiles(sourcesOutputDir);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(filesList);
        Iterable<String> options = Arrays.asList("-d", compiledOutputDir.getAbsolutePath());
        boolean compiled = compiler.getTask(null, null, null, options, null, compilationUnits).call();
        assertTrue(compiled);
    }

    static void testFilesCount(File dir, int count) {
        File[] dirContent = dir.listFiles();
        if (dirContent == null) {
            throw new AssertionError("File " + dir + " doesn't exists or it's not a directory");
        } else {
            assertEquals("Unexpected count of generated files", count, dirContent.length);
        }
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

    static List<File> getSourceFiles(String path) throws FileNotFoundException {
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

    static void deleteTestDir(File file) {
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
