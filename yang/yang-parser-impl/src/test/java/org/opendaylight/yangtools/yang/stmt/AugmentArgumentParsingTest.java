/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class AugmentArgumentParsingTest {

    private static final YangStatementSourceImpl IMPORTED = new YangStatementSourceImpl(
            "/semantic-statement-parser/augment-arg-parsing/imported.yang", false);
    private static final YangStatementSourceImpl VALID_ARGS = new YangStatementSourceImpl(
            "/semantic-statement-parser/augment-arg-parsing/root-valid-aug-args.yang", false);
    private static final YangStatementSourceImpl INVALID_REL1 = new YangStatementSourceImpl(
            "/semantic-statement-parser/augment-arg-parsing/root-invalid-rel1.yang", false);
    private static final YangStatementSourceImpl INVALID_REL2 = new YangStatementSourceImpl(
            "/semantic-statement-parser/augment-arg-parsing/root-invalid-rel2.yang", false);
    private static final YangStatementSourceImpl INVALID_ABS = new YangStatementSourceImpl(
            "/semantic-statement-parser/augment-arg-parsing/root-invalid-abs.yang", false);
    private static final YangStatementSourceImpl INVALID_ABS_PREFIXED_NO_IMP = new YangStatementSourceImpl(
            "/semantic-statement-parser/augment-arg-parsing/root-invalid-abs-no-imp.yang", false);
    private static final YangStatementSourceImpl INVALID_EMPTY = new YangStatementSourceImpl(
            "/semantic-statement-parser/augment-arg-parsing/root-invalid-empty.yang", false);
    private static final YangStatementSourceImpl INVALID_XPATH = new YangStatementSourceImpl(
            "/semantic-statement-parser/augment-arg-parsing/root-invalid-xpath.yang", false);

    @Test
    public void validAugAbsTest() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, IMPORTED, VALID_ARGS);

        final EffectiveModelContext result = reactor.build();
        assertNotNull(result);
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
    @Ignore
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
    @Ignore
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

    private static void addSources(final BuildAction reactor, final YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }
}
