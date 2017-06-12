/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BaseTypesTest {

    @Test
    public void testIsYangBuildInType() {
        assertFalse("whatever is not build-in type", BaseTypes.isYangBuildInType("whatever"));
        assertTrue("int8 is build-in type", BaseTypes.isYangBuildInType("int8"));
    }

}
