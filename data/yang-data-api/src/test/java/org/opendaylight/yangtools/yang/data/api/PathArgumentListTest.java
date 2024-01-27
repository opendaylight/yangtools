/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

class PathArgumentListTest {
    private static final PathArgumentList LIST = new StackedPathArguments(YangInstanceIdentifier.of(),
        List.of(new NodeIdentifier(QName.create("foo", "foo"))));

    @Test
    void testIsEmpty() {
        assertFalse(LIST.isEmpty());
    }

    @Test
    void testProtections() {
        assertThrows(UnsupportedOperationException.class, () -> LIST.remove(null));
        assertThrows(UnsupportedOperationException.class, () -> LIST.addAll(List.of()));
        assertThrows(UnsupportedOperationException.class, () -> LIST.removeAll(List.of()));
        assertThrows(UnsupportedOperationException.class, () -> LIST.retainAll(List.of()));
        assertThrows(UnsupportedOperationException.class, () -> LIST.clear());
        assertThrows(UnsupportedOperationException.class, () -> LIST.addAll(0, null));
    }

    @Test
    void testPathArgument() {
        final QNameModule qNameModule = QNameModule.of("urn:opendaylight.test2", "2015-08-08");
        final QName qNameRoot = QName.create(qNameModule, "root");
        final QName qNameList = QName.create(qNameModule, "list");
        final QName qNameLeaf = QName.create(qNameModule, "leaf-a");
        final Map<QName, Object> entryLeaf = new HashMap<>();
        entryLeaf.put(qNameList, "leaf");
        final NodeIdentifierWithPredicates nodeIdentifierWithPredicates = NodeIdentifierWithPredicates.of(qNameList,
            entryLeaf);
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.of(qNameRoot, qNameList)
                .node(nodeIdentifierWithPredicates).node(qNameLeaf);
        final PathArgument pathArgumentToRoot = yangInstanceIdentifier.getAncestor(1).getPathArguments().iterator()
                .next();
        final StackedPathArguments stackedPathArguments = (StackedPathArguments)yangInstanceIdentifier
            .getPathArguments();
        assertTrue(yangInstanceIdentifier.pathArgumentsEqual(yangInstanceIdentifier));
        assertEquals(pathArgumentToRoot, stackedPathArguments.get(0));
        assertEquals(4, stackedPathArguments.size());
        assertTrue(stackedPathArguments.contains(pathArgumentToRoot));
        assertEquals(0, stackedPathArguments.indexOf(pathArgumentToRoot));
        assertEquals(0, stackedPathArguments.lastIndexOf(pathArgumentToRoot));

        final StackedReversePathArguments stackedReversePathArguments =
            (StackedReversePathArguments)yangInstanceIdentifier.getReversePathArguments();
        final QName rootQname = pathArgumentToRoot.getNodeType();
        final QName leafQname = stackedReversePathArguments.get(0).getNodeType();
        assertEquals(qNameRoot, rootQname);
        assertEquals(qNameLeaf, leafQname);
        assertEquals(4, stackedReversePathArguments.size());
        assertTrue(stackedReversePathArguments.contains(pathArgumentToRoot));
        assertEquals(3, stackedReversePathArguments.indexOf(pathArgumentToRoot));
        assertEquals(3, stackedReversePathArguments.lastIndexOf(pathArgumentToRoot));

        final StackedYangInstanceIdentifier stackedYangInstanceIdentifier = (StackedYangInstanceIdentifier)
                yangInstanceIdentifier;
        final StackedYangInstanceIdentifier stackedYangInstanceIdentifierClone = stackedYangInstanceIdentifier.clone();
        final YangInstanceIdentifier yangInstanceIdentifier1 = stackedYangInstanceIdentifier.getAncestor(4);
        assertEquals(stackedYangInstanceIdentifier, stackedYangInstanceIdentifierClone);
        assertEquals(stackedReversePathArguments, yangInstanceIdentifier1.getReversePathArguments());
        assertSame(stackedYangInstanceIdentifier.getParent(), stackedYangInstanceIdentifier.getAncestor(3));

        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> stackedYangInstanceIdentifier.getAncestor(12));
        assertEquals("Depth 12 exceeds maximum depth 4", thrown.getMessage());
    }
}
