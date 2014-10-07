/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PropertyUtilsTest {

    @Test
    public void testGetIntSystemProperty() {
        final int testValue = PropertyUtils.getIntSystemProperty("file.separator", 1);
        assertEquals("Property value should be '1'.", 1, testValue);
    }
}
