/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SourceExceptionTest {
    @Mock
    public StatementSourceReference mock;

    @Before
    public void before() {
        doReturn("mock").when(mock).toString();
    }

    @Test
    public void testThrowIfFalse() {
        SourceException.throwIf(false, mock, "");
    }

    @Test
    public void testThrowIfTrueMockNull() {
        assertThrows(NullPointerException.class, () -> SourceException.throwIf(true, mock, null));
    }

    @Test
    public void testThrowIfTrueMockEmpty() {
        assertThrows(SourceException.class, () -> SourceException.throwIf(true, mock, ""));
    }

    @Test
    public void testThrowIfNullNullMockNull() {
        assertThrows(NullPointerException.class, () -> SourceException.throwIfNull(null, mock, null));
    }

    @Test
    public void testThrowIfNullNullMockEmpty() {
        assertThrows(SourceException.class, () -> SourceException.throwIfNull(null, mock, ""));
    }

    @Test
    public void testThrowIfNullMock() {
        assertSame(mock, SourceException.throwIfNull(mock, mock, ""));
    }

    @Test
    public void testUnwrapPresent() {
        assertEquals("test", SourceException.unwrap(Optional.of("test"), mock, ""));
    }

    @Test
    public void testUnwrapAbsent() {
        assertThrows(SourceException.class, () -> SourceException.unwrap(Optional.empty(), mock, ""));
    }
}
