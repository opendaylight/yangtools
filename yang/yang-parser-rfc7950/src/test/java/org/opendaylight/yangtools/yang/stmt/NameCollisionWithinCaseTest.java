/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class NameCollisionWithinCaseTest {
    @Test
    public void testChildNameCollisionOfAugmentCase() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/name-collision-within-case/foo.yang");
            fail("Expected failure due to node name collision");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertThat(cause, instanceOf(SourceException.class));
            assertThat(cause.getMessage(), startsWith(
                "Cannot add data tree child with name (foo?revision=2018-02-11)bar, a conflicting child already exists "
                        + "[at "));
        }
    }

    @Test
    public void testChildNameCollisionOfAugmentChoice() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/name-collision-within-case/bar.yang");
            fail("Expected failure due to node name collision");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertThat(cause, instanceOf(SourceException.class));
            assertThat(cause.getMessage(), startsWith(
                "Cannot add data tree child with name (bar?revision=2018-02-11)bar, a conflicting child already exists "
                        + "[at "));
        }
    }

    @Test
    public void testChildNameCollisionNormal() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/bugs/name-collision-within-case/baz.yang");
            fail("Expected failure due to node name collision");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertThat(cause, instanceOf(SourceException.class));
            assertThat(cause.getMessage(), startsWith(
                "Error in module 'baz': cannot add '(baz?revision=2018-02-28)bar'. Node name collision: "));
        }
    }
}
