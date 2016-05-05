/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import java.io.FileNotFoundException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.Module;
import java.util.List;
import java.util.Arrays;
import java.net.URISyntaxException;
import java.io.File;
import org.junit.Test;

public class MappedEffectiveBuildTest {

    @Test
    public void mappedBuildTest() throws URISyntaxException, SourceException,
            FileNotFoundException, ReactorException {
        File sourceDir = new File(getClass().getResource("/mapped-build/")
                .toURI());
        List<File> yangFiles = Arrays.asList(sourceDir.listFiles());

        assertEquals(4, yangFiles.size());

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        Map<File, Module> parseYangModelsMapped = reactor
                .buildEffectiveMappedToSource(yangFiles);

        assertEquals(2, parseYangModelsMapped.size());

        Set<Entry<File, Module>> mapEntrySet = parseYangModelsMapped.entrySet();
        for (Entry<File, Module> entry : mapEntrySet) {
            assertEquals(entry.getKey().getPath(), entry.getValue()
                    .getModuleSourcePath());
        }
    }
}
