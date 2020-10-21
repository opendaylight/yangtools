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

import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

public class YangParserIdentityTest {

    // base identity name equals identity name
    @Test(expected = SomeModifiersUnresolvedException.class)
    public void testParsingIdentityTestModule() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/identity/identitytest.yang");
        } catch (SomeModifiersUnresolvedException e) {
            StmtTestUtils.log(e, "      ");
            throw e;
        }
    }

    // same module prefixed base identity name equals identity name
    @Test(expected = SomeModifiersUnresolvedException.class)
    public void testParsingPrefixIdentityTestModule() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/identity/prefixidentitytest.yang");
        } catch (SomeModifiersUnresolvedException e) {
            StmtTestUtils.log(e, "      ");
            throw e;
        }
    }

    // imported module prefixed base identity name equals identity name, but
    // prefix differs
    @Test
    public void testParsingImportPrefixIdentityTestModule() throws Exception {
        Module module = TestUtils.findModule(StmtTestUtils.parseYangSources("/identity/import"),
            "prefiximportidentitytest").get();
        Collection<? extends ModuleImport> imports = module.getImports();
        assertEquals(imports.size(), 1);
        ModuleImport dummy = TestUtils.findImport(imports, "dummy");
        assertNotEquals(dummy.getPrefix(), module.getPrefix());
    }
}
