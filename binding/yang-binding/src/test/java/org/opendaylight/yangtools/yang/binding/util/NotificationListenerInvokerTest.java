/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.QName;

public class NotificationListenerInvokerTest {

    @Test
    public void fromTest() throws Exception {
        assertNotNull(NotificationListenerInvoker.from(TestInterface.class));
    }

    @Test(expected = IllegalStateException.class)
    public void fromWithExceptionTest() throws Throwable {
        try {
            NotificationListenerInvoker.from(TestPrivateInterface.class);
            fail("Expected IllegalAccessException");
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test(expected = WrongMethodTypeException.class)
    public void invokeNotification() throws Exception {
        final NotificationListener notificationListener = mock(NotificationListener.class);
        final MethodHandle methodHandle = mock(MethodHandle.class);
        final NotificationListenerInvoker notificationListenerInvoker =
                new NotificationListenerInvoker(ImmutableMap.of(QName.create("test"), methodHandle));

        notificationListenerInvoker.invokeNotification(notificationListener, QName.create("test"), null);
        fail("Expected WrongMethodTypeException, no method to invoke is supplied");
    }

    public interface TestInterface extends NotificationListener, Augmentation {
        QName QNAME = QName.create("test");
        void onTestNotificationInterface(TestNotificationInterface notif);
    }

    private interface TestPrivateInterface extends NotificationListener, Augmentation {
        QName QNAME = QName.create("test");
        void onTestNotificationInterface(TestNotificationInterface notif);
    }

    private interface TestNotificationInterface extends Notification {
        QName QNAME = QName.create("test");
    }
}