package org.opendaylight.yangtools.yang.data.impl;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.Node;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class CompositeNodeTOImplTest {
    @Test
    public void testSerialization() throws Exception{
        CompositeNodeTOImpl child1 = new CompositeNodeTOImpl(QName.create("ns", "2013-12-09", "child1"), null, new ArrayList<Node<?>>(), ModifyAction.REPLACE);
        CompositeNodeTOImpl child2 = new CompositeNodeTOImpl(QName.create("ns", "2013-12-09", "child2"), null, new ArrayList<Node<?>>(), ModifyAction.REPLACE);
        SimpleNodeTOImpl child3 = new SimpleNodeTOImpl(QName.create("ns", "2013-12-09", "child2"), null, "foo");

        List<Node<?>> children = new ArrayList<Node<?>>();
        children.add(child1);
        children.add(child2);
        children.add(child3);

        CompositeNodeTOImpl parent = new CompositeNodeTOImpl(QName.create("ns", "2013-12-09", "parent"), null, new ArrayList<Node<?>>(), ModifyAction.REPLACE);

        CompositeNodeTOImpl object = new CompositeNodeTOImpl(QName.create("ns", "2013-12-09", "root"), parent, children , ModifyAction.MERGE);

        CompositeNodeTOImpl clone = (CompositeNodeTOImpl) SerializationUtils.clone(object);

        assertNotNull(clone.getNodeType());
        assertEquals(ModifyAction.MERGE, clone.getModificationAction());
        assertNotNull(clone.getParent());
        assertNotNull(clone.getParent().getNodeType());
        assertNotNull(clone.getValue());
        assertEquals(3, clone.getValue().size());
        assertNotNull(clone.getValue().get(0).getNodeType());
        assertNotNull(clone.getValue().get(1).getNodeType());

        SimpleNodeTOImpl child3Clone = (SimpleNodeTOImpl) clone.getValue().get(2);

        assertNotNull(child3Clone.getNodeType());
        assertTrue(child3Clone instanceof SimpleNodeTOImpl);
        assertEquals("foo", child3Clone.getValue());

    }
}
