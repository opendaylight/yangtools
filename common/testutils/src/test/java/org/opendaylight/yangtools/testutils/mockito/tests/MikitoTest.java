/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.testutils.mockito.tests;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.opendaylight.yangtools.testutils.mockito.MoreAnswers.realOrException;

import java.io.File;
import org.junit.Test;
import org.opendaylight.yangtools.testutils.mockito.UnstubbedMethodException;

/**
 * Test to illustrate the use of the REAL_OR_EXCEPTION.
 *
 * <p>Also useful as example to contrast this approach illustrated in the MockitoExampleTutorialTest.
 *
 * @see MockitoExampleTutorialTest
 *
 * @author Michael Vorburger
 */
public class MikitoTest {

    interface SomeService {

        void foo();

        String bar(String arg);

        // Most methods on real world services have complex input (and output objects), not just int or String
        int foobar(File file);
    }

    @Test
    public void usingMikitoToCallStubbedMethod() {
        SomeService service = mock(MockSomeService.class, realOrException());
        assertEquals(123, service.foobar(new File("hello.txt")));
        assertEquals(0, service.foobar(new File("belo.txt")));
    }

    @Test
    public void usingMikitoToCallUnstubbedMethodAndExpectException() {
        MockSomeService service = mock(MockSomeService.class, realOrException());
        try {
            service.foo();
            fail();
        } catch (UnstubbedMethodException e) {
            assertThat(e.getMessage()).isEqualTo("foo() is not implemented in mockSomeService");
        }
    }

    abstract static class MockSomeService implements SomeService {
        @Override
        public int foobar(final File file) {
            return "hello.txt".equals(file.getName()) ? 123 : 0;
        }
    }
}
