/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
// FIXME: This is a sorry-ass of a test. Move the method, DuplicateFinder and this test to yang-data-util and expand
//        coverage using ImmutableNodes.
class DuplicityTest {
    @Mock
    private LeafNode<?> leaf;
    @Mock
    private ContainerNode container;

    @Test
    public void testDuplicateLeaf() {
        assertEquals(Map.of(), NormalizedNodes.findDuplicates(leaf));
    }

    @Test
    public void testDuplicateContainer() {
        doReturn(List.of()).when(container).body();
        assertEquals(Map.of(), NormalizedNodes.findDuplicates(container));
    }
}
