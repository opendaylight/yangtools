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

class PropertyUtilsTest {
    @Test
    void testGetIntSystemProperty() {
        final var testValue = PropertyUtils.getIntSystemProperty("file.separator", 1);
        assertEquals(1, testValue, "Property value should be '1'.");
    }
}
