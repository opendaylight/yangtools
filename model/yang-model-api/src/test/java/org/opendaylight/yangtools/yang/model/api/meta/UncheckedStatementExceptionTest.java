/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

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
class UncheckedStatementExceptionTest {
    @Mock
    private StatementSourceReference ref;
    @Mock
    private Throwable cause;

    private TestStatementException tse;
    private UncheckedStatementException ex;

    @BeforeEach
    void beforeEach() {
        doReturn("ref to string").when(ref).toString();
        tse = new TestStatementException(ref, "message", cause);
        ex = new UncheckedStatementException(tse);
    }

    @Test
    void cannotWriteObject() throws Exception {
        try (var oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            assertThrows(NotSerializableException.class, () -> oos.writeObject(ex));
        }
    }

    @Test
    void causeRetained() {
        assertSame(tse.getMessage(), ex.getMessage());
        assertSame(tse, ex.getCause());
        assertSame(tse.sourceRef(), ex.sourceRef());
    }
}
