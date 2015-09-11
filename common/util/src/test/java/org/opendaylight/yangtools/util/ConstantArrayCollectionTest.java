/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;

public class ConstantArrayCollectionTest {
    private static final String[] ARRAY = new String[] { "a", "bb", "ccc" };
    private static final Collection<String> REF = ImmutableList.copyOf(ARRAY);

    private static Collection<String> create() {
        return new ConstantArrayCollection<>(ARRAY.clone());
    }

    @Test
    public void testToString() {
        // Empty
        assertEquals(Collections.emptySet().toString(), new ConstantArrayCollection<>(new Object[0]).toString());

        // Normal
        assertEquals(REF.toString(), create().toString());
    }

    @Test
    public void testEquals() {
        final Collection<?> c = create();

        assertTrue(c.containsAll(REF));
        assertTrue(REF.containsAll(c));
        assertTrue(Iterables.elementsEqual(REF, c));
    }

    @Test
    public void testSimpleOperations() {
        final Collection<?> c = create();

        assertEquals(ARRAY.length, c.size());
        assertFalse(c.isEmpty());
        assertTrue(c.contains("ccc"));
        assertFalse(c.contains(""));
        assertFalse(c.contains(null));

        assertTrue(c.containsAll(Collections.<String>emptyList()));
        assertFalse(c.containsAll(Collections.singleton("")));
        assertFalse(c.containsAll(Collections.singleton(null)));
    }

    @Test
    public void testProtection() {
        final Collection<?> c = create();

        try {
            c.add(null);
            fail();
        } catch (UnsupportedOperationException e) {
        }

        try {
            c.remove(null);
            fail();
        } catch (UnsupportedOperationException e) {
        }

        try {
            c.addAll(null);
            fail();
        } catch (UnsupportedOperationException e) {
        }

        try {
            c.removeAll(null);
            fail();
        } catch (UnsupportedOperationException e) {
        }

        try {
            c.retainAll(null);
            fail();
        } catch (UnsupportedOperationException e) {
        }

        try {
            c.clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }
}
