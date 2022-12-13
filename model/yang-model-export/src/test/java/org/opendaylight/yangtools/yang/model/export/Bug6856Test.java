/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug6856Test {
    @Test
    public void testImplicitInputAndOutputInRpc() throws Exception {
        final var schemaContext = YangParserTestUtils.parseYangResources(Bug6856Test.class, "/bugs/bug-6856/foo.yang");
        assertNotNull(schemaContext);

        final var byteArrayOutputStream = new ByteArrayOutputStream();
        final var bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

        final var fooModule = schemaContext.findModule("foo", Revision.of("2017-02-28")).orElseThrow();
        YinExportUtils.writeModuleAsYinText(fooModule.asEffectiveStatement(), bufferedOutputStream);

        final String output = byteArrayOutputStream.toString();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        assertFalse(output.contains("<input>"));
        assertFalse(output.contains("<output>"));
    }

    @Test
    public void testExplicitInputAndOutputInRpc() throws Exception {
        final var schemaContext = YangParserTestUtils.parseYangResources(Bug6856Test.class, "/bugs/bug-6856/bar.yang");
        assertNotNull(schemaContext);

        final var byteArrayOutputStream = new ByteArrayOutputStream();
        final var bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

        final var barModule = schemaContext.findModule("bar", Revision.of("2017-02-28")).get();
        YinExportUtils.writeModuleAsYinText(barModule.asEffectiveStatement(), bufferedOutputStream);

        final String output = byteArrayOutputStream.toString();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        assertTrue(output.contains("<input>"));
        assertTrue(output.contains("<output>"));
    }
}
