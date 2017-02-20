/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;

public class YangParserIdentityTest {

    // base identity name equals identity name
    @Test(expected = SomeModifiersUnresolvedException.class)
    public void testParsingIdentityTestModule() throws URISyntaxException, ReactorException, FileNotFoundException {
        File yang = new File(getClass().getResource("/identity/identitytest.yang").toURI());
        InputStream stream = new NamedFileInputStream(yang, yang.getPath());
        try {
            TestUtils.loadModule(stream);
        } catch (SomeModifiersUnresolvedException e) {
            StmtTestUtils.log(e, "      ");
            throw e;
        }
    }

    // same module prefixed base identity name equals identity name
    @Test(expected = SomeModifiersUnresolvedException.class)
    public void testParsingPrefixIdentityTestModule() throws URISyntaxException,
            ReactorException, FileNotFoundException {
        File yang = new File(getClass().getResource("/identity/prefixidentitytest.yang").toURI());
        InputStream stream = new NamedFileInputStream(yang, yang.getPath());
        try {
            TestUtils.loadModule(stream);
        } catch (SomeModifiersUnresolvedException e) {
            StmtTestUtils.log(e, "      ");
            throw e;
        }
    }

    // imported module prefixed base identity name equals identity name, but
    // prefix differs
    @Test
    public void testParsingImportPrefixIdentityTestModule() throws Exception {
        Set<Module> modules = TestUtils.loadModules(getClass().getResource("/identity/import").toURI());
        Module module = TestUtils.findModule(modules, "prefiximportidentitytest");
        Set<ModuleImport> imports = module.getImports();
        assertEquals(imports.size(), 1);
        ModuleImport dummy = TestUtils.findImport(imports, "dummy");
        assertNotEquals(dummy.getPrefix(), module.getPrefix());
    }
}
