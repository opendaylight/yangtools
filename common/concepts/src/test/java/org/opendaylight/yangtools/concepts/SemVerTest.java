/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class SemVerTest {
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase().withDelimiter(":");
    private static final String SERIALIZED = """
        AC:ED:00:05:73:72:00:2A:6F:72:67:2E:6F:70:65:6E:64:61:79:6C:69:67:68:74:2E:79:61:6E:67:74:\
        6F:6F:6C:73:2E:63:6F:6E:63:65:70:74:73:2E:53:65:6D:56:65:72:00:00:00:00:00:00:00:01:02:00:\
        03:49:00:05:6D:61:6A:6F:72:49:00:05:6D:69:6E:6F:72:49:00:05:70:61:74:63:68:78:70:00:00:00:\
        01:00:00:00:02:00:00:00:03""";

    @Test
    @SuppressWarnings("SelfComparison")
    void testSemVer() {
        final var semVer = new SemVer(5);
        assertNotNull(semVer);

        assertEquals(5, semVer.major());
        assertEquals(0, semVer.minor());
        assertEquals(0, semVer.patch());

        final var semVer2 = SemVer.valueOf("1.2.3");
        assertNotNull(semVer2);

        assertEquals(1, semVer2.major());
        assertEquals(2, semVer2.minor());
        assertEquals(3, semVer2.patch());

        final var semVer3 = SemVer.valueOf("1");
        assertNotNull(semVer3);

        assertEquals(1, semVer3.major());
        assertEquals(0, semVer3.minor());
        assertEquals(0, semVer3.patch());

        final var semVer4 = SemVer.valueOf("1.2");
        assertNotNull(semVer4);

        assertEquals(1, semVer4.major());
        assertEquals(2, semVer4.minor());
        assertEquals(0, semVer4.patch());

        assertEquals(1, semVer2.compareTo(semVer3));
        assertEquals(-1, semVer3.compareTo(semVer2));
        assertEquals(0, semVer2.compareTo(semVer2));

        assertTrue(semVer2.equals(semVer2));
        assertFalse(semVer2.equals("not equal"));
        assertFalse(semVer2.equals(semVer3));

        assertEquals(semVer2.hashCode(), semVer2.hashCode());

        assertEquals("1.0.0", semVer3.toString());
    }

    @Test
    void testSerialize() throws Exception {
        final byte[] bytes;
        try (var bos = new ByteArrayOutputStream()) {
            try (var oos = new ObjectOutputStream(bos)) {
                oos.writeObject(new SemVer(1, 2, 3));
            }
            bytes = bos.toByteArray();
        }

        assertEquals(SERIALIZED, HEX_FORMAT.formatHex(bytes));
    }

    @Test
    void testDeserialize() throws Exception {
        final Object value;
        try (var oos = new ObjectInputStream(new ByteArrayInputStream(HEX_FORMAT.parseHex(SERIALIZED)))) {
            value = oos.readObject();
        }
        assertEquals(new SemVer(1, 2, 3), value);
    }
}
