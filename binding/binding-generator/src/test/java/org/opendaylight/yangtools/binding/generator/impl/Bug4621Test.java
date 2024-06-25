/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug4621Test {
    @Test
    void testMissingLeafrefTarget() {
        final var context = YangParserTestUtils.parseYangResource("/bug4621.yang");
        final var uoe = assertThrows(UnsupportedOperationException.class,
            () -> DefaultBindingGenerator.generateFor(context));
        assertEquals("Cannot ascertain type", uoe.getMessage());
        final var cause = assertInstanceOf(IllegalArgumentException.class, uoe.getCause());
        assertEquals("Failed to find leafref target /foo:neighbor/foo:mystring1", cause.getMessage());
    }
}