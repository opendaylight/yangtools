/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.testutils.mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.opendaylight.yangtools.testutils.mockito.MoreAnswers.realOrException;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Test to illustrate the use of the REAL_OR_EXCEPTION. Also useful as example to contrast this approach illustrated in
 * the MockitoExampleTutorialTest.
 *
 * @see MockitoExampleTutorialTest
 * @author Michael Vorburger
 */
class MikitoTest {

    interface SomeService {

        void foo();

        String bar(String arg);

        // Most methods on real world services have complex input (and output objects), not just int or String
        int foobar(Path file);
    }

    @Test
    void usingMikitoToCallStubbedMethod() {
        final var service = mock(MockSomeService.class, realOrException());
        assertEquals(123, service.foobar(Path.of("hello.txt")));
        assertEquals(0, service.foobar(Path.of("belo.txt")));
    }

    @Test
    void usingMikitoToCallUnstubbedMethodAndExpectException() {
        var service = mock(MockSomeService.class, realOrException());
        var ex = assertThrows(UnstubbedMethodException.class, service::foo);
        assertEquals("foo() is not implemented in mockSomeService", ex.getMessage());
    }

    abstract static class MockSomeService implements SomeService {
        @Override
        public int foobar(final Path file) {
            return Path.of("hello.txt").equals(file) ? 123 : 0;
        }
    }
}
