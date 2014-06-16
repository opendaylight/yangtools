package org.opendaylight.yangtools.util;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Utility executor, which blocks execution of tasks
 * until it execution is allowed.
 * <p/>
 * If execution of tasks is not allowed, tasks are
 * scheduled for later execution.
 */
public class ManuallyTriggeredExecutionExecutor implements Executor {

    private final static Class<?> SAME_THREAD_EXECUTOR = MoreExecutors.sameThreadExecutor().getClass();
    private final Executor delegate;

    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private boolean blocked;

    public ManuallyTriggeredExecutionExecutor(final Executor delegate) {
        Preconditions.checkNotNull(delegate, "Delegate Should not be null");
        Preconditions.checkArgument(!SAME_THREAD_EXECUTOR.equals(delegate.getClass()),
                "MoreExecutors.sameThreadExecutor() is not supported as delegate.");
        this.delegate = delegate;
        blockExecution();
    }

    @Override
    public synchronized void execute(final Runnable command) {
        if (!blocked) {
            delegate.execute(command);
        } else {
            taskQueue.add(command);
        }

    }

    /**
     * All subsequent tasks submitted will not be executed until {@link #unblockExecution()} is called.
     */
    public synchronized void blockExecution() {
        blocked = true;
    }

    /**
     * Submit all queued up tasks to the underlying executor.
     */
    public synchronized void unblockExecution() {
        while (!taskQueue.isEmpty()) {
            delegate.execute(taskQueue.poll());
        }
        blocked = false;
    }
}
