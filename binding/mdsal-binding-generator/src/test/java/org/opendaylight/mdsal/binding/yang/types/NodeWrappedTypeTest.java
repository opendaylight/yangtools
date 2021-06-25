/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NodeWrappedTypeTest {

    @Test
    public void test() {
        final NodeWrappedType nwt1 = new NodeWrappedType("obj1");
        final NodeWrappedType nwt2 = new NodeWrappedType("obj2");
        final NodeWrappedType nwt3 = new NodeWrappedType("obj1");
        final String str = "obj3";

        assertTrue("Node nwt1 should equal to itself.", nwt1.equals(nwt1));
        assertFalse("It can't be possible to compare nwt with string.", nwt1.equals(str));
        assertFalse("nwt1 shouldn't equal to nwt2.", nwt1.equals(nwt2));
        assertTrue("Node nwt1 should equal to nwt3.", nwt1.equals(nwt3));

        assertEquals("toString method is returning incorrect value.", "NodeWrappedType{wrappedType=obj1}",
                nwt1.toString());
    }
}
