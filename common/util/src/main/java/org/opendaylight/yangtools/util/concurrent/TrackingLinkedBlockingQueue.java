/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import javax.annotation.Nonnull;

/**
 * A {@link LinkedBlockingQueue} that tracks the largest queue size for debugging.
 *
 * @author Thomas Pantelis
 *
 * @param <E> the element t.ype
 */
public class TrackingLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private static final AtomicIntegerFieldUpdater<TrackingLinkedBlockingQueue> LARGEST_QUEUE_SIZE_UPDATER
        = AtomicIntegerFieldUpdater.newUpdater(TrackingLinkedBlockingQueue.class, "largestQueueSize");

    /**
     * Holds largestQueueSize, this long field should be only accessed
     * using {@link #LARGEST_QUEUE_SIZE_UPDATER}.
     */
    private volatile int largestQueueSize = 0;

    /**
     * @see LinkedBlockingQueue#LinkedBlockingQueue
     */
    public TrackingLinkedBlockingQueue() {
        super();
    }

    /**
     * @see LinkedBlockingQueue#LinkedBlockingQueue(Collection)
     */
    public TrackingLinkedBlockingQueue( final Collection<? extends E> c ) {
        super(c);
    }

    /**
     * @see LinkedBlockingQueue#LinkedBlockingQueue(int)
     */
    public TrackingLinkedBlockingQueue( final int capacity ) {
        super(capacity);
    }

    /**
     * Returns the largest queue size.
     *
     * <p>FIXME: the this return will be changed to int in a future release.
     */
    @Beta
    public long getLargestQueueSize() {
        return largestQueueSize;
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        if (super.offer( e, timeout, unit ) ) {
            updateLargestQueueSize();
            return true;
        }

        return false;
    }

    @Override
    public boolean offer(@Nonnull final E e) {
        if (super.offer( e ) ) {
            updateLargestQueueSize();
            return true;
        }

        return false;
    }

    @Override
    public void put( final E e ) throws InterruptedException {
        super.put( e );
        updateLargestQueueSize();
    }

    @Override
    public boolean add(final E e) {
        boolean result = super.add( e );
        updateLargestQueueSize();
        return result;
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        try {
            return super.addAll( c );
        } finally {
            updateLargestQueueSize();
        }
    }

    private void updateLargestQueueSize() {
        final int size = size();

        int largest;
        do {
            largest = largestQueueSize;
        } while (size > largest && !LARGEST_QUEUE_SIZE_UPDATER.weakCompareAndSet(this, largest, size));
    }
}
