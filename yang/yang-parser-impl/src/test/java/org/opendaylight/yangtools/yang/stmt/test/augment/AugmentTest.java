/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test.augment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class AugmentTest {

    private static final TestAugmentSource IMPORTED = new TestAugmentSource("imp", "/a");
    private static final TestAugmentSource VALID_ABS = new TestAugmentSource("root", "/aug1/aug11/aug111");
    private static final TestAugmentSource VALID_ABS_PREFIXED = new TestAugmentSource("root",
            "/imp:aug1/imp:aug11/imp:aug111", "imp");
    private static final TestAugmentSource VALID_REL = new TestAugmentSource("root", "aug1/aug11");
    private static final TestAugmentSource VALID_REL_PREFIXED = new TestAugmentSource("root",
            "imp:aug1/imp:aug11/imp:aug111", "imp");
    private static final TestAugmentSource INVALID_REL_WHITE_SPACE = new TestAugmentSource("root", "..   /aug1/aug11");
    private static final TestAugmentSource INVALID_REL1 = new TestAugmentSource("root", "./aug1/aug11");
    private static final TestAugmentSource INVALID_REL2 = new TestAugmentSource("root", "../aug1/aug11");
    private static final TestAugmentSource INVALID_ABS = new TestAugmentSource("root", "//aug1/aug11/aug111");
    private static final TestAugmentSource INVALID_ABS_PREFIXED_NO_IMP = new TestAugmentSource("root",
            "imp:aug1/imp:aug11/imp:aug111");
    private static final TestAugmentSource INVALID_EMPTY = new TestAugmentSource("root", "");
    private static final TestAugmentSource INVALID_XPATH = new TestAugmentSource("root", "/aug1/-");

    @Test
    public void validAugAbsTest() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, VALID_ABS);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void validAugAbsPrefixedTest() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, IMPORTED, VALID_ABS_PREFIXED);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void validAugRelTest() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, VALID_REL);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void validAugRelPrefixedTest() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, IMPORTED, VALID_REL_PREFIXED);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void validAugRelWhiteSpaceTest() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, INVALID_REL_WHITE_SPACE);

        try {
            reactor.build();
            fail("reactor.process should fail due to invalid relative path");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    @Test
    public void invalidAugRel1Test() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, INVALID_REL1);

        try {
            reactor.build();
            fail("reactor.process should fail due to invalid relative path");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    @Test
    public void invalidAugRel2Test() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, INVALID_REL2);

        try {
            reactor.build();
            fail("reactor.process should fail due to invalid relative path");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    @Test
    public void invalidAugAbs() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, INVALID_ABS);

        try {
            reactor.build();
            fail("reactor.process should fail due to invalid absolute path");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    @Test
    public void invalidAugAbsPrefixedNoImp() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, INVALID_ABS_PREFIXED_NO_IMP);

        try {
            reactor.build();
            fail("reactor.process should fail due to missing import from augment path");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    @Test
    public void invalidAugEmptyTest() throws SourceException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, INVALID_EMPTY);

        try {
            reactor.build();
            fail("reactor.process should fail due to empty path");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    @Test
    public void invalidAugXPathTest() throws SourceException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, INVALID_XPATH);

        try {
            reactor.build();
            fail("reactor.process should fail due to invalid XPath");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    private void addSources(BuildAction reactor, TestAugmentSource... sources) {
        for (TestAugmentSource source : sources) {
            reactor.addSource(source);
        }
    }

}
