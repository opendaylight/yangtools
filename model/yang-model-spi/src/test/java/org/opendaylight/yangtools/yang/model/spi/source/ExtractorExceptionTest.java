/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorMalformedArgumentException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorMissingArgumentException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorMissingStatementException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.UncheckedExtractorException;

@ExtendWith(MockitoExtension.class)
class ExtractorExceptionTest {
    @Mock
    private StatementSourceReference sourceRef;

    @Test
    void extractorMalformedArgumentException() {
        final var cause = new RuntimeException("some cause");
        final var ex = new ExtractorMalformedArgumentException(sourceRef, "foo", cause);
        assertSame(sourceRef, ex.sourceRef());
        assertEquals("Malformed argument to foo: some cause [at sourceRef]", ex.getMessage());
    }


    @Test
    void extractorMissingArgumentException() {
        final var ex = new ExtractorMissingArgumentException(sourceRef, "foo");
        assertSame(sourceRef, ex.sourceRef());
        assertEquals("Missing argument to foo [at sourceRef]", ex.getMessage());
    }

    @Test
    void extractorMissingStatementException() {
        final var ex = new ExtractorMissingStatementException(sourceRef, "foo");
        assertSame(sourceRef, ex.sourceRef());
        assertEquals("Missing foo substatement [at sourceRef]", ex.getMessage());
    }

    @Test
    void uncheckedExtractException() {
        final var cause = new ExtractorMissingArgumentException(sourceRef, "foo");
        final var ex = new UncheckedExtractorException(cause);
        assertSame(cause, ex.getCause());
        assertSame(cause.getMessage(), ex.getMessage());
        assertSame(sourceRef, ex.sourceRef());
    }
}
