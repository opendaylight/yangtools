/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class ImportRevisionsTest {

    private static final StatementStreamSource ROOT_WITH_EQUAL_DATE = sourceForResource(
        "/import-revision-date-test/root-with-equal-revision-date.yang");
    private static final StatementStreamSource IMPORTED_WITH_EQUAL_DATE = sourceForResource(
            "/import-revision-date-test/imported-module-with-equal-revision-date.yang");
    private static final StatementStreamSource ROOT_WITH_UNEQUAL_DATE = sourceForResource(
            "/import-revision-date-test/root-with-unequal-revision-date.yang");
    private static final StatementStreamSource IMPORTED_WITH_UNEQUAL_DATE = sourceForResource(
            "/import-revision-date-test/imported-module-with-unequal-revision-date.yang");
    private static final StatementStreamSource ROOT_WITH_DATE = sourceForResource(
            "/import-revision-date-test/root-with-revision-date.yang");
    private static final StatementStreamSource IMPORTED_WITHOUT_DATE = sourceForResource(
            "/import-revision-date-test/imported-module-without-revision-date.yang");
    private static final StatementStreamSource ROOT_WITHOUT_DATE = sourceForResource(
            "/import-revision-date-test/root-without-revision-date.yang");
    private static final StatementStreamSource IMPORTED_WITH_DATE = sourceForResource(
            "/import-revision-date-test/imported-module-with-revision-date.yang");
    private static final StatementStreamSource ROOT_WITH_1970_DATE = sourceForResource(
            "/import-revision-date-test/root-with-1970-revision-date.yang");
    private static final StatementStreamSource ROOT_WITH_NO_DATE = sourceForResource(
            "/import-revision-date-test/root-with-no-revision-date.yang");
    private static final StatementStreamSource IMPORTED_WITH_NO_DATE = sourceForResource(
            "/import-revision-date-test/imported-module-with-no-revision-date.yang");

    @Test
    public void equalRevisionDatesTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(ROOT_WITH_EQUAL_DATE, IMPORTED_WITH_EQUAL_DATE);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void unequalRevisionDatesTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(ROOT_WITH_UNEQUAL_DATE, IMPORTED_WITH_UNEQUAL_DATE);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);

    }

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void revisionDatesInRootOnlyTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(ROOT_WITH_DATE, IMPORTED_WITHOUT_DATE);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revisionDatesInImportedOnlyTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources( ROOT_WITHOUT_DATE, IMPORTED_WITH_DATE);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revision1970InRootOnlyTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(ROOT_WITH_1970_DATE, IMPORTED_WITHOUT_DATE);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void noRevisionInRootAndImportedTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(ROOT_WITH_NO_DATE, IMPORTED_WITH_NO_DATE);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }
}
