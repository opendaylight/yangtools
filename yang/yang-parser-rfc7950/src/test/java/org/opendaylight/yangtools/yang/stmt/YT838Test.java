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
        testGrouping("grouping.yang");
    }

    @Test
    public void testGroupingPostShadowing() throws Exception {
        testGrouping("grouping-post.yang");
    }

    @Test
    public void testTypedefShadowing() throws Exception {
        testTypedef("typedef.yang");
    }

    @Test
    public void testTypedefPostShadowing() throws Exception {
        testTypedef("typedef-post.yang");
    }

    private static void testGrouping(final String model) throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/YT838/" + model);
            fail("Expected failure due to grouping identifier shadowing");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith(
                "Duplicate name for grouping (grouping?revision=2017-12-20?idns=ns_grouping)foo [at "));
        }
    }

    private static void testTypedef(final String model) throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/YT838/" + model);
            fail("Expected failure due to type identifier shadowing");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith(
                "Duplicate name for typedef (typedef?revision=2017-12-20)foo [at "));
        }
    }
}
