/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ManuallyTriggeredExecutionExecutorTest {
    private static final int threadPoolSize = 10, numberOfTasks = 5;
    ManuallyTriggeredExecutionExecutor tested;
    private AtomicInteger counter;
    private ThreadPoolExecutor fixed;

    @Before
    public void setUp() throws Exception {
        counter = new AtomicInteger();
        fixed = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
        fixed.prestartAllCoreThreads();
        tested = new ManuallyTriggeredExecutionExecutor(MoreExecutors.listeningDecorator(fixed));
    }

    @After
    public void tearDown() throws Exception {
        fixed.shutdown();
    }

    @Test
    public void testExecute() throws Exception {
        testExecutorMethod(new Runnable() {
            @Override
            public void run() {
                tested.execute(testTask);
            }
        });
    }

    private void testExecutorMethod(Runnable runnable) throws InterruptedException {
        assertEquals(0, counter.get());
        for (int i = 0; i < numberOfTasks; i++) {
            runnable.run();
        }
        Thread.sleep(100);
        assertEquals(0, counter.get());
        tested.unblockExecution();
        Thread.sleep(100);
        assertEquals(numberOfTasks, counter.get());
    }


    private final Runnable testTask = new Runnable() {
        @Override
        public void run() {
            counter.incrementAndGet();
        }
    };
}
