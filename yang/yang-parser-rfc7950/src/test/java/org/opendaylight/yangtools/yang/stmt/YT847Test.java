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

public class YT847Test {
    @Test
    public void testChildNameCollisionOfAugmentCase() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/YT847/foo.yang");
            fail("Expected failure due to node name collision");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith(
                "Error in module 'foo': cannot add '(foo?revision=2018-02-11)bar'. Node name collision: "));
        }
    }

    @Test
    public void testChildNameCollisionOfAugmentChoice() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/YT847/bar.yang");
            fail("Expected failure due to node name collision");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith(
                "Error in module 'bar': cannot add '(bar?revision=2018-02-11)bar'. Node name collision: "));
        }
    }
}
