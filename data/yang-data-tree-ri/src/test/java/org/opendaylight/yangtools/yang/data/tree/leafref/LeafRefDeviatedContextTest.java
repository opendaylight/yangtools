/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class LeafRefDeviatedContextTest {

    @Test
    void test() {
        final var context = YangParserTestUtils.parseYangResourceDirectory("/leafref-context-test/deviated-modules");

        final var leafrefModule = context.findModule("leafref-usage").orElseThrow();
        final var leafrefQNameModule = leafrefModule.getQNameModule();
        final var cont = QName.create(leafrefQNameModule, "leafref-cont");
        final var leaf = QName.create(leafrefQNameModule, "refleaf");
        final var leafRefPath = Absolute.of(cont, leaf);
        final var leafRefContext = LeafRefContext.create(context);

        assertTrue(leafRefContext.isLeafRef(leafRefPath));

        final var sourceModule = context.findModule("leafref-source").orElseThrow();
        final var conts = QName.create(sourceModule.getQNameModule(), "conts");

        assertTrue(sourceModule.findDataChildByName(conts).isEmpty());
    }
}
