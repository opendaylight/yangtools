/*
 * Copyright (c) 2022 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.Test;

class YT1408Test extends AbstractYangTest {
    @Test
    void testChoiceCaseDeviateCase() {
        assertEffectiveModelDir("/bugs/YT1408/choice-case-deviate-case");
    }

    @Test
    void testChoiceDeviateCase() {
        assertEffectiveModelDir("/bugs/YT1408/choice-deviate-case");
    }

    @Test
    void testAugmentChoiceCaseDeviateCase() {
        assertEffectiveModelDir("/bugs/YT1408/aug-choice-case-deviate-case");
    }

    @Test
    void testAugmentChoiceDeviateCase() {
        assertEffectiveModelDir("/bugs/YT1408/aug-choice-deviate-case");
    }
}
