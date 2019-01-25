/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.export;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug6856Test {

    @Test
    public void testImplicitInputAndOutputInRpc() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(Bug6856Test.class,
            "/bugs/bug6856/foo.yang");
        assertNotNull(schemaContext);

        final OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

        final Module fooModule = schemaContext.findModule("foo", Revision.of("2017-02-28")).get();
        YinExportUtils.writeModuleAsYinText(fooModule, bufferedOutputStream);

        final String output = byteArrayOutputStream.toString();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        assertFalse(output.contains("<input>"));
        assertFalse(output.contains("<output>"));
    }

    @Test
    public void testExplicitInputAndOutputInRpc() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(Bug6856Test.class,
            "/bugs/bug6856/bar.yang");
        assertNotNull(schemaContext);

        final OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

        final Module barModule = schemaContext.findModule("bar", Revision.of("2017-02-28")).get();
        YinExportUtils.writeModuleAsYinText(barModule, bufferedOutputStream);

        final String output = byteArrayOutputStream.toString();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        assertTrue(output.contains("<input>"));
        assertTrue(output.contains("<output>"));
    }
}
