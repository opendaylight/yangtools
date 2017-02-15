/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6886Test {

    @Test
    public void yang11Test() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6886/yang11/foo.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void yang11Test2() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6886/yang11/foo2.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void yang10Test() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6886/yang10/foo.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void yang10Test2() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6886/yang10/foo2.yang");
        assertNotNull(schemaContext);
    }
}
