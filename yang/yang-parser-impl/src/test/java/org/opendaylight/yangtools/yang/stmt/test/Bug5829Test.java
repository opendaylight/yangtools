/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug5829Test {
    @Test
    public void test() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSources("/bugs/bug5829");
            fail("Test should fail due to invalid yang source.");
        } catch (Exception e) {
            assertTrue(e.getMessage().startsWith(
                    "Error in module 'foo': case nodes '(foo?revision=1970-01-01)two' and "
                            + "'(foo?revision=1970-01-01)three' in choice '(foo?revision=1970-01-01)my-choice' "
                            + "have child nodes with the same QName '(foo?revision=1970-01-01)two'."));
        }
    }
}
