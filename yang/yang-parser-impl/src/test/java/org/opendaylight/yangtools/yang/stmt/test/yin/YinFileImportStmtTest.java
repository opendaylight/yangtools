/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.retest.TestUtils;

public class YinFileImportStmtTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource
                ("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(10, modules.size());
    }

    @Test
    public void testImport() {
        Module testModule = TestUtils.findModule(modules, "ietf-netconf-monitoring");
        assertNotNull(testModule);

        Set<ModuleImport> imports = testModule.getImports();
        assertEquals(2, imports.size());

        Iterator<ModuleImport> importsIterator = imports.iterator();
        ModuleImport modImport = importsIterator.next();
        assertEquals("ietf-yang-types", modImport.getModuleName());
        assertEquals("yang", modImport.getPrefix());

        modImport = importsIterator.next();
        assertEquals("ietf-inet-types", modImport.getModuleName());
        assertEquals("inet", modImport.getPrefix());
    }
}
