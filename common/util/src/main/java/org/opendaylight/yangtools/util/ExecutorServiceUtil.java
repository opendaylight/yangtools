/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.util.concurrent.ForwardingBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for dealing with {@link ExecutorService}s.
 */
public final class ExecutorServiceUtil {
    private static final class WaitInQueueExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
            if (executor.isShutdown() ) {
                throw new RejectedExecutionException( "Executor has been shutdown." );
            }

            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                LOG.debug("Interrupted while attempting to put to the queue", e);
                throw new RejectedExecutionException("Interrupted while attempting to put to the queue", e);
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceUtil.class);
    private static final RejectedExecutionHandler WAIT_IN_QUEUE_HANDLER = new WaitInQueueExecutionHandler();

    private ExecutorServiceUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates a {@link BlockingQueue} which does not allow for non-blocking addition to the queue.
     * This is useful with {@link #waitInQueueExecutionHandler()} to turn force a
     * {@link ThreadPoolExecutor} to create as many threads as it is configured to before starting
     * to fill the queue.
     *
     * @param delegate Backing blocking queue.
     * @return A new blocking queue backed by the delegate
     */
    public static <E> BlockingQueue<E> offerFailingBlockingQueue(final BlockingQueue<E> delegate) {
        return new ForwardingBlockingQueue<E>() {
            @Override
            public boolean offer(@Nonnull final E o) {
                return false;
            }

            @Override
            protected BlockingQueue<E> delegate() {
                return delegate;
            }
        };
    }

    /**
     * Returns a {@link RejectedExecutionHandler} which blocks on the {@link ThreadPoolExecutor}'s
     * backing queue if a new thread cannot be spawned.
     *
     * @return A shared RejectedExecutionHandler instance.
     */
    public static RejectedExecutionHandler waitInQueueExecutionHandler() {
        return WAIT_IN_QUEUE_HANDLER;
    }

    /**
     * Tries to shutdown the given executor gracefully by awaiting termination for the given
     * timeout period. If the timeout elapses before termination, the executor is forcefully
     * shutdown.
     */
    public static void tryGracefulShutdown(final ExecutorService executor, long timeout,
            TimeUnit unit ) {

        executor.shutdown();

        try {
            if (!executor.awaitTermination(timeout, unit)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
