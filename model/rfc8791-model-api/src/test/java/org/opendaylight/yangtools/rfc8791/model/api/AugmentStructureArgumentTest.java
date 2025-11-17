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
    private static final QName FOO = QName.create("foons", "2025-11-17", "foo");

    @Test
    void singleToString() {
        assertEquals("AugmentStructureArgument[structure=(foons?revision=2025-11-17)foo, descendant=null]",
            new AugmentStructureArgument(FOO));
    }
}
