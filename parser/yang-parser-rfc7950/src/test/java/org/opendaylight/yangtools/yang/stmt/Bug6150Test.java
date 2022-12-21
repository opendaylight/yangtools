/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class Bug6150Test {
    @Test
    void effectiveAugmentFirstTest() throws Exception {
        assertNotNull(TestUtils.parseYangSource("/bugs/bug6150/target.yang", "/bugs/bug6150/aug-first.yang"));
    }

    @Test
    void effectiveAugmentSecondTest() throws Exception {
        assertNotNull(TestUtils.parseYangSource("/bugs/bug6150/target.yang", "/bugs/bug6150/aug-second.yang"));
    }

    @Test
    void effectiveAugmentBothTest() throws Exception {
        assertNotNull(TestUtils.parseYangSource("/bugs/bug6150/target.yang", "/bugs/bug6150/aug-first.yang",
            "/bugs/bug6150/aug-second.yang"));
    }
}
