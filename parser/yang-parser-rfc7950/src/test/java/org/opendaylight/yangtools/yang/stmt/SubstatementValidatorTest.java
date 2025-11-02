/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;

class SubstatementValidatorTest extends AbstractYangTest {
    @Test
    void noException() {
        assertEquals(3, assertEffectiveModelDir("/augment-test/augment-in-augment").getModules().size());
    }

    @Test
    void undesirableElementException() {
        assertThat(assertInvalidSubstatementExceptionDir("/substatement-validator/undesirable-element").getMessage())
            .startsWith("statement revision does not allow type substatements: 1 present [at ");
    }

    @Test
    void maximalElementCountException() {
        assertThat(assertInvalidSubstatementExceptionDir("/substatement-validator/maximal-element").getMessage())
            .startsWith("statement augment allows at most 1 description substatement: 2 present [at ");
    }

    @Test
    void missingElementException() {
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> TestUtils.loadModules("/substatement-validator/missing-element"));
        assertThat(ex.getMessage(), startsWith("Missing prefix substatement"));
    }

    @Test
    void bug6173Test() {
        assertEquals(1, assertEffectiveModelDir("/substatement-validator/empty-element").getModules().size());
    }

    @Test
    void bug4310test() {
        assertThat(assertExceptionDir("/substatement-validator/bug-4310", MissingSubstatementException.class)
            .getMessage()).startsWith("statement type requires at least 1 type substatement: none present [at ");
    }
}
