/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * A {@link LinkedBlockingQueue} that tracks the largest queue size for debugging.
 *
 * @author Thomas Pantelis
 *
 * @param <E> the element t.ype
 */
public class TrackingLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {

    private static final long serialVersionUID = 1L;

    /**
     * Holds largestQueueSize, this long field should be only accessed
     * using {@value #LARGEST_QUEUE_SIZE_UPDATER}
     */
    private volatile long largestQueueSize = 0;
    
    @SuppressWarnings("rawtypes")
    private static AtomicLongFieldUpdater<TrackingLinkedBlockingQueue> LARGEST_QUEUE_SIZE_UPDATER = AtomicLongFieldUpdater.newUpdater(TrackingLinkedBlockingQueue.class, "largestQueueSize");

    /**
     * @see LinkedBlockingQueue#LinkedBlockingQueue
     */
    public TrackingLinkedBlockingQueue() {
        super();
    }

    /**
     * @see LinkedBlockingQueue#LinkedBlockingQueue(Collection)
     */
    public TrackingLinkedBlockingQueue( Collection<? extends E> c ) {
        super(c);
    }

    /**
     * @see LinkedBlockingQueue#LinkedBlockingQueue(int)
     */
    public TrackingLinkedBlockingQueue( int capacity ) {
        super(capacity);
    }

    /**
     * Returns the largest queue size.
     */
    public long getLargestQueueSize(){
        return LARGEST_QUEUE_SIZE_UPDATER.get(this);
    }

    @Override
    public boolean offer( E e, long timeout, TimeUnit unit ) throws InterruptedException {
        if( super.offer( e, timeout, unit ) ) {
            updateLargestQueueSize();
            return true;
        }

        return false;
    }

    @Override
    public boolean offer( E e ) {
        if( super.offer( e ) ) {
            updateLargestQueueSize();
            return true;
        }

        return false;
    }

    @Override
    public void put( E e ) throws InterruptedException {
        super.put( e );
        updateLargestQueueSize();
    }

    @Override
    public boolean add( E e ) {
        boolean result = super.add( e );
        updateLargestQueueSize();
        return result;
    }

    @Override
    public boolean addAll( Collection<? extends E> c ) {
        try {
            return super.addAll( c );
        } finally {
            updateLargestQueueSize();
        }
    }

    private void updateLargestQueueSize() {
        long size = size();
        long largest = LARGEST_QUEUE_SIZE_UPDATER.get(this);
        if( size > largest ) {
            LARGEST_QUEUE_SIZE_UPDATER.compareAndSet(this, largest, size );
        }
    }
}
