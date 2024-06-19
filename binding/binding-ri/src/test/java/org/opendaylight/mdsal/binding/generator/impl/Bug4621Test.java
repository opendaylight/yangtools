/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug4621Test {
    @Test
    public void testMissingLeafrefTarget() {
        final var context = YangParserTestUtils.parseYangResource("/bug4621.yang");
        final var uoe = assertThrows(UnsupportedOperationException.class,
            () -> DefaultBindingGenerator.generateFor(context));
        assertEquals("Cannot ascertain type", uoe.getMessage());
        final var cause = uoe.getCause();
        assertThat(cause, instanceOf(IllegalArgumentException.class));
        assertEquals("Failed to find leafref target /foo:neighbor/foo:mystring1", cause.getMessage());
    }
}