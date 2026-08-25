/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.testutils.mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opendaylight.yangtools.testutils.mockito.MoreAnswers.exception;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Test to illustrate the basic use of Mockito VS the EXCEPTION_ANSWER. Also useful as example to contrast this approach
 * with the REAL_OR_EXCEPTION approach illustrated in the MikitoTest.
 *
 * @see MikitoTest
 * @author Michael Vorburger
 */
class MockitoExampleTutorialTest {

    interface SomeService {

        void foo();

        String bar(String arg);

        // Most methods on real world services have complex input (and output objects), not just int or String
        int foobar(Path file);
    }

    @Test
    @SuppressWarnings("DirectInvocationOnMock")
    void usingMockitoWithoutStubbing() {
        final var service = mock(SomeService.class);
        assertNull(service.bar("hulo"));
    }

    @Test
    @SuppressWarnings("DirectInvocationOnMock")
    void usingMockitoToStubSimpleCase() {
        final var service = mock(SomeService.class);
        when(service.foobar(any())).thenReturn(123);
        assertEquals(123, service.foobar(Path.of("hello.txt")));
    }

    @Test
    @SuppressWarnings("DirectInvocationOnMock")
    void usingMockitoToStubComplexCase() {
        final var service = mock(SomeService.class);
        when(service.foobar(any()))
            .thenAnswer(invocation -> (Path.of("hello.txt").equals(invocation.getArgument(0, Path.class)) ? 123 : 0));
        assertEquals(0, service.foobar(Path.of("belo.txt")));
    }

    @Test
    void usingMockitoExceptionException() {
        final var service = mock(SomeService.class, exception());
        assertThrows(UnstubbedMethodException.class, () -> service.foo());
    }

    @Test
    void usingMockitoNoExceptionIfStubbed() {
        final var service = mock(SomeService.class, exception());
        // NOT when(s.foobar(any())).thenReturn(123) BUT must be like this:
        doReturn(123).when(service).foobar(any());
        assertEquals(123, service.foobar(Path.of("hello.txt")));
        assertThrows(UnstubbedMethodException.class, service::foo);
    }

    @Test
    void usingMockitoToStubComplexCaseAndExceptionIfNotStubbed() {
        final var service = mock(SomeService.class, exception());
        doAnswer(invocation -> (Path.of("hello.txt").equals(invocation.getArgument(0, Path.class)) ? 123 : 0))
            .when(service).foobar(any());
        assertEquals(123, service.foobar(Path.of("hello.txt")));
        assertEquals(0, service.foobar(Path.of("belo.txt")));
    }
}
