/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefContextUtils;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class LeafRefContextTest {
    private static EffectiveModelContext context;
    private static Module rootMod;
    private static QNameModule root;
    private static LeafRefContext rootLeafRefContext;

    @BeforeClass
    public static void init() {
        context = YangParserTestUtils.parseYangResourceDirectory("/leafref-context-test/correct-modules");

        for (final Module module : context.getModules()) {
            if (module.getName().equals("leafref-test2")) {
                rootMod = module;
            }
        }

        root = rootMod.getQNameModule();
        rootLeafRefContext = LeafRefContext.create(context);
    }

    @AfterClass
    public static void cleanup() {
        context = null;
        root = null;
        rootMod = null;
        rootLeafRefContext = null;
    }

    @Test
    public void test() {

        final QName q1 = QName.create(root, "ref1");
        final QName q2 = QName.create(root, "leaf1");
        final QName q3 = QName.create(root, "cont1");
        final QName q4 = QName.create(root, "cont2");
        final QName q5 = QName.create(root, "list1");
        final QName q6 = QName.create(root, "name");

        final DataSchemaNode leafRefNode = rootMod.findDataChildByName(q1).get();
        final DataSchemaNode targetNode = rootMod.findDataChildByName(q2).get();
        final DataSchemaNode cont1Node = rootMod.findDataChildByName(q3).get();
        final DataSchemaNode cont2Node = rootMod.findDataChildByName(q4).get();
        final DataSchemaNode name1Node = rootMod.findDataChildByName(q3, q5, q6).get();

        assertTrue(LeafRefContextUtils.isLeafRef(leafRefNode, rootLeafRefContext));
        assertFalse(LeafRefContextUtils.isLeafRef(targetNode, rootLeafRefContext));

        assertTrue(LeafRefContextUtils.hasLeafRefChild(cont1Node, rootLeafRefContext));
        assertFalse(LeafRefContextUtils.hasLeafRefChild(leafRefNode, rootLeafRefContext));

        assertTrue(LeafRefContextUtils.isReferencedByLeafRef(targetNode, rootLeafRefContext));
        assertFalse(LeafRefContextUtils.isReferencedByLeafRef(leafRefNode, rootLeafRefContext));

        assertTrue(LeafRefContextUtils.hasChildReferencedByLeafRef(cont2Node, rootLeafRefContext));
        assertFalse(LeafRefContextUtils.hasChildReferencedByLeafRef(leafRefNode, rootLeafRefContext));

        Map<QName, LeafRefContext> leafRefs = LeafRefContextUtils.getAllLeafRefsReferencingThisNode(name1Node,
                rootLeafRefContext);
        assertEquals(4, leafRefs.size());
        leafRefs = LeafRefContextUtils.getAllLeafRefsReferencingThisNode(leafRefNode, rootLeafRefContext);
        assertTrue(leafRefs.isEmpty());
    }
}
