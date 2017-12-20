/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class YT838Test {

    @Test
    public void testGroupingShadowing() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/YT838/grouping.yang");
            fail("Expected failure due to grouping identifier shadowing");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith(
                "Grouping name (grouping?revision=2017-12-20)foo shadows existing grouping defined at "));
        }
    }

    @Test
    public void testTypedefShadowing() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/YT838/typedef.yang");
            fail("Expected failure due to type identifier shadowing");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith(
                "Duplicate name for typedef (typedef?revision=2017-12-20)foo [at "));
        }
    }
}
