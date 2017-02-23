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

import java.util.logging.Logger;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class IncludeResolutionTest {

    private static final Logger log = Logger.getLogger(IncludeResolutionTest.class.getName());

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
    public void includeTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(ROOT, SUBMODULE1, SUBMODULE2);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void missingIncludedSourceTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(ERROR_MODULE);
        try {
            reactor.build();
            fail("reactor.process should fail due to missing included source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
            log.info(e.getMessage());
        }

    }

    @Test
    public void missingIncludedSourceTest2() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(ERROR_SUBMODULE);
        try {
            reactor.build();
            fail("reactor.process should fail due to missing included source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
            log.info(e.getMessage());
        }

    }

    @Test
    public void missingIncludedSourceTest3() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(MISSING_PARENT_MODULE);
        try {
            reactor.build();
            fail("reactor.process should fail due to missing belongsTo source");
        } catch (ReactorException e) {
            log.info(e.getMessage());
        }

    }
}
