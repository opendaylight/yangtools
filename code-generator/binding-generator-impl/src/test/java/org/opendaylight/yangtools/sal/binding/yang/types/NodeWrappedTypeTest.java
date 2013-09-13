package org.opendaylight.yangtools.sal.binding.yang.types;

import static org.junit.Assert.*;

import org.junit.Test;

public class NodeWrappedTypeTest {

    @Test
    public void test() {
        NodeWrappedType nwt1 = new NodeWrappedType("obj1");
        NodeWrappedType nwt2 = new NodeWrappedType("obj2");
        NodeWrappedType nwt3 = new NodeWrappedType("obj1");
        String str = "obj3";

        assertTrue("Node nwt1 should equal to itself.", nwt1.equals(nwt1));
        assertFalse("It can't be possible to compare nwt with string.", nwt1.equals(str));
        assertFalse("nwt1 shouldn't equal to nwt2.", nwt1.equals(nwt2));
        assertTrue("Node nwt1 should equal to nwt3.", nwt1.equals(nwt3));

        assertEquals("toString method is returning incorrect value.", "NodeWrappedType{wrappedType=obj1}",
                nwt1.toString());
    }
}
