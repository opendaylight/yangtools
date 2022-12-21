/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.Test;

class YT1434Test extends AbstractYangTest {
    @Test
    void testUniqueViaAugment() {
        assertEffectiveModel("/bugs/YT1434/foo.yang");
    }

    @Test
    void testUniqueViaUses() {
        assertEffectiveModel("/bugs/YT1434/bar.yang");
    }

    @Test
    void testUniqueViaSubmoduleUses() {
        assertEffectiveModel("/bugs/YT1434/main-module.yang", "/bugs/YT1434/submodule.yang",
            "/bugs/YT1434/test-bug.yang");
    }
}
