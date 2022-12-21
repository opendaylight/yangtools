/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;

class YT1393Test {
    @Test
    void testUsesAugmentUnsupportedByFeatures() throws Exception {
        final var module = StmtTestUtils.parseYangSource("/bugs/YT1393/foo.yang", Set.of())
            .findModuleStatement(QName.create("foo", "foo"))
            .orElseThrow();
        assertEquals(4, module.effectiveSubstatements().size());
    }

    @Test
    void testUsesRefineUnsupportedByFeatures() throws Exception {
        final var module = StmtTestUtils.parseYangSource("/bugs/YT1393/bar.yang", Set.of())
            .findModuleStatement(QName.create("bar", "bar"))
            .orElseThrow();
        assertEquals(5, module.effectiveSubstatements().size());
    }

    @Test
    void testAugmentAugmentUnsupportedByFeatures() throws Exception {
        final var module = StmtTestUtils.parseYangSource("/bugs/YT1393/baz.yang", Set.of())
            .findModuleStatement(QName.create("baz", "baz"))
            .orElseThrow();
        assertEquals(5, module.effectiveSubstatements().size());
    }

    @Test
    void testUsesAugmentInUnsupportedByFeatures() throws Exception {
        final var module = StmtTestUtils.parseYangSource("/bugs/YT1393/xyzzy.yang", Set.of())
            .findModuleStatement(QName.create("xyzzy", "xyzzy"))
            .orElseThrow();
        assertEquals(3, module.effectiveSubstatements().size());
    }
}
