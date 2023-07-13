/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SourceExceptionTest {
    @Mock
    public StatementSourceReference mock;

    @BeforeEach
    void before() {
        doReturn("mock").when(mock).toString();
    }

    @Test
    void testThrowIfFalse() {
        SourceException.throwIf(false, mock, "");
    }

    @Test
    void testThrowIfTrueMockNull() {
        assertThrows(NullPointerException.class, () -> SourceException.throwIf(true, mock, null));
    }

    @Test
    void testThrowIfTrueMockEmpty() {
        assertThrows(SourceException.class, () -> SourceException.throwIf(true, mock, ""));
    }

    @Test
    void testThrowIfNullNullMockNull() {
        assertThrows(NullPointerException.class, () -> SourceException.throwIfNull(null, mock, null));
    }

    @Test
    void testThrowIfNullNullMockEmpty() {
        assertThrows(SourceException.class, () -> SourceException.throwIfNull(null, mock, ""));
    }

    @Test
    void testThrowIfNullMock() {
        assertSame(mock, SourceException.throwIfNull(mock, mock, ""));
    }

    @Test
    void testUnwrapPresent() {
        assertEquals("test", SourceException.unwrap(Optional.of("test"), mock, ""));
    }

    @Test
    void testUnwrapAbsent() {
        assertThrows(SourceException.class, () -> SourceException.unwrap(Optional.empty(), mock, ""));
    }
}
