/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class DuplicityTest {
    @Test
    public void testDuplicate() {
        assertEquals(Map.of(), NormalizedNodes.findDuplicates(mock(LeafNode.class)));
        assertEquals(Map.of(), NormalizedNodes.findDuplicates(mock(ContainerNode.class)));
    }
}
