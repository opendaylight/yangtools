/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.testutils.mockito.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opendaylight.yangtools.testutils.mockito.MoreAnswers.exception;

import java.io.File;
import org.junit.Test;
import org.opendaylight.yangtools.testutils.mockito.UnstubbedMethodException;

/**
 * Test to illustrate the basic use of Mockito VS the EXCEPTION_ANSWER.
 *
 * <p>Also useful as example to contrast this approach with the REAL_OR_EXCEPTION
 * approach illustrated in the MikitoTest.
 *
 * @see MikitoTest
 *
 * @author Michael Vorburger
 */
public class MockitoExampleTutorialTest {

    interface SomeService {

        void foo();

        String bar(String arg);

        // Most methods on real world services have complex input (and output objects), not just int or String
        int foobar(File file);
    }

    @Test
    public void usingMockitoWithoutStubbing() {
        SomeService service = mock(SomeService.class);
        assertNull(service.bar("hulo"));
    }

    @Test
    public void usingMockitoToStubSimpleCase() {
        SomeService service = mock(SomeService.class);
        when(service.foobar(any())).thenReturn(123);
        assertEquals(123, service.foobar(new File("hello.txt")));
    }

    @Test
    public void usingMockitoToStubComplexCase() {
        SomeService service = mock(SomeService.class);
        when(service.foobar(any())).thenAnswer(invocation -> {
            // Urgh! This is ugly.. (Mockito 2.0 may be better,
            // see http://site.mockito.org/mockito/docs/current/org/mockito/ArgumentMatcher.html)
            File file = invocation.getArgumentAt(0, File.class);
            return "hello.txt".equals(file.getName()) ? 123 : 0;
        });
        assertEquals(0, service.foobar(new File("belo.txt")));
    }

    @Test(expected = UnstubbedMethodException.class)
    public void usingMockitoExceptionException() {
        SomeService service = mock(SomeService.class, exception());
        service.foo();
    }

    @Test
    public void usingMockitoNoExceptionIfStubbed() {
        SomeService service = mock(SomeService.class, exception());
        // NOT when(s.foobar(any())).thenReturn(123) BUT must be like this:
        doReturn(123).when(service).foobar(any());
        assertEquals(123, service.foobar(new File("hello.txt")));
        try {
            service.foo();
            fail("expected NotImplementedException");
        } catch (UnstubbedMethodException e) {
            // OK
        }
    }

    @Test
    public void usingMockitoToStubComplexCaseAndExceptionIfNotStubbed() {
        SomeService service = mock(SomeService.class, exception());
        doAnswer(invocation -> {
            // Urgh! This is ugly. Mockito may be better,
            // see http://site.mockito.org/mockito/docs/current/org/mockito/ArgumentMatcher.html
            File file = (File) invocation.getArguments()[0];
            return "hello.txt".equals(file.getName()) ? 123 : 0;
        }).when(service).foobar(any());
        assertEquals(123, service.foobar(new File("hello.txt")));
        assertEquals(0, service.foobar(new File("belo.txt")));
    }

}
