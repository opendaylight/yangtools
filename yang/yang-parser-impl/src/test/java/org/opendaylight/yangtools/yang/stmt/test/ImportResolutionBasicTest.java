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
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class ImportResolutionBasicTest {

    private static final ImportBasicTestStatementSource ROOT_WITHOUT_IMPORT = new ImportBasicTestStatementSource("nature");
    private static final ImportBasicTestStatementSource IMPORT_ROOT = new ImportBasicTestStatementSource("mammal","nature");
    private static final ImportBasicTestStatementSource IMPORT_DERIVED = new ImportBasicTestStatementSource("human", "mammal");
    private static final ImportBasicTestStatementSource IMPORT_SELF = new ImportBasicTestStatementSource("egocentric", "egocentric");
    private static final ImportBasicTestStatementSource CICLE_YIN = new ImportBasicTestStatementSource("cycle-yin", "cycle-yang");
    private static final ImportBasicTestStatementSource CICLE_YANG = new ImportBasicTestStatementSource("cycle-yang", "cycle-yin");


    @Test
    public void inImportOrderTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,ROOT_WITHOUT_IMPORT,IMPORT_ROOT,IMPORT_DERIVED);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void inInverseOfImportOrderTest() throws SourceException, ReactorException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,IMPORT_DERIVED,IMPORT_ROOT,ROOT_WITHOUT_IMPORT);
        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void missingImportedSourceTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,IMPORT_DERIVED,ROOT_WITHOUT_IMPORT);
        try {
            reactor.build();
            fail("reactor.process should fail doe to misssing imported source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE,e.getPhase());
        }

    }

    @Test
    public void circularImportsTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,CICLE_YIN,CICLE_YANG);
        try {
            reactor.build();
            fail("reactor.process should fail doe to circular import");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE,e.getPhase());
        }
    }

    @Test
    public void selfImportTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor,IMPORT_SELF,IMPORT_ROOT,ROOT_WITHOUT_IMPORT);
        try {
            reactor.build();
            fail("reactor.process should fail doe to self import");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE,e.getPhase());
        }
    }


    private static void addSources(final BuildAction reactor, final ImportBasicTestStatementSource... sources) {
        for(ImportBasicTestStatementSource source : sources) {
            reactor.addSource(source);
        }
    }

}
