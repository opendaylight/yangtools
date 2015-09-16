/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import com.google.common.collect.UnmodifiableIterator;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public class PathArgumentListTest {
    private static final class TestClass extends PathArgumentList {
        @Override
        public UnmodifiableIterator<PathArgument> iterator() {
            return null;
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
        }

        try {
            l.addAll(null);
            fail();
        } catch (UnsupportedOperationException e) {
        }

        try {
            l.removeAll(null);
            fail();
        } catch (UnsupportedOperationException e) {
        }

        try {
            l.retainAll(null);
            fail();
        } catch (UnsupportedOperationException e) {
        }

        try {
            l.clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }

        try {
            l.addAll(0, null);
            fail();
        } catch (UnsupportedOperationException e) {
        }

    }
}
