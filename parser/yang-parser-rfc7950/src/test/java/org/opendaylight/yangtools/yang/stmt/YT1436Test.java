/*
 * Copyright (c) 2022 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.Test;

class YT1436Test extends AbstractYangTest {
    @Test
    void testUniqueLeafFromOtherSubmodule() {
        assertEffectiveModelDir("/bugs/YT1436/unique");
    }

    @Test
    void testAugmentGroupingFromOtherSubmodule() {
        assertEffectiveModelDir("/bugs/YT1436/augment");
    }
}
