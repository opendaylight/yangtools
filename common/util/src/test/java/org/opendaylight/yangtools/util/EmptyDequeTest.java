/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;
import org.junit.Test;

public class EmptyDequeTest {

    @Test
    public void testEmptyDeque() {
        final EmptyDeque<?> deque = EmptyDeque.instance();
        assertFalse(deque.offer(null));
        assertFalse(deque.offerFirst(null));
        assertFalse(deque.offerLast(null));
        assertNull(deque.peek());
        assertNull(deque.peekFirst());
        assertNull(deque.peekLast());
        assertNull(deque.poll());
        assertNull(deque.pollFirst());
        assertNull(deque.pollLast());

        assertEquals(0, deque.size());
        assertFalse(deque.iterator().hasNext());
        assertFalse(deque.descendingIterator().hasNext());
        assertEquals(0L, deque.spliterator().estimateSize());

        final Object[] a = deque.toArray();
        assertEquals(0, a.length);
        assertSame(a, deque.toArray());
        assertSame(a, deque.toArray(a));

        assertFalse(deque.removeFirstOccurrence(null));
        assertFalse(deque.removeLastOccurrence(null));

        try {
            deque.push(null);
            fail();
        } catch (IllegalStateException e) {
            // expeced
        }
        try {
            deque.addFirst(null);
            fail();
        } catch (IllegalStateException e) {
            // expeced
        }
        try {
            deque.addLast(null);
            fail();
        } catch (IllegalStateException e) {
            // expeced
        }

        try {
            deque.getFirst();
            fail();
        } catch (NoSuchElementException e) {
            // expeced
        }
        try {
            deque.getLast();
            fail();
        } catch (NoSuchElementException e) {
            // expeced
        }
        try {
            deque.pop();
            fail();
        } catch (NoSuchElementException e) {
            // expeced
        }
        try {
            deque.remove();
            fail();
        } catch (NoSuchElementException e) {
            // expeced
        }
        try {
            deque.removeFirst();
            fail();
        } catch (NoSuchElementException e) {
            // expeced
        }
        try {
            deque.removeLast();
            fail();
        } catch (NoSuchElementException e) {
            // expeced
        }
    }
}
