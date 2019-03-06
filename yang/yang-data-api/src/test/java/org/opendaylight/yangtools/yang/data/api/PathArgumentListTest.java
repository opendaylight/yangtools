/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.UnmodifiableIterator;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public class PathArgumentListTest {
    private static final class TestClass extends PathArgumentList {
        @Override
        public UnmodifiableIterator<PathArgument> iterator() {
            return new UnmodifiableIterator<PathArgument>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public PathArgument next() {
                    throw new NoSuchElementException();
                }
            };
        }

        @Override
        public PathArgument get(final int index) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }
    }

    @Test
    public void testIsEmpty() {
        assertFalse(new TestClass().isEmpty());
    }

    @Test
    public void testProtections() {
        final PathArgumentList l = new TestClass();

        try {
            l.remove(null);
            fail();
        } catch (UnsupportedOperationException e) {
            // Expected
        }

        try {
            l.addAll(Collections.emptyList());
            fail();
        } catch (UnsupportedOperationException e) {
            // Expected
        }

        try {
            l.removeAll(Collections.emptyList());
            fail();
        } catch (UnsupportedOperationException e) {
            // Expected
        }

        try {
            l.retainAll(Collections.emptyList());
            fail();
        } catch (UnsupportedOperationException e) {
            // Expected
        }

        try {
            l.clear();
            fail();
        } catch (UnsupportedOperationException e) {
            // Expected
        }

        try {
            l.addAll(0, null);
            fail();
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testPathArgument() {
        final QNameModule qNameModule = QNameModule.create(URI.create("urn:opendaylight.test2"),
            Revision.of("2015-08-08"));
        final QName qNameRoot = QName.create(qNameModule, "root");
        final QName qNameList = QName.create(qNameModule, "list");
        final QName qNameLeaf = QName.create(qNameModule, "leaf-a");
        final Map<QName, Object> entryLeaf = new HashMap<>();
        entryLeaf.put(qNameList, "leaf");
        final YangInstanceIdentifier.NodeIdentifierWithPredicates nodeIdentifierWithPredicates =
            new YangInstanceIdentifier.NodeIdentifierWithPredicates(qNameList , entryLeaf);
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.of(qNameRoot).node(qNameList)
                .node(nodeIdentifierWithPredicates).node(qNameLeaf);
        final PathArgument pathArgumentToRoot = yangInstanceIdentifier.getAncestor(1).getPathFromRoot().iterator()
                .next();
        final StackedPathArguments stackedPathArguments = (StackedPathArguments)yangInstanceIdentifier
            .getPathFromRoot();
        assertTrue(yangInstanceIdentifier.pathArgumentsEqual(yangInstanceIdentifier));
        assertEquals(pathArgumentToRoot, stackedPathArguments.get(0));
        assertEquals(4, stackedPathArguments.size());
        assertTrue(stackedPathArguments.contains(pathArgumentToRoot));
        assertEquals(0, stackedPathArguments.indexOf(pathArgumentToRoot));
        assertEquals(0, stackedPathArguments.lastIndexOf(pathArgumentToRoot));

        final StackedReversePathArguments stackedReversePathArguments =
            (StackedReversePathArguments)yangInstanceIdentifier.getPathTowardsRoot();
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
        final YangInstanceIdentifier yangInstanceIdentifier1 = stackedYangInstanceIdentifier.getAncestor(4);
        assertEquals(stackedReversePathArguments, yangInstanceIdentifier1.getReversePathArguments());

        try {
            stackedYangInstanceIdentifier.getAncestor(12);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}
