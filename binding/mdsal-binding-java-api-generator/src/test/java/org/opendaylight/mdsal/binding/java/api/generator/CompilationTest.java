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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.annotations.RoutingContext;
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
public class CompilationTest extends BaseCompilationTest {

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
    public void testListGeneration() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("list-gen");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("list-gen");
        generateTestSources("/compilation/list-gen", sourcesOutputDir);

        // Test if all sources are generated
        File parent = new File(sourcesOutputDir, CompilationTestUtils.NS_TEST);
        final File keyArgs = new File(parent, "KeyArgs.java");
        final File links = new File(parent, "Links.java");
        final File linksBuilder = new File(parent, "LinksBuilder.java");
        final File linksKey = new File(parent, "LinksKey.java");
        final File testData = new File(parent, "TestData.java");
        assertTrue(keyArgs.exists());
        assertTrue(links.exists());
        assertTrue(linksBuilder.exists());
        assertTrue(linksKey.exists());
        assertTrue(testData.exists());
        CompilationTestUtils.assertFilesCount(parent, 7);

        parent = new File(sourcesOutputDir, CompilationTestUtils.NS_TEST + CompilationTestUtils.FS + "links");
        final File level = new File(parent, "Level.java");
        final File linkGroup = new File(parent, "LinkGroup.java");
        final File node = new File(parent, "Node.java");
        final File nodeBuilder = new File(parent, "NodeBuilder.java");
        final File nodeList = new File(parent, "NodeList.java");
        final File nodeListBuilder = new File(parent, "NodeListBuilder.java");
        final File nodesType = new File(parent, "NodesType.java");
        assertTrue(level.exists());
        assertTrue(linkGroup.exists());
        assertTrue(node.exists());
        assertTrue(nodeBuilder.exists());
        assertTrue(nodeList.exists());
        assertTrue(nodeListBuilder.exists());
        assertTrue(nodesType.exists());
        CompilationTestUtils.assertFilesCount(parent, 8);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
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
        assertEquals(3, abstractMethods(keyArgsClass).size());

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
        assertEquals(-8829501012356283881L, suid.getLong(null));

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    /**
     * Test that nonnull getter method is generated for non-presence containers only.
     *
     * @throws Exception when any exception occurs during the test
     */
    @Test
    public void testContainerGettersGeneration() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("containers-gen");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("containers-gen");
        generateTestSources("/compilation/containers-gen", sourcesOutputDir);

        // Test if all sources were generated from 'module containers'
        File parent = new File(sourcesOutputDir, CompilationTestUtils.NS_TEST);
        assertTrue(new File(parent, "RootContainer.java").exists());
        assertTrue(new File(parent, "rootcontainer/PresenceContainer.java").exists());
        assertTrue(new File(parent, "rootcontainer/NonPresenceContainer.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 5);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
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
        assertTrue(error.getCause() instanceof NoSuchMethodException);

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
    public void testAugmentUnderUsesGeneration() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("augment-under-uses");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("augment-under-uses");
        generateTestSources("/compilation/augment-under-uses", sourcesOutputDir);

        // Test if all sources were generated from 'module foo'
        File parent = new File(sourcesOutputDir, CompilationTestUtils.NS_FOO);
        assertTrue(new File(parent, "Object.java").exists());
        assertTrue(new File(parent, "ClosedObject.java").exists());
        assertTrue(new File(parent, "OpenObject.java").exists());
        assertTrue(new File(parent, "ExplicitRouteObject.java").exists());
        assertTrue(new File(parent, "PathKeySubobject.java").exists());
        assertTrue(new File(parent, "FooData.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 11);

        parent = new File(parent, "object");
        assertTrue(new File(parent, "Nodes.java").exists());
        assertTrue(new File(parent, "NodesBuilder.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 2);

        parent = new File(sourcesOutputDir, CompilationTestUtils.NS_FOO + CompilationTestUtils.FS + "closed");
        CompilationTestUtils.assertFilesCount(parent, 1);

        parent = new File(parent, "object");
        assertTrue(new File(parent, "Link1.java").exists());
        assertTrue(new File(parent, "Link1Builder.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 2);

        parent = new File(sourcesOutputDir, CompilationTestUtils.NS_FOO + CompilationTestUtils.FS + "open");
        CompilationTestUtils.assertFilesCount(parent, 1);

        parent = new File(parent, "object");
        assertTrue(new File(parent, "Nodes1.java").exists());
        assertTrue(new File(parent, "Nodes1Builder.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 3);

        parent = new File(parent, "nodes");
        assertTrue(new File(parent, "Links.java").exists());
        assertTrue(new File(parent, "LinksBuilder.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 2);

        parent = new File(sourcesOutputDir, CompilationTestUtils.NS_FOO + CompilationTestUtils.FS + "explicit");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "route");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "object");
        assertTrue(new File(parent, "Subobjects.java").exists());
        assertTrue(new File(parent, "SubobjectsBuilder.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 3);

        parent = new File(parent, "subobjects");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "subobject");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "type");
        assertTrue(new File(parent, "PathKey.java").exists());
        assertTrue(new File(parent, "PathKeyBuilder.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 3);

        parent = new File(parent, "path");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "key");
        assertTrue(new File(parent, "PathKey.java").exists());
        assertTrue(new File(parent, "PathKeyBuilder.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 2);

        // Test if all sources were generated from 'module bar'
        parent = new File(sourcesOutputDir, CompilationTestUtils.NS_BAR);
        assertTrue(new File(parent, "BarData.java").exists());
        assertTrue(new File(parent, "BasicExplicitRouteSubobjects.java").exists());
        assertTrue(new File(parent, "ExplicitRouteSubobjects.java").exists());
        assertTrue(new File(parent, "RouteSubobjects.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 7);

        parent = new File(parent, "route");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(new File(sourcesOutputDir, CompilationTestUtils.NS_BAR), "basic");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "explicit");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "route");
        CompilationTestUtils.assertFilesCount(parent, 1);

        parent = new File(parent, "subobjects");
        CompilationTestUtils.assertFilesCount(parent, 2);
        assertTrue(new File(parent, "SubobjectType.java").exists());

        parent = new File(parent, "subobject");
        CompilationTestUtils.assertFilesCount(parent, 1);

        parent = new File(parent, "type");
        assertTrue(new File(parent, "IpPrefix.java").exists());
        assertTrue(new File(parent, "IpPrefixBuilder.java").exists());
        assertTrue(new File(parent, "Label.java").exists());
        assertTrue(new File(parent, "LabelBuilder.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 4);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testAugmentOfAugmentGeneration() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("aug-of-aug");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("aug-of-aug");
        generateTestSources("/compilation/augment-of-augment", sourcesOutputDir);

        // Test if all sources were generated from 'module foo'
        File parent = new File(sourcesOutputDir, CompilationTestUtils.NS_FOO);
        assertTrue(new File(parent, "FooData.java").exists());
        assertTrue(new File(parent, "FooListener.java").exists());
        assertTrue(new File(parent, "PathAttributes.java").exists());
        assertTrue(new File(parent, "Update.java").exists());
        assertTrue(new File(parent, "UpdateBuilder.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 8);

        parent = new File(sourcesOutputDir, CompilationTestUtils.NS_FOO + CompilationTestUtils.FS + "path");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "attributes");
        CompilationTestUtils.assertFilesCount(parent, 2);
        final File origin = new File(parent, "Origin.java");
        final File originBuilder = new File(parent, "OriginBuilder.java");
        assertTrue(origin.exists());
        assertTrue(originBuilder.exists());

        parent = new File(sourcesOutputDir, CompilationTestUtils.NS_FOO + CompilationTestUtils.FS + "update");
        CompilationTestUtils.assertFilesCount(parent, 2);
        assertTrue(new File(parent, "PathAttributes.java").exists());
        assertTrue(new File(parent, "PathAttributesBuilder.java").exists());

        // Test if all sources were generated from 'module bar'
        parent = new File(sourcesOutputDir, CompilationTestUtils.NS_BAR);
        assertTrue(new File(parent, "BarData.java").exists());
        assertTrue(new File(parent, "Destination.java").exists());
        assertTrue(new File(parent, "PathAttributes1.java").exists());
        assertTrue(new File(parent, "PathAttributes1Builder.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 7);

        parent = new File(sourcesOutputDir, CompilationTestUtils.NS_BAR + CompilationTestUtils.FS + "destination");
        CompilationTestUtils.assertFilesCount(parent, 2);
        final File destinationType = new File(parent, "DestinationType.java");
        assertTrue(destinationType.exists());

        parent = new File(parent, "destination");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "type");
        CompilationTestUtils.assertFilesCount(parent, 2);
        final File destinationIpv4 = new File(parent, "DestinationIp.java");
        final File destinationIpv4Builder = new File(parent, "DestinationIpBuilder.java");
        assertTrue(destinationIpv4.exists());
        assertTrue(destinationIpv4Builder.exists());

        parent = new File(sourcesOutputDir, CompilationTestUtils.NS_BAR + CompilationTestUtils.FS + "update");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "path");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "attributes");
        final File mpUnreachNlri = new File(parent, "MpUnreachNlri.java");
        final File mpUnreachNlriBuilder = new File(parent, "MpUnreachNlriBuilder.java");
        assertTrue(mpUnreachNlri.exists());
        assertTrue(mpUnreachNlriBuilder.exists());
        CompilationTestUtils.assertFilesCount(parent, 3);

        parent = new File(parent, "mp");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "unreach");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "nlri");
        assertTrue(new File(parent, "WithdrawnRoutes.java").exists());
        assertTrue(new File(parent, "WithdrawnRoutesBuilder.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 2);

        // Test if all sources were generated from 'module baz'
        parent = new File(sourcesOutputDir, CompilationTestUtils.NS_BAZ);
        assertTrue(new File(parent, "BazData.java").exists());
        assertTrue(new File(parent, "LinkstateDestination.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 4);

        parent = new File(sourcesOutputDir, CompilationTestUtils.NS_BAZ + CompilationTestUtils.FS + "update");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "path");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "attributes");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "mp");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "unreach");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "nlri");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "withdrawn");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "routes");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "destination");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "type");
        final File destinationLinkstate = new File(parent, "DestinationLinkstate.java");
        final File destinationLinkstateBuilder = new File(parent, "DestinationLinkstateBuilder.java");
        assertTrue(destinationLinkstate.exists());
        assertTrue(destinationLinkstateBuilder.exists());
        CompilationTestUtils.assertFilesCount(parent, 3);
        parent = new File(parent, "destination");
        CompilationTestUtils.assertFilesCount(parent, 1);
        parent = new File(parent, "linkstate");
        final File links = new File(parent, "Links.java");
        final File linksBuilder = new File(parent, "LinksBuilder.java");
        assertTrue(links.exists());
        assertTrue(linksBuilder.exists());
        CompilationTestUtils.assertFilesCount(parent, 3);
        parent = new File(parent, "links");
        final File source = new File(parent, "Source.java");
        final File sourceBuilder = new File(parent, "SourceBuilder.java");
        assertTrue(source.exists());
        assertTrue(sourceBuilder.exists());
        CompilationTestUtils.assertFilesCount(parent, 3);
        parent = new File(parent, "source");
        final File address = new File(parent, "Address.java");
        final File addressBuilder = new File(parent, "AddressBuilder.java");
        assertTrue(address.exists());
        assertTrue(addressBuilder.exists());
        CompilationTestUtils.assertFilesCount(parent, 2);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testLeafReturnTypes() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("leaf-return-types");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("leaf-return-types");
        generateTestSources("/compilation/leaf-return-types", sourcesOutputDir);

        final File parent = new File(sourcesOutputDir, CompilationTestUtils.NS_TEST);
        assertTrue(new File(parent, "TestData.java").exists());
        assertTrue(new File(parent, "Nodes.java").exists());
        assertTrue(new File(parent, "NodesBuilder.java").exists());
        assertTrue(new File(parent, "Alg.java").exists());
        CompilationTestUtils.assertFilesCount(parent, 5);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final String pkg = CompilationTestUtils.BASE_PKG + ".urn.opendaylight.test.rev131008";
        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
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
        Object arg1 = Decimal64.valueOf("1.4");
        expectedMsg = String.format("Invalid range: %s, expected: %s.", arg1, rangeConstraints);
        CompilationTestUtils.assertContainsRestrictionCheck(builderObj, method, expectedMsg, arg1);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testGenerationContextReferenceExtension() throws IOException, URISyntaxException,
            ClassNotFoundException {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("context-reference");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("context-reference");
        generateTestSources("/compilation/context-reference", sourcesOutputDir);

        // Test if all sources are generated
        final File fooParent = new File(sourcesOutputDir, CompilationTestUtils.NS_FOO);
        CompilationTestUtils.assertFilesCount(fooParent, 4);
        assertTrue(new File(fooParent, "FooData.java").exists());
        assertTrue(new File(fooParent, "Nodes.java").exists());
        assertTrue(new File(fooParent, "NodesBuilder.java").exists());

        final File barParent = new File(sourcesOutputDir, CompilationTestUtils.NS_BAR);
        CompilationTestUtils.assertFilesCount(barParent, 3);
        assertTrue(new File(barParent, "BarData.java").exists());
        assertTrue(new File(barParent, "IdentityClass.java").exists());

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        final Class<?> nodesClass = Class.forName(CompilationTestUtils.BASE_PKG
            + ".urn.opendaylight.foo.rev131008.Nodes", true, loader);
        final Class<?> identityClass = Class
                .forName(CompilationTestUtils.BASE_PKG + ".urn.opendaylight.bar.rev131008.IdentityClass", true, loader);

        // test identity
        final Class<?> baseIdentity = Class.forName("org.opendaylight.yangtools.yang.binding.BaseIdentity", true,
            loader);
        assertEquals(ImmutableList.of(baseIdentity), Arrays.asList(identityClass.getInterfaces()));

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
    public void compilationTest() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("yang");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("yang");
        generateTestSources("/yang", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testBug586() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("bug586");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("bug586");
        generateTestSources("/compilation/bug586", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testBug4760() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("bug4760");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("bug4760");
        generateTestSources("/compilation/bug4760", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    /**
     * Test handling nested uses-augmentations.
     */
    @Test
    public void testBug1172() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("bug1172");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("bug1172");
        generateTestSources("/compilation/bug1172", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testBug5461() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("bug5461");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("bug5461");
        generateTestSources("/compilation/bug5461", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testBug5882() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("bug5882");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("bug5882");
        generateTestSources("/compilation/bug5882", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final File parent = new File(sourcesOutputDir, CompilationTestUtils.NS_BUG5882);
        assertTrue(new File(parent, "FooData.java").exists());
        assertTrue(new File(parent, "TypedefCurrent.java").exists());
        assertTrue(new File(parent, "TypedefDeprecated.java").exists());

        try (URLClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() })) {
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
            assertThat(clsTypedefDepr.getAnnotations()[0].toString(), startsWith("@java.lang.Deprecated"));
            assertEquals(0, clsTypedefCur.getAnnotations().length);
            assertEquals(1, clsGroupingDepr.getAnnotations().length);
            assertThat(clsGroupingDepr.getAnnotations()[0].toString(), startsWith("@java.lang.Deprecated"));
            assertEquals(0, clsGroupingCur.getAnnotations().length);
            assertEquals(0, clsTypeDef1.getAnnotations().length);
            assertEquals(1, clsTypeDef2.getAnnotations().length);
            assertThat(clsTypeDef2.getAnnotations()[0].toString(), startsWith("@java.lang.Deprecated"));
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
    public void testBug1377() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("bug1377");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("bug1377");

        generateTestSources("/compilation/bug1377", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
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
    public void testMdsal327() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal327");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal327");
        generateTestSources("/compilation/mdsal327", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testMdsal365() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal365");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal365");
        generateTestSources("/compilation/mdsal365", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testMdsal395() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal395");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal395");
        generateTestSources("/compilation/mdsal395", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void classNamesColisionTest() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("class-name-collision");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("class-name-collision");
        generateTestSources("/compilation/class-name-collision", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void innerEnumerationNameCollisionTest() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal321");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal321");
        generateTestSources("/compilation/mdsal321", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void twoNestedUnionsTest() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal320");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal320");
        generateTestSources("/compilation/mdsal320", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testMdsal425() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal425");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal425");
        generateTestSources("/compilation/mdsal425", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testMdsal426() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal426");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal426");
        generateTestSources("/compilation/mdsal426", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testMdsal529() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal529");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal529");
        generateTestSources("/compilation/mdsal529", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testMdsal589() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal589");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal589");
        generateTestSources("/compilation/mdsal589", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testMdsal533() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal533");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal533");
        generateTestSources("/compilation/mdsal533", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testMdsal664() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal664");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal664");
        generateTestSources("/compilation/mdsal664", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testUnionStringPatterns() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("union-string-pattern");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("union-string-pattern");
        generateTestSources("/compilation/union-string-pattern", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final ClassLoader loader = new URLClassLoader(new URL[]{compiledOutputDir.toURI().toURL()});
        final Class<?> fooClass = Class.forName(CompilationTestUtils.BASE_PKG + ".foo.norev.Foo", true, loader);

        final Field patterns = fooClass.getDeclaredField(TypeConstants.PATTERN_CONSTANT_NAME);
        assertEquals(List.class, patterns.getType());

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void yangDataCompilation() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("yang-data-gen");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("yang-data-gen");

        generateTestSources("/compilation/yang-data-gen", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        final ClassLoader loader = new URLClassLoader(new URL[]{compiledOutputDir.toURI().toURL()});
        final List<String> artifactNames = List.of(
                // module with top level container
                "$YangModuleInfoImpl", "YangDataDemoData", "RootContainer", "RootContainerBuilder",

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

        for (String name : artifactNames) {
            final String className = CompilationTestUtils.BASE_PKG + ".urn.test.yang.data.demo.rev220222." + name;
            // ensure class source is generated
            final String srcPath = className.replace('.', File.separatorChar) + ".java";
            assertTrue(srcPath + " exists", new File(sourcesOutputDir, srcPath).exists());
            // ensure class is loadable
            Class.forName(className, true, loader);
        }

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private static void testReturnTypeIdentityref(final Class<?> clazz, final String methodName,
            final String returnTypeStr) throws NoSuchMethodException {
        Class<?> returnType = clazz.getMethod(methodName).getReturnType();
        assertTrue(returnType.isInterface());
        assertEquals(returnTypeStr, returnType.getName());
    }

    private static void testReturnTypeInstanceIdentitifer(final ClassLoader loader, final Class<?> clazz,
            final String methodName) throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        final Method method = clazz.getMethod(methodName);
        final Class<?> rawReturnType = Class.forName("org.opendaylight.yangtools.yang.binding.InstanceIdentifier", true,
            loader);
        assertEquals(rawReturnType, method.getReturnType());
        final Type returnType = method.getGenericReturnType();
        assertTrue(returnType instanceof ParameterizedType);
        final ParameterizedType pt = (ParameterizedType) returnType;
        final Type[] parameters = pt.getActualTypeArguments();
        assertEquals(1, parameters.length);
        final Type parameter = parameters[0];
        assertTrue(parameter instanceof WildcardType);
        final WildcardType wildcardType = (WildcardType) parameter;
        assertEquals("?", wildcardType.toString());
    }
}
