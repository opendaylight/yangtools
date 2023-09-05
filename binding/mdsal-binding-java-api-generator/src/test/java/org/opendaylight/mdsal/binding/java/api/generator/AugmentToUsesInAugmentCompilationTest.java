/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Test;

public class AugmentToUsesInAugmentCompilationTest extends BaseCompilationTest {

    @Test
    public void testAugmentToUsesInAugment() throws IOException, URISyntaxException {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("augment-uses-to-augment");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("augment-uses-to-augment");
        generateTestSources("/compilation/augment-uses-to-augment", sourcesOutputDir);

        // Test if all sources are generated from 'module foo'
        final File fooParent = new File(sourcesOutputDir, CompilationTestUtils.NS_FOO);
        assertTrue(new File(fooParent, "FooData.java").exists());
        assertTrue(new File(fooParent, "IgpLinkAttributes.java").exists());
        assertTrue(new File(fooParent, "Link1.java").exists());
        assertTrue(new File(fooParent, "Link1Builder.java").exists());
        CompilationTestUtils.assertFilesCount(fooParent, 5);
        final File fooSvcParent = new File(sourcesOutputDir, CompilationTestUtils.NS_SVC_FOO);
        CompilationTestUtils.assertFilesCount(fooSvcParent, 1);

        // Test if all sources are generated from 'module bar'
        final File barParent = new File(sourcesOutputDir, CompilationTestUtils.NS_BAR);
        assertTrue(new File(barParent, "BarData.java").exists());
        assertTrue(new File(barParent, "NetworkTopology.java").exists());
        assertTrue(new File(barParent, "NetworkTopologyBuilder.java").exists());
        assertTrue(new File(barParent, "Link.java").exists());
        assertTrue(new File(barParent, "LinkAttributes.java").exists());
        CompilationTestUtils.assertFilesCount(barParent, 7);
        final File barSvcParent = new File(sourcesOutputDir, CompilationTestUtils.NS_SVC_BAR);
        CompilationTestUtils.assertFilesCount(barSvcParent, 1);

        final File networkParent = new File(barParent, "network");
        CompilationTestUtils.assertFilesCount(networkParent, 1);
        final File topologyParent = new File(networkParent, "topology");
        assertTrue(new File(topologyParent, "Topology.java").exists());
        assertTrue(new File(topologyParent, "TopologyBuilder.java").exists());
        assertTrue(new File(topologyParent, "TopologyKey.java").exists());
        CompilationTestUtils.assertFilesCount(topologyParent, 3);

        final File linkParent = new File(barParent, "link");
        assertTrue(new File(linkParent, "Link.java").exists());
        assertTrue(new File(linkParent, "LinkBuilder.java").exists());
        assertTrue(new File(linkParent, "LinkKey.java").exists());
        CompilationTestUtils.assertFilesCount(linkParent, 3);

        // Test if all sources are generated from 'module baz'
        final File bazParent = new File(sourcesOutputDir, CompilationTestUtils.NS_BAZ);
        assertTrue(new File(bazParent, "BazData.java").exists());
        assertTrue(new File(bazParent, "IgpLinkAttributes1.java").exists());
        assertTrue(new File(bazParent, "IgpLinkAttributes1Builder.java").exists());
        assertTrue(new File(bazParent, "LinkAttributes.java").exists());
        CompilationTestUtils.assertFilesCount(bazParent, 5);
        final File bazSvcParent = new File(sourcesOutputDir, CompilationTestUtils.NS_SVC_BAZ);
        CompilationTestUtils.assertFilesCount(bazSvcParent, 1);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });

        try {
            Class<?> link1Class = Class.forName(CompilationTestUtils.BASE_PKG + ".urn.opendaylight.foo.rev131008.Link1",
                true, loader);
            String augmentableNode = CompilationTestUtils.BASE_PKG + ".urn.opendaylight.bar.rev131008.link.Link";
            CompilationTestUtils.testAugmentation(link1Class, augmentableNode);

            Class<?> igpLinkAttributesClass = Class.forName(CompilationTestUtils.BASE_PKG
                    + ".urn.opendaylight.foo.rev131008.IgpLinkAttributes", true, loader);
            CompilationTestUtils.assertImplementsIfc(link1Class, igpLinkAttributesClass);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Class for augment wasn't generated", e);
        }

        try {
            Class<?> igpLinkAttributes1Class = Class.forName(CompilationTestUtils.BASE_PKG
                + ".urn.opendaylight.baz.rev131008.IgpLinkAttributes1", true, loader);
            CompilationTestUtils.testAugmentation(igpLinkAttributes1Class, CompilationTestUtils.BASE_PKG
                + ".urn.opendaylight.foo.rev131008.igp.link.attributes.IgpLinkAttributes");

            Class<?> linkAttributesClass = Class.forName(CompilationTestUtils.BASE_PKG
                + ".urn.opendaylight.baz.rev131008.LinkAttributes", true, loader);
            CompilationTestUtils.assertImplementsIfc(igpLinkAttributes1Class, linkAttributesClass);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Class for augment wasn't generated", e);
        }

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

}
