/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;

class AugmentStructureArgumentTest {
    // https://en.wikipedia.org/wiki/Velvet_Revolution
    private static final QName FOO = QName.create("cs", "1989-11-17", "CZ");
    private static final QName BAR = QName.create("cs", "1989-11-17", "SVK");
    // https://en.wikipedia.org/wiki/Dissolution_of_Czechoslovakia
    private static final QName BAZ = QName.create("cs", "1992-07-17", "SVK");
    // a completely different thing
    private static final QName XYZZY = QName.create("xns", "2025-11-17", "xyzzy");

    @Test
    void singleToString() {
        assertEquals("AugmentStructureArgument [qnames=[(cs?revision=1989-11-17)CZ]]",
            new AugmentStructureArgument(FOO).toString());
    }

    @Test
    void continuousToString() {
        assertEquals("AugmentStructureArgument [qnames=[(cs?revision=1989-11-17)CZ, SVK]]",
            new AugmentStructureArgument(FOO, BAR).toString());
    }

    @Test
    void differentNsToString() {
        assertEquals(
            "AugmentStructureArgument [qnames=[(cs?revision=1989-11-17)CZ, SVK, (xns?revision=2025-11-17)xyzzy]]",
            new AugmentStructureArgument(FOO, BAR, XYZZY).toString());
    }

    @Test
    void differentRevToString() {
        assertEquals(
            "AugmentStructureArgument [qnames=[(cs?revision=1989-11-17)CZ, SVK, (cs?revision=1992-07-17)SVK]]",
            new AugmentStructureArgument(FOO, BAR, BAZ).toString());
    }
}
