/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.contract.Naming;

public class UnionWithMultipleIdentityrefsTest extends BaseCompilationTest {
    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("union-with-multiple-identityrefs");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("union-with-multiple-identityrefs");
        generateTestSources("/compilation/union-with-multiple-identityrefs", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> identOneClass = Class.forName(CompilationTestUtils.BASE_PKG
                + ".urn.opendaylight.yang.union.test.rev220428.IdentOne", true, loader);
        Class<?> identTwoClass = Class.forName(CompilationTestUtils.BASE_PKG
                + ".urn.opendaylight.yang.union.test.rev220428.IdentTwo", true, loader);
        Class<?> unionTypeClass = Class.forName(CompilationTestUtils.BASE_PKG
                + ".urn.opendaylight.yang.union.test.rev220428.UnionType", true, loader);

        Object identOneValue = identOneClass.getDeclaredField(Naming.VALUE_STATIC_FIELD_NAME).get(null);
        Object identTwoValue = identTwoClass.getDeclaredField(Naming.VALUE_STATIC_FIELD_NAME).get(null);

        Constructor<?> unionTypeIdentOneConstructor = CompilationTestUtils.assertContainsConstructor(unionTypeClass,
                identOneClass);
        Constructor<?> unionTypeIdentTwoConstructor = CompilationTestUtils.assertContainsConstructor(unionTypeClass,
                identTwoClass);
        Object unionTypeOne = unionTypeIdentOneConstructor.newInstance(identOneValue);
        Object unionTypeTwo = unionTypeIdentTwoConstructor.newInstance(identTwoValue);

        Method getIdentityOne = unionTypeClass.getDeclaredMethod("getIdentOne");
        Object actualIdentityOne = getIdentityOne.invoke(unionTypeOne);
        assertEquals(identOneValue, actualIdentityOne);

        Method getIdentityTwo = unionTypeClass.getDeclaredMethod("getIdentTwo");
        Object actualIdentityTwo = getIdentityTwo.invoke(unionTypeTwo);
        assertEquals(identTwoValue, actualIdentityTwo);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
