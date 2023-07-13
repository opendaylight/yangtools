/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for TrackingLinkedBlockingQueue.
 *
 * @author Thomas Pantelis
 */
public class TrackingLinkedBlockingQueueTest {

    @Test
    public void testOffer() throws InterruptedException {
        TrackingLinkedBlockingQueue<String> queue = new TrackingLinkedBlockingQueue<>(2);

        assertTrue(queue.offer("1"), "offer");
        assertEquals(1, queue.getLargestQueueSize(), "getLargestQueueSize");
        assertEquals(1, queue.size(), "size");

        assertTrue(queue.offer("2", 1, TimeUnit.MILLISECONDS), "offer");
        assertEquals(2, queue.getLargestQueueSize(), "getLargestQueueSize");
        assertEquals(2, queue.size(), "size");

        assertFalse(queue.offer("3"), "offer");
        assertEquals(2, queue.getLargestQueueSize(), "getLargestQueueSize");
        assertEquals(2, queue.size(), "size");

        assertFalse(queue.offer("4", 1, TimeUnit.MILLISECONDS), "offer");
        assertEquals(2, queue.getLargestQueueSize(), "getLargestQueueSize");
        assertEquals(2, queue.size(), "size");
    }

    @Test
    public void testPut() throws InterruptedException {
        TrackingLinkedBlockingQueue<String> queue = new TrackingLinkedBlockingQueue<>();

        queue.put("1");
        assertEquals(1, queue.getLargestQueueSize(), "getLargestQueueSize");
        assertEquals(1, queue.size(), "size");

        queue.put("2");
        assertEquals(2, queue.getLargestQueueSize(), "getLargestQueueSize");
        assertEquals(2, queue.size(), "size");
    }

    @Test
    public void testAdd() {
        TrackingLinkedBlockingQueue<String> queue = new TrackingLinkedBlockingQueue<>(2);

        assertTrue(queue.add("1"), "add");
        assertEquals(1, queue.getLargestQueueSize(), "getLargestQueueSize");
        assertEquals(1, queue.size(), "size");

        assertTrue(queue.add("2"), "add");
        assertEquals(2, queue.getLargestQueueSize(), "getLargestQueueSize");
        assertEquals(2, queue.size(), "size");

        assertThrows(IllegalStateException.class, () -> queue.add("3"));
        assertEquals(2, queue.getLargestQueueSize(), "getLargestQueueSize");
        assertEquals(2, queue.size(), "size");
    }

    @Test
    public void testAddAll() {
        TrackingLinkedBlockingQueue<String> queue = new TrackingLinkedBlockingQueue<>(3);

        queue.addAll(Arrays.asList("1", "2"));
        assertEquals(2, queue.getLargestQueueSize(), "getLargestQueueSize");
        assertEquals(2, queue.size(), "size");

        assertThrows(IllegalStateException.class, () -> queue.addAll(Arrays.asList("3", "4")));
        assertEquals(3, queue.getLargestQueueSize(), "getLargestQueueSize");
        assertEquals(3, queue.size(), "size");
    }
}
