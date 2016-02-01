package org.opendaylight.yangtools.yang.data.impl.schema.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;

public class BuilderTest {
    final QName ROOT_CONTAINER = QName.create("test.namespace.builder.test", "2016-01-01", "root-container");
    final QName LIST_MAIN = QName.create(ROOT_CONTAINER, "list-ordered-by-user-with-key");
    final QName LIST_MAIN_CHILD_QNAME_1 = QName.create(ROOT_CONTAINER, "leaf-a");
    final YangInstanceIdentifier.NodeIdentifier LIST_MAIN_NI = new YangInstanceIdentifier.NodeIdentifier(LIST_MAIN);

    final MapEntryNode LIST_MAIN_CHILD_1 = ImmutableNodes.mapEntry(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 1);
    final MapEntryNode LIST_MAIN_CHILD_2 = ImmutableNodes.mapEntry(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 2);
    final MapEntryNode LIST_MAIN_CHILD_3 = ImmutableNodes.mapEntry(LIST_MAIN, LIST_MAIN_CHILD_QNAME_1, 3);
    final Integer size = 3;

    @Test
    public void ImmutableOrderedMapBuilderTest() {
        LinkedList<MapEntryNode> mapEntryNodeColl = new LinkedList();
        mapEntryNodeColl.add(LIST_MAIN_CHILD_3);

        final Map<QName, Object> keys = new HashMap<QName, Object>();
        keys.put(LIST_MAIN_CHILD_QNAME_1, 1);
        final YangInstanceIdentifier.NodeIdentifierWithPredicates mapEntryPath = new YangInstanceIdentifier.NodeIdentifierWithPredicates(
                LIST_MAIN, keys);

        OrderedMapNode orderedMapNodeCreateNull = ImmutableOrderedMapNodeBuilder.create()
                .withNodeIdentifier(LIST_MAIN_NI)
                .withChild(LIST_MAIN_CHILD_1)
                .addChild(LIST_MAIN_CHILD_2)
                .withValue(mapEntryNodeColl)
                .build();

        OrderedMapNode orderedMapNodeCreateSize = ImmutableOrderedMapNodeBuilder.create(size)
                .withNodeIdentifier(LIST_MAIN_NI)
                .build();
        OrderedMapNode orderedMapNodeCreateNode = ImmutableOrderedMapNodeBuilder.create(orderedMapNodeCreateNull)
                .removeChild(mapEntryPath)
                .build();

        assertEquals(size, (Integer) orderedMapNodeCreateNull.getSize());
        assertEquals(orderedMapNodeCreateNode.getSize(), orderedMapNodeCreateNull.getSize() - 1);
        assertEquals(LIST_MAIN_NI, orderedMapNodeCreateSize.getIdentifier());
        assertEquals(orderedMapNodeCreateNull.getChild(0), LIST_MAIN_CHILD_1);
        assertEquals((Integer) orderedMapNodeCreateNull.getValue().size(), size);
        assertNotNull(orderedMapNodeCreateNull.hashCode());
        assertEquals((Integer) orderedMapNodeCreateNull.getValue().size(), size);
    }
}