/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableBiMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class BiMapYangNamespaceContextTest {
    private static final QNameModule FOO = QNameModule.of("foo");
    private static final QNameModule BAR = QNameModule.of("bar");
    private static final QNameModule BAZ = QNameModule.of("baz");

    private final BiMapYangNamespaceContext context = new BiMapYangNamespaceContext(
        ImmutableBiMap.of("foo", FOO, "bar", BAR));

    @Test
    public void testEquals() {
        assertTrue(context.equals(context));
        assertTrue(context.equals(new BiMapYangNamespaceContext(ImmutableBiMap.of("foo", FOO, "bar", BAR))));
        assertFalse(context.equals(null));
        assertFalse(context.equals(new BiMapYangNamespaceContext(ImmutableBiMap.of("foo", FOO))));
        assertFalse(context.equals(new BiMapYangNamespaceContext(ImmutableBiMap.of("bar", BAR))));
    }

    @Test
    public void testPrefixForNamespace() {
        assertEquals(Optional.of("foo"), context.findPrefixForNamespace(FOO));
        assertEquals(Optional.of("bar"), context.findPrefixForNamespace(BAR));
        assertEquals(Optional.empty(), context.findPrefixForNamespace(BAZ));
    }

    @Test
    public void testNamespaceForPrefix() {
        assertEquals(Optional.of(FOO), context.findNamespaceForPrefix("foo"));
        assertEquals(Optional.of(BAR), context.findNamespaceForPrefix("bar"));
        assertEquals(Optional.empty(), context.findNamespaceForPrefix("baz"));
    }

    @Test
    public void testReadWrite() throws IOException {
        final byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (DataOutputStream dos = new DataOutputStream(bos)) {
                context.writeTo(dos);
            }
            bytes = bos.toByteArray();
        }

        final BiMapYangNamespaceContext other;
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            other = BiMapYangNamespaceContext.readFrom(dis);
        }

        assertEquals(context, other);
    }

    @Test
    public void testCreateQName() {
        assertEquals(QName.create(FOO, "some"), context.createQName("foo", "some"));
    }
}
