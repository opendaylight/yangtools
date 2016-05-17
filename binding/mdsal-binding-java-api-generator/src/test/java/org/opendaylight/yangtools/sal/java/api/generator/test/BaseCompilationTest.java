/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;

public abstract class BaseCompilationTest {

    protected BindingGenerator bindingGenerator;

    @BeforeClass
    public static void createTestDirs() {
        if (CompilationTestUtils.TEST_DIR.exists()) {
            CompilationTestUtils.deleteTestDir(CompilationTestUtils.TEST_DIR);
        }
        assertTrue(CompilationTestUtils.GENERATOR_OUTPUT_DIR.mkdirs());
        assertTrue(CompilationTestUtils.COMPILER_OUTPUT_DIR.mkdirs());
    }

    @Before
    public void init() {
        bindingGenerator = new BindingGeneratorImpl(true);
    }

}
