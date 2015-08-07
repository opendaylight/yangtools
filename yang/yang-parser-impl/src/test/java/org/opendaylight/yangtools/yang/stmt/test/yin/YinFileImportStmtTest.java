/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test.yin;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.text.ParseException;
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
        modules = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, modules.size());

    }

    @Test
    public void testImport() throws ParseException {
        Module testModule = TestUtils.findModule(modules, "ietf-netconf-monitoring");
        assertNotNull(testModule);

        Set<ModuleImport> imports = testModule.getImports();
        assertEquals(2, imports.size());

        Iterator<ModuleImport> importsIterator = imports.iterator();
        ModuleImport moduleImport = importsIterator.next();

        assertThat(moduleImport.getModuleName(), anyOf(is("ietf-yang-types"), is("ietf-inet-types")));
        assertThat(moduleImport.getPrefix(), anyOf(is("yang"), is("inet")));

        moduleImport = importsIterator.next();
        assertThat(moduleImport.getModuleName(), anyOf(is("ietf-yang-types"), is("ietf-inet-types")));
        assertThat(moduleImport.getPrefix(), anyOf(is("yang"), is("inet")));
    }
}
