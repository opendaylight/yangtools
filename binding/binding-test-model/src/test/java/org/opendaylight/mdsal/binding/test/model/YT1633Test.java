/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal483.norev.Foo;

class YT1633Test {
    @Test
    void identityObjectIsSingleton() throws Exception {
        final var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(Foo.VALUE);
        }

        final var bytes = baos.toByteArray();
        assertEquals("""
            aced0005737200476f72672e6f70656e6461796c696768742e79616e672e67656e2e76312e75726e2e746573742e6f70656e6461796\
            c696768742e6d6473616c3438332e6e6f7265762e466f6f243100000000000000010200007870""",
            HexFormat.of().formatHex(bytes));

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertSame(Foo.VALUE, ois.readObject());
        }
    }
}
