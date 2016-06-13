/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.BASE_PKG;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.COMPILER_OUTPUT_PATH;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.FS;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.GENERATOR_OUTPUT_PATH;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.cleanUp;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.getSourceFiles;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.testCompilation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Test correct functionality of copy constructor of generated builder classes.
 */
public class Bug532Test extends BaseCompilationTest {

    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "bug532");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "bug532");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        generateTestSources("/compilation/list-gen-test", sourcesOutputDir);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> linksKeyClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.LinksKey", true, loader);
        Class<?> linksClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.Links", true, loader);
        Class<?> linksBuilderClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.LinksBuilder", true,
                loader);
        Class<?> levelClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.links.Level", true, loader);
        Class<?> nodeClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.links.Node", true, loader);
        Class<?> nodeListClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.links.NodeList", true,
                loader);

        // init default values
        Byte expectedId = Byte.valueOf("5");
        String expectedName = "test-link";
        Integer expectedSize = Integer.valueOf(10);


        Object expectedLevel = Mockito.mock(levelClass);
        Integer expectedLinksId = Integer.valueOf(11);
        Object expectedNode = Mockito.mock(nodeClass);
        List<?> expectedNodeList = Lists.newArrayList(Mockito.mock(nodeListClass), Mockito.mock(nodeListClass));
        Constructor<?> keyConstructor = linksKeyClass.getDeclaredConstructor(Byte.class, String.class, Integer.class);
        Object expectedKey = keyConstructor.newInstance(expectedId, expectedName, expectedSize);

        // create Links object
        Object linksBuilder = linksBuilderClass.newInstance();
        linksBuilderClass.getDeclaredMethod("setKey", linksKeyClass).invoke(linksBuilder, expectedKey);
        linksBuilderClass.getDeclaredMethod("setLevel", levelClass).invoke(linksBuilder, expectedLevel);
        linksBuilderClass.getDeclaredMethod("setLinksId", Integer.class).invoke(linksBuilder, expectedLinksId);
        linksBuilderClass.getDeclaredMethod("setNode", nodeClass).invoke(linksBuilder, expectedNode);
        linksBuilderClass.getDeclaredMethod("setNodeList", List.class).invoke(linksBuilder, expectedNodeList);
        Object links = linksBuilderClass.getDeclaredMethod("build").invoke(linksBuilder);

        // create LinksBuilder object with constructor with Links object
        // argument
        Constructor<?> linksBuilderConstructor = linksBuilderClass.getDeclaredConstructor(linksClass);
        assertNotNull(linksBuilderConstructor);
        Object linksBuilderTested = linksBuilderConstructor.newInstance(links);

        // get values from LinksBuilder
        Object actualKey = linksBuilderClass.getDeclaredMethod("getKey").invoke(linksBuilderTested);
        Object actualId = linksBuilderClass.getDeclaredMethod("getId").invoke(linksBuilderTested);
        Object actualName = linksBuilderClass.getDeclaredMethod("getName").invoke(linksBuilderTested);
        Object actualSize = linksBuilderClass.getDeclaredMethod("getSize").invoke(linksBuilderTested);
        Object actualLevel = linksBuilderClass.getDeclaredMethod("getLevel").invoke(linksBuilderTested);
        Object actualLinksId = linksBuilderClass.getDeclaredMethod("getLinksId").invoke(linksBuilderTested);
        Object actualNode = linksBuilderClass.getDeclaredMethod("getNode").invoke(linksBuilderTested);
        Object actualNodeList = linksBuilderClass.getDeclaredMethod("getNodeList").invoke(linksBuilderTested);

        // test
        assertEquals(expectedKey, actualKey);
        assertEquals(expectedId, actualId);
        assertEquals(expectedName, actualName);
        assertEquals(expectedSize, actualSize);
        assertEquals(expectedLevel, actualLevel);
        assertEquals(expectedLinksId, actualLinksId);
        assertEquals(expectedNode, actualNode);
        assertEquals(expectedNodeList, actualNodeList);

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private void generateTestSources(final String resourceDirPath, final File sourcesOutputDir) throws Exception {
        final List<File> sourceFiles = getSourceFiles(resourceDirPath);
        final SchemaContext context = TestUtils.parseYangSources(sourceFiles);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(ImmutableSet.copyOf(types));
        generator.generateToFile(sourcesOutputDir);
    }

}
