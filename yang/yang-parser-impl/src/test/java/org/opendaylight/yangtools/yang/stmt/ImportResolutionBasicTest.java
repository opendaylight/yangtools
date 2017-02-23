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

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class ImportResolutionBasicTest {

    private static final StatementStreamSource ROOT_WITHOUT_IMPORT = sourceForResource(
            "/semantic-statement-parser/import-arg-parsing/nature.yang");
    private static final StatementStreamSource IMPORT_ROOT = sourceForResource(
            "/semantic-statement-parser/import-arg-parsing/mammal.yang");
    private static final StatementStreamSource IMPORT_DERIVED = sourceForResource(
            "/semantic-statement-parser/import-arg-parsing/human.yang");
    private static final StatementStreamSource IMPORT_SELF = sourceForResource(
            "/semantic-statement-parser/import-arg-parsing/egocentric.yang");
    private static final StatementStreamSource CYCLE_YIN = sourceForResource(
            "/semantic-statement-parser/import-arg-parsing/cycle-yin.yang");
    private static final StatementStreamSource CYCLE_YANG = sourceForResource(
            "/semantic-statement-parser/import-arg-parsing/cycle-yang.yang");
    private static final StatementStreamSource FOO = sourceForResource(
            "/semantic-statement-parser/bug2649/foo.yang");
    private static final StatementStreamSource IMPORT = sourceForResource(
            "/semantic-statement-parser/bug2649/import-module.yang");


    @Test
    public void inImportOrderTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(ROOT_WITHOUT_IMPORT, IMPORT_ROOT, IMPORT_DERIVED);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void inInverseOfImportOrderTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(IMPORT_DERIVED, IMPORT_ROOT, ROOT_WITHOUT_IMPORT);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void missingImportedSourceTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(IMPORT_DERIVED, ROOT_WITHOUT_IMPORT);
        try {
            reactor.build();
            fail("reactor.process should fail due to missing imported source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_PRE_LINKAGE, e.getPhase());
        }

    }

    @Test
    public void circularImportsTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(CYCLE_YIN, CYCLE_YANG);
        try {
            reactor.build();
            fail("reactor.process should fail due to circular import");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_PRE_LINKAGE, e.getPhase());
        }
    }

    @Test
    public void selfImportTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(IMPORT_SELF, IMPORT_ROOT, ROOT_WITHOUT_IMPORT);
        try {
            reactor.build();
            fail("reactor.process should fail due to self import");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_PRE_LINKAGE, e.getPhase());
        }
    }

    @Test
    public void bug2649Test() throws SourceException, ReactorException{
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(FOO, IMPORT);

        EffectiveSchemaContext buildEffective = reactor.buildEffective();
        assertNotNull(buildEffective);
    }
}
