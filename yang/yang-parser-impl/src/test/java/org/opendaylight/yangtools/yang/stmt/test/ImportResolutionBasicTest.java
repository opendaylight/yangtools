/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class ImportResolutionBasicTest {

    private static final YangStatementSourceImpl ROOT_WITHOUT_IMPORT = new YangStatementSourceImpl(
            "/semantic-statement-parser/import-arg-parsing/nature.yang", false);
    private static final YangStatementSourceImpl IMPORT_ROOT = new YangStatementSourceImpl(
            "/semantic-statement-parser/import-arg-parsing/mammal.yang", false);
    private static final YangStatementSourceImpl IMPORT_DERIVED = new YangStatementSourceImpl(
            "/semantic-statement-parser/import-arg-parsing/human.yang", false);
    private static final YangStatementSourceImpl IMPORT_SELF = new YangStatementSourceImpl(
            "/semantic-statement-parser/import-arg-parsing/egocentric.yang", false);
    private static final YangStatementSourceImpl CYCLE_YIN = new YangStatementSourceImpl(
            "/semantic-statement-parser/import-arg-parsing/cycle-yin.yang", false);
    private static final YangStatementSourceImpl CYCLE_YANG = new YangStatementSourceImpl(
            "/semantic-statement-parser/import-arg-parsing/cycle-yang.yang", false);
    private static final YangStatementSourceImpl FOO = new YangStatementSourceImpl(
            "/semantic-statement-parser/bug2649/foo.yang", false);
    private static final YangStatementSourceImpl IMPORT = new YangStatementSourceImpl(
            "/semantic-statement-parser/bug2649/import-module.yang", false);


    @Test
    public void inImportOrderTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, ROOT_WITHOUT_IMPORT, IMPORT_ROOT, IMPORT_DERIVED);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void inInverseOfImportOrderTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, IMPORT_DERIVED, IMPORT_ROOT, ROOT_WITHOUT_IMPORT);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void missingImportedSourceTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, IMPORT_DERIVED, ROOT_WITHOUT_IMPORT);
        try {
            reactor.build();
            fail("reactor.process should fail due to missing imported source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
        }

    }

    @Test
    public void circularImportsTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, CYCLE_YIN, CYCLE_YANG);
        try {
            reactor.build();
            fail("reactor.process should fail due to circular import");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
        }
    }

    @Test
    public void selfImportTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, IMPORT_SELF, IMPORT_ROOT, ROOT_WITHOUT_IMPORT);
        try {
            reactor.build();
            fail("reactor.process should fail due to self import");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
        }
    }

    @Test
    public void bug2649Test() throws SourceException, ReactorException{
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, FOO, IMPORT);

        EffectiveSchemaContext buildEffective = reactor.buildEffective();
        assertNotNull(buildEffective);
    }

    private void addSources(final BuildAction reactor, final YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }

}
