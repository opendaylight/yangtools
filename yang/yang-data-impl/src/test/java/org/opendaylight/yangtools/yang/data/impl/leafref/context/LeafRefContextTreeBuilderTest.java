/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref.context;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
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
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeafRefContextTreeBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(LeafRefContextTreeBuilderTest.class);

    private static EffectiveModelContext context;
    private static Module tstMod;
    private static QNameModule tst;
    private static LeafRefContext rootLeafRefContext;

    @BeforeClass
    public static void init() {
        context = YangParserTestUtils.parseYangResourceDirectory("/leafref-context-test/correct-modules");

        for (final Module module : context.getModules()) {
            if (module.getName().equals("leafref-test")) {
                tstMod = module;
            }
        }

        tst = tstMod.getQNameModule();

        rootLeafRefContext = LeafRefContext.create(context);
    }

    @AfterClass
    public static void cleanup() {
        context = null;
        tst = null;
        tstMod = null;
        rootLeafRefContext = null;
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

        LOG.debug("******* Test 1 ************");
        LOG.debug("Original definition string: {}", leafRefCtx.getLeafRefTargetPathString());
        LOG.debug("Parsed leafref path: {}", leafRefCtx.getLeafRefTargetPath());
        LOG.debug("Absolute leafref path: {}", leafRefCtx.getAbsoluteLeafRefTargetPath());
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

        LOG.debug("******* Test 2 ************");
        LOG.debug("Original definition string2: {}", leafRefCtx2.getLeafRefTargetPathString());
        LOG.debug("Parsed leafref path2: {}", leafRefCtx2.getLeafRefTargetPath());
        LOG.debug("Absolute leafref path2: {}", leafRefCtx2.getAbsoluteLeafRefTargetPath());
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

        LOG.debug("******* Test 3 ************");
        LOG.debug("Original definition string2: {}", leafRefCtx3.getLeafRefTargetPathString());
        LOG.debug("Parsed leafref path2: {}", leafRefCtx3.getLeafRefTargetPath());
        LOG.debug("Absolute leafref path2: {}", leafRefCtx3.getAbsoluteLeafRefTargetPath());
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

        final DataSchemaNode odlContrProjNameNode = tstMod.findDataChildByName(q1, q2, q3).get();

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

        final DataSchemaNode odlProjNameNode = tstMod.findDataChildByName(q1, q2, q3).get();

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
        final DataSchemaNode con1 = tstMod.findDataChildByName(q16).get();
        final List<LeafRefContext> allLeafRefChilds = LeafRefContextUtils.findAllLeafRefChilds(con1,
            rootLeafRefContext);

        assertNotNull(allLeafRefChilds);
        assertFalse(allLeafRefChilds.isEmpty());
        assertEquals(4, allLeafRefChilds.size());

        final QName q17 = QName.create(tst, "odl-contributor");
        final DataSchemaNode odlContributorNode = tstMod.findDataChildByName(q17).get();
        List<LeafRefContext> allChildsReferencedByLeafRef = LeafRefContextUtils.findAllChildsReferencedByLeafRef(
                odlContributorNode, rootLeafRefContext);

        assertNotNull(allChildsReferencedByLeafRef);
        assertFalse(allChildsReferencedByLeafRef.isEmpty());
        assertEquals(1, allChildsReferencedByLeafRef.size());

        allChildsReferencedByLeafRef = LeafRefContextUtils.findAllChildsReferencedByLeafRef(con1, rootLeafRefContext);

        assertNotNull(allChildsReferencedByLeafRef);
        assertTrue(allChildsReferencedByLeafRef.isEmpty());
    }

    @Test
    public void incorrectLeafRefPathTest() {
        final IllegalStateException ise = assertThrows(IllegalStateException.class,
            () -> YangParserTestUtils.parseYangResourceDirectory("/leafref-context-test/incorrect-modules"));
        final Throwable ype = ise.getCause();
        assertThat(ype, instanceOf(YangParserException.class));
        final Throwable reactor = ype.getCause();
        assertThat(reactor, instanceOf(ReactorException.class));
        final Throwable source = reactor.getCause();
        assertThat(source, instanceOf(SourceException.class));
        assertThat(source.getMessage(), startsWith("token recognition error at: './' at 1:2"));
    }
}
