/*
 * Copyright (c) 2022 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.Test;

class YT1445Test extends AbstractYangTest {
    @Test
    void testUniqueTopLevelGrouping() {
        assertEffectiveModel("/bugs/YT1445/top-level-grouping/foo.yang");
    }

    @Test
    void testUniqueInListGrouping() {
        assertEffectiveModel("/bugs/YT1445/list-grouping/foo.yang");
    }

    @Test
    void testUniqueInGroupingUsedByGrouping() {
        assertEffectiveModel("/bugs/YT1445/nested-grouping/foo.yang");
    }
}
