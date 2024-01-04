/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.ir.IOSupport;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

class IOSupportTest {
    private static YangIRSource FOO;

    @BeforeAll
    static void beforeClass() throws Exception {
        FOO = TextToIRTransformer.transformText(TestUtils.assertSchemaSource("/bugs/YT1089/foo.yang"));
    }

    @Test
    void testSerializedSize() throws IOException {
        final byte[] bytes = serialize(FOO.rootStatement());
        assertEquals(485, bytes.length);
    }

    @Test
    void testSerdes() throws IOException {
        final var orig = FOO.rootStatement();
        assertEquals(orig, deserialize(serialize(orig)));
    }

    private static byte[] serialize(final IRStatement stmt) throws IOException {
        final var baos = new ByteArrayOutputStream();

        try (var dos = new DataOutputStream(baos)) {
            IOSupport.writeStatement(dos, stmt);
        }

        return baos.toByteArray();
    }

    private static IRStatement deserialize(final byte[] bytes) throws IOException {
        try (var dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            final var stmt = IOSupport.readStatement(dis);
            assertEquals(0, dis.available());
            return stmt;
        }
    }
}
