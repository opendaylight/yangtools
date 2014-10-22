/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ReferencedTypeImplTest {

    @Test
    public void testCreateNewReferencedType() {
        ReferencedTypeImpl refType = new ReferencedTypeImpl("org.opendaylight.yangtools.test", "RefTypeTest");
        assertEquals("RefTypeTest", refType.getName());
    }

    @Test
    public void testToStringMethod() {
        ReferencedTypeImpl refType = new ReferencedTypeImpl("org.opendaylight.yangtools.test", "RefTypeTest");
        assertTrue(refType.toString().contains("RefTypeTest"));
    }
}
