/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug4410Test {

    @Test
    public void test() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSources("/bugs/bug4410");
            fail("SomeModifiersUnresolvedException should be thrown.");
        } catch (SomeModifiersUnresolvedException e) {
            final Throwable suppressed2levelsDown = e.getSuppressed()[0].getSuppressed()[0];
            assertTrue(suppressed2levelsDown instanceof InferenceException);
            final String message = suppressed2levelsDown.getMessage();
            assertTrue(message.startsWith("Type [(foo?revision=1970-01-01)"));
            assertTrue(message.endsWith("was not found."));
        }
    }
}
