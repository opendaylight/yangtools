package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MapUnkeyedListTest extends AbstractDomSerializerTest {

    @Test
    public void mapUnkeyedListTest () {
        MapEntryNodeDomSerializer mapEntryNodeDomSerializer = new MapEntryNodeDomSerializer(doc, mockDispatcher);
        UnkeyedListEntryNodeDomSerializer unkeyedListEntryNodeDomSerializer = new UnkeyedListEntryNodeDomSerializer
                (doc, mockDispatcher);
        MapNodeDomSerializer mapNodeDomSerializer = new MapNodeDomSerializer(mapEntryNodeDomSerializer);
        UnkeyedListNodeDomSerializer unkeyedListNodeDomSerializer = new UnkeyedListNodeDomSerializer
                (unkeyedListEntryNodeDomSerializer);
        assertTrue(mapEntryNodeDomSerializer.equals(mapNodeDomSerializer.getListEntryNodeSerializer()));
        assertTrue(unkeyedListEntryNodeDomSerializer.equals(unkeyedListNodeDomSerializer.getListEntryNodeSerializer()));
    }
}