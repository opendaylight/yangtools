/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class YT838Test {
    @Test
    public void testGroupingShadowing() {
        testGrouping("grouping.yang");
    }

    @Test
    public void testGroupingPostShadowing() {
        testGrouping("grouping-post.yang");
    }

    @Test
    public void testTypedefShadowing() {
        testTypedef("typedef.yang");
    }

    @Test
    public void testTypedefPostShadowing() {
        testTypedef("typedef-post.yang");
    }

    private static void testGrouping(final String model) {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/bugs/YT838/" + model));

        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(),
            startsWith("Duplicate name for grouping (grouping?revision=2017-12-20)foo [at "));
    }

    private static void testTypedef(final String model) {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSource("/bugs/YT838/" + model));
        final Throwable cause = ex.getCause();
        assertTrue(cause instanceof SourceException);
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Duplicate name for typedef (typedef?revision=2017-12-20)foo [at "));
    }
}
