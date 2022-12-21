/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

class Bug9005Test extends AbstractYangTest {
    @Test
    void test() {
        final var foo = assertEffectiveModelDir("/bugs/bug9005").findModule("foo", Revision.of("2017-07-07"))
            .orElseThrow();

        final var imports = foo.getImports();
        assertEquals(1, imports.size());
        final var imp1 = imports.iterator().next();
        assertEquals(Unqualified.of("bar-2"), imp1.getModuleName());
        assertEquals("bar", imp1.getPrefix());
        assertEquals(Revision.ofNullable("2000-01-02"), imp1.getRevision());

        final var submodules = foo.getSubmodules();
        assertEquals(1, submodules.size());
        final var submodule = submodules.iterator().next();
        final var subImports = submodule.getImports();

        assertEquals(1, subImports.size());
        final var subImp1 = subImports.iterator().next();
        assertEquals(Unqualified.of("bar-1"), subImp1.getModuleName());
        assertEquals("bar", subImp1.getPrefix());
        assertEquals(Revision.ofNullable("2000-01-01"), subImp1.getRevision());
    }
}
