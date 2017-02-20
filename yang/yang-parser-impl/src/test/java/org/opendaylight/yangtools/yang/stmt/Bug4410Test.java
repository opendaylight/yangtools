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

import com.google.common.base.Throwables;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

public class Bug4410Test {

    @Test
    public void test() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/bugs/bug4410");
            fail("SomeModifiersUnresolvedException should be thrown.");
        } catch (SomeModifiersUnresolvedException e) {
            Throwable rootCause = Throwables.getRootCause(e);
            assertTrue(rootCause instanceof InferenceException);
            final String message = rootCause.getMessage();
            assertTrue(message.startsWith("Type [(foo?revision=1970-01-01)"));
            assertTrue(message.contains("was not found"));
        }
    }
}
