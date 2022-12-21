/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;

class YinFileImportStmtTest extends AbstractYinModulesTest {
    @Test
    void testImport() {
        Module testModule = context.findModules("ietf-netconf-monitoring").iterator().next();
        assertNotNull(testModule);

        Collection<? extends ModuleImport> imports = testModule.getImports();
        assertEquals(2, imports.size());

        Iterator<? extends ModuleImport> importsIterator = imports.iterator();
        ModuleImport moduleImport = importsIterator.next();

        final var nameMatch = anyOf(is(Unqualified.of("ietf-yang-types")),
            is(Unqualified.of("ietf-inet-types")));

        assertThat(moduleImport.getModuleName(), nameMatch);
        assertThat(moduleImport.getPrefix(), anyOf(is("yang"), is("inet")));

        moduleImport = importsIterator.next();
        assertThat(moduleImport.getModuleName(), nameMatch);
        assertThat(moduleImport.getPrefix(), anyOf(is("yang"), is("inet")));
    }
}
