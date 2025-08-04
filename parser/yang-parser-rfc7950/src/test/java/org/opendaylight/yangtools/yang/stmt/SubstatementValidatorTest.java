/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

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
        assertInvalidSubstatementExceptionDir("/substatement-validator/undesirable-element",
            startsWith("TYPE is not valid for REVISION. Error in module undesirable "
                + "(QNameModule{ns=urn:opendaylight.undesirable, rev=2015-11-11}) [at "));
    }

    @Test
    void maximalElementCountException() {
        assertInvalidSubstatementExceptionDir("/substatement-validator/maximal-element",
            startsWith("Maximal count of DESCRIPTION for AUGMENT is 1, detected 2. Error in module baz "
                + "(QNameModule{ns=urn:opendaylight.baz, rev=2015-11-11}) [at "));
    }

    @Test
    void missingElementException() {
        // FIXME: should be MissingSubstatementException?
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> TestUtils.loadModules("/substatement-validator/missing-element"));
        final var actual = ex.getMessage();
        assertThat(actual, startsWith("No prefix statement in"));
    }

    @Test
    void bug6173Test() {
        assertEquals(1, assertEffectiveModelDir("/substatement-validator/empty-element").getModules().size());
    }

    @Test
    void bug4310test() {
        assertExceptionDir("/substatement-validator/bug-4310", MissingSubstatementException.class,
            startsWith("TYPE is missing TYPE. Minimal count is 1. Error in module bug4310 "
                + "(QNameModule{ns=urn:opendaylight.bug4310}) [at "));
    }
}
