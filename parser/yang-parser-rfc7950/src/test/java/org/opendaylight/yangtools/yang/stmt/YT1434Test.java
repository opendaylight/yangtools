/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.Test;

public class YT1434Test extends AbstractYangTest {
    @Test
    public void testUniqueViaAugment() {
        assertEffectiveModel("/bugs/YT1434/foo.yang");
    }

    @Test
    public void testUniqueViaUses() {
        assertEffectiveModel("/bugs/YT1434/bar.yang");
    }

    @Test
    public void testUniqueViaSubmoduleUses() {
        assertEffectiveModel("/bugs/YT1434/main-module.yang", "/bugs/YT1434/submodule.yang",
                "/bugs/YT1434/test-bug.yang");
    }
}
