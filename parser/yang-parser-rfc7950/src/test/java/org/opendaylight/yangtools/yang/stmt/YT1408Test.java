/*
 * Copyright (c) 2022 Verizon and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.Test;

public class YT1408Test extends AbstractYangTest {
    @Test
    public void testChoiceCaseDeviateCase() throws AssertionError {
        assertEffectiveModelDir("/bugs/YT1408/choice-case-deviate-case");
    }

    @Test
    public void testChoiceDeviateCase() throws AssertionError {
        assertEffectiveModelDir("/bugs/YT1408/choice-deviate-case");
    }

    @Test
    public void testAugmentChoiceCaseDeviateCase() throws AssertionError {
        assertEffectiveModelDir("/bugs/YT1408/aug-choice-case-deviate-case");
    }

    @Test
    public void testAugmentChoiceDeviateCase() throws AssertionError {
        assertEffectiveModelDir("/bugs/YT1408/aug-choice-deviate-case");
    }
}
