/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.export.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.export.YinExportUtils;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug6856Test {

    @Test
    public void testImplicitInputAndOutputInRpc() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(Bug6856Test.class,
            "/bugs/bug6856/foo.yang");
        assertNotNull(schemaContext);

        final OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-02-28");

        final Module fooModule = schemaContext.findModuleByName("foo", revision);
        YinExportUtils.writeModuleToOutputStream(schemaContext, fooModule, bufferedOutputStream);

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

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-02-28");

        final Module barModule = schemaContext.findModuleByName("bar", revision);
        YinExportUtils.writeModuleToOutputStream(schemaContext, barModule, bufferedOutputStream);

        final String output = byteArrayOutputStream.toString();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        assertTrue(output.contains("<input>"));
        assertTrue(output.contains("<output>"));
    }
}
