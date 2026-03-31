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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;

import com.google.common.base.VerifyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.codegen.BlockBuilder.FastVerifier;
import org.opendaylight.yangtools.binding.codegen.BlockBuilder.StrictVerifier;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class BlockBuilderTest {
    @Mock
    private Logger log;

    // Note: behavior selected by environment variable
    private final BlockBuilder bb = new BlockBuilder();

    @Test
    void runningWithStrict() {
        assertInstanceOf(StrictVerifier.class, BlockBuilder.ARGUMENT_VERIFIER);
    }

    @Test
    void verifyStrNull() {
        assertThrows(NullPointerException.class, () -> bb.str((String) null));
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
    void nullPropSelectsFast() {
        doNothing().when(log).debug("Using fast verification");
        assertInstanceOf(FastVerifier.class, BlockBuilder.selectArgumentVerifier(log, null));
    }

    @Test
    void falsePropSelectsFast() {
        doNothing().when(log).info("Using fast verification");
        final var fast = assertInstanceOf(FastVerifier.class, BlockBuilder.selectArgumentVerifier(log, "false"));

        // fast checker does not detect these conditions:
        final var strWithNl = "abc\n";
        assertSame(strWithNl, fast.verifyStr(strWithNl));
        final var strEmpty = "";
        assertSame(strEmpty, fast.verifyNonEmptyStr(strEmpty));
        final var txtWithoutNl = "abc";
        assertSame(txtWithoutNl, fast.verifyTxt(txtWithoutNl));
        assertNull(fast.verifyTxt(null, -1));
    }

    @Test
    void truePropSelectsStrict() {
        doNothing().when(log).info("Using strict verification");
        assertInstanceOf(StrictVerifier.class, BlockBuilder.selectArgumentVerifier(log, "true"));
    }

    @Test
    void malformedPropSelectsStrict() {
        doNothing().when(log)
            .warn("Bad {} value '{}', using strict verification", "odl.binding.codegen.verify", "TRUE");
        assertInstanceOf(StrictVerifier.class, BlockBuilder.selectArgumentVerifier(log, "TRUE"));
    }
}
