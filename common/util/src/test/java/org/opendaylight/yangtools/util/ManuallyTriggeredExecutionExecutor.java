package org.opendaylight.yangtools.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ForwardingListeningExecutorService;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Utility executor, which blocks execution of tasks
 * until it execution is allowed.
 *
 * If execution of tasks is not allowed, tasks are
 * scheduled for later execution.
 *
 *
 *
 */
public class ManuallyTriggeredExecutionExecutor extends ForwardingListeningExecutorService {

    private final static Class<?> SAME_THREAD_EXECUTOR = MoreExecutors.sameThreadExecutor().getClass();
    private final ListeningExecutorService delegate;

    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private boolean blocked;

    public ManuallyTriggeredExecutionExecutor(final ListeningExecutorService delegate) {
        Preconditions.checkNotNull(delegate, "Delegate Should not be null");
        Preconditions.checkArgument(!SAME_THREAD_EXECUTOR.equals(delegate.getClass()),
                "MoreExecutors.sameThreadExecutor() is not supported as delegate.");
        this.delegate = delegate;
        blockExecution();
    }

    @Override
    public void execute(final Runnable command) {
        if(!blocked) {
            super.execute(command);
        } else {
            taskQueue.add(command);
        }

    }

    public synchronized void blockExecution() {
        blocked = true;
    }

    public synchronized void unblockExecution() {
        while(!taskQueue.isEmpty()) {
            super.execute(taskQueue.poll());
        }
        blocked = false;
    }

    @Override
    protected ListeningExecutorService delegate() {
        return delegate;
    }



}
