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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
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
        testFilesCount(parent, 6);

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
        testFilesCount(parent, 7);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> keyArgsClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.KeyArgs", true, loader);
        Class<?> linksClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.Links", true, loader);
        Class<?> linksKeyClass = Class.forName(BASE_PKG + ".urn.opendaylight.test.rev131008.LinksKey", true, loader);

        // Test generated 'grouping key-args'
        try {
            assertTrue(keyArgsClass.isInterface());
            assertEquals(3, keyArgsClass.getDeclaredMethods().length);

            Method getId = keyArgsClass.getMethod("getId");
            assertEquals(Byte.class, getId.getReturnType());
            Method getName = keyArgsClass.getMethod("getName");
            assertEquals(String.class, getName.getReturnType());
            Method getSize = keyArgsClass.getMethod("getSize");
            assertEquals(Integer.class, getSize.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Required method not found in " + keyArgsClass, e);
        }

        // test generated 'list links'
        assertTrue(linksClass.isInterface());
        // FIXME: anyxml
        assertEquals(5, linksClass.getDeclaredMethods().length);
        testImplementsIfc(linksClass, keyArgsClass);

        // Test list key constructor arguments ordering
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

        // Test if all sources were generated from 'module foo'
        File parent = new File(sourcesOutputDir, NS_FOO);
        assertTrue(new File(parent, "Object.java").exists());
        assertTrue(new File(parent, "OpenObject.java").exists());
        assertTrue(new File(parent, "ExplicitRouteObject.java").exists());
        assertTrue(new File(parent, "PathKeySubobject.java").exists());
        testFilesCount(parent, 7);

        parent = new File(parent, "object");
        assertTrue(new File(parent, "Nodes.java").exists());
        assertTrue(new File(parent, "NodesBuilder.java").exists());
        testFilesCount(parent, 2);

        parent = new File(sourcesOutputDir, NS_FOO + FS + "open");
        testFilesCount(parent, 1);

        parent = new File(parent, "object");
        assertTrue(new File(parent, "Nodes1.java").exists());
        assertTrue(new File(parent, "Nodes1Builder.java").exists());
        testFilesCount(parent, 3);

        parent = new File(parent, "nodes");
        assertTrue(new File(parent, "Links.java").exists());
        assertTrue(new File(parent, "LinksBuilder.java").exists());
        testFilesCount(parent, 2);

        parent = new File(sourcesOutputDir, NS_FOO + FS + "explicit");
        testFilesCount(parent, 1);
        parent = new File(parent, "route");
        testFilesCount(parent, 1);
        parent = new File(parent, "object");
        assertTrue(new File(parent, "Subobjects.java").exists());
        assertTrue(new File(parent, "SubobjectsBuilder.java").exists());
        testFilesCount(parent, 3);

        parent = new File(parent, "subobjects");
        testFilesCount(parent, 1);
        parent = new File(parent, "subobject");
        testFilesCount(parent, 1);
        parent = new File(parent, "type");
        assertTrue(new File(parent, "PathKey.java").exists());
        assertTrue(new File(parent, "PathKeyBuilder.java").exists());
        testFilesCount(parent, 3);

        parent = new File(parent, "path");
        testFilesCount(parent, 1);
        parent = new File(parent, "key");
        assertTrue(new File(parent, "PathKey.java").exists());
        assertTrue(new File(parent, "PathKeyBuilder.java").exists());
        testFilesCount(parent, 2);

        // Test if all sources were generated from 'module bar'
        parent = new File(sourcesOutputDir, NS_BAR);
        assertTrue(new File(parent, "BasicExplicitRouteSubobjects.java").exists());
        assertTrue(new File(parent, "ExplicitRouteSubobjects.java").exists());
        testFilesCount(parent, 3);

        parent = new File(parent, "basic");
        testFilesCount(parent, 1);
        parent = new File(parent, "explicit");
        testFilesCount(parent, 1);
        parent = new File(parent, "route");
        testFilesCount(parent, 1);

        parent = new File(parent, "subobjects");
        testFilesCount(parent, 2);
        assertTrue(new File(parent, "SubobjectType.java").exists());

        parent = new File(parent, "subobject");
        testFilesCount(parent, 1);

        parent = new File(parent, "type");
        assertTrue(new File(parent, "IpPrefix.java").exists());
        assertTrue(new File(parent, "IpPrefixBuilder.java").exists());
        assertTrue(new File(parent, "Label.java").exists());
        assertTrue(new File(parent, "LabelBuilder.java").exists());
        testFilesCount(parent, 4);

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
        testFilesCount(parent, 6);

        parent = new File(sourcesOutputDir, NS_FOO + FS + "path");
        testFilesCount(parent, 1);
        parent = new File(parent, "attributes");
        testFilesCount(parent, 2);
        File origin = new File(parent, "Origin.java");
        File originBuilder = new File(parent, "OriginBuilder.java");
        assertTrue(origin.exists());
        assertTrue(originBuilder.exists());

        parent = new File(sourcesOutputDir, NS_FOO + FS + "update");
        testFilesCount(parent, 2);
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
        testFilesCount(parent, 5);

        parent = new File(sourcesOutputDir, NS_BAR + FS + "destination");
        testFilesCount(parent, 2);
        File destinationType = new File(parent, "DestinationType.java");
        assertTrue(destinationType.exists());

        parent = new File(parent, "destination");
        testFilesCount(parent, 1);
        parent = new File(parent, "type");
        testFilesCount(parent, 2);
        File destinationIpv4 = new File(parent, "DestinationIp.java");
        File destinationIpv4Builder = new File(parent, "DestinationIpBuilder.java");
        assertTrue(destinationIpv4.exists());
        assertTrue(destinationIpv4Builder.exists());

        parent = new File(sourcesOutputDir, NS_BAR + FS + "update");
        testFilesCount(parent, 1);
        parent = new File(parent, "path");
        testFilesCount(parent, 1);
        parent = new File(parent, "attributes");
        File mpUnreachNlri = new File(parent, "MpUnreachNlri.java");
        File mpUnreachNlriBuilder = new File(parent, "MpUnreachNlriBuilder.java");
        assertTrue(mpUnreachNlri.exists());
        assertTrue(mpUnreachNlriBuilder.exists());
        testFilesCount(parent, 3);

        parent = new File(parent, "mp");
        testFilesCount(parent, 1);
        parent = new File(parent, "unreach");
        testFilesCount(parent, 1);
        parent = new File(parent, "nlri");
        File withdrawnRoutes = new File(parent, "WithdrawnRoutes.java");
        File withdrawnRoutesBuilder = new File(parent, "WithdrawnRoutesBuilder.java");
        assertTrue(withdrawnRoutes.exists());
        assertTrue(withdrawnRoutesBuilder.exists());
        testFilesCount(parent, 2);

        // Test if all sources were generated from 'module baz'
        parent = new File(sourcesOutputDir, NS_BAZ);
        testFilesCount(parent, 2);
        File linkstateDestination = new File(parent, "LinkstateDestination.java");
        assertTrue(linkstateDestination.exists());

        parent = new File(sourcesOutputDir, NS_BAZ + FS + "update");
        testFilesCount(parent, 1);
        parent = new File(parent, "path");
        testFilesCount(parent, 1);
        parent = new File(parent, "attributes");
        testFilesCount(parent, 1);
        parent = new File(parent, "mp");
        testFilesCount(parent, 1);
        parent = new File(parent, "unreach");
        testFilesCount(parent, 1);
        parent = new File(parent, "nlri");
        testFilesCount(parent, 1);
        parent = new File(parent, "withdrawn");
        testFilesCount(parent, 1);
        parent = new File(parent, "routes");
        testFilesCount(parent, 1);
        parent = new File(parent, "destination");
        testFilesCount(parent, 1);
        parent = new File(parent, "type");
        File destinationLinkstate = new File(parent, "DestinationLinkstate.java");
        File destinationLinkstateBuilder = new File(parent, "DestinationLinkstateBuilder.java");
        assertTrue(destinationLinkstate.exists());
        assertTrue(destinationLinkstateBuilder.exists());
        testFilesCount(parent, 3);
        parent = new File(parent, "destination");
        testFilesCount(parent, 1);
        parent = new File(parent, "linkstate");
        File links = new File(parent, "Links.java");
        File linksBuilder = new File(parent, "LinksBuilder.java");
        assertTrue(links.exists());
        assertTrue(linksBuilder.exists());
        testFilesCount(parent, 3);
        parent = new File(parent, "links");
        File source = new File(parent, "Source.java");
        File sourceBuilder = new File(parent, "SourceBuilder.java");
        assertTrue(source.exists());
        assertTrue(sourceBuilder.exists());
        testFilesCount(parent, 3);
        parent = new File(parent, "source");
        File address = new File(parent, "Address.java");
        File addressBuilder = new File(parent, "AddressBuilder.java");
        assertTrue(address.exists());
        assertTrue(addressBuilder.exists());
        testFilesCount(parent, 2);

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
        testFilesCount(parent, 4);
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
        testReturnType(nodesClass, "getIdBinary", b.getClass());
        testReturnType(nodesClass, "getIdBits", pkg + ".Nodes$IdBits", loader);
        testReturnType(nodesClass, "isIdBoolean", "java.lang.Boolean", loader);
        testReturnType(nodesClass, "getIdDecimal64", "java.math.BigDecimal", loader);
        testReturnType(nodesClass, "isIdEmpty", "java.lang.Boolean", loader);
        testReturnType(nodesClass, "getIdEnumeration", pkg + ".Nodes$IdEnumeration", loader);
        testReturnTypeIdentityref(nodesClass, "getIdIdentityref", pkg + ".Alg");
        testReturnTypeInstanceIdentitifer(loader, nodesClass, "getIdInstanceIdentifier");
        testReturnType(nodesClass, "getId8", "java.lang.Byte", loader);
        testReturnType(nodesClass, "getId16", "java.lang.Short", loader);
        testReturnType(nodesClass, "getId32", "java.lang.Integer", loader);
        testReturnType(nodesClass, "getId64", "java.lang.Long", loader);
        testReturnType(nodesClass, "getIdLeafref", "java.lang.Long", loader);
        testReturnType(nodesClass, "getIdString", "java.lang.String", loader);
        testReturnType(nodesClass, "getIdU8", "java.lang.Short", loader);
        testReturnType(nodesClass, "getIdU16", "java.lang.Integer", loader);
        testReturnType(nodesClass, "getIdU32", "java.lang.Long", loader);
        testReturnType(nodesClass, "getIdU64", "java.math.BigInteger", loader);
        testReturnType(nodesClass, "getIdUnion", pkg + ".Nodes$IdUnion", loader);

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
        testFilesCount(fooParent, 3);
        assertTrue(new File(fooParent, "FooData.java").exists());
        assertTrue(new File(fooParent, "Nodes.java").exists());
        assertTrue(new File(fooParent, "NodesBuilder.java").exists());

        File barParent = new File(sourcesOutputDir, NS_BAR);
        testFilesCount(barParent, 1);
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

    private void testReturnType(Class<?> clazz, String methodName, Class<?> returnType) throws Exception {
        Method method;
        try {
            method = clazz.getMethod(methodName);
            assertEquals(returnType, method.getReturnType());
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Method '" + methodName + "' not found");
        }
    }

    private void testReturnType(Class<?> clazz, String methodName, String returnTypeStr, ClassLoader loader)
            throws Exception {
        Class<?> returnType;
        try {
            returnType = Class.forName(returnTypeStr, true, loader);
            testReturnType(clazz, methodName, returnType);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Return type of method '" + methodName + "' not found");
        }
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
