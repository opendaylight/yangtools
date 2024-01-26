/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class QNameModuleTest {
    @Test
    void hashCodeValues() {
        final var mod1 = QNameModule.create(XMLNamespace.of("foo"), Optional.empty());
        assertEquals(3149755, mod1.hashCode());
        final var mod2 = QNameModule.create(XMLNamespace.of("foo"), Revision.of("2024-01-26"));
        assertEquals(-610191810, mod2.hashCode());
    }
}
