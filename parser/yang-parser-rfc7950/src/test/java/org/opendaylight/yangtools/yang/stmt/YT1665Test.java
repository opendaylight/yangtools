/*
 * Copyright (c) 2025 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsEffectiveStatement;

class YT1665Test extends AbstractYangTest {
    @Test
    void testNegativeMinElements() {
        assertSourceExceptionMessage("/bugs/YT1665/foo.yang")
            .startsWith("Invalid min-elements argument \"-1\" at offset 0: '-' is not a valid non-zero-digit [at ");
    }

    @Test
    void testLeadingZeroMinElements() {
        assertSourceExceptionMessage("/bugs/YT1665/bar.yang")
            .startsWith("Invalid min-elements argument \"01\" at offset 0: '0' is not a valid non-zero-digit [at ");
    }

    @Test
    void testMalformedMinElements() {
        assertSourceExceptionMessage("/bugs/YT1665/baz.yang")
            .startsWith("Invalid min-elements argument \"1a\" at offset 1: 'a' is not a valid DIGIT [at ");
    }

    @Test
    void testOutOfRangeMinElements() {
        assertEquals(MinElementsArgument.of(123456789012345L),
            assertEffectiveModel("/bugs/YT1665/xyzzy.yang")
            .findModuleStatement(QNameModule.of("xyzzy"))
            .orElseThrow()
            .findFirstEffectiveSubstatement(ListEffectiveStatement.class)
            .orElseThrow()
            .findFirstEffectiveSubstatementArgument(MinElementsEffectiveStatement.class)
            .orElseThrow());
    }
}
