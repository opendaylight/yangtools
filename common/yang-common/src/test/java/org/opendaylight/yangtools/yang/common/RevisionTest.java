/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class RevisionTest {
    @Test
    public void testOf() {
        assertEquals("2017-12-25", Revision.of("2017-12-25").toString());
    }

    @Test
    public void testOfNull() {
        assertThrows(NullPointerException.class, () -> Revision.of(null));
    }

    @Test
    public void testOfEmpty() {
        assertThrows(DateTimeParseException.class, () -> Revision.of(""));
    }

    @Test
    public void testOfInvalid() {
        assertThrows(DateTimeParseException.class, () -> Revision.of("invalid"));
    }

    @Test
    public void testOfInvalidDate1() {
        assertThrows(DateTimeParseException.class, () -> Revision.of("2017-13-01"));
    }

    @Test
    public void testOfInvalidDate2() {
        assertThrows(DateTimeParseException.class, () -> Revision.of("2017-12-00"));
    }

    @Test
    public void testOfInvalidDate3() {
        assertThrows(DateTimeParseException.class, () -> Revision.of("2017-12-32"));
    }

    @Test
    public void testEquals() {
        final Revision rev1 = Revision.of("2017-12-25");
        final Revision rev1dup = Revision.of("2017-12-25");
        final Revision rev2 = Revision.of("2017-12-26");

        assertFalse(rev1.equals(null));
        assertTrue(rev1.equals(rev1));
        assertTrue(rev1.equals(rev1dup));
        assertTrue(rev1dup.equals(rev1));
        assertFalse(rev1.equals(rev2));
        assertFalse(rev2.equals(rev1));
    }

    @Test
    public void testOfNullable() {
        assertEquals(Optional.empty(), Revision.ofNullable(null));

        final Optional<Revision> opt = Revision.ofNullable("2017-12-25");
        assertTrue(opt.isPresent());
        assertEquals("2017-12-25", opt.orElseThrow().toString());
    }

    @Test
    public void testCompareOptional() {
        assertEquals(0, Revision.compare(Optional.empty(), Optional.empty()));
        assertEquals(0, Revision.compare(Revision.ofNullable("2017-12-25"), Revision.ofNullable("2017-12-25")));
        assertEquals(-1, Revision.compare(Optional.empty(), Revision.ofNullable("2017-12-25")));
        assertEquals(1, Revision.compare(Revision.ofNullable("2017-12-25"), Optional.empty()));
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        final var source = Revision.of("2017-12-25");
        final var bos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(source);
        }

        final var bytes = bos.toByteArray();
        assertEquals(81, bytes.length);

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertEquals(source, ois.readObject());
        }
    }
}
