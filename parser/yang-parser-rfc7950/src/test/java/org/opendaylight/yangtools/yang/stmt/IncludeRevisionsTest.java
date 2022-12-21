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
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.ReactorDeclaredModel;

class IncludeRevisionsTest {

    private static final StatementStreamSource EQUAL_ROOT = sourceForResource("/revisions/equal-root.yang");
    private static final StatementStreamSource EQUAL_REV = sourceForResource("/revisions/equal-rev.yang");
    private static final StatementStreamSource UNEQUAL_ROOT = sourceForResource("/revisions/unequal-root.yang");
    private static final StatementStreamSource UNEQUAL_REV = sourceForResource("/revisions/unequal-rev.yang");
    private static final StatementStreamSource SUBMOD_ONLY_ROOT = sourceForResource("/revisions/submod-only-root.yang");
    private static final StatementStreamSource SUBMOD_ONLY_REV = sourceForResource("/revisions/submod-only-rev.yang");
    private static final StatementStreamSource MOD_ONLY_ROOT = sourceForResource("/revisions/mod-only-root.yang");
    private static final StatementStreamSource MOD_ONLY_REV = sourceForResource("/revisions/mod-only-rev.yang");
    private static final StatementStreamSource NOWHERE_ROOT = sourceForResource("/revisions/nowhere-root.yang");
    private static final StatementStreamSource NOWHERE_REV = sourceForResource("/revisions/nowhere-rev.yang");

    @Test
    void revsEqualTest() throws ReactorException {
        ReactorDeclaredModel result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(EQUAL_REV, EQUAL_ROOT)
            .build();
        assertNotNull(result);
    }

    @Test
    void revsUnequalTest() throws ReactorException {
        BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild().addSources(UNEQUAL_REV, UNEQUAL_ROOT);
        try {
            reactor.build();
            fail("reactor.process should fail due to unequal revisions in include and submodule");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
        }
    }

    @Test
    void revIncludeOnly() throws ReactorException {
        ReactorDeclaredModel result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(SUBMOD_ONLY_REV, SUBMOD_ONLY_ROOT)
            .build();
        assertNotNull(result);
    }

    @Test
    void revInModuleOnly() throws ReactorException {
        BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild().addSources(MOD_ONLY_REV, MOD_ONLY_ROOT);
        try {
            reactor.build();
            fail("reactor.process should fail due to missing revision in included submodule");
        } catch (ReactorException e) {
            assertTrue(e instanceof SomeModifiersUnresolvedException);
            assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, e.getPhase());
        }
    }

    @Test
    void revNowhereTest() throws ReactorException {
        ReactorDeclaredModel result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(NOWHERE_REV, NOWHERE_ROOT)
            .build();
        assertNotNull(result);
    }
}
