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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

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
    void revsEqualTest() throws Exception {
        var result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(EQUAL_REV, EQUAL_ROOT)
            .build();
        assertNotNull(result);
    }

    @Test
    void revsUnequalTest() {
        var reactor = RFC7950Reactors.defaultReactor().newBuild().addSources(UNEQUAL_REV, UNEQUAL_ROOT);
        var ex = assertThrows(SomeModifiersUnresolvedException.class, reactor::build);
        assertEquals(ModelProcessingPhase.INIT, ex.getPhase());
    }

    @Test
    void revIncludeOnly() throws Exception {
        var result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(SUBMOD_ONLY_REV, SUBMOD_ONLY_ROOT)
            .build();
        assertNotNull(result);
    }

    @Test
    void revInModuleOnly() {
        var reactor = RFC7950Reactors.defaultReactor().newBuild().addSources(MOD_ONLY_REV, MOD_ONLY_ROOT);
        var ex = assertThrows(SomeModifiersUnresolvedException.class, reactor::build);
        assertEquals(ModelProcessingPhase.INIT, ex.getPhase());
    }

    @Test
    void revNowhereTest() throws Exception {
        var result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(NOWHERE_REV, NOWHERE_ROOT)
            .build();
        assertNotNull(result);
    }
}
