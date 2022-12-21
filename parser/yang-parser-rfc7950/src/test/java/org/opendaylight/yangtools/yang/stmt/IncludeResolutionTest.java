/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.ReactorDeclaredModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IncludeResolutionTest {

    private static final Logger LOG = LoggerFactory.getLogger(IncludeResolutionTest.class);

    private static final StatementStreamSource ROOT = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/root-module.yang");
    private static final StatementStreamSource SUBMODULE1 = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/submodule-1.yang");
    private static final StatementStreamSource SUBMODULE2 = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/submodule-2.yang");
    private static final StatementStreamSource ERROR_MODULE = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/error-module.yang");
    private static final StatementStreamSource ERROR_SUBMODULE = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/error-submodule.yang");

    private static final StatementStreamSource MISSING_PARENT_MODULE = sourceForResource(
        "/semantic-statement-parser/include-arg-parsing/missing-parent.yang");

    @Test
    void includeTest() throws SourceException, ReactorException {
        ReactorDeclaredModel result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(ROOT, SUBMODULE1, SUBMODULE2)
            .build();
        assertNotNull(result);
    }

    @Test
    void missingIncludedSourceTest() throws SourceException {
        BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild().addSource(ERROR_MODULE);
        try {
            reactor.build();
            fail("reactor.process should fail due to missing included source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
            LOG.info(e.getMessage());
        }

    }

    @Test
    void missingIncludedSourceTest2() throws SourceException {
        BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild().addSource(ERROR_SUBMODULE);
        try {
            reactor.build();
            fail("reactor.process should fail due to missing included source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
            LOG.info(e.getMessage());
        }

    }

    @Test
    void missingIncludedSourceTest3() throws SourceException, ReactorException {
        BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild().addSource(MISSING_PARENT_MODULE);
        try {
            reactor.build();
            fail("reactor.process should fail due to missing belongsTo source");
        } catch (ReactorException e) {
            LOG.info(e.getMessage());
        }

    }
}
