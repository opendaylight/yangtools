/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.codegen.ArgumentVerifier.FastVerifier;
import org.opendaylight.yangtools.binding.codegen.ArgumentVerifier.StrictVerifier;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class ArgumentVerifierTest {
    @Mock
    private Logger log;

    @Test
    void nullPropSelectsFast() {
        doNothing().when(log).debug("Using fast verification");
        assertInstanceOf(FastVerifier.class, ArgumentVerifier.selectArgumentVerifier(log, null));
    }

    @Test
    void falsePropSelectsFast() {
        doNothing().when(log).info("Using fast verification");
        final var fast = assertInstanceOf(FastVerifier.class, ArgumentVerifier.selectArgumentVerifier(log, "false"));

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
        assertInstanceOf(StrictVerifier.class, ArgumentVerifier.selectArgumentVerifier(log, "true"));
    }

    @Test
    void malformedPropSelectsStrict() {
        doNothing().when(log)
            .warn("Bad {} value '{}', using strict verification", "odl.binding.codegen.verify", "TRUE");
        assertInstanceOf(StrictVerifier.class, ArgumentVerifier.selectArgumentVerifier(log, "TRUE"));
    }
}
