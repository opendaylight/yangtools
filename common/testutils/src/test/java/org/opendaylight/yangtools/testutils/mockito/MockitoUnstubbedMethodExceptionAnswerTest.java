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

import java.io.Closeable;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MockitoUnstubbedMethodExceptionAnswerTest {
    @Test
    void testAnswering() throws IOException {
        Closeable mock = Mockito.mock(Closeable.class, MoreAnswers.exception());
        String message = assertThrows(UnstubbedMethodException.class, mock::close).getMessage();
        assertEquals("close() is not stubbed in mock of java.io.Closeable", message);
    }
}
