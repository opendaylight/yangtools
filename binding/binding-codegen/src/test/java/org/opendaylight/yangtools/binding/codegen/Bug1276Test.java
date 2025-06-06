/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.jupiter.api.Test;

/**
 * Test for BG-1276. Previous construction of union constructor
 *
 * <p><code>
 * public IpAddress(Arg1 _arg1) {
 *     super();
 *     this._arg1 = _arg1;
 *     this._arg2 = null;
 *     this._value = null;
 * }
 * </code>
 *
 * <p>was incorrect and setting
 *
 * <p><code>this._value == null</code>
 *
 * <p>was replaced with setting _value to correct value, for example:
 *
 * <p><code>this._value = arg1.getValue()</code> or
 * <code>this._value = _arg1.getValue().toString().toCharArray()</code>
 */
class Bug1276Test extends BaseCompilationTest {
    @Test
    void test() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("bug1276");
        final var compiledOutputDir = CompilationTestUtils.compilerOutput("bug1276");
        generateTestSources("/compilation/bug1276", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toUri().toURL() });
        Class<?> ipAddressClass = Class.forName(CompilationTestUtils.BASE_PKG + ".test.yang.union.rev140715.IpAddress",
            true, loader);
        Class<?> ipv4AddressClass = Class.forName(CompilationTestUtils.BASE_PKG
            + ".test.yang.union.rev140715.Ipv4Address", true, loader);
        Class<?> hostClass = Class.forName(CompilationTestUtils.BASE_PKG + ".test.yang.union.rev140715.Host", true,
            loader);

        Constructor<?> ipAddressConstructor = CompilationTestUtils.assertContainsConstructor(ipAddressClass,
            ipv4AddressClass);
        Constructor<?> ipv4addressConstructor = CompilationTestUtils.assertContainsConstructor(ipv4AddressClass,
            String.class);
        Constructor<?> hostConstructor = CompilationTestUtils.assertContainsConstructor(hostClass, ipAddressClass);

        // test IpAddress with Ipv4Address argument
        Object ipv4address = ipv4addressConstructor.newInstance("192.168.0.1");
        Object ipAddress = ipAddressConstructor.newInstance(ipv4address);

        // test Host with IpAddress argument
        assertNotNull(hostConstructor.newInstance(ipAddress));

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
