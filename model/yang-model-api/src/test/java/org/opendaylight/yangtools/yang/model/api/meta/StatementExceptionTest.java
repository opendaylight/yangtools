/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatementExceptionTest {
    @Mock
    private StatementSourceReference ref;
    @Mock
    private Throwable cause;

    private TestStatementException ex;

    @BeforeEach
    void beforeEach() {
        doReturn("ref to string").when(ref).toString();
        ex = new TestStatementException(ref, "message", cause);
    }

    @Test
    void cannotWriteObject() throws Exception {
        try (var oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            assertThrows(NotSerializableException.class, () -> oos.writeObject(ex));
        }
    }

    @Test
    void sourceRefRetained() {
        assertSame(ref, ex.sourceRef());
    }

    @Test
    void causeRetained() {
        assertSame(cause, ex.getCause());
    }

    @Test
    void messagePropagatesRef() {
        assertEquals("message [at ref to string]", ex.getMessage());
    }
}
