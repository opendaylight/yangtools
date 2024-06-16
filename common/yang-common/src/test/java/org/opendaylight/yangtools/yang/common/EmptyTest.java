/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class EmptyTest {
    @Test
    void testInstanceNotNull() {
        assertNotNull(Empty.value());
    }

    @Test
    void testToString() {
        assertEquals("empty", Empty.value().toString());
    }

    @Test
    void testSerialization() throws Exception {
        final var bos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(Empty.value());
        }

        final var bytes = bos.toByteArray();
        assertEquals("""
            aced00057372002a6f72672e6f70656e6461796c696768742e79616e67746f6f6c732e79616e672e636f6d6d6f6e2e4576310000000\
            0000000000200007870""", HexFormat.of().formatHex(bytes));

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertSame(Empty.value(), ois.readObject());
        }
    }
}
