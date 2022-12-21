/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.jupiter.api.Test;

class YT838Test extends AbstractYangTest {
    @Test
    void testGroupingShadowing() {
        testGrouping("grouping.yang");
    }

    @Test
    void testGroupingPostShadowing() {
        testGrouping("grouping-post.yang");
    }

    @Test
    void testTypedefShadowing() {
        testTypedef("typedef.yang");
    }

    @Test
    void testTypedefPostShadowing() {
        testTypedef("typedef-post.yang");
    }

    private static void testGrouping(final String model) {
        assertSourceException(startsWith("Duplicate name for grouping (grouping?revision=2017-12-20)foo [at "),
            "/bugs/YT838/" + model);
    }

    private static void testTypedef(final String model) {
        assertSourceException(startsWith("Duplicate name for typedef (typedef?revision=2017-12-20)foo [at "),
            "/bugs/YT838/" + model);
    }
}
