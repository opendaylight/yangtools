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
import org.opendaylight.yangtools.yang.parser.impl.DefaultReactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;

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
    private static final StatementStreamSource ROOT_WITH_NO_DATE = sourceForResource(
            "/import-revision-date-test/root-with-no-revision-date.yang");
    private static final StatementStreamSource IMPORTED_WITH_NO_DATE = sourceForResource(
            "/import-revision-date-test/imported-module-with-no-revision-date.yang");

    @Test
    public void equalRevisionDatesTest() throws ReactorException {
        EffectiveModelContext result = DefaultReactors.defaultReactor().newBuild()
                .addSources(ROOT_WITH_EQUAL_DATE, IMPORTED_WITH_EQUAL_DATE)
                .build();
        assertNotNull(result);
    }

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void unequalRevisionDatesTest() throws ReactorException {
        DefaultReactors.defaultReactor().newBuild()
                .addSources(ROOT_WITH_UNEQUAL_DATE, IMPORTED_WITH_UNEQUAL_DATE)
                .build();
    }

    @Test(expected = SomeModifiersUnresolvedException.class)
    public void revisionDatesInRootOnlyTest() throws ReactorException {
        EffectiveModelContext result = DefaultReactors.defaultReactor().newBuild()
                .addSources(ROOT_WITH_DATE, IMPORTED_WITHOUT_DATE)
                .build();
        assertNotNull(result);
    }

    @Test
    public void revisionDatesInImportedOnlyTest() throws ReactorException {
        EffectiveModelContext result = DefaultReactors.defaultReactor().newBuild()
                .addSources(ROOT_WITHOUT_DATE, IMPORTED_WITH_DATE)
                .build();
        assertNotNull(result);
    }

    @Test
    public void noRevisionInRootAndImportedTest() throws ReactorException {
        EffectiveModelContext result = DefaultReactors.defaultReactor().newBuild()
                .addSources(ROOT_WITH_NO_DATE, IMPORTED_WITH_NO_DATE)
                .build();
        assertNotNull(result);
    }
}
