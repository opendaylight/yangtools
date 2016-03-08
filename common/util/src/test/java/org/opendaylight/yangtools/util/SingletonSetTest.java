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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.Iterator;
import org.junit.Test;

public class SingletonSetTest {
    private static final String ELEMENT = "element";

    private static SingletonSet<?> nullSet() {
        return SingletonSet.of(null);
    }

    @Test
    public void testNullSingleton() {
        final SingletonSet<?> s = nullSet();

        assertFalse(s.isEmpty());
        assertEquals(1, s.size());
        assertFalse(s.contains(""));
        assertTrue(s.contains(null));
        assertNull(s.getElement());
        assertEquals(0, s.hashCode());
        assertEquals(s, Collections.singleton(null));
        assertNotEquals(s, Collections.singleton(""));
        assertNotEquals(s, "");
        assertEquals(s, s);
        assertNotEquals(s, null);
        assertEquals(Collections.singleton(null).toString(), s.toString());
    }

    @Test
    public void testRegularSingleton() {
        final SingletonSet<?> s = SingletonSet.of(ELEMENT);

        assertFalse(s.isEmpty());
        assertEquals(1, s.size());
        assertFalse(s.contains(""));
        assertFalse(s.contains(null));
        assertTrue(s.contains(ELEMENT));

        assertSame(ELEMENT, s.getElement());
        assertEquals(ELEMENT.hashCode(), s.hashCode());
        assertEquals(s, Collections.singleton(ELEMENT));
        assertNotEquals(s, Collections.singleton(""));
        assertNotEquals(s, Collections.singleton(null));
        assertNotEquals(s, "");
        assertEquals(s, s);
        assertNotEquals(s, null);
        assertEquals(Collections.singleton(ELEMENT).toString(), s.toString());
    }

    @Test
    public void testIterator() {
        final SingletonSet<?> s = SingletonSet.of(ELEMENT);
        final Iterator<?> it = s.iterator();

        assertTrue(it.hasNext());
        assertSame(ELEMENT, it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRejectedAdd() {
        final SingletonSet<?> s = nullSet();
        s.add(null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRejectedAddAll() {
        final SingletonSet<?> s = nullSet();
        s.addAll(null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRejectedClear() {
        final SingletonSet<?> s = nullSet();
        s.clear();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRejectedRemove() {
        final SingletonSet<?> s = nullSet();
        s.remove(null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRejectedRemoveAll() {
        final SingletonSet<?> s = nullSet();
        s.removeAll(null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRejectedRetainAll() {
        final SingletonSet<?> s = nullSet();
        s.retainAll(null);
    }
}
