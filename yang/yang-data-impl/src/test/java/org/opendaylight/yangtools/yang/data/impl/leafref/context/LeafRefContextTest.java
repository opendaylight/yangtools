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
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
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

        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterSchemaTree(q1);
        assertTrue(LeafRefContextUtils.isLeafRef(stack, rootLeafRefContext));
        assertFalse(LeafRefContextUtils.hasLeafRefChild(stack, rootLeafRefContext));
        assertFalse(LeafRefContextUtils.hasChildReferencedByLeafRef(stack, rootLeafRefContext));

        stack.exit();
        stack.enterSchemaTree(q2);
        assertFalse(LeafRefContextUtils.isLeafRef(stack, rootLeafRefContext));
        assertTrue(LeafRefContextUtils.isReferencedByLeafRef(stack, rootLeafRefContext));

        stack.exit();
        stack.enterSchemaTree(q3);
        assertTrue(LeafRefContextUtils.hasLeafRefChild(stack, rootLeafRefContext));
        assertFalse(LeafRefContextUtils.isReferencedByLeafRef(stack, rootLeafRefContext));

        stack.exit();
        stack.enterSchemaTree(q4);
        assertTrue(LeafRefContextUtils.hasChildReferencedByLeafRef(stack, rootLeafRefContext));

        stack.exit();
        stack.enterSchemaTree(q3, q5, q6);
        Map<QName, LeafRefContext> leafRefs = LeafRefContextUtils.getAllLeafRefsReferencingThisNode(stack,
                rootLeafRefContext);
        stack.clear();
        assertEquals(4, leafRefs.size());
        stack.enterSchemaTree(q1);
        leafRefs = LeafRefContextUtils.getAllLeafRefsReferencingThisNode(stack, rootLeafRefContext);
        stack.clear();
        assertTrue(leafRefs.isEmpty());
    }
}
