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

class YT1896Test {

    @Test
    void testUnusedLibrarySubmoduleCrashes() throws Exception {
        final var reactor = RFC7950Reactors.defaultReactor().newBuild();

        // 1. Add an UNRELATED module as the primary source
        reactor.addSource(StmtTestUtils.sourceForResource("/bugs/YT1896/bar.yang"));

        // 2. Add the module and submodule as UNUSED library sources
        reactor.addLibSource(
            StmtTestUtils.sourceForResource("/bugs/YT1896/foo.yang"));
        reactor.addLibSource(
            StmtTestUtils.sourceForResource("/bugs/YT1896/foo-sub.yang"));

        // 3. This should not crash with InferenceException: Submodule was not resolved
        final var context = reactor.buildEffective();

        assertNotNull(context);
    }
}
