/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;
import org.junit.Test;

public class DuplicityTest {
    @Test
    public void testDuplicate() {
        final LeafNode<?> leafNode = mock(LeafNode.class);
        final ContainerNode containerNode = mock(ContainerNode.class);
        final Map<NormalizedNode, DuplicateEntry> normalizedNodeDuplicateEntryMapNode = NormalizedNodes
                .findDuplicates(leafNode);
        final Map<NormalizedNode, DuplicateEntry> normalizedNodeDuplicateEntryMapContainer = NormalizedNodes
                .findDuplicates(containerNode);
        assertEquals(0, normalizedNodeDuplicateEntryMapNode.size());
        assertEquals(0, normalizedNodeDuplicateEntryMapContainer.size());
    }
}
