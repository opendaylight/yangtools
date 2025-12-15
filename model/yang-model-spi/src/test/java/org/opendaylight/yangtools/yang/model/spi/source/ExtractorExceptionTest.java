/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.UncheckedExtractorException;

@ExtendWith(MockitoExtension.class)
class ExtractorExceptionTest {
    @Mock
    private StatementSourceReference sourceRef;

    @Test
    void extractorException() {
        final var ex = new ExtractorException(sourceRef, "foo message");
        assertSame(sourceRef, ex.sourceRef());
        assertEquals("foo message [at sourceRef]", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void extractorExceptionWithCause() {
        final var cause = new RuntimeException("some cause");
        final var ex = new ExtractorException(sourceRef, "bar message", cause);
        assertSame(sourceRef, ex.sourceRef());
        assertEquals("bar message [at sourceRef]", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void uncheckedExtractException() {
        final var cause = new ExtractorException(sourceRef, "foo message");
        final var ex = new UncheckedExtractorException(cause);
        assertSame(cause, ex.getCause());
        assertSame(cause.getMessage(), ex.getMessage());
        assertSame(sourceRef, ex.sourceRef());
    }
}
