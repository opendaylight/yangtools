/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * Some common test utilities.
 *
 * @author Thomas Pantelis
 */
public class CommonTestUtils {

    public interface Invoker {
        ListenableFuture<?> invokeExecutor( ListeningExecutorService executor,
                CountDownLatch blockingLatch );
    };

    public static final Invoker SUBMIT_CALLABLE = new Invoker() {
        @Override
        public ListenableFuture<?> invokeExecutor( ListeningExecutorService executor,
                final CountDownLatch blockingLatch ) {
            return executor.submit( new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if( blockingLatch != null ) {
                        Uninterruptibles.awaitUninterruptibly( blockingLatch );
                    }
                    return null;
                }
            } );
        }
    };

    public static final Invoker SUBMIT_RUNNABLE =  new Invoker() {
        @Override
        public ListenableFuture<?> invokeExecutor( ListeningExecutorService executor,
                final CountDownLatch blockingLatch ) {
            return executor.submit( new Runnable() {
                @Override
                public void run() {
                    if( blockingLatch != null ) {
                        Uninterruptibles.awaitUninterruptibly( blockingLatch );
                    }
                }
            } );
        }
    };

    public static final Invoker SUBMIT_RUNNABLE_WITH_RESULT = new Invoker() {
        @Override
        public ListenableFuture<?> invokeExecutor( ListeningExecutorService executor,
                final CountDownLatch blockingLatch ) {
            return executor.submit( new Runnable() {
                @Override
                public void run() {
                    if( blockingLatch != null ) {
                        Uninterruptibles.awaitUninterruptibly( blockingLatch );
                    }
                }
            }, "foo" );
        }
    };

    public static void testThreadPoolExecution( final ExecutorService executor,
            final int numTasksToRun, final String expThreadPrefix ) throws Exception {

        System.out.println( "\nTesting " + executor.getClass().getSimpleName() + " with " +
                numTasksToRun + " tasks." );

        final CountDownLatch tasksRunLatch = new CountDownLatch( numTasksToRun );
        final ConcurrentMap<Thread, AtomicLong> taskCountPerThread = new ConcurrentHashMap<>();
        final AtomicReference<AssertionError> threadError = new AtomicReference<>();

        Stopwatch stopWatch = new Stopwatch();
        stopWatch.start();

        new Thread() {
            @Override
            public void run() {
                for( int i = 0; i < numTasksToRun; i++ ) {
//                    if(i%100 == 0) {
//                        Uninterruptibles.sleepUninterruptibly( 20, TimeUnit.MICROSECONDS );
//                    }

                    executor.execute( new Task( tasksRunLatch, taskCountPerThread,
                                                threadError, expThreadPrefix ) );
                }
            }
        }.start();

        boolean done = tasksRunLatch.await( 15, TimeUnit.SECONDS );

        stopWatch.stop();

        if( !done ) {
            fail( (numTasksToRun - tasksRunLatch.getCount()) + " tasks did not execute" );
        }

        if( threadError.get() != null ) {
            throw threadError.get();
        }

        System.out.println( taskCountPerThread.size() + " threads used:" );
        for( Map.Entry<Thread, AtomicLong> e : taskCountPerThread.entrySet() ) {
            System.out.println( "  " + e.getKey().getName() + " - " + e.getValue() + " tasks" );
        }

        System.out.println( "\n" + executor );
        System.out.println( "\nElapsed time: " + stopWatch );
        System.out.println();

        executor.shutdownNow();
    }

    private static class Task implements Runnable {
        final CountDownLatch tasksRunLatch;
        final ConcurrentMap<Thread, AtomicLong> taskCountPerThread;
        final AtomicReference<AssertionError> threadError;
        final String expThreadPrefix;

        Task( CountDownLatch tasksRunLatch, ConcurrentMap<Thread, AtomicLong> taskCountPerThread,
                AtomicReference<AssertionError> threadError, String expThreadPrefix ) {
            this.tasksRunLatch = tasksRunLatch;
            this.taskCountPerThread = taskCountPerThread;
            this.threadError = threadError;
            this.expThreadPrefix = expThreadPrefix;
        }

        @Override
        public void run() {
            try {
                assertEquals( "Thread name starts with " + expThreadPrefix, true,
                        Thread.currentThread().getName().startsWith( expThreadPrefix ) );

                AtomicLong count = taskCountPerThread.get( Thread.currentThread() );
                if( count == null ) {
                    count = new AtomicLong( 0 );
                    AtomicLong prev = taskCountPerThread.putIfAbsent( Thread.currentThread(), count );
                    if( prev != null ) {
                        count = prev;
                    }
                }

                count.incrementAndGet();

            } catch( AssertionError e ) {
                threadError.set( e );
            } finally {
                tasksRunLatch.countDown();
            }
        }
    }
}
