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
import org.opendaylight.yangtools.yang.stmt.test.TestStatementSource.ModuleEntry;

public class ImportResolutionTest {

    private static final ModuleEntry modNature = new ModuleEntry("nature", "2000-01-01");
    private static final ModuleEntry modMammal = new ModuleEntry("mammal", "2000-01-01");
    private static final ModuleEntry modHuman = new ModuleEntry("human", "2000-01-01");
    private static final ModuleEntry modEgocentric = new ModuleEntry("egocentric", "2000-01-01");
    private static final ModuleEntry modCycleYin = new ModuleEntry("cycle-yin", "2000-01-01");
    private static final ModuleEntry modCycleYang = new ModuleEntry("cycle-yang", "2000-01-01");

    private static final TestStatementSource ROOT_WITHOUT_IMPORT = new TestStatementSource(modNature);
    private static final TestStatementSource IMPORT_ROOT = new TestStatementSource(modMammal, modNature);
    private static final TestStatementSource IMPORT_DERIVED = new TestStatementSource(modHuman, modMammal);
    private static final TestStatementSource IMPORT_SELF = new TestStatementSource(modEgocentric, modEgocentric);
    private static final TestStatementSource CICLE_YIN = new TestStatementSource(modCycleYin, modCycleYang);
    private static final TestStatementSource CICLE_YANG = new TestStatementSource(modCycleYang, modCycleYin);

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
            fail("reactor.process should fail due to misssing imported source");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
        }
    }

    @Test
    public void circularImportsTest() throws SourceException {
        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, CICLE_YIN, CICLE_YANG);
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

    private static void addSources(final BuildAction reactor, final TestStatementSource... sources) {
        for (TestStatementSource source : sources) {
            reactor.addSource(source);
        }
    }

}
