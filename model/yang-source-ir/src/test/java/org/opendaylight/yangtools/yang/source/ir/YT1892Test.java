/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.source.ir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.StringYangTextSource;

class YT1892Test {
    @Test
    void emptyFileReportsCorrectError() {
        final var source = new StringYangTextSource(new SourceIdentifier("foo"), "");
        final var ex = assertThrows(SourceSyntaxException.class, () -> YangTextParser.parseSource(source));
        assertEquals("mismatched input '<EOF>' expecting {SEP, IDENTIFIER} [at foo.yang:1:1]", ex.getMessage());
        final var ref = ex.sourceRef();
        assertEquals(StatementOrigin.DECLARATION, ref.statementOrigin());
        assertSame(ref, ref.declarationReference());
        assertEquals("foo.yang:1:1", ref.toString());
    }

    @Test
    void errorOnColumn1() {
        final var source = new StringYangTextSource(new SourceIdentifier("foo"), "m");
        final var ex = assertThrows(SourceSyntaxException.class, () -> YangTextParser.parseSource(source));
        assertEquals("mismatched input '<EOF>' expecting {';', '{', SEP} [at foo.yang:1:1]", ex.getMessage());
        final var ref = ex.sourceRef();
        assertEquals(StatementOrigin.DECLARATION, ref.statementOrigin());
        assertSame(ref, ref.declarationReference());
        assertEquals("foo.yang:1:1", ref.toString());
    }
}
