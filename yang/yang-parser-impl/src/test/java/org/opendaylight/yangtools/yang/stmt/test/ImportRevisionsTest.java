/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class ImportRevisionsTest {

    private static final YangStatementSourceImpl ROOT_WITH_EQUAL_DATE = new YangStatementSourceImpl
            ("/import-revision-date-test/root-with-equal-revision-date.yang", false);
    private static final YangStatementSourceImpl IMPORTED_WITH_EQUAL_DATE = new YangStatementSourceImpl
            ("/import-revision-date-test/imported-module-with-equal-revision-date.yang", false);
    private static final YangStatementSourceImpl ROOT_WITH_UNEQUAL_DATE = new YangStatementSourceImpl
            ("/import-revision-date-test/root-with-unequal-revision-date.yang", false);
    private static final YangStatementSourceImpl IMPORTED_WITH_UNEQUAL_DATE = new YangStatementSourceImpl
            ("/import-revision-date-test/imported-module-with-unequal-revision-date.yang", false);
    private static final YangStatementSourceImpl ROOT_WITH_DATE = new YangStatementSourceImpl
            ("/import-revision-date-test/root-with-revision-date.yang", false);
    private static final YangStatementSourceImpl IMPORTED_WITHOUT_DATE = new YangStatementSourceImpl
            ("/import-revision-date-test/imported-module-without-revision-date.yang", false);
    private static final YangStatementSourceImpl ROOT_WITHOUT_DATE = new YangStatementSourceImpl
            ("/import-revision-date-test/root-without-revision-date.yang", false);
    private static final YangStatementSourceImpl IMPORTED_WITH_DATE = new YangStatementSourceImpl
            ("/import-revision-date-test/imported-module-with-revision-date.yang", false);
    private static final YangStatementSourceImpl ROOT_WITH_1970_DATE = new YangStatementSourceImpl
            ("/import-revision-date-test/root-with-1970-revision-date.yang", false);
    private static final YangStatementSourceImpl ROOT_WITH_NO_DATE = new YangStatementSourceImpl
            ("/import-revision-date-test/root-with-no-revision-date.yang", false);
    private static final YangStatementSourceImpl IMPORTED_WITH_NO_DATE = new YangStatementSourceImpl
            ("/import-revision-date-test/imported-module-with-no-revision-date.yang", false);

    @Test
    public void equalRevisionDatesTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ROOT_WITH_EQUAL_DATE, IMPORTED_WITH_EQUAL_DATE);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void unequalRevisionDatesTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ROOT_WITH_UNEQUAL_DATE, IMPORTED_WITH_UNEQUAL_DATE);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);

    }

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void revisionDatesInRootOnlyTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ROOT_WITH_DATE, IMPORTED_WITHOUT_DATE);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revisionDatesInImportedOnlyTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ROOT_WITHOUT_DATE, IMPORTED_WITH_DATE);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revision1970InRootOnlyTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ROOT_WITH_1970_DATE, IMPORTED_WITHOUT_DATE);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void noRevisionInRootAndImportedTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ROOT_WITH_NO_DATE, IMPORTED_WITH_NO_DATE);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }
}
