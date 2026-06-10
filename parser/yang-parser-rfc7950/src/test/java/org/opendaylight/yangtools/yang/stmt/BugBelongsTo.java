/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;

class BugBelongsTo {

    @Test
    void testUnusedLibrarySubmoduleDoesNotCrash() throws Exception {
        final var reactor = RFC7950Reactors.defaultReactor().newBuild();

        // 1. Add the main module as a primary source
        reactor.addSource(StmtTestUtils.sourceForResource("/bugs/bugBelongsTo/foo.yang"));

        // 2. Add the unused submodule as a library source
        reactor.addLibSource(StmtTestUtils.sourceForResource("/bugs/bugBelongsTo/foo-sub.yang"));

        // 3. This should successfully build without throwing InferenceException
        final var context = reactor.buildEffective();

        assertNotNull(context);
    }
}