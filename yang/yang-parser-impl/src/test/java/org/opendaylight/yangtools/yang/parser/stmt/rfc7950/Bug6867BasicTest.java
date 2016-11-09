/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6867BasicTest {

    @Test
    public void valid10Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/basic-test/valid-10.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void valid11Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/basic-test/valid-11.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void invalid10Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/basic-test/invalid-10.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            e.getCause().getMessage().startsWith("NOTIFICATION is not valid for CONTAINER");
        }
    }

    @Test
    public void invalid11Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/basic-test/invalid-11.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final SomeModifiersUnresolvedException e) {
            e.getCause().getMessage().startsWith("RPC is not valid for CONTAINER");
        }
    }

    @Test
    public void anyData11Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/basic-test/anydata-11.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void anyData10Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/basic-test/anydata-10.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            e.getCause().getMessage().startsWith("anydata is not a YANG statement or use of extension");
        }
    }

    @Test
    public void yangModelTest() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources("/rfc7950/model");
        assertNotNull(schemaContext);
    }
}