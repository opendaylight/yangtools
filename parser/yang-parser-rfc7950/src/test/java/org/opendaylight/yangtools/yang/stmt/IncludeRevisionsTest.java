/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

class IncludeRevisionsTest {
    @Test
    void revsEqualTest() throws Exception {
        assertNotNull(RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/revisions/equal-rev.yang"))
            .addSource(sourceForResource("/revisions/equal-root.yang"))
            .buildDeclared());
    }

    @Test
    void revsUnequalTest() {
        var ex = assertThrows(SomeModifiersUnresolvedException.class, () -> RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/revisions/unequal-rev.yang"))
            .addSource(sourceForResource("/revisions/unequal-root.yang"))
            .buildDeclared());
        assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, ex.getPhase());
        final var cause = assertInstanceOf(InferenceException.class, ex.getCause());
        assertEquals("Included submodule unequal-rev was not found [at unequal-root:5:5]", cause.getMessage());
    }

    @Test
    void revIncludeOnly() throws Exception {
        assertNotNull(RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/revisions/submod-only-rev.yang"))
            .addSource(sourceForResource("/revisions/submod-only-root.yang"))
            .buildDeclared());
    }

    @Test
    void revInModuleOnly() {
        var ex = assertThrows(SomeModifiersUnresolvedException.class, () -> RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/revisions/mod-only-rev.yang"))
            .addSource(sourceForResource("/revisions/mod-only-root.yang")).buildDeclared());
        assertEquals(ModelProcessingPhase.SOURCE_LINKAGE, ex.getPhase());
        final var cause = assertInstanceOf(InferenceException.class, ex.getCause());
        assertEquals("Included submodule mod-only-rev was not found [at mod-only-root:5:5]", cause.getMessage());
    }

    @Test
    void revNowhereTest() throws Exception {
        assertNotNull(RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/revisions/nowhere-rev.yang"))
            .addSource(sourceForResource("/revisions/nowhere-root.yang"))
            .buildDeclared());
    }
}
