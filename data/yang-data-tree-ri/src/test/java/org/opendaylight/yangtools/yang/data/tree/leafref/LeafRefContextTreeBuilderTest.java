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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LeafRefContextTreeBuilderTest {
    private static final Logger LOG = LoggerFactory.getLogger(LeafRefContextTreeBuilderTest.class);

    private static EffectiveModelContext context;
    private static Module tstMod;
    private static QNameModule tst;
    private static LeafRefContext rootLeafRefContext;

    @BeforeAll
    static void init() {
        context = YangParserTestUtils.parseYangResourceDirectory("/leafref-context-test/correct-modules");

        for (final var module : context.getModules()) {
            if (module.getName().equals("leafref-test")) {
                tstMod = module;
            }
        }

        tst = tstMod.getQNameModule();

        rootLeafRefContext = LeafRefContext.create(context);
    }

    @AfterAll
    static void cleanup() {
        context = null;
        tst = null;
        tstMod = null;
        rootLeafRefContext = null;
    }

    @Test
    void buildLeafRefContextTreeTest1() {

        final var q1 = QName.create(tst, "odl-project");
        final var q2 = QName.create(tst, "project");
        final var q3 = QName.create(tst, "project-lead");

        final var leafRefCtx = rootLeafRefContext.getReferencingChildByName(q1)
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
    void buildLeafRefContextTreeTest2() {

        final var q1 = QName.create(tst, "odl-project");
        final var q2 = QName.create(tst, "project");
        final var q4 = QName.create(tst, "project-lead2");

        final var leafRefCtx2 = rootLeafRefContext.getReferencingChildByName(q1)
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
    void buildLeafRefContextTreeXPathTest() {
        final var q1 = QName.create(tst, "odl-project");
        final var q2 = QName.create(tst, "project");
        final var q5 = QName.create(tst, "ch1");
        final var q6 = QName.create(tst, "c1");
        final var q7 = QName.create(tst, "ch2");
        final var q8 = QName.create(tst, "l1");
        final var leafRefCtx3 = rootLeafRefContext.getReferencingChildByName(q1)
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
    void buildLeafRefContextTreeTest4() {
        final var q9 = QName.create(tst, "odl-project");
        final var q10 = QName.create(tst, "project");
        final var q11 = QName.create(tst, "name");

        final var leafRefCtx4 = rootLeafRefContext.getReferencedChildByName(q9)
                .getReferencedChildByName(q10).getReferencedChildByName(q11);

        assertNotNull(leafRefCtx4);
        assertTrue(leafRefCtx4.isReferenced());
        assertEquals(6, leafRefCtx4.getAllReferencedByLeafRefCtxs().size());

    }

    @Test
    void leafRefContextUtilsTest() {
        final var q1 = QName.create(tst, "odl-contributor");
        final var q2 = QName.create(tst, "contributor");
        final var q3 = QName.create(tst, "odl-project-name");

        final var found = rootLeafRefContext.getLeafRefReferencingContext(Absolute.of(q1, q2, q3));
        assertNotNull(found);
        assertTrue(found.isReferencing());
        assertNotNull(found.getLeafRefTargetPath());
        assertEquals(rootLeafRefContext
            .getReferencingChildByName(q1).getReferencingChildByName(q2).getReferencingChildByName(q3), found);
    }

    @Test
    void leafRefContextUtilsTest2() {
        final var q1 = QName.create(tst, "odl-project");
        final var q2 = QName.create(tst, "project");
        final var q3 = QName.create(tst, "name");

        final var node = Absolute.of(q1, q2, q3);
        LeafRefContext found = rootLeafRefContext.getLeafRefReferencingContext(node);
        assertNull(found);

        found = rootLeafRefContext.getLeafRefReferencedByContext(node);

        assertNotNull(found);
        assertTrue(found.isReferenced());
        assertFalse(found.getAllReferencedByLeafRefCtxs().isEmpty());
        assertEquals(6, found.getAllReferencedByLeafRefCtxs().size());
        assertEquals(rootLeafRefContext
            .getReferencedChildByName(q1).getReferencedChildByName(q2).getReferencedChildByName(q3), found);
    }

    @Test
    void leafRefContextUtilsTest3() {
        final var q16 = QName.create(tst, "con1");
        final var con1 = Absolute.of(q16);

        final var allLeafRefChilds = rootLeafRefContext.findAllLeafRefChilds(con1);

        assertNotNull(allLeafRefChilds);
        assertFalse(allLeafRefChilds.isEmpty());
        assertEquals(4, allLeafRefChilds.size());

        var allChildsReferencedByLeafRef = rootLeafRefContext.findAllChildsReferencedByLeafRef(
            Absolute.of(QName.create(tst, "odl-contributor")));

        assertNotNull(allChildsReferencedByLeafRef);
        assertFalse(allChildsReferencedByLeafRef.isEmpty());
        assertEquals(1, allChildsReferencedByLeafRef.size());

        allChildsReferencedByLeafRef = rootLeafRefContext.findAllChildsReferencedByLeafRef(con1);

        assertNotNull(allChildsReferencedByLeafRef);
        assertTrue(allChildsReferencedByLeafRef.isEmpty());
    }

    @Test
    void incorrectLeafRefPathTest() {
        final var ise = assertThrows(IllegalStateException.class,
            () -> YangParserTestUtils.parseYangResourceDirectory("/leafref-context-test/incorrect-modules"));
        final var ype = ise.getCause();
        final var reactor = ype.getCause();
        assertInstanceOf(ReactorException.class, reactor);
        final var source = reactor.getCause();
        assertInstanceOf(SourceException.class, source);
        assertTrue(source.getMessage().startsWith("token recognition error at: './' at 1:2"));
    }
}
