/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.opendaylight.yangtools.leafref.parser.LeafRefYangSyntaxErrorException;

import org.junit.BeforeClass;
import org.opendaylight.yangtools.leafrefcontext.api.LeafRefContext;
import org.opendaylight.yangtools.leafrefcontext.builder.LeafRefContextTreeBuilder;
import org.opendaylight.yangtools.leafrefcontext.utils.LeafRefContextUtils;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.leafref.parser.LeafRefPathSyntaxErrorException;
import java.io.IOException;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import java.io.File;
import org.junit.Test;

public class LeafRefContextTreeBuilderTest {

    private static SchemaContext context;
    private static Module impMod;
    private static Module tstMod;
    private static QNameModule imp;
    private static QNameModule tst;
    private static LeafRefContext rootLeafRefContext;

    @BeforeClass
    public static void init() throws URISyntaxException, IOException,
            YangSyntaxErrorException, LeafRefYangSyntaxErrorException {
        File resourceFile = new File(
                LeafRefContextTreeBuilderTest.class
                        .getResource(
                                "/leafref-context-test/correct-modules/leafref-test.yang")
                        .toURI());
        File resourceDir = resourceFile.getParentFile();

        YangParserImpl parser = YangParserImpl.getInstance();
        context = parser.parseFile(resourceFile, resourceDir);

        Set<Module> modules = context.getModules();
        for (Module module : modules) {
            if (module.getName().equals("import-mod")) {
                impMod = module;
            }
            if (module.getName().equals("leafref-test")) {
                tstMod = module;
            }
        }

        imp = impMod.getQNameModule();
        tst = tstMod.getQNameModule();

        LeafRefContextTreeBuilder leafRefContextTreeBuilder = new LeafRefContextTreeBuilder(
                context);

        rootLeafRefContext = leafRefContextTreeBuilder
                .buildLeafRefContextTree();
    }

    @Test
    public void buildLeafRefContextTreeTest1() {

        QName q1 = QName.create(tst, "odl-project");
        QName q2 = QName.create(tst, "project");
        QName q3 = QName.create(tst, "project-lead");

        LeafRefContext leafRefCtx = rootLeafRefContext
                .getReferencingChildByName(q1).getReferencingChildByName(q2)
                .getReferencingChildByName(q3);

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
        System.out
                .println(leafRefCtx.getAbsoluteLeafRefTargetPath().toString());
    }

    @Test
    public void buildLeafRefContextTreeTest2() {

        QName q1 = QName.create(tst, "odl-project");
        QName q2 = QName.create(tst, "project");
        QName q4 = QName.create(tst, "project-lead2");

        LeafRefContext leafRefCtx2 = rootLeafRefContext
                .getReferencingChildByName(q1).getReferencingChildByName(q2)
                .getReferencingChildByName(q4);

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
        System.out.println(leafRefCtx2.getAbsoluteLeafRefTargetPath()
                .toString());
        System.out.println();

    }

    @Test
    public void buildLeafRefContextTreeXPathTest() {
        QName q1 = QName.create(tst, "odl-project");
        QName q2 = QName.create(tst, "project");
        QName q5 = QName.create(tst, "ch1");
        QName q6 = QName.create(tst, "c1");
        QName q7 = QName.create(tst, "ch2");
        QName q8 = QName.create(tst, "l1");
        LeafRefContext leafRefCtx3 = rootLeafRefContext
                .getReferencingChildByName(q1).getReferencingChildByName(q2)
                .getReferencingChildByName(q5).getReferencingChildByName(q6)
                .getReferencingChildByName(q7).getReferencingChildByName(q6)
                .getReferencingChildByName(q8);

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
        System.out.println(leafRefCtx3.getAbsoluteLeafRefTargetPath()
                .toString());
        System.out.println();
    }

    @Test
    public void buildLeafRefContextTreeTest4() {
        QName q9 = QName.create(tst, "odl-project");
        QName q10 = QName.create(tst, "project");
        QName q11 = QName.create(tst, "name");

        LeafRefContext leafRefCtx4 = rootLeafRefContext
                .getReferencedByChildByName(q9).getReferencedByChildByName(q10)
                .getReferencedByChildByName(q11);

        assertNotNull(leafRefCtx4);
        assertTrue(leafRefCtx4.isReferencedBy());
        assertEquals(6, leafRefCtx4.getAllReferencedByLeafRefCtxs().size());

    }

    @Test
    public void leafRefContextUtilsTest() {
        QName q1 = QName.create(tst, "odl-contributor");
        QName q2 = QName.create(tst, "contributor");
        QName q3 = QName.create(tst, "odl-project-name");

        LeafRefContext odlContrProjNameCtx = rootLeafRefContext
                .getReferencingChildByName(q1).getReferencingChildByName(q2)
                .getReferencingChildByName(q3);

        DataSchemaNode odlContrProjNameNode = ((DataNodeContainer) ((DataNodeContainer) tstMod
                .getDataChildByName(q1)).getDataChildByName(q2))
                .getDataChildByName(q3);

        LeafRefContext foundOdlContrProjNameCtx = LeafRefContextUtils
                .getLeafRefReferencingContext(odlContrProjNameNode,
                        rootLeafRefContext);

        assertNotNull(foundOdlContrProjNameCtx);
        assertTrue(foundOdlContrProjNameCtx.isReferencing());
        assertNotNull(foundOdlContrProjNameCtx.getLeafRefTargetPath());
        assertEquals(odlContrProjNameCtx, foundOdlContrProjNameCtx);
    }

    @Test
    public void leafRefContextUtilsTest2() {
        QName q1 = QName.create(tst, "odl-project");
        QName q2 = QName.create(tst, "project");
        QName q3 = QName.create(tst, "name");

        LeafRefContext leafRefCtx = rootLeafRefContext
                .getReferencedByChildByName(q1).getReferencedByChildByName(q2)
                .getReferencedByChildByName(q3);

        DataSchemaNode odlProjNameNode = ((DataNodeContainer) ((DataNodeContainer) tstMod
                .getDataChildByName(q1)).getDataChildByName(q2))
                .getDataChildByName(q3);

        LeafRefContext foundOdlProjNameCtx = LeafRefContextUtils
                .getLeafRefReferencingContext(odlProjNameNode,
                        rootLeafRefContext);

        assertNull(foundOdlProjNameCtx);

        foundOdlProjNameCtx = LeafRefContextUtils
                .getLeafRefReferencedByContext(odlProjNameNode,
                        rootLeafRefContext);

        assertNotNull(foundOdlProjNameCtx);
        assertTrue(foundOdlProjNameCtx.isReferencedBy());
        assertFalse(foundOdlProjNameCtx.getAllReferencedByLeafRefCtxs()
                .isEmpty());
        assertEquals(6, foundOdlProjNameCtx.getAllReferencedByLeafRefCtxs()
                .size());
        assertEquals(leafRefCtx, foundOdlProjNameCtx);
    }

    @Test
    public void leafRefContextUtilsTest3() {
        QName q16 = QName.create(tst, "con1");
        DataSchemaNode con1 = tstMod.getDataChildByName(q16);
        List<LeafRefContext> allLeafRefChilds = LeafRefContextUtils
                .findAllLeafRefChilds(con1, rootLeafRefContext);

        assertNotNull(allLeafRefChilds);
        assertFalse(allLeafRefChilds.isEmpty());
        assertEquals(4, allLeafRefChilds.size());

        QName q17 = QName.create(tst, "odl-contributor");
        DataSchemaNode odlContributorNode = tstMod.getDataChildByName(q17);
        List<LeafRefContext> allChildsReferencedByLeafRef = LeafRefContextUtils
                .findAllChildsReferencedByLeafRef(odlContributorNode,
                        rootLeafRefContext);

        assertNotNull(allChildsReferencedByLeafRef);
        assertFalse(allChildsReferencedByLeafRef.isEmpty());
        assertEquals(1, allChildsReferencedByLeafRef.size());

        allChildsReferencedByLeafRef = LeafRefContextUtils
                .findAllChildsReferencedByLeafRef(con1, rootLeafRefContext);

        assertNotNull(allChildsReferencedByLeafRef);
        assertTrue(allChildsReferencedByLeafRef.isEmpty());

    }

    @Test(expected = LeafRefPathSyntaxErrorException.class)
    public void incorrectLeafRefPathTest() throws URISyntaxException,
            IOException, YangSyntaxErrorException,
            LeafRefYangSyntaxErrorException {
        File resourceFile = new File(getClass().getResource(
                "/leafref-context-test/incorrect-modules/leafref-test.yang")
                .toURI());
        File resourceDir = resourceFile.getParentFile();

        YangParserImpl parser = YangParserImpl.getInstance();
        SchemaContext context = parser.parseFile(resourceFile, resourceDir);

        LeafRefContextTreeBuilder leafRefContextTreeBuilder = new LeafRefContextTreeBuilder(
                context);

        leafRefContextTreeBuilder.buildLeafRefContextTree();

    }

}
