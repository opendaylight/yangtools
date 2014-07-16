/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util.concurrent;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * Unit tests for QueuedNotificationManager.
 *
 * @author Thomas Pantelis
 */
public class QueuedNotificationManagerTest {

    static class TestListener<N> {

        private final List<N> actual;
        private volatile int expCount;
        private volatile CountDownLatch latch;
        volatile long sleepTime = 0;
        volatile RuntimeException runtimeEx;
        volatile Error jvmError;

        TestListener( int expCount ) {
            actual = Collections.synchronizedList( Lists.<N>newArrayListWithCapacity( expCount ) );
            reset( expCount );
        }

        void reset( int expCount ) {
            this.expCount = expCount;
            latch = new CountDownLatch( expCount );
            actual.clear();
        }

        void onNotification( N data ) {

            try {
                if( sleepTime > 0 ) {
                    Uninterruptibles.sleepUninterruptibly( sleepTime, TimeUnit.MILLISECONDS );
                }

                actual.add( data );

                RuntimeException localRuntimeEx = runtimeEx;
                if( localRuntimeEx != null ) {
                    runtimeEx = null;
                    throw localRuntimeEx;
                }

                Error localJvmError = jvmError;
                if( localJvmError != null ) {
                    jvmError = null;
                    throw localJvmError;
                }

            } finally {
                latch.countDown();
            }
        }

        void verifyNotifications() {
            boolean done = Uninterruptibles.awaitUninterruptibly( latch, 5, TimeUnit.SECONDS );
            if( !done ) {
                long actualCount = latch.getCount();
                fail( "Received " + (expCount - actualCount) + " notifications. Expected " + expCount );
            }
        }

        void verifyNotifications( List<N> expected ) {
            verifyNotifications();
            assertEquals( "Notifications", Lists.newArrayList( expected ), actual );
        }

        // Implement bad hashCode/equals methods to verify it doesn't screw up the
        // QueuedNotificationManager as it should use reference identity.
        @Override
        public int hashCode(){
            return 1;
        }

        @Override
        public boolean equals( Object obj ){
            TestListener<?> other = (TestListener<?>) obj;
            return other != null;
        }
    }

    static class TestListener2<N> extends TestListener<N> {
        TestListener2( int expCount ) {
            super(expCount);
        }
    }

    static class TestListener3<N> extends TestListener<N> {
        TestListener3( int expCount ) {
            super(expCount);
        }
    }

    static class TestNotifier<N> implements QueuedNotificationManager.Invoker<TestListener<N>,N> {

        @Override
        public void invokeListener( TestListener<N> listener, N notification ) {
            listener.onNotification( notification );
        }
    }

    private ExecutorService queueExecutor;

    @After
    public void tearDown() {
        if( queueExecutor != null ) {
            queueExecutor.shutdownNow();
        }
    }

    @Test(timeout=10000)
    public void testNotificationsWithSingleListener() {

        queueExecutor = Executors.newFixedThreadPool( 2 );
        NotificationManager<TestListener<Integer>, Integer> manager =
                new QueuedNotificationManager<>( queueExecutor, new TestNotifier<Integer>() );

        int initialCount = 6;
        int nNotifications = 100;

        TestListener<Integer> listener = new TestListener<>( nNotifications );
        listener.sleepTime = 20;

        manager.addNotifications( listener, Arrays.asList( 1, 2 ) );
        manager.addNotification( listener, 3 );
        manager.addNotifications( listener, Arrays.asList( 4, 5 ) );
        manager.addNotification( listener, 6 );

        manager.addNotifications( null, Collections.<Integer>emptyList() );
        manager.addNotifications( listener, null );
        manager.addNotification( listener, null );

        Uninterruptibles.sleepUninterruptibly( 100, TimeUnit.MILLISECONDS );

        listener.sleepTime = 0;

        List<Integer> expNotifications = Lists.newArrayListWithCapacity( nNotifications );
        expNotifications.addAll( Arrays.asList( 1, 2, 3, 4, 5, 6 ) );
        for( int i = 1; i <= nNotifications - initialCount; i++ ) {
            Integer v = Integer.valueOf( initialCount + i );
            expNotifications.add( v );
            manager.addNotification( listener, v );
        }

        listener.verifyNotifications( expNotifications );
    }

    @Test(timeout=10000)
    public void testNotificationsWithMultipleListeners() {

        int nListeners = 5;
        queueExecutor = Executors.newFixedThreadPool( nListeners );
        final NotificationManager<TestListener<Integer>, Integer> manager =
                new QueuedNotificationManager<>( queueExecutor, new TestNotifier<Integer>() );

        final int nNotifications = 10000;

        System.out.println( "Testing " + nListeners + " listeners with " + nNotifications +
                            " notifications each..." );

        Stopwatch stopWatch = new Stopwatch();
        stopWatch.start();

        List<TestListener<Integer>> listeners = Lists.newArrayList();
        for( int i = 1; i <= nListeners; i++ ) {
            final TestListener<Integer> listener =
                    i == 2 ? new TestListener2<Integer>( nNotifications ) :
                    i == 3 ? new TestListener3<Integer>( nNotifications ) :
                                      new TestListener<Integer>( nNotifications );
            listeners.add( listener );
            new Thread( new Runnable() {
                @Override
                public void run() {

                    for( int i = 1; i <= nNotifications; i++ ) {
                        Integer v = Integer.valueOf( i );
                        manager.addNotification( listener, v );
                    }
                }
            }, "TestListener" + i ).start();
        }

        for( TestListener<Integer> listener: listeners ) {
            listener.verifyNotifications();
        }

        stopWatch.stop();

        System.out.println( "Elapsed time: " + stopWatch );
        System.out.println( queueExecutor );
    }

    @Test(timeout=10000)
    public void testNotificationsWithListenerRuntimeEx() {

        queueExecutor = Executors.newFixedThreadPool( 1 );
        NotificationManager<TestListener<Integer>, Integer> manager =
                new QueuedNotificationManager<>( queueExecutor, new TestNotifier<Integer>() );

        TestListener<Integer> listener = new TestListener<>( 2 );
        listener.runtimeEx = new RuntimeException( "mock" );

        manager.addNotification( listener, 1 );
        manager.addNotification( listener, 2 );

        listener.verifyNotifications();
    }

    @Test(timeout=10000)
    public void testNotificationsWithListenerJVMError() {

        final CountDownLatch errorCaughtLatch = new CountDownLatch( 1 );
        queueExecutor = new ThreadPoolExecutor( 1, 1, 0, TimeUnit.SECONDS,
                                                new LinkedBlockingQueue<Runnable>() ) {
             @Override
             public void execute( final Runnable command ) {
                 super.execute( new Runnable() {
                    @Override
                    public void run() {
                        try {
                            command.run();
                        } catch( Error e ) {
                            errorCaughtLatch.countDown();
                        }
                    }
                });
             }
        };

        NotificationManager<TestListener<Integer>, Integer> manager =
                new QueuedNotificationManager<>( queueExecutor, new TestNotifier<Integer>() );

        TestListener<Integer> listener = new TestListener<>( 2 );
        listener.jvmError = new Error( "mock" );

        manager.addNotification( listener, 1 );

        assertEquals( "JVM Error caught", true, Uninterruptibles.awaitUninterruptibly(
                                                       errorCaughtLatch, 5, TimeUnit.SECONDS ) );

        manager.addNotification( listener, 2 );

        listener.verifyNotifications();
    }
}
