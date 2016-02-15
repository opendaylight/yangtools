/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MapUnkeyedListTest {

    @Test
    public void mapUnkeyedListTest () {
        final MapEntryNodeDomSerializer mapEntryNodeDomSerializer = new MapEntryNodeDomSerializer
                (DomSerializerTestUtils.DOC, DomSerializerTestUtils.MOCK_DISPATCHER);
        final UnkeyedListEntryNodeDomSerializer unkeyedListEntryNodeDomSerializer = new UnkeyedListEntryNodeDomSerializer
                (DomSerializerTestUtils.DOC, DomSerializerTestUtils.MOCK_DISPATCHER);
        final MapNodeDomSerializer mapNodeDomSerializer = new MapNodeDomSerializer(mapEntryNodeDomSerializer);
        final UnkeyedListNodeDomSerializer unkeyedListNodeDomSerializer = new UnkeyedListNodeDomSerializer
                (unkeyedListEntryNodeDomSerializer);

        assertTrue(mapEntryNodeDomSerializer.equals(mapNodeDomSerializer.getListEntryNodeSerializer()));
        assertTrue(unkeyedListEntryNodeDomSerializer.equals(unkeyedListNodeDomSerializer.getListEntryNodeSerializer()));
    }
}