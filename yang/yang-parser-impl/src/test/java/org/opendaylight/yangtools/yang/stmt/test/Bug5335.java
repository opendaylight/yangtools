/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug5335 {

    @Test
    public void incorrectTest1() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/incorrect/case-1");
        assertNotNull(context);
    }

    @Test
    public void incorrectTest2() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/incorrect/case-2");
        assertNotNull(context);
    }

    @Test
    public void incorrectTest3() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/incorrect/case-2");
        assertNotNull(context);
    }

    @Test
    public void correctTest1() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/correct/case-1");
        assertNotNull(context);
    }

    @Test
    public void correctTest2() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/correct/case-2");
        assertNotNull(context);
    }

    @Test
    public void correctTest3() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/correct/case-3");
        assertNotNull(context);
    }

    @Test
    public void correctTest4() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/correct/case-4");
        assertNotNull(context);
    }
}
