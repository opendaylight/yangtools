/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.cleanUp;
import static org.opendaylight.yangtools.binding.codegen.CompilationTestUtils.testCompilation;

import org.junit.jupiter.api.Test;

class UnionTypedefUnusedImportTest extends BaseCompilationTest {
    @Test
    void testUnionTypedefUnusedImport() throws Exception {
        final var sourcesOutputDir = CompilationTestUtils.generatorOutput("union-typedef");
        final var types = generateTestSources("/compilation/union-typedef", sourcesOutputDir);
        assertEquals(2, types.size());

        final var compiledOutputDir = CompilationTestUtils.compilerOutput("union-typedef");
        testCompilation(sourcesOutputDir, compiledOutputDir);
        cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
