/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class EmptyDequeTest {
    @Test
    void testEmptyDeque() {
        final var deque = EmptyDeque.instance();
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

        final var a = deque.toArray();
        assertEquals(0, a.length);
        assertSame(a, deque.toArray());
        assertSame(a, deque.toArray(a));

        assertFalse(deque.removeFirstOccurrence(null));
        assertFalse(deque.removeLastOccurrence(null));

        assertThrows(IllegalStateException.class, () -> deque.push(null));
        assertThrows(IllegalStateException.class, () -> deque.addFirst(null));
        assertThrows(IllegalStateException.class, () -> deque.addLast(null));
        assertThrows(NoSuchElementException.class, () -> deque.getFirst());
        assertThrows(NoSuchElementException.class, () -> deque.getLast());
        assertThrows(NoSuchElementException.class, () -> deque.pop());
        assertThrows(NoSuchElementException.class, () -> deque.remove());
        assertThrows(NoSuchElementException.class, () -> deque.removeFirst());
        assertThrows(NoSuchElementException.class, () -> deque.removeLast());
    }
}
