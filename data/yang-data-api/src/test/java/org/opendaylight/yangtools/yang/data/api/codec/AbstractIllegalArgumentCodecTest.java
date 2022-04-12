/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.codec;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AbstractIllegalArgumentCodecTest {
    private static final class TestCodec extends AbstractIllegalArgumentCodec<String, String> {
        @Override
        protected String deserializeImpl(final String product) {
            throw new AssertionError("Should never be invoked");
        }

        @Override
        protected String serializeImpl(final String input) {
            throw new AssertionError("Should never be invoked");
        }
    }

    private final TestCodec codec = new TestCodec();

    @Test
    public void testNullDeserialize() {
        assertThrows(NullPointerException.class, () -> codec.deserialize(null));
    }

    @Test
    public void testNullSerialize() {
        assertThrows(NullPointerException.class, () -> codec.serialize(null));
    }
}
