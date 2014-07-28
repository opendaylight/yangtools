/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Unit tests for DeadlockDetectingListeningExecutorService.
 *
 * @author Thomas Pantelis
 */
public class DeadlockDetectingListeningExecutorServiceTest {

    interface Invoker {
        ListenableFuture<?> invokeExecutor( ListeningExecutorService executor );
    };

    static final Invoker SUBMIT_CALLABLE = new Invoker() {
        @Override
        public ListenableFuture<?> invokeExecutor( ListeningExecutorService executor ) {
            return executor.submit( new Callable<String>() {
                @Override
                public String call() throws Exception{
                    return "foo";
                }
            } );
        }
    };

    static final Invoker SUBMIT_RUNNABLE =  new Invoker() {
        @Override
        public ListenableFuture<?> invokeExecutor( ListeningExecutorService executor ) {
            return executor.submit( new Runnable() {
                @Override
                public void run(){
                }
            } );
        }
    };

    static final Invoker SUBMIT_RUNNABLE_WITH_RESULT = new Invoker() {
        @Override
        public ListenableFuture<?> invokeExecutor( ListeningExecutorService executor ) {
            return executor.submit( new Runnable() {
                @Override
                public void run(){
                }
            }, "foo" );
        }
    };

    interface InitialInvoker {
        void invokeExecutor( ListeningExecutorService executor, Runnable task );
    };

    static final InitialInvoker SUBMIT = new InitialInvoker() {
        @Override
        public void invokeExecutor( ListeningExecutorService executor, Runnable task ) {
            executor.submit( task );
        }
    };

    static final InitialInvoker EXECUTE = new InitialInvoker() {
        @Override
        public void invokeExecutor( ListeningExecutorService executor, Runnable task ) {
            executor.execute( task );
        }
    };

    @SuppressWarnings("serial")
    public static class TestDeadlockException extends Exception {
    }

    public static Function<Void, Exception> DEADLOCK_EXECUTOR_FUNCTION = new Function<Void, Exception>() {
        @Override
        public Exception apply( Void notUsed ) {
            return new TestDeadlockException();
        }
    };

    DeadlockDetectingListeningExecutorService executor;

    @Before
    public void setup() {
        executor = new DeadlockDetectingListeningExecutorService( Executors.newSingleThreadExecutor(),
                                                                  DEADLOCK_EXECUTOR_FUNCTION );
    }

    @Test
    public void testBlockingSubmitOffExecutor() throws Exception {

        // Test submit with Callable.

        ListenableFuture<String> future = executor.submit( new Callable<String>() {
            @Override
            public String call() throws Exception{
                return "foo";
            }
        } );

        assertEquals( "Future result", "foo", future.get( 5, TimeUnit.SECONDS ) );

        // Test submit with Runnable.

        executor.submit( new Runnable() {
            @Override
            public void run(){
            }
        } ).get();

        // Test submit with Runnable and value.

        future = executor.submit( new Runnable() {
            @Override
            public void run(){
            }
        }, "foo" );

        assertEquals( "Future result", "foo", future.get( 5, TimeUnit.SECONDS ) );
    }

    @Test
    public void testNonBlockingSubmitOnExecutorThread() throws Throwable {

        testNonBlockingSubmitOnExecutorThread( SUBMIT, SUBMIT_CALLABLE );
        testNonBlockingSubmitOnExecutorThread( SUBMIT, SUBMIT_RUNNABLE );
        testNonBlockingSubmitOnExecutorThread( SUBMIT, SUBMIT_RUNNABLE_WITH_RESULT );

        testNonBlockingSubmitOnExecutorThread( EXECUTE, SUBMIT_CALLABLE );
    }

    void testNonBlockingSubmitOnExecutorThread( InitialInvoker initialInvoker,
                                                final Invoker invoker ) throws Throwable {

        final AtomicReference<Throwable> caughtEx = new AtomicReference<>();
        final CountDownLatch futureCompletedLatch = new CountDownLatch( 1 );

        Runnable task = new Runnable() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public void run() {

                Futures.addCallback( invoker.invokeExecutor( executor ), new FutureCallback() {
                    @Override
                    public void onSuccess( Object result ) {
                        futureCompletedLatch.countDown();
                    }

                    @Override
                    public void onFailure( Throwable t ) {
                        caughtEx.set( t );
                        futureCompletedLatch.countDown();
                    }
                } );
            }

        };

        initialInvoker.invokeExecutor( executor, task );

        assertTrue( "Task did not complete - executor likely deadlocked",
                    futureCompletedLatch.await( 5, TimeUnit.SECONDS ) );

        if( caughtEx.get() != null ) {
            throw caughtEx.get();
        }
    }

    @Test
    public void testBlockingSubmitOnExecutorThread() throws Exception {

        testBlockingSubmitOnExecutorThread( SUBMIT, SUBMIT_CALLABLE );
        testBlockingSubmitOnExecutorThread( SUBMIT, SUBMIT_RUNNABLE );
        testBlockingSubmitOnExecutorThread( SUBMIT, SUBMIT_RUNNABLE_WITH_RESULT );

        testBlockingSubmitOnExecutorThread( EXECUTE, SUBMIT_CALLABLE );
    }

    void testBlockingSubmitOnExecutorThread( InitialInvoker initialInvoker,
                                             final Invoker invoker ) throws Exception {

        final AtomicReference<Throwable> caughtEx = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch( 1 );

        Runnable task = new Runnable() {
            @Override
            public void run() {

                try {
                    invoker.invokeExecutor( executor ).get();
                } catch( ExecutionException e ) {
                    caughtEx.set( e.getCause() );
                } catch( Throwable e ) {
                    caughtEx.set( e );
                } finally {
                    latch.countDown();
                }
            }

        };

        initialInvoker.invokeExecutor( executor, task );

        assertTrue( "Task did not complete - executor likely deadlocked",
                    latch.await( 5, TimeUnit.SECONDS ) );

        assertNotNull( "Expected exception thrown", caughtEx.get() );
        assertEquals( "Caught exception type", TestDeadlockException.class, caughtEx.get().getClass() );
    }
}
