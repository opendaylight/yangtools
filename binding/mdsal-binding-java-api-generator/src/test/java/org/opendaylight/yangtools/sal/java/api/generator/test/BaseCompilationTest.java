/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public abstract class BaseCompilationTest {

    protected YangParserImpl parser;
    protected BindingGenerator bindingGenerator;

    @BeforeClass
    public static void createTestDirs() {
        if (TEST_DIR.exists()) {
            deleteTestDir(TEST_DIR);
        }
        assertTrue(GENERATOR_OUTPUT_DIR.mkdirs());
        assertTrue(COMPILER_OUTPUT_DIR.mkdirs());
    }

    @Before
    public void init() {
        parser = new YangParserImpl();
        bindingGenerator = new BindingGeneratorImpl();
    }

}
