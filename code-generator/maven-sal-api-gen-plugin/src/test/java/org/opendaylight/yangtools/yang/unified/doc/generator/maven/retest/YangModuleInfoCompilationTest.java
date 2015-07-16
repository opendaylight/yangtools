/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.unified.doc.generator.maven.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.maven.sal.api.gen.plugin.CodeGeneratorImpl;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

/**
 * Test correct generation of YangModuleInfo class.
 *
 */
// TODO: most of private static methods are copied from
// binding-java-api-generator project - reorganize compilation tests
public class YangModuleInfoCompilationTest {
    public static final String FS = File.separator;
    private static final String BASE_PKG = "org.opendaylight.yang.gen.v1";

    private static final String TEST_PATH = "target" + FS + "test";
    private static final File TEST_DIR = new File(TEST_PATH);

    private static final String GENERATOR_OUTPUT_PATH = TEST_PATH + FS + "src";
    private static final File GENERATOR_OUTPUT_DIR = new File(GENERATOR_OUTPUT_PATH);
    private static final String COMPILER_OUTPUT_PATH = TEST_PATH + FS + "bin";
    private static final File COMPILER_OUTPUT_DIR = new File(COMPILER_OUTPUT_PATH);

    @BeforeClass
    public static void createTestDirs() {
        if (TEST_DIR.exists()) {
            deleteTestDir(TEST_DIR);
        }
        assertTrue(GENERATOR_OUTPUT_DIR.mkdirs());
        assertTrue(COMPILER_OUTPUT_DIR.mkdirs());
    }

    @Test
    public void compilationTest() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "yang");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdirs());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "yang");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdirs());

        generateTestSources("/yang-module-info", sourcesOutputDir);

        // Test if $YangModuleInfoImpl.java file is generated
        final String BASE_PATH = "org" + FS + "opendaylight" + FS + "yang" + FS + "gen" + FS + "v1";
        final String NS_TEST = BASE_PATH + FS + "yang" + FS + "test" + FS + "main" + FS + "rev140630";
        File parent = new File(sourcesOutputDir, NS_TEST);
        File keyArgs = new File(parent, "$YangModuleInfoImpl.java");
        assertTrue(keyArgs.exists());

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        // Create URLClassLoader
        URL[] urls = new URL[2];
        urls[0] = compiledOutputDir.toURI().toURL();
        urls[1] = new File(System.getProperty("user.dir")).toURI().toURL();
        ClassLoader loader = new URLClassLoader(urls);

        // Load class
        Class<?> yangModuleInfoClass = Class.forName(BASE_PKG + ".yang.test.main.rev140630.$YangModuleInfoImpl", true,
                loader);

        // Test generated $YangModuleInfoImpl class
        assertFalse(yangModuleInfoClass.isInterface());
        Method getInstance = assertContainsMethod(yangModuleInfoClass, YangModuleInfo.class, "getInstance");
        Object yangModuleInfo = getInstance.invoke(null);

        // Test getImportedModules method
        Method getImportedModules = assertContainsMethod(yangModuleInfoClass, Set.class, "getImportedModules");
        Object importedModules = getImportedModules.invoke(yangModuleInfo);
        assertTrue(importedModules instanceof Set);

        YangModuleInfo infoImport = null;
        YangModuleInfo infoSub1 = null;
        YangModuleInfo infoSub2 = null;
        YangModuleInfo infoSub3 = null;
        for (Object importedModule : (Set<?>) importedModules) {
            assertTrue(importedModule instanceof YangModuleInfo);
            YangModuleInfo ymi = (YangModuleInfo) importedModule;
            String name = ymi.getName();

            switch (name) {
            case "import-module":
                infoImport = ymi;
                break;
            case "submodule1":
                infoSub1 = ymi;
                break;
            case "submodule2":
                infoSub2 = ymi;
                break;
            case "submodule3":
                infoSub3 = ymi;
            }
        }
        assertNotNull(infoImport);
        assertNotNull(infoSub1);
        assertNotNull(infoSub2);
        assertNotNull(infoSub3);

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private void generateTestSources(String resourceDirPath, File sourcesOutputDir) throws Exception {
        final List<File> sourceFiles = getSourceFiles(resourceDirPath);
        final SchemaContext context = RetestUtils.parseYangSources(sourceFiles);
        CodeGeneratorImpl codegen = new CodeGeneratorImpl();
        codegen.setBuildContext(new DefaultBuildContext());
        codegen.generateSources(context, sourcesOutputDir, context.getModules());
    }

    private static void testCompilation(File sourcesOutputDir, File compiledOutputDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        List<File> filesList = getJavaFiles(sourcesOutputDir);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(filesList);
        Iterable<String> options = Arrays.asList("-d", compiledOutputDir.getAbsolutePath());
        boolean compiled = compiler.getTask(null, null, null, options, null, compilationUnits).call();
        assertTrue(compiled);
    }

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

    private static List<File> getSourceFiles(String path) throws Exception {
        final URI resPath = YangModuleInfoCompilationTest.class.getResource(path).toURI();
        final File sourcesDir = new File(resPath);
        final URI currentDir = new File(System.getProperty("user.dir")).toURI();
        if (sourcesDir.exists()) {
            final List<File> sourceFiles = new ArrayList<>();
            final File[] fileArray = sourcesDir.listFiles();
            if (fileArray == null) {
                throw new IllegalArgumentException("Unable to locate files in " + sourcesDir);
            }
            for (File sourceFile : fileArray) {
                sourceFiles.add(new File(currentDir.relativize(sourceFile.toURI()).toString()));
            }
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

    private static Method assertContainsMethod(Class<?> clazz, Class<?> returnType, String name, Class<?>... args) {
        try {
            Method m = clazz.getDeclaredMethod(name, args);
            assertEquals(returnType, m.getReturnType());
            return m;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method " + name + " with args " + Arrays.toString(args)
                    + " does not exists in class " + clazz.getSimpleName());
        }
    }

    private static void cleanUp(File... resourceDirs) {
        for (File resourceDir : resourceDirs) {
            if (resourceDir.exists()) {
                deleteTestDir(resourceDir);
            }
        }
    }

}
