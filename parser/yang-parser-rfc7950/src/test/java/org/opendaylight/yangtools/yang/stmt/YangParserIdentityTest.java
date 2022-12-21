/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;

class YangParserIdentityTest extends AbstractYangTest {

    // base identity name equals identity name
    @Test
    void testParsingIdentityTestModule() {
        assertInferenceException(startsWith("Unable to resolve identity (urn:test.identitytest?revision="
            + "2014-09-17)test and base identity"), "/identity/identitytest.yang");
    }

    // same module prefixed base identity name equals identity name
    @Test
    void testParsingPrefixIdentityTestModule() {
        assertInferenceException(startsWith("Unable to resolve identity (urn:test.prefixidentitytest?revision="
            + "2014-09-24)prefixtest and base identity"), "/identity/prefixidentitytest.yang");
    }

    // imported module prefixed base identity name equals identity name, but
    // prefix differs
    @Test
    void testParsingImportPrefixIdentityTestModule() {
        final var module = assertEffectiveModelDir("/identity/import").findModules("prefiximportidentitytest")
            .iterator().next();
        final var imports = module.getImports();
        assertEquals(1, imports.size());
        ModuleImport dummy = TestUtils.findImport(imports, "dummy");
        assertNotEquals(dummy.getPrefix(), module.getPrefix());
    }
}
