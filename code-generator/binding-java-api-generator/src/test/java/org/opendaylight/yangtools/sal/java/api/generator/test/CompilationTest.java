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
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.*;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.binding.annotations.RoutingContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Test correct code generation.
 *
 */
public class CompilationTest extends BaseCompilationTest {

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

        // Test if all sources are generated
        File parent = new File(sourcesOutputDir, NS_TEST);
        File keyArgs = new File(parent, "KeyArgs.java");
        File links = new File(parent, "Links.java");
        File linksBuilder = new File(parent, "LinksBuilder.java");
        File linksKey = new File(parent, "LinksKey.java");
        File testData = new File(parent, "TestData.java");
        assertTrue(keyArgs.exists());
        assertTrue(links.exists());
        assertTrue(linksBuilder.exists());
        assertTrue(linksKey.exists());
        assertTrue(testData.exists());
        assertFilesCount(parent, 6);

        parent = new File(sourcesOutputDir, NS_TEST + FS + "links");
        File level = new File(parent, "Level.java");
        File linkGroup = new File(parent, "LinkGroup.java");
        File node = new File(parent, "Node.java");
        File nodeBuilder = new File(parent, "NodeBuilder.java");
        File nodeList = new File(parent, "NodeList.java");
        File nodeListBuilder = new File(parent, "NodeListBuilder.java");
        File nodesType = new File(parent, "NodesType.java");
        assertTrue(level.exists());
        assertTrue(linkGroup.exists());
        assertTrue(node.exists());
        assertTrue(nodeBuilder.exists());
        assertTrue(nodeList.exists());
        assertTrue(nodeListBuilder.exists());
        assertTrue(nodesType.exists());
        assertFilesCount(parent, 7);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> keyArgsClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.KeyArgs", true, loader);
        Class<?> linksClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.Links", true, loader);
        Class<?> linksKeyClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.LinksKey", true, loader);

        // Test generated 'grouping key-args'
        assertTrue(keyArgsClass.isInterface());
        assertEquals(2, keyArgsClass.getDeclaredMethods().length);
        assertContainsMethod(keyArgsClass, String.class, "getName");
        assertContainsMethod(keyArgsClass, Integer.class, "getSize");

        // Test generated 'list links'
        assertTrue(linksClass.isInterface());
        // TODO: anyxml
        assertEquals(6, linksClass.getDeclaredMethods().length);
        assertImplementsIfc(linksClass, keyArgsClass);

        // Test list key constructor arguments ordering
        assertContainsConstructor(linksKeyClass, Byte.class, String.class, Integer.class);
        // Test serialVersionUID generation
        Field suid = assertContainsField(linksKeyClass, "serialVersionUID", Long.TYPE);
        suid.setAccessible(true);
        assertEquals(-8829501012356283881L, suid.getLong(null));

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

        // Test if all sources were generated from 'module foo'
        File parent = new File(sourcesOutputDir, NS_FOO);
        assertTrue(new File(parent, "Object.java").exists());
        assertTrue(new File(parent, "OpenObject.java").exists());
        assertTrue(new File(parent, "ExplicitRouteObject.java").exists());
        assertTrue(new File(parent, "PathKeySubobject.java").exists());
        assertFilesCount(parent, 7);

        parent = new File(parent, "object");
        assertTrue(new File(parent, "Nodes.java").exists());
        assertTrue(new File(parent, "NodesBuilder.java").exists());
        assertFilesCount(parent, 2);

        parent = new File(sourcesOutputDir, NS_FOO + FS + "open");
        assertFilesCount(parent, 1);

        parent = new File(parent, "object");
        assertTrue(new File(parent, "Nodes1.java").exists());
        assertTrue(new File(parent, "Nodes1Builder.java").exists());
        assertFilesCount(parent, 3);

        parent = new File(parent, "nodes");
        assertTrue(new File(parent, "Links.java").exists());
        assertTrue(new File(parent, "LinksBuilder.java").exists());
        assertFilesCount(parent, 2);

        parent = new File(sourcesOutputDir, NS_FOO + FS + "explicit");
        assertFilesCount(parent, 1);
        parent = new File(parent, "route");
        assertFilesCount(parent, 1);
        parent = new File(parent, "object");
        assertTrue(new File(parent, "Subobjects.java").exists());
        assertTrue(new File(parent, "SubobjectsBuilder.java").exists());
        assertFilesCount(parent, 3);

        parent = new File(parent, "subobjects");
        assertFilesCount(parent, 1);
        parent = new File(parent, "subobject");
        assertFilesCount(parent, 1);
        parent = new File(parent, "type");
        assertTrue(new File(parent, "PathKey.java").exists());
        assertTrue(new File(parent, "PathKeyBuilder.java").exists());
        assertFilesCount(parent, 3);

        parent = new File(parent, "path");
        assertFilesCount(parent, 1);
        parent = new File(parent, "key");
        assertTrue(new File(parent, "PathKey.java").exists());
        assertTrue(new File(parent, "PathKeyBuilder.java").exists());
        assertFilesCount(parent, 2);

        // Test if all sources were generated from 'module bar'
        parent = new File(sourcesOutputDir, NS_BAR);
        assertTrue(new File(parent, "BasicExplicitRouteSubobjects.java").exists());
        assertTrue(new File(parent, "ExplicitRouteSubobjects.java").exists());
        assertFilesCount(parent, 3);

        parent = new File(parent, "basic");
        assertFilesCount(parent, 1);
        parent = new File(parent, "explicit");
        assertFilesCount(parent, 1);
        parent = new File(parent, "route");
        assertFilesCount(parent, 1);

        parent = new File(parent, "subobjects");
        assertFilesCount(parent, 2);
        assertTrue(new File(parent, "SubobjectType.java").exists());

        parent = new File(parent, "subobject");
        assertFilesCount(parent, 1);

        parent = new File(parent, "type");
        assertTrue(new File(parent, "IpPrefix.java").exists());
        assertTrue(new File(parent, "IpPrefixBuilder.java").exists());
        assertTrue(new File(parent, "Label.java").exists());
        assertTrue(new File(parent, "LabelBuilder.java").exists());
        assertFilesCount(parent, 4);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testAugmentOfAugmentGeneration() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "aug-of-aug");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "aug-of-aug");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        final List<File> sourceFiles = getSourceFiles("/compilation/augment-of-augment");
        final Set<Module> modulesToBuild = parser.parseYangModels(sourceFiles);
        final SchemaContext context = parser.resolveSchemaContext(modulesToBuild);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(new HashSet<>(types));
        generator.generateToFile(sourcesOutputDir);

        // Test if all sources were generated from 'module foo'
        File parent = new File(sourcesOutputDir, NS_FOO);
        File fooListener = new File(parent, "FooListener.java");
        File pathAttributes = new File(parent, "PathAttributes.java");
        File update = new File(parent, "Update.java");
        File updateBuilder = new File(parent, "UpdateBuilder.java");
        assertTrue(fooListener.exists());
        assertTrue(pathAttributes.exists());
        assertTrue(update.exists());
        assertTrue(updateBuilder.exists());
        assertFilesCount(parent, 6);

        parent = new File(sourcesOutputDir, NS_FOO + FS + "path");
        assertFilesCount(parent, 1);
        parent = new File(parent, "attributes");
        assertFilesCount(parent, 2);
        File origin = new File(parent, "Origin.java");
        File originBuilder = new File(parent, "OriginBuilder.java");
        assertTrue(origin.exists());
        assertTrue(originBuilder.exists());

        parent = new File(sourcesOutputDir, NS_FOO + FS + "update");
        assertFilesCount(parent, 2);
        pathAttributes = new File(parent, "PathAttributes.java");
        File pathAttributesBuilder = new File(parent, "PathAttributesBuilder.java");
        assertTrue(pathAttributes.exists());
        assertTrue(pathAttributesBuilder.exists());

        // Test if all sources were generated from 'module bar'
        parent = new File(sourcesOutputDir, NS_BAR);
        File destination = new File(parent, "Destination.java");
        File pathAttributes1 = new File(parent, "PathAttributes1.java");
        File pathAttributes1Builder = new File(parent, "PathAttributes1Builder.java");
        assertTrue(destination.exists());
        assertTrue(pathAttributes1.exists());
        assertTrue(pathAttributes1Builder.exists());
        assertFilesCount(parent, 5);

        parent = new File(sourcesOutputDir, NS_BAR + FS + "destination");
        assertFilesCount(parent, 2);
        File destinationType = new File(parent, "DestinationType.java");
        assertTrue(destinationType.exists());

        parent = new File(parent, "destination");
        assertFilesCount(parent, 1);
        parent = new File(parent, "type");
        assertFilesCount(parent, 2);
        File destinationIpv4 = new File(parent, "DestinationIp.java");
        File destinationIpv4Builder = new File(parent, "DestinationIpBuilder.java");
        assertTrue(destinationIpv4.exists());
        assertTrue(destinationIpv4Builder.exists());

        parent = new File(sourcesOutputDir, NS_BAR + FS + "update");
        assertFilesCount(parent, 1);
        parent = new File(parent, "path");
        assertFilesCount(parent, 1);
        parent = new File(parent, "attributes");
        File mpUnreachNlri = new File(parent, "MpUnreachNlri.java");
        File mpUnreachNlriBuilder = new File(parent, "MpUnreachNlriBuilder.java");
        assertTrue(mpUnreachNlri.exists());
        assertTrue(mpUnreachNlriBuilder.exists());
        assertFilesCount(parent, 3);

        parent = new File(parent, "mp");
        assertFilesCount(parent, 1);
        parent = new File(parent, "unreach");
        assertFilesCount(parent, 1);
        parent = new File(parent, "nlri");
        File withdrawnRoutes = new File(parent, "WithdrawnRoutes.java");
        File withdrawnRoutesBuilder = new File(parent, "WithdrawnRoutesBuilder.java");
        assertTrue(withdrawnRoutes.exists());
        assertTrue(withdrawnRoutesBuilder.exists());
        assertFilesCount(parent, 2);

        // Test if all sources were generated from 'module baz'
        parent = new File(sourcesOutputDir, NS_BAZ);
        assertFilesCount(parent, 2);
        File linkstateDestination = new File(parent, "LinkstateDestination.java");
        assertTrue(linkstateDestination.exists());

        parent = new File(sourcesOutputDir, NS_BAZ + FS + "update");
        assertFilesCount(parent, 1);
        parent = new File(parent, "path");
        assertFilesCount(parent, 1);
        parent = new File(parent, "attributes");
        assertFilesCount(parent, 1);
        parent = new File(parent, "mp");
        assertFilesCount(parent, 1);
        parent = new File(parent, "unreach");
        assertFilesCount(parent, 1);
        parent = new File(parent, "nlri");
        assertFilesCount(parent, 1);
        parent = new File(parent, "withdrawn");
        assertFilesCount(parent, 1);
        parent = new File(parent, "routes");
        assertFilesCount(parent, 1);
        parent = new File(parent, "destination");
        assertFilesCount(parent, 1);
        parent = new File(parent, "type");
        File destinationLinkstate = new File(parent, "DestinationLinkstate.java");
        File destinationLinkstateBuilder = new File(parent, "DestinationLinkstateBuilder.java");
        assertTrue(destinationLinkstate.exists());
        assertTrue(destinationLinkstateBuilder.exists());
        assertFilesCount(parent, 3);
        parent = new File(parent, "destination");
        assertFilesCount(parent, 1);
        parent = new File(parent, "linkstate");
        File links = new File(parent, "Links.java");
        File linksBuilder = new File(parent, "LinksBuilder.java");
        assertTrue(links.exists());
        assertTrue(linksBuilder.exists());
        assertFilesCount(parent, 3);
        parent = new File(parent, "links");
        File source = new File(parent, "Source.java");
        File sourceBuilder = new File(parent, "SourceBuilder.java");
        assertTrue(source.exists());
        assertTrue(sourceBuilder.exists());
        assertFilesCount(parent, 3);
        parent = new File(parent, "source");
        File address = new File(parent, "Address.java");
        File addressBuilder = new File(parent, "AddressBuilder.java");
        assertTrue(address.exists());
        assertTrue(addressBuilder.exists());
        assertFilesCount(parent, 2);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testLeafReturnTypes() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "leaf-return-types");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "leaf-return-types");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        final List<File> sourceFiles = getSourceFiles("/compilation/leaf-return-types");
        final Set<Module> modulesToBuild = parser.parseYangModels(sourceFiles);
        final SchemaContext context = parser.resolveSchemaContext(modulesToBuild);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(new HashSet<>(types));
        generator.generateToFile(sourcesOutputDir);

        File parent = new File(sourcesOutputDir, NS_TEST);
        assertFilesCount(parent, 4);
        assertTrue(new File(parent, "TestData.java").exists());
        assertTrue(new File(parent, "Nodes.java").exists());
        assertTrue(new File(parent, "NodesBuilder.java").exists());
        assertTrue(new File(parent, "Alg.java").exists());

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        String pkg = BASE_PKG + ".urn.opendaylight.test.rev131008";
        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> nodesClass = Class.forName(pkg + ".Nodes", true, loader);

        // Test methods return type
        byte[] b = new byte[] {};
        assertContainsMethod(nodesClass, b.getClass(), "getIdBinary");
        assertContainsMethod(nodesClass, pkg + ".Nodes$IdBits", "getIdBits", loader);
        assertContainsMethod(nodesClass, Boolean.class, "isIdBoolean");
        assertContainsMethod(nodesClass, BigDecimal.class, "getIdDecimal64");
        assertContainsMethod(nodesClass, Boolean.class, "isIdEmpty");
        assertContainsMethod(nodesClass, pkg + ".Nodes$IdEnumeration", "getIdEnumeration", loader);
        testReturnTypeIdentityref(nodesClass, "getIdIdentityref", pkg + ".Alg");
        testReturnTypeInstanceIdentitifer(loader, nodesClass, "getIdInstanceIdentifier");
        assertContainsMethod(nodesClass, Byte.class, "getId8");
        assertContainsMethod(nodesClass, Short.class, "getId16");
        assertContainsMethod(nodesClass, Integer.class, "getId32");
        assertContainsMethod(nodesClass, Long.class, "getId64");
        assertContainsMethod(nodesClass, Long.class, "getIdLeafref");
        assertContainsMethod(nodesClass, String.class, "getIdString");
        assertContainsMethod(nodesClass, Short.class, "getIdU8");
        assertContainsMethod(nodesClass, Integer.class, "getIdU16");
        assertContainsMethod(nodesClass, Long.class, "getIdU32");
        assertContainsMethod(nodesClass, BigInteger.class, "getIdU64");
        assertContainsMethod(nodesClass, pkg + ".Nodes$IdUnion", "getIdUnion", loader);

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testGenerationContextReferenceExtension() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "context-reference");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "context-reference");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        final List<File> sourceFiles = getSourceFiles("/compilation/context-reference");
        final Set<Module> modulesToBuild = parser.parseYangModels(sourceFiles);
        final SchemaContext context = parser.resolveSchemaContext(modulesToBuild);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(new HashSet<>(types));
        generator.generateToFile(sourcesOutputDir);

        // Test if all sources are generated
        File fooParent = new File(sourcesOutputDir, NS_FOO);
        assertFilesCount(fooParent, 3);
        assertTrue(new File(fooParent, "FooData.java").exists());
        assertTrue(new File(fooParent, "Nodes.java").exists());
        assertTrue(new File(fooParent, "NodesBuilder.java").exists());

        File barParent = new File(sourcesOutputDir, NS_BAR);
        assertFilesCount(barParent, 1);
        assertTrue(new File(barParent, "IdentityClass.java").exists());

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> nodesClass = Class.forName(BASE_PKG + ".urn.opendaylight.foo.rev131008.Nodes", true, loader);
        Class<?> identityClass = Class
                .forName(BASE_PKG + ".urn.opendaylight.bar.rev131008.IdentityClass", true, loader);

        // test identity
        try {
            identityClass.getConstructor();
            Class<?> baseIdentity = Class.forName("org.opendaylight.yangtools.yang.binding.BaseIdentity", true, loader);
            assertEquals(baseIdentity, identityClass.getSuperclass());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("IdentityClass must have no-arg constructor");
        }

        // Test annotation
        try {
            Method getId = nodesClass.getMethod("getId");
            Annotation[] annotations = getId.getAnnotations();
            assertEquals(1, annotations.length);
            Annotation routingContext = annotations[0];
            assertEquals(RoutingContext.class, routingContext.annotationType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method getId() not found");
        }

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

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        cleanUp(sourcesOutputDir, compiledOutputDir);
    }


    private void testReturnTypeIdentityref(Class<?> clazz, String methodName, String returnTypeStr) throws Exception {
        Method method;
        java.lang.reflect.Type returnType;
        try {
            method = clazz.getMethod(methodName);
            assertEquals(java.lang.Class.class, method.getReturnType());
            returnType = method.getGenericReturnType();
            assertTrue(returnType instanceof ParameterizedType);
            ParameterizedType pt = (ParameterizedType) returnType;
            java.lang.reflect.Type[] parameters = pt.getActualTypeArguments();
            assertEquals(1, parameters.length);
            java.lang.reflect.Type parameter = parameters[0];
            assertTrue(parameter instanceof WildcardType);
            WildcardType wildcardType = (WildcardType) parameter;
            assertEquals("? extends " + returnTypeStr, wildcardType.toString());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method '" + methodName + "' not found");
        }
    }

    private void testReturnTypeInstanceIdentitifer(ClassLoader loader, Class<?> clazz, String methodName)
            throws Exception {
        Method method;
        Class<?> rawReturnType;
        java.lang.reflect.Type returnType;
        try {
            method = clazz.getMethod(methodName);
            rawReturnType = Class.forName("org.opendaylight.yangtools.yang.binding.InstanceIdentifier", true, loader);
            assertEquals(rawReturnType, method.getReturnType());
            returnType = method.getGenericReturnType();
            assertTrue(returnType instanceof ParameterizedType);
            ParameterizedType pt = (ParameterizedType) returnType;
            java.lang.reflect.Type[] parameters = pt.getActualTypeArguments();
            assertEquals(1, parameters.length);
            java.lang.reflect.Type parameter = parameters[0];
            assertTrue(parameter instanceof WildcardType);
            WildcardType wildcardType = (WildcardType) parameter;
            assertEquals("?", wildcardType.toString());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method '" + methodName + "' not found");
        }
    }

}
