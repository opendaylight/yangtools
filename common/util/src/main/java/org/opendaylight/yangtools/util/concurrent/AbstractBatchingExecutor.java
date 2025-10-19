/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages queuing and dispatching tasks for multiple workers concurrently. Tasks are queued on a per-worker
 * basis and dispatched serially to each worker via an {@link Executor}.
 *
 * <p>This class optimizes its memory footprint by only allocating and maintaining a queue and executor
 * task for a worker when there are pending tasks. On the first task(s), a queue is created and a dispatcher task is
 * submitted to the executor to dispatch the queue to the associated worker. Any subsequent tasks that occur before all
 * previous tasks have been dispatched are appended to the existing queue. When all tasks have been dispatched,
 * the queue and dispatcher task are discarded.
 *
 * @author Thomas Pantelis
 * @author Robert Varga
 *
 * @param <K> worker key type
 * @param <T> task type
 */
abstract class AbstractBatchingExecutor<K, T> extends AbstractSimpleIdentifiable<String> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBatchingExecutor.class);

    /**
     * Caps the maximum number of attempts to offer a task to a particular worker. Each attempt window is 1 minute, so
     * an offer times out after roughly 10 minutes.
     */
    private static final int MAX_NOTIFICATION_OFFER_MINUTES = 10;
    private static final long GIVE_UP_NANOS = TimeUnit.MINUTES.toNanos(MAX_NOTIFICATION_OFFER_MINUTES);
    private static final long TASK_WAIT_NANOS = TimeUnit.MILLISECONDS.toNanos(10);

    private final ConcurrentMap<K, DispatcherTask> dispatcherTasks = new ConcurrentHashMap<>();
    private final @NonNull Executor executor;
    private final int maxQueueCapacity;

    AbstractBatchingExecutor(final @NonNull String name, final @NonNull Executor executor, final int maxQueueCapacity) {
        super(name);
        this.executor = requireNonNull(executor);
        checkArgument(maxQueueCapacity > 0, "Invalid maxQueueCapacity %s must be > 0", maxQueueCapacity);
        this.maxQueueCapacity = maxQueueCapacity;
    }

    /**
     * Returns the maximum worker queue capacity.
     */
    final int maxQueueCapacity() {
        return maxQueueCapacity;
    }

    /**
     * Returns the {@link Executor} to used for dispatcher tasks.
     */
    final @NonNull Executor executor() {
        return executor;
    }

    // FIXME: YANGTOOLS-1016: allow explicit blocking control
    final void submitTask(final K key, final T task) {
        submitTasks(key, Collections.singletonList(requireNonNull(task)));
    }

    // FIXME: YANGTOOLS-1016: allow explicit blocking control with return of un-enqueued tasks (or removal from input)
    final void submitTasks(final K key, final Iterable<T> tasks) {
        if (tasks == null || key == null) {
            return;
        }

        LOG.trace("{}: submitTasks for worker {}: {}", getIdentifier(), key, tasks);

        // Keep looping until we are either able to add a new DispatcherTask or are able to add our tasks to an existing
        // DispatcherTask. Eventually one or the other will occur.
        try {
            Iterator<T> it = tasks.iterator();

            while (true) {
                DispatcherTask task = dispatcherTasks.get(key);
                if (task == null) {
                    // No task found, try to insert a new one
                    final DispatcherTask newTask = new DispatcherTask(key, it);
                    task = dispatcherTasks.putIfAbsent(key, newTask);
                    if (task == null) {
                        // We were able to put our new task - now submit it to the executor and we're done. If it throws
                        // a RejectedExecutionException, let that propagate to the caller.
                        runTask(key, newTask);
                        break;
                    }

                    // We have a racing task, hence we can continue, but we need to refresh our iterator from the task.
                    it = newTask.recoverItems();
                }

                final boolean completed = task.submitTasks(it);
                if (!completed) {
                    // Task is indicating it is exiting before it has consumed all the items and is exiting. Rather
                    // than spinning on removal, we try to replace it.
                    final DispatcherTask newTask = new DispatcherTask(key, it);
                    if (dispatcherTasks.replace(key, task, newTask)) {
                        runTask(key, newTask);
                        break;
                    }

                    // We failed to replace the task, hence we need retry. Note we have to recover the items to be
                    // published from the new task.
                    it = newTask.recoverItems();
                    LOG.debug("{}: retrying task queueing for {}", getIdentifier(), key);
                    continue;
                }

                // All tasks have either been delivered or we have timed out and warned about the ones we have failed
                // to deliver. In any case we are done here.
                break;
            }
        } catch (InterruptedException e) {
            // We were interrupted trying to offer to the worker's queue. Somebody's probably telling us to quit.
            LOG.warn("{}: Interrupted trying to add to {} worker's queue", getIdentifier(), key);
        }

        LOG.trace("{}: submitTasks done for worker {}", getIdentifier(), key);
    }

    final Stream<DispatcherTask> streamTasks() {
        return dispatcherTasks.values().stream();
    }

    abstract void executeBatch(K key, @NonNull ImmutableList<T> tasks) throws Exception;

    private void runTask(final K key, final DispatcherTask task) {
        LOG.debug("{}: Submitting DispatcherTask for worker {}", getIdentifier(), key);
        executor.execute(task);
    }

    /**
     * Executor task for a single worker that queues tasks and sends them serially to the worker.
     */
    final class DispatcherTask implements Runnable {
        private final Lock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();
        private final Condition notFull = lock.newCondition();
        private final @NonNull K key;

        @GuardedBy("lock")
        private final Queue<T> queue = new ArrayDeque<>();
        @GuardedBy("lock")
        private boolean exiting;

        DispatcherTask(final @NonNull K key, final @NonNull Iterator<T> tasks) {
            this.key = requireNonNull(key);
            while (tasks.hasNext()) {
                final T task = tasks.next();
                if (task != null) {
                    queue.add(task);
                }
            }
        }

        @NonNull Iterator<T> recoverItems() {
            // This violates @GuardedBy annotation, but is invoked only when the task is not started and will never
            // get started, hence this is safe.
            return queue.iterator();
        }

        @NonNull K key() {
            return key;
        }

        int size() {
            lock.lock();
            try {
                return queue.size();
            } finally {
                lock.unlock();
            }
        }

        boolean submitTasks(final @NonNull Iterator<T> tasks) throws InterruptedException {
            final long start = System.nanoTime();
            final long deadline = start + GIVE_UP_NANOS;

            lock.lock();
            try {
                // Lock may have blocked for some time, we need to take that into account. We may have exceeded
                // the deadline, but that is unlikely and even in that case we can make some progress without further
                // blocking.
                long canWait = deadline - System.nanoTime();

                while (true) {
                    // Check the exiting flag - if true then #run is in the process of exiting so return false
                    // to indicate such. Otherwise, offer the tasks to the queue.
                    if (exiting) {
                        return false;
                    }

                    final int avail = maxQueueCapacity - queue.size();
                    if (avail <= 0) {
                        if (canWait <= 0) {
                            LOG.warn("{}: Failed to offer tasks {} to the queue for worker {}. Exceeded "
                                + "maximum allowable time of {} minutes; the worker is likely in an unrecoverable "
                                + "state (deadlock or endless loop). ", getIdentifier(), ImmutableList.copyOf(tasks),
                                key, MAX_NOTIFICATION_OFFER_MINUTES);
                            return true;
                        }

                        canWait = notFull.awaitNanos(canWait);
                        continue;
                    }

                    for (int i = 0; i < avail; ++i) {
                        if (!tasks.hasNext()) {
                            notEmpty.signal();
                            return true;
                        }

                        queue.add(tasks.next());
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        @Holding("lock")
        private boolean waitForQueue() {
            long timeout = TASK_WAIT_NANOS;

            while (queue.isEmpty()) {
                if (timeout <= 0) {
                    return false;
                }

                try {
                    timeout = notEmpty.awaitNanos(timeout);
                } catch (InterruptedException e) {
                    // The executor is probably shutting down so log as debug.
                    LOG.debug("{}: Interrupted trying to remove from {} worker's queue", getIdentifier(), key);
                    return false;
                }
            }

            return true;
        }

        @Override
        public void run() {
            try {
                // Loop until we've dispatched all the tasks in the queue.
                while (true) {
                    final @NonNull ImmutableList<T> tasks;

                    lock.lock();
                    try {
                        if (!waitForQueue()) {
                            exiting = true;
                            break;
                        }

                        // Splice the entire queue
                        tasks = ImmutableList.copyOf(queue);
                        queue.clear();

                        notFull.signalAll();
                    } finally {
                        lock.unlock();
                    }

                    invokeWorker(tasks);
                }
            } finally {
                // We're exiting, gracefully or not - either way make sure we always remove
                // ourselves from the cache.
                dispatcherTasks.remove(key, this);
            }
        }

        @SuppressWarnings("checkstyle:illegalCatch")
        private void invokeWorker(final @NonNull ImmutableList<T> tasks) {
            LOG.debug("{}: Invoking worker {} with tasks: {}", getIdentifier(), key, tasks);
            try {
                executeBatch(key, tasks);
            } catch (Exception e) {
                // We'll let a RuntimeException from the worker slide and keep sending any remaining tasks.
                LOG.error("{}: Error invoking worker {} with {}", getIdentifier(), key, tasks, e);
            }
        }
    }
}
