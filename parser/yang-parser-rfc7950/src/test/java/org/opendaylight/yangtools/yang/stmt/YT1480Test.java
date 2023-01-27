/*
 * Copyright (c) 2023 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.RepeatedTest;

class YT1480Test extends AbstractYangTest {
    // FIXME: YANGTOOLS-1475: do not repeat the test once we have a way to force predictable execution order
    @RepeatedTest(4)
    void testReplaceAndDeviateNode() {
        assertEffectiveModelDir("/bugs/YT1480");
    }
}
