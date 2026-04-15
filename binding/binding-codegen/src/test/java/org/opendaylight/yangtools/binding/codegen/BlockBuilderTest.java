/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.VerifyException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.codegen.ArgumentVerifier.StrictVerifier;

class BlockBuilderTest {
    // Note: behavior selected by environment variable
    private final BlockBuilder bb = new BlockBuilder();

    @Test
    void runningWithStrict() {
        assertInstanceOf(StrictVerifier.class, ArgumentVerifier.INSTANCE);
    }

    @Test
    void verifyStrNullStr() {
        assertThrows(NullPointerException.class, () -> bb.str((String) null));
    }

    @Test
    void verifyStrNullSb() {
        assertSame(bb, bb.str((StringBuilder) null));
        assertEquals("", bb.toRawString());
    }

    @Test
    void verifyStrEmpty() {
        final var ex = assertThrows(VerifyException.class, () -> bb.str(""));
        assertEquals("empty str", ex.getMessage());
    }

    @Test
    void verifyStrNewLine() {
        final var ex = assertThrows(VerifyException.class, () -> bb.str("ab\ncd"));
        assertEquals("newline at offset 2 of 'ab\ncd'", ex.getMessage());
    }

    @Test
    void verifyTxtNull() {
        assertThrows(NullPointerException.class, () -> bb.txt(null));
    }

    @Test
    void verifyTxtEmpty() {
        final var ex = assertThrows(VerifyException.class, () -> bb.txt(""));
        assertEquals("empty txt", ex.getMessage());
    }

    @Test
    void verifyTxtNoNewline() {
        final var ex = assertThrows(VerifyException.class, () -> bb.txt("acd"));
        assertEquals("no newline in 'acd'", ex.getMessage());
    }

    @Test
    void buildEmpty() {
        final var ex = assertThrows(VerifyException.class, bb::build);
        assertEquals("empty block", ex.getMessage());
    }

    @Test
    void buildUnterminatedLineFirst() {
        bb.str("abc");

        final var ex = assertThrows(VerifyException.class, bb::build);
        assertEquals("unterminated line abc", ex.getMessage());
    }

    @Test
    void buildUnterminatedLineSecond() {
        bb.eol("abc").str("def");

        final var ex = assertThrows(VerifyException.class, bb::build);
        assertEquals("unterminated line def", ex.getMessage());
    }

    @Test
    void buildUnterminatedLineThird() {
        bb.eol("abc").eol("def").str("ghi");

        final var ex = assertThrows(VerifyException.class, bb::build);
        assertEquals("unterminated line ghi", ex.getMessage());
    }

    @Test
    void buildNewlineOne() {
        assertEquals(Block.ofLine(""), bb.nl().build());
    }

    @Test
    void buildNewlineTwo() {
        assertEquals(Block.ofLines("", ""), bb.nl().nl().build());
    }

    @Test
    void buildNewlineThree() {
        assertEquals(Block.ofLines("", "", ""), bb.nl().nl().nl().build());
    }

    @Test
    void buildOne() {
        assertEquals(Block.ofLine("abc"), bb.eol("abc").build());
    }

    @Test
    void buildTwo() {
        // FIXME: exact result
        assertEquals(Block.ofLines("abc", "def").toRawString(), bb.eol("abc").eol("def").build().toRawString());
    }

    @Test
    void buildThree() {
        // FIXME: exact result
        assertEquals(Block.ofLines("abc", "def", "ghi").toRawString(),
            bb.eol("abc").eol("def").eol("ghi").build().toRawString());
    }
}
