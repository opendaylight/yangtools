/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableTable;
import com.google.common.io.Resources;
import java.io.File;
import java.util.List;
import org.junit.Test;

public class GenerateSourcesTest extends AbstractCodeGeneratorTest {
    @Test
    public void test() throws Exception {
        assertMojoExecution(new YangToSourcesProcessor(
            new File(Resources.getResource(GenerateSourcesTest.class, "/yang").toURI()), List.of(),
            List.of(new FileGeneratorArg("mockGenerator")), project, false, yangProvider),
            mock -> {
                doReturn(ImmutableTable.of()).when(mock).generateFiles(any(), any(), any());
            },
            mock -> {
                verify(mock).generateFiles(any(), any(), any());
            });
    }
}
