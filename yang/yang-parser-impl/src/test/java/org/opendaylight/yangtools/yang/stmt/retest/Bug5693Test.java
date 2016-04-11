/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;

public class Bug5693Test {

    private final String pathToModuleFoo = getClass().getResource("/bugs/bug5693/foo.xml").getPath();
    private final String pathToModuleBar = getClass().getResource("/bugs/bug5693/bar.xml").getPath();

    @Test
    public void bug5693Test() throws ReactorException, FileNotFoundException {
        Set<Module> modules = TestUtils.loadYinModules(getStreams());
        assertEquals(2, modules.size());
    }

    private List<InputStream> getStreams() throws FileNotFoundException {
        List<InputStream> streams = new ArrayList<>();

        streams.add(new NamedFileInputStream(new File(pathToModuleFoo), pathToModuleFoo));
        streams.add(new NamedFileInputStream(new File(pathToModuleBar), pathToModuleBar));

        return streams;
    }
}
