/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.VerifyException;
import java.util.List;
import org.junit.jupiter.api.Test;

class BlockTest {
    @Test
    void emptyOneToBuilder() {
        final var bb = Block.builder();
        Block1.EMPTY.appendTo(bb);
        assertEquals("\n", bb.toRawString());
    }

    @Test
    void emptyTwoToBuilder() {
        final var bb = Block.builder();
        Block2.EMPTY.appendTo(bb);
        assertEquals("\n\n", bb.toRawString());
    }

    @Test
    void ofLineEmpty() {
        assertSame(Block1.EMPTY, Block.ofLine(""));
    }

    @Test
    void ofLineFoo() {
        assertEquals(new Block1("foo"), Block.ofLine("foo"));
    }

    @Test
    void ofLinesEmptyEmpty() {
        assertSame(Block2.EMPTY, Block.ofLines("", ""));
    }

    @Test
    void ofLinesEmptyEmptyArrayEmpty() {
        assertSame(Block2.EMPTY, Block.ofLines("", "", new String[0]));
    }

    @Test
    void ofLinesEmptyFoo() {
        assertEquals(new Block2("\nfoo", 0), Block.ofLines("", "foo"));
        assertEquals(new Block2("foo\n", 3), Block.ofLines("foo", ""));
    }

    @Test
    void ofLinesEmptyEmptyEmpty() {
        assertEquals(new BlockN("\n\n"), Block.ofLines("", "", ""));
    }

    @Test
    void ofLinesEmptyList() {
        final var ex = assertThrows(VerifyException.class, () -> Block.ofLines(List.of()));
        assertEquals("no lines", ex.getMessage());
    }

    @Test
    void ofLinesListEmpty() {
        assertSame(Block1.EMPTY, Block.ofLines(List.of("")));
    }

    @Test
    void ofLinesListEmptyEmpty() {
        assertSame(Block2.EMPTY, Block.ofLines(List.of("", "")));
    }

    @Test
    void ofLinesListEmptyEmptyEmpty() {
        assertEquals(new BlockN("\n\n"), Block.ofLines(List.of("", "", "")));
    }

    @Test
    void ofLinesList() {
        assertEquals(new BlockN("foo\nbar\nbaz"), Block.ofLines(List.of("foo", "bar", "baz")));
    }
}
