/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.yangtools.util.ExecutorServiceUtil;
import org.opendaylight.yangtools.util.concurrent.ThreadPoolExecutorTest.Task;

/**
 * Unit tests for CountingRejectedExecutionHandler.
 *
 * @author Thomas Pantelis
 */
public class CountingRejectedExecutionHandlerTest {

    private ThreadPoolExecutor executor;

    @After
    public void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    public void testCallerRunsPolicyHandler() throws InterruptedException {

        CountDownLatch tasksRunLatch = new CountDownLatch(1);
        CountDownLatch blockLatch = new CountDownLatch(1);

        executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
                ExecutorServiceUtil.offerFailingBlockingQueue(new LinkedBlockingQueue<>()));

        CountingRejectedExecutionHandler countingHandler = CountingRejectedExecutionHandler.newCallerRunsPolicy();
        executor.setRejectedExecutionHandler(countingHandler);

        executor.execute(new Task(tasksRunLatch, blockLatch));

        int tasks = 5;
        for (int i = 0; i < tasks - 1; i++) {
            executor.execute(new Task(null, null, null, null, 0));
        }

        assertEquals("getRejectedTaskCount", tasks - 1, countingHandler.getRejectedTaskCount());

        blockLatch.countDown();

        assertTrue("Tasks complete", tasksRunLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testAbortPolicyHandler() throws InterruptedException {

        CountDownLatch tasksRunLatch = new CountDownLatch(1);
        CountDownLatch blockLatch = new CountDownLatch(1);

        executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
                ExecutorServiceUtil.offerFailingBlockingQueue(new LinkedBlockingQueue<>()));

        CountingRejectedExecutionHandler countingHandler = CountingRejectedExecutionHandler.newAbortPolicy();
        executor.setRejectedExecutionHandler(countingHandler);

        executor.execute(new Task(tasksRunLatch, blockLatch));

        int tasks = 5;
        for (int i = 0; i < tasks - 1; i++) {
            assertThrows(RejectedExecutionException.class, () -> executor.execute(new Task(null, null, null, null, 0)));
        }

        assertEquals("getRejectedTaskCount", tasks - 1, countingHandler.getRejectedTaskCount());

        blockLatch.countDown();

        assertTrue("Tasks complete", tasksRunLatch.await(5, TimeUnit.SECONDS));
    }
}
