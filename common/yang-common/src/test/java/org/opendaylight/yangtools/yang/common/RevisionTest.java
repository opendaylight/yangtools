/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class RevisionTest {
    @Test
    void testOf() {
        assertEquals("2017-12-25", Revision.of("2017-12-25").toString());
    }

    @Test
    void testOfNull() {
        assertThrows(NullPointerException.class, () -> Revision.of(null));
    }

    @Test
    void testOfEmpty() {
        assertThrowsParse("", "Text '' could not be parsed at index 0");
    }

    @Test
    void testOfInvalid() {
        assertThrowsParse("invalid", "Text 'invalid' could not be parsed at index 0");
    }

    @Test
    void testOfInvalidDate1() {
        assertThrowsParse("2017-13-01",
            "Text '2017-13-01' could not be parsed: Invalid value for MonthOfYear (valid values 1 - 12): 13");
    }

    @Test
    void testOfInvalidDate2() {
        assertThrowsParse("2017-12-00",
            "Text '2017-12-00' could not be parsed: Invalid value for DayOfMonth (valid values 1 - 28/31): 0");
    }

    @Test
    void testOfInvalidDate3() {
        assertThrowsParse("2017-12-32",
            "Text '2017-12-32' could not be parsed: Invalid value for DayOfMonth (valid values 1 - 28/31): 32");
    }

    private static void assertThrowsParse(final String input, final String expectedMessage) {
        final var ex = assertThrows(DateTimeParseException.class, () -> Revision.of(input));
        assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    void testEquals() {
        final var rev1 = Revision.of("2017-12-25");
        final var rev1dup = Revision.of("2017-12-25");
        final var rev2 = Revision.of("2017-12-26");

        assertFalse(rev1.equals(null));
        assertTrue(rev1.equals(rev1));
        assertTrue(rev1.equals(rev1dup));
        assertTrue(rev1dup.equals(rev1));
        assertFalse(rev1.equals(rev2));
        assertFalse(rev2.equals(rev1));
    }

    @Test
    void testOfNullable() {
        assertEquals(Optional.empty(), Revision.ofNullable(null));
        assertEquals(Optional.of(Revision.of("2017-12-25")), Revision.ofNullable("2017-12-25"));
    }

    @Test
    void testCompareOptional() {
        assertEquals(0, Revision.compare(Optional.empty(), Optional.empty()));
        assertEquals(0, Revision.compare(Revision.ofNullable("2017-12-25"), Revision.ofNullable("2017-12-25")));
        assertEquals(-1, Revision.compare(Optional.empty(), Revision.ofNullable("2017-12-25")));
        assertEquals(1, Revision.compare(Revision.ofNullable("2017-12-25"), Optional.empty()));
    }

    @Test
    void testSerializationRevision() throws Exception {
        final var revision = Revision.of("2017-12-25");
        assertEquals(revision, testSerialization(revision, 78));
    }

    @Test
    void testSerializationNotRevision() throws Exception {
        assertSame(NotRevision.of(), testSerialization(NotRevision.of(), 68));
    }

    private static RevisionUnion testSerialization(final RevisionUnion obj, final int expectedLength) throws Exception {
        final var bos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
        }

        final var bytes = bos.toByteArray();
        assertEquals(expectedLength, bytes.length);

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return assertInstanceOf(obj.getClass(), ois.readObject());
        }
    }
}
