/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.EventListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListenerRegistrationTest {
    @Mock
    private Runnable callback;
    @Mock
    private EventListener instance;

    @Test
    void rejectNullCallback() {
        assertThrows(NullPointerException.class, () -> ListenerRegistration.of(instance, null));
        assertThrows(NullPointerException.class, () -> ListenerRegistration.of(null, callback));
    }

    @Test
    void testClose() {
        try (var reg = ListenerRegistration.of(instance, callback)) {
            doNothing().when(callback).run();
        }
        verify(callback).run();
    }

    @Test
    void testMultipleClose() {
        try (var reg = ListenerRegistration.of(instance, callback)) {
            doNothing().when(callback).run();
            reg.close();
        }
        verify(callback).run();
    }

    @Test
    void testToString() {
        doReturn("instanceToString").when(instance).toString();
        doReturn("callbackToString").when(callback).toString();
        assertEquals("CallbackListenerRegistration{closed=false, instance=instanceToString, callback=callbackToString}",
            ListenerRegistration.of(instance, callback).toString());
    }

    @Test
    void testGetInstance() {
        assertSame(instance, ListenerRegistration.of(instance, callback).getInstance());
    }
}
