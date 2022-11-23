/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.assertRegularFile;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.annotations.RoutingContext;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test correct code generation.
 *
 */
class CompilationTest extends BaseCompilationTest {
    /*
     * Java 8 allows JaCoCo to hook onto interfaces, as well as
     * generating a default implementation. We only want to check
     * abstract methods.
     */
    private static Collection<Method> abstractMethods(final Class<?> clazz) {
        // Filter out
        return Collections2.filter(Arrays.asList(clazz.getDeclaredMethods()),
            input -> Modifier.isAbstract(input.getModifiers()));
    }

    @Test
    void testListGeneration() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("list-gen");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("list-gen");
        generateTestSources("/compilation/list-gen", sourcesOutputDir);

        // Test if all sources are generated
        var parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_TEST);
        assertRegularFile(parent, "KeyArgs.java");
        assertRegularFile(parent, "Links.java");
        assertRegularFile(parent, "LinksBuilder.java");
        assertRegularFile(parent, "LinksKey.java");
        assertRegularFile(parent, "TestData.java");
        CompilationTestUtils.assertFilesCount(parent, 6);
        final var svcParent = sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_TEST);
        CompilationTestUtils.assertFilesCount(svcParent, 1);

        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_TEST).resolve("links");
        assertRegularFile(parent, "Level.java");
        assertRegularFile(parent, "LinkGroup.java");
        assertRegularFile(parent, "Node.java");
        assertRegularFile(parent, "NodeBuilder.java");
        assertRegularFile(parent, "NodeList.java");
        assertRegularFile(parent, "NodeListBuilder.java");
        assertRegularFile(parent, "NodesType.java");
        CompilationTestUtils.assertFilesCount(parent, 8);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() });
        final Class<?> keyArgsClass = Class.forName(CompilationTestUtils.BASE_PKG
            + ".urn.opendaylight.test.rev131008.KeyArgs", true, loader);
        final Class<?> linksClass = Class.forName(CompilationTestUtils.BASE_PKG
            + ".urn.opendaylight.test.rev131008.Links", true, loader);
        final Class<?> linksKeyClass = Class.forName(CompilationTestUtils.BASE_PKG
            + ".urn.opendaylight.test.rev131008.LinksKey", true, loader);

        // Test generated 'grouping key-args'
        assertTrue(keyArgsClass.isInterface());
        CompilationTestUtils.assertContainsMethod(keyArgsClass, String.class, "getName");
        CompilationTestUtils.assertContainsMethod(keyArgsClass, Integer.class, "getSize");
        assertEquals(2, abstractMethods(keyArgsClass).size());

        // Test generated 'list links'
        assertTrue(linksClass.isInterface());
        CompilationTestUtils.assertImplementsIfc(linksClass, keyArgsClass);
        assertEquals(8, abstractMethods(linksClass).size());
        CompilationTestUtils.assertContainsMethod(linksClass,
            "org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.links.Text", "getText", loader);
        CompilationTestUtils.assertContainsMethod(linksClass,
            "org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.links.Text", "requireText", loader);

        // Test list key constructor arguments ordering
        CompilationTestUtils.assertContainsConstructor(linksKeyClass, Byte.class, String.class, Integer.class);
        // Test serialVersionUID generation
        final Field suid = CompilationTestUtils.assertContainsField(linksKeyClass, "serialVersionUID", Long.TYPE);
        suid.setAccessible(true);
        assertEquals(-4330476182227230308L, suid.getLong(null));

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    /**
     * Test that nonnull getter method is generated for non-presence containers only.
     *
     * @throws Exception when any exception occurs during the test
     */
    @Test
    void testContainerGettersGeneration() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("containers-gen");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("containers-gen");
        generateTestSources("/compilation/containers-gen", sourcesOutputDir);

        // Test if all sources were generated from 'module containers'
        var parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_TEST);
        assertRegularFile(parent, "RootContainer.java");
        assertRegularFile(parent, "rootcontainer/PresenceContainer.java");
        assertRegularFile(parent, "rootcontainer/NonPresenceContainer.java");
        CompilationTestUtils.assertFilesCount(parent, 4);
        var svcParent = sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_TEST);
        CompilationTestUtils.assertFilesCount(svcParent, 1);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() });
        final Class<?> rootClass = Class.forName(CompilationTestUtils.BASE_PKG
                + ".urn.opendaylight.test.rev131008.RootContainer", true, loader);

        // Test generated 'container root'
        assertTrue(rootClass.isInterface());
        assertEquals(3, abstractMethods(rootClass).size());

        // Test generated getter and not-generated nonnull method for presence container
        CompilationTestUtils.assertContainsMethod(rootClass,
                "org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.rootcontainer.PresenceContainer",
                "getPresenceContainer", loader);
        final var error = assertThrows(AssertionError.class, () ->
                CompilationTestUtils.assertContainsMethod(rootClass,
                        "org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.rootcontainer.PresenceContainer",
                        "nonnullPresenceContainer", loader));
        assertInstanceOf(NoSuchMethodException.class, error.getCause());

        // Test generated getter and nonnull methods for non-presence container
        CompilationTestUtils.assertContainsMethod(rootClass,
                "org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.rootcontainer.NonPresenceContainer",
                "getNonPresenceContainer", loader);
        CompilationTestUtils.assertContainsMethod(rootClass,
                "org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.rootcontainer.NonPresenceContainer",
                "nonnullNonPresenceContainer", loader);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testAugmentUnderUsesGeneration() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("augment-under-uses");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("augment-under-uses");
        generateTestSources("/compilation/augment-under-uses", sourcesOutputDir);

        // Test if all sources were generated from 'module foo'
        var parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_FOO);
        assertRegularFile(parent, "Object.java");
        assertRegularFile(parent, "ClosedObject.java");
        assertRegularFile(parent, "OpenObject.java");
        assertRegularFile(parent, "ExplicitRouteObject.java");
        assertRegularFile(parent, "PathKeySubobject.java");
        assertRegularFile(parent, "FooData.java");
        CompilationTestUtils.assertFilesCount(parent, 10);
        var svcParent = sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_FOO);
        CompilationTestUtils.assertFilesCount(svcParent, 1);

        parent = parent.resolve("object");
        assertRegularFile(parent, "Nodes.java");
        assertRegularFile(parent, "NodesBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 2);

        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_FOO).resolve("closed");
        CompilationTestUtils.assertFilesCount(parent, 1);

        parent = parent.resolve("object");
        assertRegularFile(parent, "Link1.java");
        assertRegularFile(parent, "Link1Builder.java");
        CompilationTestUtils.assertFilesCount(parent, 2);

        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_FOO).resolve("open");
        CompilationTestUtils.assertFilesCount(parent, 1);

        parent = parent.resolve("object");
        assertRegularFile(parent, "Nodes1.java");
        assertRegularFile(parent, "Nodes1Builder.java");
        CompilationTestUtils.assertFilesCount(parent, 3);

        parent = parent.resolve("nodes");
        assertRegularFile(parent, "Links.java");
        assertRegularFile(parent, "LinksBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 2);

        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_FOO).resolve("explicit");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("route");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("object");
        assertRegularFile(parent, "Subobjects.java");
        assertRegularFile(parent, "SubobjectsBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 3);

        parent = parent.resolve("subobjects");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("subobject");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("type");
        assertRegularFile(parent, "PathKey.java");
        assertRegularFile(parent, "PathKeyBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 3);

        parent = parent.resolve("path");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("key");
        assertRegularFile(parent, "PathKey.java");
        assertRegularFile(parent, "PathKeyBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 2);

        // Test if all sources were generated from 'module bar'
        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_BAR);
        assertRegularFile(parent, "BarData.java");
        assertRegularFile(parent, "BasicExplicitRouteSubobjects.java");
        assertRegularFile(parent, "ExplicitRouteSubobjects.java");
        assertRegularFile(parent, "RouteSubobjects.java");
        CompilationTestUtils.assertFilesCount(parent, 6);
        svcParent = sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_BAR);
        CompilationTestUtils.assertFilesCount(svcParent, 1);

        parent = parent.resolve("route");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_BAR).resolve("basic");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("explicit");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("route");
        CompilationTestUtils.assertFilesCount(parent, 1);

        parent = parent.resolve("subobjects");
        CompilationTestUtils.assertFilesCount(parent, 2);
        assertRegularFile(parent, "SubobjectType.java");

        parent = parent.resolve("subobject");
        CompilationTestUtils.assertFilesCount(parent, 1);

        parent = parent.resolve("type");
        assertRegularFile(parent, "IpPrefix.java");
        assertRegularFile(parent, "IpPrefixBuilder.java");
        assertRegularFile(parent, "Label.java");
        assertRegularFile(parent, "LabelBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 4);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testAugmentOfAugmentGeneration() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("aug-of-aug");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("aug-of-aug");
        generateTestSources("/compilation/augment-of-augment", sourcesOutputDir);

        // Test if all sources were generated from 'module foo'
        var parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_FOO);
        assertRegularFile(parent, "FooData.java");
        assertRegularFile(parent, "PathAttributes.java");
        assertRegularFile(parent, "Update.java");
        assertRegularFile(parent, "UpdateBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 6);
        var svcParent = sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_FOO);
        CompilationTestUtils.assertFilesCount(svcParent, 1);

        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_FOO).resolve("path");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("attributes");
        CompilationTestUtils.assertFilesCount(parent, 2);
        assertRegularFile(parent, "Origin.java");
        assertRegularFile(parent, "OriginBuilder.java");

        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_FOO).resolve("update");
        CompilationTestUtils.assertFilesCount(parent, 2);
        assertRegularFile(parent, "PathAttributes.java");
        assertRegularFile(parent, "PathAttributesBuilder.java");

        // Test if all sources were generated from 'module bar'
        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_BAR);
        assertRegularFile(parent, "BarData.java");
        assertRegularFile(parent, "Destination.java");
        assertRegularFile(parent, "PathAttributes1.java");
        assertRegularFile(parent, "PathAttributes1Builder.java");
        CompilationTestUtils.assertFilesCount(parent, 6);
        svcParent = sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_BAR);
        CompilationTestUtils.assertFilesCount(svcParent, 1);

        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_BAR).resolve("destination");
        CompilationTestUtils.assertFilesCount(parent, 2);
        assertRegularFile(parent, "DestinationType.java");

        parent = parent.resolve("destination");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("type");
        CompilationTestUtils.assertFilesCount(parent, 2);
        assertRegularFile(parent, "DestinationIp.java");
        assertRegularFile(parent, "DestinationIpBuilder.java");

        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_BAR).resolve("update");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("path");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("attributes");
        assertRegularFile(parent, "MpUnreachNlri.java");
        assertRegularFile(parent, "MpUnreachNlriBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 3);

        parent = parent.resolve("mp");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("unreach");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("nlri");
        assertRegularFile(parent, "WithdrawnRoutes.java");
        assertRegularFile(parent, "WithdrawnRoutesBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 2);

        // Test if all sources were generated from 'module baz'
        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_BAZ);
        assertRegularFile(parent, "BazData.java");
        assertRegularFile(parent, "LinkstateDestination.java");
        CompilationTestUtils.assertFilesCount(parent, 3);
        svcParent = sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_BAZ);
        CompilationTestUtils.assertFilesCount(svcParent, 1);

        parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_BAZ).resolve("update");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("path");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("attributes");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("mp");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("unreach");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("nlri");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("withdrawn");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("routes");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("destination");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("type");
        assertRegularFile(parent, "DestinationLinkstate.java");
        assertRegularFile(parent, "DestinationLinkstateBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 3);
        parent = parent.resolve("destination");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = parent.resolve("linkstate");
        assertRegularFile(parent, "Links.java");
        assertRegularFile(parent, "LinksBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 3);
        parent = parent.resolve("links");
        assertRegularFile(parent, "Source.java");
        assertRegularFile(parent, "SourceBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 3);
        parent = parent.resolve("source");
        assertRegularFile(parent, "Address.java");
        assertRegularFile(parent, "AddressBuilder.java");
        CompilationTestUtils.assertFilesCount(parent, 2);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testLeafReturnTypes() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("leaf-return-types");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("leaf-return-types");
        generateTestSources("/compilation/leaf-return-types", sourcesOutputDir);

        final var parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_TEST);
        assertRegularFile(parent, "TestData.java");
        assertRegularFile(parent, "Nodes.java");
        assertRegularFile(parent, "NodesBuilder.java");
        assertRegularFile(parent, "Alg.java");
        CompilationTestUtils.assertFilesCount(parent, 4);
        CompilationTestUtils.assertFilesCount(sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_TEST), 1);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final String pkg = CompilationTestUtils.BASE_PKG + ".urn.opendaylight.test.rev131008";
        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() });
        final Class<?> nodesClass = Class.forName(pkg + ".Nodes", true, loader);
        final Class<?> builderClass = Class.forName(pkg + ".NodesBuilder", true, loader);

        // Test methods return type
        final byte[] b = new byte[] {};
        CompilationTestUtils.assertContainsMethod(nodesClass, b.getClass(), "getIdBinary");
        CompilationTestUtils.assertContainsMethod(nodesClass, pkg + ".Nodes$IdBits", "getIdBits", loader);
        CompilationTestUtils.assertContainsMethod(nodesClass, Boolean.class, "getIdBoolean");
        CompilationTestUtils.assertContainsMethod(nodesClass, Decimal64.class, "getIdDecimal64");
        CompilationTestUtils.assertContainsMethod(nodesClass, Empty.class, "getIdEmpty");
        CompilationTestUtils.assertContainsMethod(nodesClass, pkg + ".Nodes$IdEnumeration", "getIdEnumeration", loader);
        testReturnTypeIdentityref(nodesClass, "getIdIdentityref", pkg + ".Alg");
        testReturnTypeInstanceIdentitifer(loader, nodesClass, "getIdInstanceIdentifier");
        CompilationTestUtils.assertContainsMethod(nodesClass, Byte.class, "getId8");
        CompilationTestUtils.assertContainsMethod(nodesClass, Short.class, "getId16");
        CompilationTestUtils.assertContainsMethod(nodesClass, Integer.class, "getId32");
        CompilationTestUtils.assertContainsMethod(nodesClass, Long.class, "getId64");
        CompilationTestUtils.assertContainsMethod(nodesClass, Long.class, "getIdLeafref");
        CompilationTestUtils.assertContainsMethod(nodesClass, String.class, "getIdString");
        CompilationTestUtils.assertContainsMethod(nodesClass, Uint8.class, "getIdU8");
        CompilationTestUtils.assertContainsMethod(nodesClass, Uint16.class, "getIdU16");
        CompilationTestUtils.assertContainsMethod(nodesClass, Uint32.class, "getIdU32");
        CompilationTestUtils.assertContainsMethod(nodesClass, Uint64.class, "getIdU64");
        CompilationTestUtils.assertContainsMethod(nodesClass, pkg + ".Nodes$IdUnion", "getIdUnion", loader);

        final Object builderObj = builderClass.getDeclaredConstructor().newInstance();

        Method method = CompilationTestUtils.assertContainsMethod(builderClass, builderClass, "setIdBinary",
            b.getClass());
        final List<Range<Integer>> lengthConstraints = new ArrayList<>();
        lengthConstraints.add(Range.closed(1, 10));
        byte[] arg = new byte[] {};
        String expectedMsg = String.format("Invalid length: %s, expected: %s.", HexFormat.of().formatHex(arg),
            lengthConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(builderObj, method, expectedMsg, arg);

        method = CompilationTestUtils.assertContainsMethod(builderClass, builderClass, "setIdDecimal64",
            Decimal64.class);
        final List<Range<Decimal64>> rangeConstraints = new ArrayList<>();
        rangeConstraints.add(Range.closed(Decimal64.valueOf("1.5"), Decimal64.valueOf("5.5")));
        Object arg1 = Decimal64.valueOf("1.4").scaleTo(4);
        expectedMsg = String.format("Invalid range: %s, expected: %s.", arg1, rangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(builderObj, method, expectedMsg, arg1);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testGenerationContextReferenceExtension() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("context-reference");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("context-reference");
        generateTestSources("/compilation/context-reference", sourcesOutputDir);

        // Test if all sources are generated
        final var fooParent = sourcesOutputDir.resolve(CompilationTestUtils.NS_FOO);
        CompilationTestUtils.assertFilesCount(fooParent, 3);
        CompilationTestUtils.assertFilesCount(sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_FOO), 1);
        assertRegularFile(fooParent, "FooData.java");
        assertRegularFile(fooParent, "Nodes.java");
        assertRegularFile(fooParent, "NodesBuilder.java");

        final var barParent = sourcesOutputDir.resolve(CompilationTestUtils.NS_BAR);
        CompilationTestUtils.assertFilesCount(barParent, 2);
        CompilationTestUtils.assertFilesCount(sourcesOutputDir.resolve(CompilationTestUtils.NS_SVC_BAR), 1);
        assertRegularFile(barParent, "BarData.java");
        assertRegularFile(barParent, "IdentityClass.java");

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() });
        final Class<?> nodesClass = Class.forName(CompilationTestUtils.BASE_PKG
            + ".urn.opendaylight.foo.rev131008.Nodes", true, loader);
        final Class<?> identityClass = Class
                .forName(CompilationTestUtils.BASE_PKG + ".urn.opendaylight.bar.rev131008.IdentityClass", true, loader);

        // test identity
        final Class<?> baseIdentity = Class.forName("org.opendaylight.yangtools.binding.BaseIdentity", true,
            loader);
        assertEquals(List.of(baseIdentity), Arrays.asList(identityClass.getInterfaces()));

        // Test annotation
        final Method getId;
        try {
            getId = nodesClass.getMethod("getId");
        } catch (final NoSuchMethodException e) {
            throw new AssertionError("Method getId() not found", e);
        }

        assertEquals(ImmutableSet.of(RoutingContext.class), Arrays.stream(getId.getAnnotations())
            .map(Annotation::annotationType).collect(Collectors.toSet()));
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void compilationTest() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("yang");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("yang");
        generateTestSources("/yang", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testBug586() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("bug586");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("bug586");
        generateTestSources("/compilation/bug586", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testBug4760() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("bug4760");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("bug4760");
        generateTestSources("/compilation/bug4760", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    /**
     * Test handling nested uses-augmentations.
     */
    @Test
    void testBug1172() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("bug1172");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("bug1172");
        generateTestSources("/compilation/bug1172", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testBug5461() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("bug5461");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("bug5461");
        generateTestSources("/compilation/bug5461", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testBug5882() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("bug5882");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("bug5882");
        generateTestSources("/compilation/bug5882", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final var parent = sourcesOutputDir.resolve(CompilationTestUtils.NS_BUG5882);
        assertRegularFile(parent, "FooData.java");
        assertRegularFile(parent, "TypedefCurrent.java");
        assertRegularFile(parent, "TypedefDeprecated.java");

        try (var loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() })) {
            final String pkg = CompilationTestUtils.BASE_PKG + ".urn.yang.foo.rev160102";
            final Class<?> cls = loader.loadClass(pkg + ".FooData");
            final Class<?> clsContainer = loader.loadClass(pkg + ".ContainerMain");
            final Class<?> clsTypedefDepr = loader.loadClass(pkg + ".TypedefDeprecated");
            final Class<?> clsTypedefCur = loader.loadClass(pkg + ".TypedefCurrent");
            final Class<?> clsGroupingDepr = loader.loadClass(pkg + ".GroupingDeprecated");
            final Class<?> clsGroupingCur = loader.loadClass(pkg + ".GroupingCurrent");
            final Class<?> clsTypeDef1 = loader.loadClass(pkg + ".Typedef1");
            final Class<?> clsTypeDef2 = loader.loadClass(pkg + ".Typedef2");
            final Class<?> clsTypeDef3 = loader.loadClass(pkg + ".Typedef3");
            assertEquals(1, clsTypedefDepr.getAnnotations().length);
            assertThat(clsTypedefDepr.getAnnotations()[0].toString()).startsWith("@java.lang.Deprecated");
            assertEquals(0, clsTypedefCur.getAnnotations().length);
            assertEquals(1, clsGroupingDepr.getAnnotations().length);
            assertThat(clsGroupingDepr.getAnnotations()[0].toString()).startsWith("@java.lang.Deprecated");
            assertEquals(0, clsGroupingCur.getAnnotations().length);
            assertEquals(0, clsTypeDef1.getAnnotations().length);
            assertEquals(1, clsTypeDef2.getAnnotations().length);
            assertThat(clsTypeDef2.getAnnotations()[0].toString()).startsWith("@java.lang.Deprecated");
            assertEquals(0, clsTypeDef3.getAnnotations().length);

            /*methods inside container*/
            assertTrue(clsContainer.getMethod("getContainerMainLeafDepr").isAnnotationPresent(Deprecated.class));
            assertTrue(clsContainer.getMethod("getContainerMainListDepr").isAnnotationPresent(Deprecated.class));
            assertTrue(clsContainer.getMethod("getContainerMainChoiceDepr").isAnnotationPresent(Deprecated.class));
            assertFalse(clsContainer.getMethod("getContainerMainLeafCurrent").isAnnotationPresent(Deprecated.class));
            assertFalse(clsContainer.getMethod("getContainerMainListCurrent").isAnnotationPresent(Deprecated.class));
            assertFalse(clsContainer.getMethod("getContainerMainChoiceCur").isAnnotationPresent(Deprecated.class));

            /*methods inside module*/
            assertTrue(cls.getMethod("getContainerMainLeafDepr").isAnnotationPresent(Deprecated.class));
            assertTrue(cls.getMethod("getContainerMainListDepr").isAnnotationPresent(Deprecated.class));
            assertTrue(cls.getMethod("getContainerMainChoiceDepr").isAnnotationPresent(Deprecated.class));
            assertFalse(cls.getMethod("getContainerMainLeafCurrent").isAnnotationPresent(Deprecated.class));
            assertFalse(cls.getMethod("getContainerMainListCurrent").isAnnotationPresent(Deprecated.class));
            assertFalse(cls.getMethod("getContainerMainChoiceCur").isAnnotationPresent(Deprecated.class));
            assertTrue(cls.getMethod("getLeafDeprecated").isAnnotationPresent(Deprecated.class));
        }

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    /**
     * Test if class generated for node from grouping implements ChildOf.
     */
    @Test
    void testBug1377() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("bug1377");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("bug1377");

        generateTestSources("/compilation/bug1377", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() });
        final Class<?> outputActionClass = Class.forName(CompilationTestUtils.BASE_PKG
                + ".urn.test.foo.rev140717.action.action.output.action._case.OutputAction", true, loader);
        final Class<?> actionClass = Class.forName(CompilationTestUtils.BASE_PKG + ".urn.test.foo.rev140717.Action",
            true, loader);

        // Test generated 'container output-action'
        assertTrue(outputActionClass.isInterface());
        CompilationTestUtils.assertImplementsParameterizedIfc(outputActionClass, ChildOf.class.toString(),
            actionClass.getCanonicalName());

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testMdsal327() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal327");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal327");
        generateTestSources("/compilation/mdsal327", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testMdsal365() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal365");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal365");
        generateTestSources("/compilation/mdsal365", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testMdsal395() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal395");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal395");
        generateTestSources("/compilation/mdsal395", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void classNamesColisionTest() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("class-name-collision");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("class-name-collision");
        generateTestSources("/compilation/class-name-collision", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void innerEnumerationNameCollisionTest() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal321");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal321");
        generateTestSources("/compilation/mdsal321", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void twoNestedUnionsTest() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal320");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal320");
        generateTestSources("/compilation/mdsal320", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testMdsal425() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal425");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal425");
        generateTestSources("/compilation/mdsal425", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testMdsal426() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal426");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal426");
        generateTestSources("/compilation/mdsal426", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testMdsal529() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal529");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal529");
        generateTestSources("/compilation/mdsal529", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testMdsal589() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal589");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal589");
        generateTestSources("/compilation/mdsal589", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testMdsal533() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal533");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal533");
        generateTestSources("/compilation/mdsal533", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testMdsal664() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal664");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal664");
        generateTestSources("/compilation/mdsal664", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testUnionStringPatterns() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("union-string-pattern");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("union-string-pattern");
        generateTestSources("/compilation/union-string-pattern", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final var loader = new URLClassLoader(new URL[]{compiledOutputDir.toUri().toURL()});
        final var fooClass = Class.forName(CompilationTestUtils.BASE_PKG + ".foo.norev.Foo", true, loader);

        final var patterns = fooClass.getDeclaredField(TypeConstants.PATTERN_CONSTANT_NAME);
        assertEquals(List.class, patterns.getType());

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void yangDataCompilation() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("yang-data-gen");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("yang-data-gen");

        generateTestSources("/compilation/yang-data-gen", sourcesOutputDir);

        final var artifactNames = List.of(
            // module with top level container
            "YangDataDemoData", "RootContainer", "RootContainerBuilder",

            // yang-data artifacts
            "YangDataWithContainer", "YangDataWithContainerBuilder",
            "YangDataWithList", "YangDataWithListBuilder",
            "YangDataWithLeaf", "YangDataWithLeafBuilder",
            "YangDataWithLeafList", "YangDataWithLeafListBuilder",
            "YangDataWithAnydata", "YangDataWithAnydataBuilder",
            "YangDataWithAnyxml", "YangDataWithAnyxmlBuilder",

            // yang-data content artifacts
            "yang.data.with.container.ContainerFromYangData",
            "yang.data.with.container.ContainerFromYangDataBuilder",
            "yang.data.with.list.ListFromYangData", "yang.data.with.list.ListFromYangDataBuilder",
            "yang.data.with.anydata.AnydataFromYangData", "yang.data.with.anyxml.AnyxmlFromYangData",

            // yang-data artifacts using groups
            "YangDataWithContainerFromGroup", "YangDataWithContainerFromGroupBuilder",
            "YangDataWithListFromGroup", "YangDataWithListFromGroupBuilder",
            "YangDataWithLeafFromGroup", "YangDataWithLeafFromGroupBuilder",
            "YangDataWithLeafListFromGroup", "YangDataWithLeafListFromGroupBuilder",
            "YangDataWithAnydataFromGroup", "YangDataWithAnydataFromGroupBuilder",
            "YangDataWithAnyxmlFromGroup", "YangDataWithAnyxmlFromGroupBuilder",

            // group artifacts
            "GrpForContainer", "GrpForList", "GrpForLeaf", "GrpForLeafList", "GrpForAnydata", "GrpForAnyxml",

            // group content artifacts
            "grp._for.container.ContainerFromGroup", "grp._for.container.ContainerFromGroupBuilder",
            "grp._for.list.ListFromGroup", "grp._for.list.ListFromGroupBuilder",
            "grp._for.anydata.AnydataFromGroup", "grp._for.anyxml.AnyxmlFromGroup",

            // artifacts for non-ascii template naming: yang data artifact, inner container + builder
            "$ľaľaho$20$papľuhu", "$ľaľaho$20$papľuhuBuilder",
            "$ľaľaho$20$papľuhu$.LatinNaming", "$ľaľaho$20$papľuhu$.LatinNamingBuilder",
            "привет", "приветBuilder", "привет$.CyrillicNaming", "привет$.CyrillicNamingBuilder"
        );

        for (var name : artifactNames) {
            final var className = CompilationTestUtils.BASE_PKG + ".urn.test.yang.data.demo.rev220222." + name;
            // ensure class source is generated
            final var srcPath = className.replace('.', File.separatorChar) + ".java";
            assertRegularFile(sourcesOutputDir, srcPath);
        }

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private static void testReturnTypeIdentityref(final Class<?> clazz, final String methodName,
            final String returnTypeStr) {
        final Class<?> returnType;
        try {
            returnType = clazz.getMethod(methodName).getReturnType();
        } catch (NoSuchMethodException | SecurityException e) {
            throw new AssertionError(e);
        }
        assertTrue(returnType.isInterface());
        assertEquals(returnTypeStr, returnType.getName());
    }

    private static void testReturnTypeInstanceIdentitifer(final ClassLoader loader, final Class<?> clazz,
            final String methodName) {
        final Method method;
        try {
            method = clazz.getMethod(methodName);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new AssertionError(e);
        }
        final Class<?> rawReturnType;
        try {
            rawReturnType = Class.forName("org.opendaylight.yangtools.binding.BindingInstanceIdentifier", true,
                loader);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
        assertEquals(rawReturnType, method.getGenericReturnType());
    }
}
