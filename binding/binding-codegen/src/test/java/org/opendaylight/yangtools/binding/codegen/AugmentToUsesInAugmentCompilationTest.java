/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.assertRegularFile;

import java.net.URL;
import java.net.URLClassLoader;
import org.junit.jupiter.api.Test;

class AugmentToUsesInAugmentCompilationTest extends BaseCompilationTest {
    @Test
    void testAugmentToUsesInAugment() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("augment-uses-to-augment");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("augment-uses-to-augment");
        generateTestSources("/compilation/augment-uses-to-augment", sourcesOutputDir);

        // Test if all sources are generated from 'module foo'
        final var fooParent = sourcesOutputDir.resolve(CompilationTestUtils.NS_FOO);
        assertRegularFile(fooParent, "FooData.java");
        assertRegularFile(fooParent, "IgpLinkAttributes.java");
        assertRegularFile(fooParent, "Link1.java");
        assertRegularFile(fooParent, "Link1Builder.java");
        CompilationTestUtils.assertFilesCount(fooParent, 5);
        CompilationTestUtils.assertFilesCount(sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_FOO), 1);

        // Test if all sources are generated from 'module bar'
        final var barParent = sourcesOutputDir.resolve(CompilationTestUtils.NS_BAR);
        assertRegularFile(barParent, "BarData.java");
        assertRegularFile(barParent, "NetworkTopology.java");
        assertRegularFile(barParent, "NetworkTopologyBuilder.java");
        assertRegularFile(barParent, "Link.java");
        assertRegularFile(barParent, "LinkAttributes.java");
        CompilationTestUtils.assertFilesCount(barParent, 7);
        CompilationTestUtils.assertFilesCount(sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_BAR), 1);

        final var networkParent = barParent.resolve("network");
        CompilationTestUtils.assertFilesCount(networkParent, 1);
        final var topologyParent = networkParent.resolve("topology");
        assertRegularFile(topologyParent, "Topology.java");
        assertRegularFile(topologyParent, "TopologyBuilder.java");
        assertRegularFile(topologyParent, "TopologyKey.java");
        CompilationTestUtils.assertFilesCount(topologyParent, 3);

        final var linkParent = barParent.resolve("link");
        assertRegularFile(linkParent, "Link.java");
        assertRegularFile(linkParent, "LinkBuilder.java");
        assertRegularFile(linkParent, "LinkKey.java");
        CompilationTestUtils.assertFilesCount(linkParent, 3);

        // Test if all sources are generated from 'module baz'
        final var bazParent = sourcesOutputDir.resolve(CompilationTestUtils.NS_BAZ);
        assertRegularFile(bazParent, "BazData.java");
        assertRegularFile(bazParent, "IgpLinkAttributes1.java");
        assertRegularFile(bazParent, "IgpLinkAttributes1Builder.java");
        assertRegularFile(bazParent, "LinkAttributes.java");
        CompilationTestUtils.assertFilesCount(bazParent, 5);
        CompilationTestUtils.assertFilesCount(sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_BAZ), 1);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() });

        Class<?> link1Class = Class.forName(CompilationTestUtils.BASE_PKG + ".urn.opendaylight.foo.rev131008.Link1",
            true, loader);
        String augmentableNode = CompilationTestUtils.BASE_PKG + ".urn.opendaylight.bar.rev131008.link.Link";
        CompilationTestUtils.testAugmentation(link1Class, augmentableNode);

        Class<?> igpLinkAttributesClass = Class.forName(CompilationTestUtils.BASE_PKG
            + ".urn.opendaylight.foo.rev131008.IgpLinkAttributes", true, loader);
        CompilationTestUtils.assertImplementsIfc(link1Class, igpLinkAttributesClass);

        Class<?> igpLinkAttributes1Class = Class.forName(CompilationTestUtils.BASE_PKG
            + ".urn.opendaylight.baz.rev131008.IgpLinkAttributes1", true, loader);
        CompilationTestUtils.testAugmentation(igpLinkAttributes1Class, CompilationTestUtils.BASE_PKG
            + ".urn.opendaylight.foo.rev131008.igp.link.attributes.IgpLinkAttributes");

        Class<?> linkAttributesClass = Class.forName(CompilationTestUtils.BASE_PKG
            + ".urn.opendaylight.baz.rev131008.LinkAttributes", true, loader);
        CompilationTestUtils.assertImplementsIfc(igpLinkAttributes1Class, linkAttributesClass);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
