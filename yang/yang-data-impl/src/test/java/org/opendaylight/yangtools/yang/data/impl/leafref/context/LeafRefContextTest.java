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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefContextUtils;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class LeafRefContextTest {

    private static SchemaContext context;
    private static Module rootMod;
    private static QNameModule root;
    private static LeafRefContext rootLeafRefContext;

    @BeforeClass
    public static void init() throws URISyntaxException, FileNotFoundException, ReactorException {

        final File resourceFile = new File(LeafRefContextTreeBuilderTest.class.getResource(
                "/leafref-context-test/correct-modules/leafref-test2.yang").toURI());

        final File resourceDir = resourceFile.getParentFile();

        context = YangParserTestUtils.parseYangSources(Arrays.asList(resourceDir.listFiles()));

        final Set<Module> modules = context.getModules();
        for (final Module module : modules) {
            if (module.getName().equals("leafref-test2")) {
                rootMod = module;
            }
        }

        root = rootMod.getQNameModule();
        rootLeafRefContext = LeafRefContext.create(context);
    }

    @Test
    public void test() {

        final QName q1 = QName.create(root, "ref1");
        final QName q2 = QName.create(root, "leaf1");
        final QName q3 = QName.create(root, "cont1");
        final QName q4 = QName.create(root, "cont2");
        final QName q5 = QName.create(root, "list1");
        final QName q6 = QName.create(root, "name");

        final DataSchemaNode leafRefNode = rootMod.getDataChildByName(q1);
        final DataSchemaNode targetNode = rootMod.getDataChildByName(q2);
        final DataSchemaNode cont1Node = rootMod.getDataChildByName(q3);
        final DataSchemaNode cont2Node = rootMod.getDataChildByName(q4);
        final DataSchemaNode name1Node = ((DataNodeContainer) ((DataNodeContainer) rootMod.getDataChildByName(q3))
                .getDataChildByName(q5)).getDataChildByName(q6);

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
