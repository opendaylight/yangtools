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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class SourceExceptionTest {
    private StatementSourceReference mock;

    @Before
    public void before() {
        mock = mock(StatementSourceReference.class);
        doReturn("mock").when(mock).toString();
    }

    @Test
    public void testThrowIfFalse() {
        SourceException.throwIf(false, mock, "");
    }

    @Test(expected = NullPointerException.class)
    public void testThrowIfTrueMockNull() {
        SourceException.throwIf(true, mock, null);
    }

    @Test(expected = SourceException.class)
    public void testThrowIfTrueMockEmpty() {
        SourceException.throwIf(true, mock, "");
    }

    @Test(expected = NullPointerException.class)
    public void testThrowIfNullNullMockNull() {
        SourceException.throwIfNull(null, mock, null);
    }

    @Test(expected = SourceException.class)
    public void testThrowIfNullNullMockEmpty() {
        SourceException.throwIfNull(null, mock, "");
    }

    @Test
    public void testThrowIfNullMock() {
        assertSame(mock, SourceException.throwIfNull(mock, mock, ""));
    }

    @Test
    public void testUnwrapPresent() {
        assertEquals("test", SourceException.unwrap(Optional.of("test"), mock, ""));
    }

    @Test(expected = SourceException.class)
    public void testUnwrapAbsent() {
        SourceException.unwrap(Optional.empty(), mock, "");
    }
}
