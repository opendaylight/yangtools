/*
 * Copyright (c) 2016 Intel corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Test;

/**
 * union constructor with indentityref
 * previously identityref was ignored so that there is no constructor
 * for indentityref
 *
 */
public class UnionWithIdentityrefTest extends BaseCompilationTest {

    @Test
    public void test() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("union-with-identityref");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("union-with-identityref");
        generateTestSources("/compilation/union-with-identityref", sourcesOutputDir);

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });
        Class<?> identBaseClass = Class.forName(CompilationTestUtils.BASE_PKG + ".urn.opendaylight.yang.union.test.rev160509.IdentBase", true, loader);
        Class<?> identOneClass = Class.forName(CompilationTestUtils.BASE_PKG + ".urn.opendaylight.yang.union.test.rev160509.IdentOne", true, loader);
        Class<?> unionTypeClass = Class.forName(CompilationTestUtils.BASE_PKG + ".urn.opendaylight.yang.union.test.rev160509.UnionType", true, loader);

        // test UnionType with IdentOne argument
        Constructor<?> unionTypeIdentBaseConstructor = CompilationTestUtils.assertContainsConstructor(unionTypeClass, Class.class);
        Object unionType = unionTypeIdentBaseConstructor.newInstance(identOneClass);

        Method getUint8 = unionTypeClass.getDeclaredMethod("getUint8");
        Object actualUint8 = getUint8.invoke(unionType);
        assertNull(actualUint8);

        Method getIdentityref = unionTypeClass.getDeclaredMethod("getIdentityref");
        Object actualIdentityref = getIdentityref.invoke(unionType);
        assertEquals(identOneClass, actualIdentityref);

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
