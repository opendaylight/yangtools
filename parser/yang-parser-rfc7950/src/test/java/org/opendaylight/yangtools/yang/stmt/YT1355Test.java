/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

class YT1355Test extends AbstractYangTest {
    @Test
    void multipleSubmodulesCanBeResolved() {
        final var modules = assertEffectiveModel(
            "/bugs/YT1355/foo/foo.yang", "/bugs/YT1355/foo/xyzzy.yang",
            "/bugs/YT1355/bar/bar.yang", "/bugs/YT1355/bar/xyzzy.yang").getModuleStatements().values();
        assertEquals(2, modules.size());

        for (var module : modules) {
            final var submodules = module.submodules();
            assertEquals(1, submodules.size());
            final var submodule = submodules.iterator().next();
            assertEquals(Unqualified.of("xyzzy"), submodule.argument());
            final var belongsTo = submodule.belongsTo();
            assertEquals(module.argument(), belongsTo.argument());
            assertEquals(module.prefixArgument(), belongsTo.prefixArgument());
        }
    }
}
