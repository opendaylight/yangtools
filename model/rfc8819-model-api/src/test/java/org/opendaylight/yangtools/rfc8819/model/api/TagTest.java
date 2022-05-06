/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.Test;

public class TagTest {

    @Test
    public void testIfIsIetfTag() {
        Stream.of("ietf:first-tag", "ietf:second:tag:tag")
                .map(Tag::valueOf).forEach(tag -> assertEquals(TagPrefix.IETF, tag.getPrefix()));
    }

    @Test
    public void testIfIsVendorTag() {
        Stream.of("vendor:first-tag", "vendor:second:tag:tag")
                .map(Tag::valueOf).forEach(tag -> assertEquals(TagPrefix.VENDOR, tag.getPrefix()));
    }

    @Test
    public void testIfIsUserTag() {
        Stream.of("user:first-tag", "user:second:tag:tag")
                .map(Tag::valueOf).forEach(tag -> assertEquals(TagPrefix.USER, tag.getPrefix()));
    }

    @Test
    public void testIfIsTagInvalid() {
        Stream.of("", "\n", "\t", "ietf:tag\ntag")
                .forEach(tag -> assertThrows(IllegalArgumentException.class, () -> Tag.valueOf(tag)));
    }

}
