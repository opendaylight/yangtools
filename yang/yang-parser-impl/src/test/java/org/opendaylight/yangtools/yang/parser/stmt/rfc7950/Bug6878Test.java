/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.text.ParseException;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6878Test {

    @Test
    public void testParsingYang11XPathFunction() throws ReactorException, FileNotFoundException, URISyntaxException,
            ParseException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6878/foo.yang");
        assertNotNull(schemaContext);
    }

    @Ignore
    @Test
    public void shouldFailOnInvalidYang10Model() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6878/foo10-invalid.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith("XPath function re-match() is not valid for yang-version 1."));
        }
    }

    @Ignore
    @Test
    public void shouldFailOnInvalidYang10Model2() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6878/foo10-invalid-2.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith("XPath function deref() is not valid for yang-version 1."));
        }
    }
}
