/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

/**
 * Test correct code generation.
 * 
 */
public class CompilationTest {
    private static final String FS = File.separator;
    private static final String TEST_PATH = "target" + FS + "test";
    private static final File TEST_DIR = new File(TEST_PATH);

    private static final String GENERATOR_OUTPUT_PATH = TEST_PATH + FS + "src";
    private static final File GENERATOR_OUTPUT_DIR = new File(GENERATOR_OUTPUT_PATH);
    private static final String COMPILER_OUTPUT_PATH = TEST_PATH + FS + "bin";
    private static final File COMPILER_OUTPUT_DIR = new File(COMPILER_OUTPUT_PATH);

    private YangParserImpl parser;
    private BindingGenerator bindingGenerator;

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
    public void cleanUp(File... resourceDirs) {
        for (File resourceDir : resourceDirs) {
            if (resourceDir.exists()) {
                deleteTestDir(resourceDir);
            }
        }
    }

    @Test
    public void testListGeneration() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "list-gen");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "list-gen");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        final List<File> sourceFiles = getSourceFiles("/compilation/list-gen");
        final Set<Module> modulesToBuild = parser.parseYangModels(sourceFiles);
        final SchemaContext context = parser.resolveSchemaContext(modulesToBuild);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(new HashSet<>(types));
        generator.generateToFile(sourcesOutputDir);

        File parent = new File(sourcesOutputDir, "org" + FS + "opendaylight" + FS + "yang" + FS + "gen" + FS + "v1"
                + FS + "urn" + FS + "opendaylight" + FS + "test" + FS + "rev131008");
        File linksKeyFile = new File(parent, "LinksKey.java");
        assertTrue(new File(parent, "KeyArgs.java").exists());
        assertTrue(new File(parent, "Links.java").exists());
        assertTrue(new File(parent, "LinksBuilder.java").exists());
        assertTrue(linksKeyFile.exists());
        assertTrue(new File(parent, "TestData.java").exists());
        assertTrue(new File(parent, "links" + FS + "Level.java").exists());
        assertTrue(new File(parent, "links" + FS + "LinkGroup.java").exists());
        assertTrue(new File(parent, "links" + FS + "Node.java").exists());
        assertTrue(new File(parent, "links" + FS + "NodeBuilder.java").exists());
        assertTrue(new File(parent, "links" + FS + "NodeList.java").exists());
        assertTrue(new File(parent, "links" + FS + "NodeListBuilder.java").exists());
        assertTrue(linksKeyFile.exists());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        List<File> filesList = getJavaFiles(sourcesOutputDir);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(filesList);
        Iterable<String> options = Arrays.asList("-d", compiledOutputDir.getAbsolutePath());
        boolean compiled = compiler.getTask(null, null, null, options, null, compilationUnits).call();
        assertTrue(compiled);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> linksKeyClass = Class.forName("org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.LinksKey",
                true, loader);

        // test list key constructor arguments ordering
        try {
            linksKeyClass.getConstructor(Byte.class, String.class, Integer.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Parameters of list key constructor are not properly ordered");
        }

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testAugmentUnderUsesGeneration() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "augment-under-uses");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "augment-under-uses");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        final List<File> sourceFiles = getSourceFiles("/compilation/augment-under-uses");
        final Set<Module> modulesToBuild = parser.parseYangModels(sourceFiles);
        final SchemaContext context = parser.resolveSchemaContext(modulesToBuild);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(new HashSet<>(types));
        generator.generateToFile(sourcesOutputDir);

        File parent = new File(sourcesOutputDir + FS + "org" + FS + "opendaylight" + FS + "yang" + FS + "gen" + FS
                + "v1" + FS + "urn" + FS + "opendaylight" + FS + "test" + FS + "rev131008");
        assertTrue(new File(parent, "Object.java").exists());
        assertTrue(new File(parent, "OpenObject.java").exists());
        assertTrue(new File(parent, "object" + FS + "Nodes.java").exists());
        assertTrue(new File(parent, "object" + FS + "NodesBuilder.java").exists());
        assertTrue(new File(parent, "open" + FS + "object" + FS + "Nodes1.java").exists());
        assertTrue(new File(parent, "open" + FS + "object" + FS + "Nodes1Builder.java").exists());
        assertTrue(new File(parent, "open" + FS + "object" + FS + "nodes" + FS + "Links.java").exists());
        assertTrue(new File(parent, "open" + FS + "object" + FS + "nodes" + FS + "LinksBuilder.java").exists());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        List<File> filesList = getJavaFiles(sourcesOutputDir);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(filesList);
        Iterable<String> options = Arrays.asList("-d", compiledOutputDir.getAbsolutePath());
        boolean compiled = compiler.getTask(null, null, null, options, null, compilationUnits).call();
        assertTrue(compiled);

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void compilationTest() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "yang");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "yang");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        final List<File> sourceFiles = getSourceFiles("/yang");
        final Set<Module> modulesToBuild = parser.parseYangModels(sourceFiles);
        final SchemaContext context = parser.resolveSchemaContext(modulesToBuild);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(new HashSet<>(types));
        generator.generateToFile(sourcesOutputDir);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        List<File> filesList = getJavaFiles(sourcesOutputDir);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(filesList);
        Iterable<String> options = Arrays.asList("-d", compiledOutputDir.getAbsolutePath());
        boolean compiled = compiler.getTask(null, null, null, options, null, compilationUnits).call();
        assertTrue(compiled);
    }

    private List<File> getSourceFiles(String path) {
        final String resPath = getClass().getResource(path).getPath();
        final File sourcesDir = new File(resPath);
        final List<File> sourceFiles = new ArrayList<>();
        final File[] fileArray = sourcesDir.listFiles();
        sourceFiles.addAll(Arrays.asList(fileArray));
        return sourceFiles;
    }

    private static void deleteTestDir(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteTestDir(f);
            }
        }
        if (!file.delete()) {
            throw new RuntimeException("Failed to clean up after test");
        }
    }

    /**
     * Search recursively given directory for *.java files.
     * 
     * @param directory
     *            directory to search
     * @return List of java files found
     */
    private List<File> getJavaFiles(File directory) {
        List<File> result = new ArrayList<File>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                result.addAll(getJavaFiles(file));
            } else {
                String absPath = file.getAbsolutePath();
                if (absPath.endsWith(".java")) {
                    result.add(file);
                }
            }
        }
        return result;
    }
}
