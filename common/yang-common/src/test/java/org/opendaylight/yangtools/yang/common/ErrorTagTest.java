/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;

class ErrorTagTest {
    @Test
    void testSerialization() throws Exception {
        final var expected = new ErrorTag("test");

        final byte[] bytes;
        try (var bos = new ByteArrayOutputStream()) {
            try (var oos = new ObjectOutputStream(bos)) {
                oos.writeObject(expected);
            }
            bytes = bos.toByteArray();
        }

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertEquals(expected, ois.readObject());
        }
    }
}
