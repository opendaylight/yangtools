/*
 * Copyright (c) 2023 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;

class YT1474Test extends AbstractYangTest {
    private static final List<String> FOO_BAR = List.of("/bugs/YT1474/foo.yang", "/bugs/YT1474/bar.yang");
    private static final List<String> BAR_FOO = List.of("/bugs/YT1474/bar.yang", "/bugs/YT1474/foo.yang");
    private static final Set<QName> NO_FEATURES = Set.of();

    @Test
    void testEmptyGroupingFooBar1() {
        assertEffectiveModel(FOO_BAR, NO_FEATURES);
    }

    @Test
    void testEmptyGroupingFooBar2() {
        assertEffectiveModel(FOO_BAR, NO_FEATURES);
    }

    @Test
    void testEmptyGroupingBarFoo1() {
        assertEffectiveModel(BAR_FOO, NO_FEATURES);
    }

    @Test
    void testEmptyGroupingBarFoo2() {
        assertEffectiveModel(BAR_FOO, NO_FEATURES);
    }
}
