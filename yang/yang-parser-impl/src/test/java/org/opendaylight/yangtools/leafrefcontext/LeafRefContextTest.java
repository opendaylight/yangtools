/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

import static org.junit.Assert.*;

import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.leafrefcontext.api.LeafRefContext;
import org.opendaylight.yangtools.leafrefcontext.builder.LeafRefContextTreeBuilder;
import org.opendaylight.yangtools.leafrefcontext.utils.LeafRefContextUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class LeafRefContextTest {

    private static SchemaContext context;
    private static Module rootMod;
    private static QNameModule root;
    private static LeafRefContext rootLeafRefContext;

    @BeforeClass
    public static void init() throws URISyntaxException, IOException,
            YangSyntaxErrorException {

        File resourceFile = new File(
                LeafRefContextTreeBuilderTest.class
                        .getResource(
                                "/leafref-context-test/correct-modules/leafref-test2.yang")
                        .toURI());

        File resourceDir = resourceFile.getParentFile();

        YangParserImpl parser = YangParserImpl.getInstance();
        context = parser.parseFile(resourceFile, resourceDir);

        Set<Module> modules = context.getModules();
        for (Module module : modules) {
            if (module.getName().equals("leafref-test2")) {
                rootMod = module;
            }
        }

        root = rootMod.getQNameModule();

        LeafRefContextTreeBuilder leafRefContextTreeBuilder = new LeafRefContextTreeBuilder(
                context);

        rootLeafRefContext = leafRefContextTreeBuilder
                .buildLeafRefContextTree();
    }

    @Test
    public void test() {

        QName q1 = QName.create(root, "ref1");
        QName q2 = QName.create(root, "leaf1");
        QName q3 = QName.create(root, "cont1");
        QName q4 = QName.create(root, "cont2");
        QName q5 = QName.create(root, "list1");
        QName q6 = QName.create(root, "name");

        DataSchemaNode leafRefNode = rootMod.getDataChildByName(q1);
        DataSchemaNode targetNode = rootMod.getDataChildByName(q2);
        DataSchemaNode cont1Node = rootMod.getDataChildByName(q3);
        DataSchemaNode cont2Node = rootMod.getDataChildByName(q4);
        DataSchemaNode name1Node = ((DataNodeContainer) ((DataNodeContainer) rootMod
                .getDataChildByName(q3)).getDataChildByName(q5))
                .getDataChildByName(q6);

        assertTrue(LeafRefContextUtils.isLeafRef(leafRefNode,
                rootLeafRefContext));
        assertFalse(LeafRefContextUtils.isLeafRef(targetNode,
                rootLeafRefContext));

        assertTrue(LeafRefContextUtils.hasLeafRefChild(cont1Node,
                rootLeafRefContext));
        assertFalse(LeafRefContextUtils.hasLeafRefChild(leafRefNode,
                rootLeafRefContext));

        assertTrue(LeafRefContextUtils.isReferencedByLeafRef(targetNode,
                rootLeafRefContext));
        assertFalse(LeafRefContextUtils.isReferencedByLeafRef(leafRefNode,
                rootLeafRefContext));

        assertTrue(LeafRefContextUtils.hasChildReferencedByLeafRef(cont2Node,
                rootLeafRefContext));
        assertFalse(LeafRefContextUtils.hasChildReferencedByLeafRef(
                leafRefNode, rootLeafRefContext));

        Map<QName, LeafRefContext> leafRefs = LeafRefContextUtils
                .getAllLeafRefsReferencingThisNode(name1Node,
                        rootLeafRefContext);
        assertEquals(4, leafRefs.size());
        leafRefs = LeafRefContextUtils.getAllLeafRefsReferencingThisNode(
                leafRefNode, rootLeafRefContext);
        assertTrue(leafRefs.isEmpty());
    }
}
