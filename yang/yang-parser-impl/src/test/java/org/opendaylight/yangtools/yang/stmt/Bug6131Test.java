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

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug6131Test {
    @Test
    public void test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSources("/bugs/bug6131");
            fail("A NullPointerException should have been thrown.");
        } catch (final Exception e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertTrue(e.getCause() instanceof NullPointerException);
        }
    }
}