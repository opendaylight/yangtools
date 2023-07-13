/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HashCodeBuilderTest {
    @Test
    public void testAllMethodsOfHashCodeBuilder() {
        final HashCodeBuilder<String> builder = new HashCodeBuilder<>();
        assertEquals(1, builder.build(), "Default hash code should be '1'.");

        int nextHashCode = HashCodeBuilder.nextHashCode(1, "test");
        assertEquals(3556529, nextHashCode, "Next hash code should be '3556529'.");

        builder.addArgument("another test");
        assertEquals(-700442706, builder.build(), "Updated internal hash code should be '700442706'.");
    }
}
