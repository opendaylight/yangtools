/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;

public class SubstatementValidatorTest extends AbstractYangTest {
    @Test
    public void noException() throws Exception {
        assertEquals(3, assertEffectiveModelDir("/augment-test/augment-in-augment").getModules().size());
    }

    @Test
    public void undesirableElementException() throws Exception {
        assertInvalidSubstatementExceptionDir("/substatement-validator/undesirable-element",
            startsWith("TYPE is not valid for REVISION. Error in module undesirable "
                + "(QNameModule{ns=urn:opendaylight.undesirable, rev=2015-11-11}) [at "));
    }

    @Test
    public void maximalElementCountException() throws Exception {
        assertInvalidSubstatementExceptionDir("/substatement-validator/maximal-element",
            startsWith("Maximal count of DESCRIPTION for AUGMENT is 1, detected 2. Error in module baz "
                + "(QNameModule{ns=urn:opendaylight.baz, rev=2015-11-11}) [at "));
    }

    @Test
    public void missingElementException() {
        // FIXME: should be MissingSubstatementException?
        assertSourceExceptionDir("/substatement-validator/missing-element",
            startsWith("Missing prefix statement [at "));
    }

    @Test
    public void bug6173Test() throws Exception {
        assertEquals(1, assertEffectiveModelDir("/substatement-validator/empty-element").getModules().size());
    }

    @Test
    public void bug4310test() throws Exception {
        assertExceptionDir("/substatement-validator/bug-4310", MissingSubstatementException.class,
            startsWith("TYPE is missing TYPE. Minimal count is 1. Error in module bug4310 "
                + "(QNameModule{ns=urn:opendaylight.bug4310}) [at "));
    }
}
