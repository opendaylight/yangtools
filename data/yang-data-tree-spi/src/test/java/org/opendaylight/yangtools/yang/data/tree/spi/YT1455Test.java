/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class YT1455Test {
    @Mock
    public DistinctNodeContainer<PathArgument, NormalizedNode> oldData;
    @Mock
    public DistinctNodeContainer<PathArgument, NormalizedNode> newData;
    @Mock
    public UnkeyedListNode child;

    @Test
    public void testDeleteUnkeyedList() {
        final var childId = new NodeIdentifier(QName.create("foo", "foo"));
        doReturn(childId).when(child).name();
        doReturn(List.of(child)).when(oldData).body();
        doReturn(List.of()).when(newData).body();
        doReturn(null).when(newData).childByArg(childId);

        final var delta = DataTreeCandidateNodes.containerDelta(oldData, newData);
        assertEquals(1, delta.size());

        final var first = delta.iterator().next();
        assertEquals(childId, first.name());
        assertEquals(ModificationType.DELETE, first.modificationType());
        assertEquals(child, first.dataBefore());
        assertEquals(null, first.dataAfter());
        assertEquals(0, first.childNodes().size());
    }
}
