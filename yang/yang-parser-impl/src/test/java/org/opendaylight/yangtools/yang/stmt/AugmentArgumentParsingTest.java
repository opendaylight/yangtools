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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class AugmentArgumentParsingTest {

    private static final StatementStreamSource IMPORTED = sourceForResource(
            "/semantic-statement-parser/augment-arg-parsing/imported.yang");
    private static final StatementStreamSource VALID_ARGS = sourceForResource(
            "/semantic-statement-parser/augment-arg-parsing/root-valid-aug-args.yang");
    private static final StatementStreamSource INVALID_REL1 = sourceForResource(
            "/semantic-statement-parser/augment-arg-parsing/root-invalid-rel1.yang");
    private static final StatementStreamSource INVALID_REL2 = sourceForResource(
            "/semantic-statement-parser/augment-arg-parsing/root-invalid-rel2.yang");
    private static final StatementStreamSource INVALID_ABS = sourceForResource(
            "/semantic-statement-parser/augment-arg-parsing/root-invalid-abs.yang");
    private static final StatementStreamSource INVALID_ABS_PREFIXED_NO_IMP = sourceForResource(
            "/semantic-statement-parser/augment-arg-parsing/root-invalid-abs-no-imp.yang");
    private static final StatementStreamSource INVALID_EMPTY = sourceForResource(
            "/semantic-statement-parser/augment-arg-parsing/root-invalid-empty.yang");
    private static final StatementStreamSource INVALID_XPATH = sourceForResource(
            "/semantic-statement-parser/augment-arg-parsing/root-invalid-xpath.yang");

    @Test
    public void validAugAbsTest() throws ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(IMPORTED, VALID_ARGS);

        final EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void invalidAugRel1Test() {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(INVALID_REL1);

        try {
            reactor.build();
            fail("reactor.process should fail due to invalid relative path");
        } catch (ReactorException e) {
            assertSourceExceptionCause(e, "Augment argument './aug1/aug11' is not valid");
        }
    }

    @Test
    public void invalidAugRel2Test() {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(INVALID_REL2);

        try {
            reactor.build();
            fail("reactor.process should fail due to invalid relative path");
        } catch (ReactorException e) {
            assertSourceExceptionCause(e, "Augment argument '../aug1/aug11' is not valid");
        }
    }

    @Test
    public void invalidAugAbs() {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(INVALID_ABS);

        try {
            reactor.build();
            fail("reactor.process should fail due to invalid absolute path");
        } catch (ReactorException e) {
            assertSourceExceptionCause(e, "Augment argument '//aug1/aug11/aug111' is not valid");
        }
    }

    @Test
    public void invalidAugAbsPrefixedNoImp() {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(INVALID_ABS_PREFIXED_NO_IMP);

        try {
            reactor.build();
            fail("reactor.process should fail due to missing import from augment path");
        } catch (ReactorException e) {
            assertSourceExceptionCause(e, "Failed to parse node 'imp:aug1'");
        }
    }

    @Test
    @Ignore
    public void invalidAugEmptyTest() throws SourceException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(INVALID_EMPTY);

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
        reactor.addSources(INVALID_XPATH);

        try {
            reactor.build();
            fail("reactor.process should fail due to invalid XPath");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    private static void assertSourceExceptionCause(final Throwable e, final String start) {
        final Throwable cause = e.getCause();
        assertTrue(cause instanceof SourceException);
        assertTrue(cause.getMessage().startsWith(start));
    }
}
