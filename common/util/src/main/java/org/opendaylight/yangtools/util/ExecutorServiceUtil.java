/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ForwardingBlockingQueue;

/**
 * Utility methods for dealing with {@link ExecutorService}s.
 */
public final class ExecutorServiceUtil {
	private static final class WaitInQueueExecutionHandler implements RejectedExecutionHandler {
		@Override
		public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
			try {
				executor.getQueue().put(r);
			} catch (InterruptedException e) {
				LOG.debug("Intterupted while waiting for queue", e);
				throw new RejectedExecutionException("Interrupted while waiting for queue", e);
			}
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceUtil.class);
	private static final RejectedExecutionHandler WAIT_IN_QUEUE_HANDLER = new WaitInQueueExecutionHandler();

	private ExecutorServiceUtil() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Create a {@link BlockingQueue} which does not allow for non-blocking addition to the queue.
	 * This is useful with {@link #waitInQueueExecutionHandler()} to turn force a
	 * {@link ThreadPoolExecutor} to create as many threads as it is configured to before starting
	 * to fill the queue.
	 *
	 * @param delegate Backing blocking queue.
	 * @return A new blocking queue backed by the delegate
	 */
	public <E> BlockingQueue<E> offerFailingBlockingQueue(final BlockingQueue<E> delegate) {
		return new ForwardingBlockingQueue<E>() {
			@Override
			public boolean offer(final E o) {
				return false;
			}

			@Override
			protected BlockingQueue<E> delegate() {
				return delegate;
			}
		};
	}

	/**
	 * Return a {@link RejectedExecutionHandler} which blocks on the {@link ThreadPoolExecutor}'s
	 * backing queue if a new thread cannot be spawned.
	 *
	 * @return A shared RejectedExecutionHandler instance.
	 */
	public RejectedExecutionHandler waitInQueueExecutionHandler() {
		return WAIT_IN_QUEUE_HANDLER;
	}
}
