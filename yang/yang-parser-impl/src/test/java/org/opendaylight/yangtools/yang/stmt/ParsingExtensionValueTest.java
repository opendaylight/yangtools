/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Test for testing of extensions and their arguments.
 *
 * @author Lukas Sedlak &lt;lsedlak@cisco.com&gt;
 */
public class ParsingExtensionValueTest {

    @Test
    public void extensionTest() throws Exception {
        try {
            TestUtils.loadModules(getClass().getResource("/extensions").toURI());
        } catch (Exception e) {
            assertEquals(SomeModifiersUnresolvedException.class, e.getClass());
            assertTrue(e.getCause() instanceof SourceException);
            assertTrue(e.getCause().getMessage().startsWith("ext:id is not a YANG statement or use of extension"));
        }
    }
}
