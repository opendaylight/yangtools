/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class LeafRefContextTest {
    private static EffectiveModelContext context;
    private static Module rootMod;
    private static QNameModule root;
    private static LeafRefContext rootLeafRefContext;

    @BeforeAll
    static void init() {
        context = YangParserTestUtils.parseYangResourceDirectory("/leafref-context-test/correct-modules");

        for (final var module : context.getModules()) {
            if (module.getName().equals("leafref-test2")) {
                rootMod = module;
            }
        }

        root = rootMod.getQNameModule();
        rootLeafRefContext = LeafRefContext.create(context);
    }

    @AfterAll
    static void cleanup() {
        context = null;
        root = null;
        rootMod = null;
        rootLeafRefContext = null;
    }

    @Test
    void test() {
        final var q1 = QName.create(root, "ref1");
        final var q2 = QName.create(root, "leaf1");
        final var q3 = QName.create(root, "cont1");
        final var q4 = QName.create(root, "cont2");
        final var q5 = QName.create(root, "list1");
        final var q6 = QName.create(root, "name");

        final var leafRefNode = Absolute.of(q1);
        final var targetNode = Absolute.of(q2);
        final var cont1Node = Absolute.of(q3);
        final var cont2Node = Absolute.of(q4);
        final var name1Node = Absolute.of(q3, q5, q6);

        assertTrue(rootLeafRefContext.isLeafRef(leafRefNode));
        assertFalse(rootLeafRefContext.isLeafRef(targetNode));

        assertTrue(rootLeafRefContext.hasLeafRefChild(cont1Node));
        assertFalse(rootLeafRefContext.hasLeafRefChild(leafRefNode));

        assertTrue(rootLeafRefContext.isReferencedByLeafRef(targetNode));
        assertFalse(rootLeafRefContext.isReferencedByLeafRef(leafRefNode));

        assertTrue(rootLeafRefContext.hasChildReferencedByLeafRef(cont2Node));
        assertFalse(rootLeafRefContext.hasChildReferencedByLeafRef(leafRefNode));

        var leafRefs = rootLeafRefContext.getAllLeafRefsReferencingThisNode(name1Node);
        assertEquals(4, leafRefs.size());
        leafRefs = rootLeafRefContext.getAllLeafRefsReferencingThisNode(leafRefNode);
        assertTrue(leafRefs.isEmpty());
    }
}
