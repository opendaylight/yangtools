/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Test correct generation of YangModuleInfo class.
 *
 */
// TODO: most of private static methods are copied from
// binding-java-api-generator project - reorganize compilation tests
public class YangModuleInfoCompilationTest {
    public static final String FS = File.separator;
    private static final String BASE_PKG = "org.opendaylight.yang.svc.v1";

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
        final String BASE_PATH = "org" + FS + "opendaylight" + FS + "yang" + FS + "svc" + FS + "v1";
        final String NS_TEST = BASE_PATH + FS + "yang" + FS + "test" + FS + "main" + FS + "rev140630";
        File parent = new File(sourcesOutputDir, NS_TEST);
        File keyArgs = new File(parent, "YangModuleInfoImpl.java");
        assertTrue(keyArgs.exists());

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        // Create URLClassLoader
        URL[] urls = new URL[2];
        urls[0] = compiledOutputDir.toURI().toURL();
        urls[1] = new File(System.getProperty("user.dir")).toURI().toURL();
        ClassLoader loader = new URLClassLoader(urls);

        // Load class
        Class<?> yangModuleInfoClass =
            Class.forName(BASE_PKG + ".yang.test.main.rev140630.YangModuleInfoImpl", true, loader);

        // Test generated $YangModuleInfoImpl class
        assertFalse(yangModuleInfoClass.isInterface());
        Method getInstance = assertContainsMethod(yangModuleInfoClass, YangModuleInfo.class, "getInstance");
        Object yangModuleInfo = getInstance.invoke(null);

        // Test getImportedModules method
        Method getImportedModules = assertContainsMethod(yangModuleInfoClass, ImmutableSet.class, "getImportedModules");
        Object importedModules = getImportedModules.invoke(yangModuleInfo);
        assertTrue(importedModules instanceof Set);

        YangModuleInfo infoImport = null;
        YangModuleInfo infoSub1 = null;
        YangModuleInfo infoSub2 = null;
        YangModuleInfo infoSub3 = null;
        for (Object importedModule : (Set<?>) importedModules) {
            assertTrue(importedModule instanceof YangModuleInfo);
            YangModuleInfo ymi = (YangModuleInfo) importedModule;
            String name = ymi.getName().getLocalName();

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
                    break;
                default:
                    // no-op
            }
        }
        assertNotNull(infoImport);
        assertThat(infoImport.getYangTextCharSource().readFirstLine(), startsWith("module import-module"));
        assertNotNull(infoSub1);
        assertThat(infoSub1.getYangTextCharSource().readFirstLine(), startsWith("submodule submodule1"));
        assertNotNull(infoSub2);
        assertThat(infoSub2.getYangTextCharSource().readFirstLine(), startsWith("submodule submodule2"));
        assertNotNull(infoSub3);
        assertThat(infoSub3.getYangTextCharSource().readFirstLine(), startsWith("submodule submodule3"));

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private static void generateTestSources(final String resourceDirPath, final File sourcesOutputDir)
            throws Exception {
        final List<File> sourceFiles = getSourceFiles(resourceDirPath);
        final EffectiveModelContext context = YangParserTestUtils.parseYangFiles(sourceFiles);
        final Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> codegen = new JavaFileGenerator(Map.of())
            .generateFiles(context, Set.copyOf(context.getModules()),
                (module, representation) -> Optional.of(resourceDirPath + File.separator + module.getName()
                    + YangConstants.RFC6020_YANG_FILE_EXTENSION));

        assertEquals(15, codegen.size());
        assertEquals(14, codegen.row(GeneratedFileType.SOURCE).size());
        assertEquals(1, codegen.row(GeneratedFileType.RESOURCE).size());

        for (Entry<GeneratedFilePath, GeneratedFile> entry : codegen.row(GeneratedFileType.SOURCE).entrySet()) {
            final Path path = new File(sourcesOutputDir,
                entry.getKey().getPath().replace(GeneratedFilePath.SEPARATOR, File.separatorChar)).toPath();

            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                entry.getValue().writeBody(out);
            }
        }
    }

    @Test
    public void generateTestSourcesWithAdditionalConfig() throws Exception {
        final List<File> sourceFiles = getSourceFiles("/yang-module-info");
        final EffectiveModelContext context = YangParserTestUtils.parseYangFiles(sourceFiles);
        JavaFileGenerator codegen = new JavaFileGenerator(Map.of("test", "test"));
        Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> files = codegen.generateFiles(context,
            Set.copyOf(context.getModules()), (module, representation) -> Optional.of(module.getName()));
        assertEquals(15, files.size());
        assertEquals(14, files.row(GeneratedFileType.SOURCE).size());
        assertEquals(1, files.row(GeneratedFileType.RESOURCE).size());
    }

    private static void testCompilation(final File sourcesOutputDir, final File compiledOutputDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        List<File> filesList = getJavaFiles(sourcesOutputDir);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(filesList);
        Iterable<String> options = Arrays.asList("-d", compiledOutputDir.getAbsolutePath());
        List<Diagnostic<?>> diags = new ArrayList<>();
        boolean compiled = compiler.getTask(null, null, diags::add, options, null, compilationUnits).call();
        if (!compiled) {
            fail("Compilation failed with " + diags);
        }
    }

    private static List<File> getJavaFiles(final File directory) {
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

    private static List<File> getSourceFiles(final String path) throws Exception {
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

    private static void deleteTestDir(final File file) {
        if (file.isDirectory()) {
            File[] filesToDelete = file.listFiles();
            if (filesToDelete != null) {
                for (File ftd : filesToDelete) {
                    deleteTestDir(ftd);
                }
            }
        }
        if (!file.delete()) {
            throw new RuntimeException("Failed to clean up after test");
        }
    }

    private static Method assertContainsMethod(final Class<?> clazz, final Class<?> returnType, final String name,
            final Class<?>... args) {
        try {
            Method method = clazz.getDeclaredMethod(name, args);
            assertEquals(returnType, method.getReturnType());
            return method;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method " + name + " with args " + Arrays.toString(args)
                    + " does not exists in class " + clazz.getSimpleName(), e);
        }
    }

    private static void cleanUp(final File... resourceDirs) {
        for (File resourceDir : resourceDirs) {
            if (resourceDir.exists()) {
                deleteTestDir(resourceDir);
            }
        }
    }

}
