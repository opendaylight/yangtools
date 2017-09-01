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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefContextUtils;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class LeafRefContextTreeBuilderTest {

    private static SchemaContext context;
    private static Module impMod;
    private static Module tstMod;
    private static QNameModule imp;
    private static QNameModule tst;
    private static LeafRefContext rootLeafRefContext;

    @BeforeClass
    public static void init() {
        context = YangParserTestUtils.parseYangResourceDirectory("/leafref-context-test/correct-modules");

        final Set<Module> modules = context.getModules();
        for (final Module module : modules) {
            if (module.getName().equals("import-mod")) {
                impMod = module;
            }
            if (module.getName().equals("leafref-test")) {
                tstMod = module;
            }
        }

        imp = impMod.getQNameModule();
        tst = tstMod.getQNameModule();

        rootLeafRefContext = LeafRefContext.create(context);
    }

    @Test
    public void buildLeafRefContextTreeTest1() {

        final QName q1 = QName.create(tst, "odl-project");
        final QName q2 = QName.create(tst, "project");
        final QName q3 = QName.create(tst, "project-lead");

        final LeafRefContext leafRefCtx = rootLeafRefContext.getReferencingChildByName(q1)
                .getReferencingChildByName(q2).getReferencingChildByName(q3);

        assertTrue(leafRefCtx.isReferencing());
        assertNotNull(leafRefCtx.getLeafRefTargetPath());
        assertFalse(leafRefCtx.getLeafRefTargetPath().isAbsolute());
        assertNotNull(leafRefCtx.getAbsoluteLeafRefTargetPath());
        assertTrue(leafRefCtx.getAbsoluteLeafRefTargetPath().isAbsolute());

        System.out.println();
        System.out.println("******* Test 1 ************");
        System.out.println("Original definition string:");
        System.out.println(leafRefCtx.getLeafRefTargetPathString());
        System.out.println("Parsed leafref path:");
        System.out.println(leafRefCtx.getLeafRefTargetPath().toString());
        System.out.println("Absolute leafref path:");
        System.out.println(leafRefCtx.getAbsoluteLeafRefTargetPath().toString());
    }

    @Test
    public void buildLeafRefContextTreeTest2() {

        final QName q1 = QName.create(tst, "odl-project");
        final QName q2 = QName.create(tst, "project");
        final QName q4 = QName.create(tst, "project-lead2");

        final LeafRefContext leafRefCtx2 = rootLeafRefContext.getReferencingChildByName(q1)
                .getReferencingChildByName(q2).getReferencingChildByName(q4);

        assertTrue(leafRefCtx2.isReferencing());
        assertNotNull(leafRefCtx2.getLeafRefTargetPath());
        assertTrue(leafRefCtx2.getLeafRefTargetPath().isAbsolute());
        assertNotNull(leafRefCtx2.getAbsoluteLeafRefTargetPath());
        assertTrue(leafRefCtx2.getAbsoluteLeafRefTargetPath().isAbsolute());

        System.out.println();
        System.out.println("******* Test 2 ************");
        System.out.println("Original definition string2:");
        System.out.println(leafRefCtx2.getLeafRefTargetPathString());
        System.out.println("Parsed leafref path2:");
        System.out.println(leafRefCtx2.getLeafRefTargetPath().toString());
        System.out.println("Absolute leafref path2:");
        System.out.println(leafRefCtx2.getAbsoluteLeafRefTargetPath().toString());
        System.out.println();

    }

    @Test
    public void buildLeafRefContextTreeXPathTest() {
        final QName q1 = QName.create(tst, "odl-project");
        final QName q2 = QName.create(tst, "project");
        final QName q5 = QName.create(tst, "ch1");
        final QName q6 = QName.create(tst, "c1");
        final QName q7 = QName.create(tst, "ch2");
        final QName q8 = QName.create(tst, "l1");
        final LeafRefContext leafRefCtx3 = rootLeafRefContext.getReferencingChildByName(q1)
                .getReferencingChildByName(q2).getReferencingChildByName(q5).getReferencingChildByName(q6)
                .getReferencingChildByName(q7).getReferencingChildByName(q6).getReferencingChildByName(q8);

        assertTrue(leafRefCtx3.isReferencing());
        assertNotNull(leafRefCtx3.getLeafRefTargetPath());
        assertFalse(leafRefCtx3.getLeafRefTargetPath().isAbsolute());
        assertNotNull(leafRefCtx3.getAbsoluteLeafRefTargetPath());
        assertTrue(leafRefCtx3.getAbsoluteLeafRefTargetPath().isAbsolute());

        System.out.println();
        System.out.println("******* Test 3 ************");
        System.out.println("Original definition string2:");
        System.out.println(leafRefCtx3.getLeafRefTargetPathString());
        System.out.println("Parsed leafref path2:");
        System.out.println(leafRefCtx3.getLeafRefTargetPath().toString());
        System.out.println("Absolute leafref path2:");
        System.out.println(leafRefCtx3.getAbsoluteLeafRefTargetPath().toString());
        System.out.println();
    }

    @Test
    public void buildLeafRefContextTreeTest4() {
        final QName q9 = QName.create(tst, "odl-project");
        final QName q10 = QName.create(tst, "project");
        final QName q11 = QName.create(tst, "name");

        final LeafRefContext leafRefCtx4 = rootLeafRefContext.getReferencedChildByName(q9)
                .getReferencedChildByName(q10).getReferencedChildByName(q11);

        assertNotNull(leafRefCtx4);
        assertTrue(leafRefCtx4.isReferenced());
        assertEquals(6, leafRefCtx4.getAllReferencedByLeafRefCtxs().size());

    }

    @Test
    public void leafRefContextUtilsTest() {
        final QName q1 = QName.create(tst, "odl-contributor");
        final QName q2 = QName.create(tst, "contributor");
        final QName q3 = QName.create(tst, "odl-project-name");

        final LeafRefContext odlContrProjNameCtx = rootLeafRefContext.getReferencingChildByName(q1)
                .getReferencingChildByName(q2).getReferencingChildByName(q3);

        final DataSchemaNode odlContrProjNameNode = ((DataNodeContainer) ((DataNodeContainer) tstMod
                .getDataChildByName(q1)).getDataChildByName(q2)).getDataChildByName(q3);

        final LeafRefContext foundOdlContrProjNameCtx = LeafRefContextUtils.getLeafRefReferencingContext(
                odlContrProjNameNode, rootLeafRefContext);

        assertNotNull(foundOdlContrProjNameCtx);
        assertTrue(foundOdlContrProjNameCtx.isReferencing());
        assertNotNull(foundOdlContrProjNameCtx.getLeafRefTargetPath());
        assertEquals(odlContrProjNameCtx, foundOdlContrProjNameCtx);
    }

    @Test
    public void leafRefContextUtilsTest2() {
        final QName q1 = QName.create(tst, "odl-project");
        final QName q2 = QName.create(tst, "project");
        final QName q3 = QName.create(tst, "name");

        final LeafRefContext leafRefCtx = rootLeafRefContext.getReferencedChildByName(q1).getReferencedChildByName(q2)
                .getReferencedChildByName(q3);

        final DataSchemaNode odlProjNameNode = ((DataNodeContainer) ((DataNodeContainer) tstMod.getDataChildByName(q1))
                .getDataChildByName(q2)).getDataChildByName(q3);

        LeafRefContext foundOdlProjNameCtx = LeafRefContextUtils.getLeafRefReferencingContext(odlProjNameNode,
                rootLeafRefContext);

        assertNull(foundOdlProjNameCtx);

        foundOdlProjNameCtx = LeafRefContextUtils.getLeafRefReferencedByContext(odlProjNameNode, rootLeafRefContext);

        assertNotNull(foundOdlProjNameCtx);
        assertTrue(foundOdlProjNameCtx.isReferenced());
        assertFalse(foundOdlProjNameCtx.getAllReferencedByLeafRefCtxs().isEmpty());
        assertEquals(6, foundOdlProjNameCtx.getAllReferencedByLeafRefCtxs().size());
        assertEquals(leafRefCtx, foundOdlProjNameCtx);
    }

    @Test
    public void leafRefContextUtilsTest3() {
        final QName q16 = QName.create(tst, "con1");
        final DataSchemaNode con1 = tstMod.getDataChildByName(q16);
        final List<LeafRefContext> allLeafRefChilds = LeafRefContextUtils
                .findAllLeafRefChilds(con1, rootLeafRefContext);

        assertNotNull(allLeafRefChilds);
        assertFalse(allLeafRefChilds.isEmpty());
        assertEquals(4, allLeafRefChilds.size());

        final QName q17 = QName.create(tst, "odl-contributor");
        final DataSchemaNode odlContributorNode = tstMod.getDataChildByName(q17);
        List<LeafRefContext> allChildsReferencedByLeafRef = LeafRefContextUtils.findAllChildsReferencedByLeafRef(
                odlContributorNode, rootLeafRefContext);

        assertNotNull(allChildsReferencedByLeafRef);
        assertFalse(allChildsReferencedByLeafRef.isEmpty());
        assertEquals(1, allChildsReferencedByLeafRef.size());

        allChildsReferencedByLeafRef = LeafRefContextUtils.findAllChildsReferencedByLeafRef(con1, rootLeafRefContext);

        assertNotNull(allChildsReferencedByLeafRef);
        assertTrue(allChildsReferencedByLeafRef.isEmpty());

    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void incorrectLeafRefPathTest() {
        final SchemaContext context = YangParserTestUtils.parseYangResourceDirectory(
            "/leafref-context-test/incorrect-modules");
        LeafRefContext.create(context);
    }

}
