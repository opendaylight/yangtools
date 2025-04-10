/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.BASE_PKG;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.cleanUp;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.testCompilation;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test correct functionality of copy constructor of generated builder classes.
 */
class Bug532Test extends BaseCompilationTest {
    @Test
    void test() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("bug532");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("bug532");
        generateTestSources("/compilation/list-gen-test", sourcesOutputDir);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() });
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
        Integer expectedSize = 10;


        Object expectedLevel = Mockito.mock(levelClass);
        Integer expectedLinksId = 11;
        Object expectedNode = Mockito.mock(nodeClass);
        List<?> expectedNodeList = List.of(Mockito.mock(nodeListClass), Mockito.mock(nodeListClass));
        Constructor<?> keyConstructor = linksKeyClass.getDeclaredConstructor(Byte.class, String.class, Integer.class);
        Object expectedKey = keyConstructor.newInstance(expectedId, expectedName, expectedSize);

        // create Links object
        Object linksBuilder = linksBuilderClass.getDeclaredConstructor().newInstance();
        linksBuilderClass.getDeclaredMethod("withKey", linksKeyClass).invoke(linksBuilder, expectedKey);
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
        Object actualKey = linksBuilderClass.getDeclaredMethod("key").invoke(linksBuilderTested);
        Object actualId = linksBuilderClass.getDeclaredMethod("getId").invoke(linksBuilderTested);
        Object actualName = linksBuilderClass.getDeclaredMethod("getName").invoke(linksBuilderTested);
        final Object actualSize = linksBuilderClass.getDeclaredMethod("getSize").invoke(linksBuilderTested);
        final Object actualLevel = linksBuilderClass.getDeclaredMethod("getLevel").invoke(linksBuilderTested);
        final Object actualLinksId = linksBuilderClass.getDeclaredMethod("getLinksId").invoke(linksBuilderTested);
        final Object actualNode = linksBuilderClass.getDeclaredMethod("getNode").invoke(linksBuilderTested);
        final Object actualNodeList = linksBuilderClass.getDeclaredMethod("getNodeList").invoke(linksBuilderTested);

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
}
