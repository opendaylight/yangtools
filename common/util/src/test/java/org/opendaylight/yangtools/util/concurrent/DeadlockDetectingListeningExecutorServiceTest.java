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
    public void testSubmitWithoutPotentialDeadlock() throws Exception {

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
    public void testSubmitWithPotentialDeadlock() throws Throwable {

        testSubmitWithPotentialDeadlock( new Invoker() {
            @Override
            public ListenableFuture<?> invokeExecutor( ListeningExecutorService executor ) {
                return executor.submit( new Callable<String>() {
                    @Override
                    public String call() throws Exception{
                        return "foo";
                    }
                } );
            }
        } );

        testSubmitWithPotentialDeadlock( new Invoker() {
            @Override
            public ListenableFuture<?> invokeExecutor( ListeningExecutorService executor ) {
                return executor.submit( new Runnable() {
                    @Override
                    public void run(){
                    }
                } );
            }
        } );

        testSubmitWithPotentialDeadlock( new Invoker() {
            @Override
            public ListenableFuture<?> invokeExecutor( ListeningExecutorService executor ) {
                return executor.submit( new Runnable() {
                    @Override
                    public void run(){
                    }
                }, "foo" );
            }
        } );
    }

    void testSubmitWithPotentialDeadlock( final Invoker invoker ) throws Throwable {

        final AtomicReference<Throwable> caughtEx = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch( 1 );
        executor.execute( new Runnable() {
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

        } );

        assertTrue( "Task did not complete - executor likely deadlocked",
                    latch.await( 5, TimeUnit.SECONDS ) );

        assertNotNull( "Expected exception thrown", caughtEx.get() );
        assertEquals( "Caught exception type", TestDeadlockException.class, caughtEx.get().getClass() );
    }
}
