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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ObjectRegistrationTest {
    @Mock
    private Runnable callback;
    @Mock
    private Object instance;

    @Test
    void rejectNullCallback() {
        assertThrows(NullPointerException.class, () -> ObjectRegistration.of(instance, null));
        assertThrows(NullPointerException.class, () -> ObjectRegistration.of(null, callback));
    }

    @Test
    void testClose() {
        try (var reg = ObjectRegistration.of(instance, callback)) {
            doNothing().when(callback).run();
        }
        verify(callback).run();
    }

    @Test
    void testMultipleClose() {
        try (var reg = ObjectRegistration.of(instance, callback)) {
            doNothing().when(callback).run();
            reg.close();
        }
        verify(callback).run();
    }

    @Test
    void testToString() {
        doReturn("instanceToString").when(instance).toString();
        doReturn("callbackToString").when(callback).toString();
        assertEquals("CallbackObjectRegistration{closed=false, instance=instanceToString, callback=callbackToString}",
            ObjectRegistration.of(instance, callback).toString());
    }

    @Test
    void testGetInstance() {
        assertSame(instance, ObjectRegistration.of(instance, callback).getInstance());
    }
}
