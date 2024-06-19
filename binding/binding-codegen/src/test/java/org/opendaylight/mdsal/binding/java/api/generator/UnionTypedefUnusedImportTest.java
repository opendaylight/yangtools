/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.cleanUp;
import static org.opendaylight.mdsal.binding.java.api.generator.CompilationTestUtils.testCompilation;

import java.io.File;
import org.junit.Test;

public class UnionTypedefUnusedImportTest extends BaseCompilationTest {
    @Test
    public void testUnionTypedefUnusedImport() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("union-typedef");
        final var types = generateTestSources("/compilation/union-typedef", sourcesOutputDir);
        assertEquals(2, types.size());

        final File compiledOutputDir = CompilationTestUtils.compilerOutput("union-typedef");
        testCompilation(sourcesOutputDir, compiledOutputDir);
        cleanUp(sourcesOutputDir, compiledOutputDir);
    }
}
