/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug7146Test {

    @Test
    public void shouldFailOnSyntaxError() throws ReactorException {
        try {
            StmtTestUtils.parseYangSources(sourceForResource("/bugs/bug7146/foo.yang"));
            fail("RuntimeException should have been thrown because of an unknown character in yang module.");
        } catch (IllegalArgumentException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof YangSyntaxErrorException);
            assertTrue(cause.getMessage().contains("extraneous input '#'"));
        }
    }
}
