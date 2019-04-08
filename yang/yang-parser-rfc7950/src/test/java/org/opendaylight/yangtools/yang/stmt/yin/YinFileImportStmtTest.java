/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

public class YinFileImportStmtTest extends AbstractYinModulesTest {

    @Test
    public void testImport() throws ParseException {
        Module testModule = TestUtils.findModule(context, "ietf-netconf-monitoring").get();
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
