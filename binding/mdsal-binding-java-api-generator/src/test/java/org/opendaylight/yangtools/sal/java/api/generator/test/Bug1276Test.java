/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Previous construction of union constructor
 *
 * <code>
 * public IpAddress(Arg1 _arg1) {
 *     super();
 *     this._arg1 = _arg1;
 *     this._arg2 = null;
 *     this._value = null;
 * }
 * </code>
 *
 * was incorrect and setting
 *
 * <code>this._value == null</code>
 *
 * was replaced with setting _value to correct value, for example:
 *
 * <code>this._value = arg1.getValue()</code> or
 * <code>this._value = _arg1.getValue().toString().toCharArray()</code>
 *
 */
public class Bug1276Test extends BaseCompilationTest {

    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = new File(CompilationTestUtils.GENERATOR_OUTPUT_PATH + CompilationTestUtils.FS + "bug1276");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(CompilationTestUtils.COMPILER_OUTPUT_PATH + CompilationTestUtils.FS + "bug1276");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        generateTestSources("/compilation/bug1276", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> ipAddressClass = Class.forName(CompilationTestUtils.BASE_PKG + ".test.yang.union.rev140715.IpAddress", true, loader);
        Class<?> ipv4AddressClass = Class.forName(CompilationTestUtils.BASE_PKG + ".test.yang.union.rev140715.Ipv4Address", true, loader);
        Class<?> hostClass = Class.forName(CompilationTestUtils.BASE_PKG + ".test.yang.union.rev140715.Host", true, loader);

        Constructor<?> ipAddressConstructor = CompilationTestUtils.assertContainsConstructor(ipAddressClass, ipv4AddressClass);
        Constructor<?> ipv4addressConstructor = CompilationTestUtils.assertContainsConstructor(ipv4AddressClass, String.class);
        Constructor<?> hostConstructor = CompilationTestUtils.assertContainsConstructor(hostClass, ipAddressClass);

        // test IpAddress with Ipv4Address argument
        Object ipv4address = ipv4addressConstructor.newInstance("192.168.0.1");
        Object ipAddress = ipAddressConstructor.newInstance(ipv4address);
        Method getValue = ipAddressClass.getDeclaredMethod("getValue");
        char[] expected = "192.168.0.1".toCharArray();
        Object actual = getValue.invoke(ipAddress);
        assertTrue(actual instanceof char[]);
        assertTrue(Arrays.equals(expected, (char[]) actual));

        // test Host with IpAddress argument
        Object host = hostConstructor.newInstance(ipAddress);
        getValue = hostClass.getDeclaredMethod("getValue");
        actual = getValue.invoke(host);
        assertTrue(actual instanceof char[]);
        assertTrue(Arrays.equals(expected, (char[]) actual));

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private void generateTestSources(final String resourceDirPath, final File sourcesOutputDir) throws Exception {
        final List<File> sourceFiles = CompilationTestUtils.getSourceFiles(resourceDirPath);
        final SchemaContext context = RetestUtils.parseYangSources(sourceFiles);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(ImmutableSet.copyOf(types));
        generator.generateToFile(sourcesOutputDir);
    }

}
