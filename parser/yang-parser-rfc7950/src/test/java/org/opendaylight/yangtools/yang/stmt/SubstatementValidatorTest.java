/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class SubstatementValidatorTest {
    @Test
    public void noException() throws Exception {
        assertEquals(3, TestUtils.loadModules("/augment-test/augment-in-augment").getModules().size());
    }

    @Test
    public void undesirableElementException() throws Exception {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModules("/substatement-validator/undesirable-element"));
        final var cause = ex.getCause();
        assertThat(cause, instanceOf(InvalidSubstatementException.class));
        assertThat(cause.getMessage(), startsWith("TYPE is not valid for REVISION. Error in module undesirable "
            + "(QNameModule{ns=urn:opendaylight.undesirable, rev=2015-11-11}) [at "));
    }

    @Test
    public void maximalElementCountException() throws Exception {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModules("/substatement-validator/maximal-element"));
        final var cause = ex.getCause();
        assertThat(cause, instanceOf(InvalidSubstatementException.class));
        assertThat(cause.getMessage(), startsWith("Maximal count of DESCRIPTION for AUGMENT is 1, detected 2. Error in "
            + "module baz (QNameModule{ns=urn:opendaylight.baz, rev=2015-11-11}) [at "));
    }

    @Test
    public void missingElementException() {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModules("/substatement-validator/missing-element"));
        final var cause = ex.getCause();
        // FIXME: should be MissingSubstatementException?
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Missing prefix statement [at "));
    }

    @Test
    public void bug6173Test() throws Exception {
        assertEquals(1, TestUtils.loadModules("/substatement-validator/empty-element").getModules().size());
    }

    @Test
    public void bug4310test() throws Exception {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModules("/substatement-validator/bug-4310"));
        final var cause = ex.getCause();
        assertThat(cause, instanceOf(MissingSubstatementException.class));
        assertThat(cause.getMessage(), startsWith("TYPE is missing TYPE. Minimal count is 1. Error in module bug4310 "
            + "(QNameModule{ns=urn:opendaylight.bug4310}) [at "));
    }
}
