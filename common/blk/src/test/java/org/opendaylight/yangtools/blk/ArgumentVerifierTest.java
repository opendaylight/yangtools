/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.blk;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class ArgumentVerifierTest {
    @Mock
    private Logger log;

    @Test
    void nullPropSelectsFast() {
        doNothing().when(log).debug("Using quick verification");
        assertInstanceOf(QuickArgumentVerifier.class, ArgumentVerifier.selectArgumentVerifier(log, null));
    }

    @Test
    void falsePropSelectsQuick() {
        doNothing().when(log).info("Using quick verification");
        final var quick = assertInstanceOf(QuickArgumentVerifier.class,
            ArgumentVerifier.selectArgumentVerifier(log, "quick"));

        // quick checker does not detect these conditions:
        final var strWithNl = "abc\n";
        assertSame(strWithNl, quick.verifyStr(strWithNl));
        final var txtWithoutNl = "abc";
        assertSame(txtWithoutNl, quick.verifyTxt(txtWithoutNl));
    }

    @Test
    void truePropSelectsStrict() {
        doNothing().when(log).info("Using strict verification");
        assertInstanceOf(StrictAdugmentVerifier.class, ArgumentVerifier.selectArgumentVerifier(log, "strict"));
    }

    @Test
    void malformedPropSelectsStrict() {
        doNothing().when(log)
            .warn("Bad {} value '{}', using strict verification", "odl.binding.codegen.verify", "bad value");
        assertInstanceOf(StrictAdugmentVerifier.class, ArgumentVerifier.selectArgumentVerifier(log, "bad value"));
    }
}
