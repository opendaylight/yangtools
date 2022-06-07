/*
 * Copyright (c) 2022 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.Test;

public class YT1436Test extends AbstractYangTest {
    @Test
    public void testUniqueLeafFromOtherSubmodule() {
        assertEffectiveModelDir("/bugs/YT1436/unique");
    }

    @Test
    public void testAugmentGroupingFromOtherSubmodule() {
        assertEffectiveModelDir("/bugs/YT1436/augment");
    }
}
