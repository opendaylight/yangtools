/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.testutils.mockito.tests;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.testutils.mockito.MoreAnswers.exception;

import java.io.Closeable;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.testutils.mockito.UnstubbedMethodException;

public class MockitoUnstubbedMethodExceptionAnswerTest {

    @Test
    public void testAnswering() throws IOException {
        Closeable mock = Mockito.mock(Closeable.class, exception());
        try {
            mock.close();
            fail();
        } catch (UnstubbedMethodException e) {
            assertThat(e.getMessage()).isEqualTo("close() is not stubbed in mock of java.io.Closeable");
        }

    }

}
