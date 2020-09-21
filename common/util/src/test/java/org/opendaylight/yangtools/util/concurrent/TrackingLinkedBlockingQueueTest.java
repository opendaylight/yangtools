/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * Unit tests for TrackingLinkedBlockingQueue.
 *
 * @author Thomas Pantelis
 */
public class TrackingLinkedBlockingQueueTest {

    @Test
    public void testOffer() throws InterruptedException {
        TrackingLinkedBlockingQueue<String> queue = new TrackingLinkedBlockingQueue<>(2);

        assertTrue("offer", queue.offer("1"));
        assertEquals("getLargestQueueSize", 1, queue.getLargestQueueSize());
        assertEquals("size", 1, queue.size());

        assertTrue("offer", queue.offer("2", 1, TimeUnit.MILLISECONDS));
        assertEquals("getLargestQueueSize", 2, queue.getLargestQueueSize());
        assertEquals("size", 2, queue.size());

        assertFalse("offer", queue.offer("3"));
        assertEquals("getLargestQueueSize", 2, queue.getLargestQueueSize());
        assertEquals("size", 2, queue.size());

        assertFalse("offer", queue.offer("4", 1, TimeUnit.MILLISECONDS));
        assertEquals("getLargestQueueSize", 2, queue.getLargestQueueSize());
        assertEquals("size", 2, queue.size());
    }

    @Test
    public void testPut() throws InterruptedException {
        TrackingLinkedBlockingQueue<String> queue = new TrackingLinkedBlockingQueue<>();

        queue.put("1");
        assertEquals("getLargestQueueSize", 1, queue.getLargestQueueSize());
        assertEquals("size", 1, queue.size());

        queue.put("2");
        assertEquals("getLargestQueueSize", 2, queue.getLargestQueueSize());
        assertEquals("size", 2, queue.size());
    }

    @Test
    public void testAdd() {
        TrackingLinkedBlockingQueue<String> queue = new TrackingLinkedBlockingQueue<>(2);

        assertTrue("add", queue.add("1"));
        assertEquals("getLargestQueueSize", 1, queue.getLargestQueueSize());
        assertEquals("size", 1, queue.size());

        assertTrue("add", queue.add("2"));
        assertEquals("getLargestQueueSize", 2, queue.getLargestQueueSize());
        assertEquals("size", 2, queue.size());

        assertThrows(IllegalStateException.class, () -> queue.add("3"));
        assertEquals("getLargestQueueSize", 2, queue.getLargestQueueSize());
        assertEquals("size", 2, queue.size());
    }

    @Test
    public void testAddAll() {
        TrackingLinkedBlockingQueue<String> queue = new TrackingLinkedBlockingQueue<>(3);

        queue.addAll(Arrays.asList("1", "2"));
        assertEquals("getLargestQueueSize", 2, queue.getLargestQueueSize());
        assertEquals("size", 2, queue.size());

        assertThrows(IllegalStateException.class, () -> queue.addAll(Arrays.asList("3", "4")));
        assertEquals("getLargestQueueSize", 3, queue.getLargestQueueSize());
        assertEquals("size", 3, queue.size());
    }
}
