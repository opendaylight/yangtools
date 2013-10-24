/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class AugmentToUsesInAugmentCompilationTest extends BaseCompilationTest {

    @Test
    public void testAugmentToUsesInAugment() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "augment-uses-to-augment");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "augment-uses-to-augment");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        final List<File> sourceFiles = getSourceFiles("/compilation/augment-uses-to-augment");
        final Set<Module> modulesToBuild = parser.parseYangModels(sourceFiles);
        final SchemaContext context = parser.resolveSchemaContext(modulesToBuild);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(new HashSet<>(types));
        generator.generateToFile(sourcesOutputDir);

        // Test if all sources are generated
        File fooParent = new File(sourcesOutputDir, NS_FOO);
        assertEquals(4, fooParent.listFiles().length);
        assertTrue(new File(fooParent, "IgpLinkAttributes.java").exists());
        assertTrue(new File(fooParent, "Link1.java").exists());
        assertTrue(new File(fooParent, "Link1Builder.java").exists());

        File bazParent = new File(sourcesOutputDir, NS_BAZ);
        assertEquals(4, bazParent.listFiles().length);
        assertTrue(new File(bazParent, "IgpLinkAttributes1.java").exists());
        assertTrue(new File(bazParent, "IgpLinkAttributes1Builder.java").exists());
        assertTrue(new File(bazParent, "LinkAttributes.java").exists());

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });

        try {
            Class<?> igpLinkAttributes1Class = Class.forName(BASE_PKG
                    + ".urn.opendaylight.baz.rev131008.IgpLinkAttributes1", true, loader);
            String augmentableNode = BASE_PKG + ".urn.opendaylight.foo.rev131008.igp.link.attributes.IgpLinkAttributes";
            testAugmentation(igpLinkAttributes1Class, augmentableNode);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("IdentityClass must have no-arg constructor");
        }

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

}
