/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class YT1039Test extends AbstractYangTest {
    @Test
    void testUsesRefineAnyxml() {
        assertEquals(2, assertEffectiveModelDir("/bugs/YT1039").getModuleStatements().size());
    }
}
