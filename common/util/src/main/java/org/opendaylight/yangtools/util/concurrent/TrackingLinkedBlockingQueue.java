/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import com.google.common.annotations.Beta;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link LinkedBlockingQueue} that tracks the largest queue size for debugging.
 *
 * @author Thomas Pantelis
 *
 * @param <E> the element t.ype
 */
public class TrackingLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private static final VarHandle LARGEST_QUEUE_SIZE;

    static {
        try {
            LARGEST_QUEUE_SIZE = MethodHandles.lookup()
                .findVarHandle(TrackingLinkedBlockingQueue.class, "largestQueueSize", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Holds largestQueueSize, this field should be only updated using {@link #LARGEST_QUEUE_SIZE}.
     */
    private volatile int largestQueueSize;

    /**
     * See {@link LinkedBlockingQueue#LinkedBlockingQueue()}.
     */
    public TrackingLinkedBlockingQueue() {
    }

    /**
     * See {@link LinkedBlockingQueue#LinkedBlockingQueue(Collection)}.
     */
    @SuppressWarnings("checkstyle:parameterName")
    public TrackingLinkedBlockingQueue(final @NonNull Collection<? extends E> c) {
        super(c);
    }

    /**
     * See {@link LinkedBlockingQueue#LinkedBlockingQueue(int)}.
     */
    public TrackingLinkedBlockingQueue(final int capacity) {
        super(capacity);
    }

    /**
     * Returns the largest queue size.
     */
    @Beta
    // FIXME: 11.0.0: return int
    public long getLargestQueueSize() {
        return largestQueueSize;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        if (super.offer(e, timeout, unit)) {
            updateLargestQueueSize();
            return true;
        }

        return false;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean offer(final E e) {
        if (super.offer(e)) {
            updateLargestQueueSize();
            return true;
        }

        return false;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void put(final E e) throws InterruptedException {
        super.put(e);
        updateLargestQueueSize();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean add(final E e) {
        boolean result = super.add(e);
        updateLargestQueueSize();
        return result;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean addAll(final Collection<? extends E> c) {
        try {
            return super.addAll(c);
        } finally {
            updateLargestQueueSize();
        }
    }

    private void updateLargestQueueSize() {
        final int size = size();

        int largest = (int) LARGEST_QUEUE_SIZE.getAcquire(this);
        while (largest < size) {
            largest = (int) LARGEST_QUEUE_SIZE.compareAndExchangeRelease(this, largest, size);
        }
    }
}
