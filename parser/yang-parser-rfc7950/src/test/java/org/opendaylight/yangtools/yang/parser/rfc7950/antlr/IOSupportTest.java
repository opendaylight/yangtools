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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.ir.IOSupport;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.repo.api.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToIRTransformer;

public class IOSupportTest {
    private static YangIRSchemaSource FOO;

    @BeforeClass
    public static void beforeClass() throws Exception {
        FOO = TextToIRTransformer.transformText(
            YangTextSchemaSource.forResource(IOSupportTest.class, "/bugs/YT1089/foo.yang"));
    }

    @AfterClass
    public static void afterClass() {
        FOO = null;
    }

    @Test
    public void testSerializedSize() throws IOException {
        final byte[] bytes = serialize(FOO.getRootStatement());
        assertEquals(485, bytes.length);
    }

    @Test
    public void testSerdes() throws IOException {
        final IRStatement orig = FOO.getRootStatement();
        final IRStatement copy = deserialize(serialize(orig));
        assertEquals(orig, copy);
    }

    private static byte[] serialize(final IRStatement stmt) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (DataOutputStream dos = new DataOutputStream(baos)) {
            IOSupport.writeStatement(dos, stmt);
        }

        return baos.toByteArray();
    }

    private static IRStatement deserialize(final byte[] bytes) throws IOException {
        final IRStatement stmt;
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            stmt = IOSupport.readStatement(dis);
            assertEquals(0, dis.available());
        }
        return stmt;
    }
}
