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
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class IncludeRevisionsTest {

    private static final StatementStreamSource EQUAL_ROOT = sourceForResource("/revisions/equal-root.yang");
    private static final StatementStreamSource EQUAL_REV = sourceForResource("/revisions/equal-rev.yang");
    private static final StatementStreamSource UNEQUAL_ROOT = sourceForResource("/revisions/unequal-root.yang");
    private static final StatementStreamSource UNEQUAL_REV = sourceForResource("/revisions/unequal-rev.yang");
    private static final StatementStreamSource SUBMOD_ONLY_ROOT = sourceForResource("/revisions/submod-only-root.yang");
    private static final StatementStreamSource SUBMOD_ONLY_REV = sourceForResource("/revisions/submod-only-rev.yang");
    private static final StatementStreamSource MOD_ONLY_ROOT = sourceForResource("/revisions/mod-only-root.yang");
    private static final StatementStreamSource MOD_ONLY_REV = sourceForResource("/revisions/mod-only-rev.yang");
    private static final StatementStreamSource MOD_ONLY_1970_ROOT = sourceForResource("/revisions/mod-1970-root.yang");
    private static final StatementStreamSource MOD_ONLY_1970_REV = sourceForResource("/revisions/mod-1970-rev.yang");
    private static final StatementStreamSource NOWHERE_ROOT = sourceForResource("/revisions/nowhere-root.yang");
    private static final StatementStreamSource NOWHERE_REV = sourceForResource("/revisions/nowhere-rev.yang");

    @Test
    public void revsEqualTest() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, EQUAL_REV, EQUAL_ROOT);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revsUnequalTest() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, UNEQUAL_REV, UNEQUAL_ROOT);

        try {
            reactor.build();
            fail("reactor.process should fail due to unequal revisions in include and submodule");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
        }
    }

    @Test
    public void revIncludeOnly() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, SUBMOD_ONLY_REV, SUBMOD_ONLY_ROOT);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revInModuleOnly() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, MOD_ONLY_REV, MOD_ONLY_ROOT);

        try {
            reactor.build();
            fail("reactor.process should fail due to missing revision in included submodule");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
        }
    }

    @Test
    public void rev1970InModuleOnlyTest() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, MOD_ONLY_1970_REV, MOD_ONLY_1970_ROOT);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void revNowhereTest() throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, NOWHERE_REV, NOWHERE_ROOT);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    private static void addSources(final CrossSourceStatementReactor.BuildAction reactor, final StatementStreamSource... sources) {
        for (StatementStreamSource source : sources) {
            reactor.addSource(source);
        }
    }
}
