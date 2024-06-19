/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.mdsal661.norev.Foo;

public class Mdsal661Test {
    @Test
    public void testLengthEnforcerReject() {
        assertThrows(IllegalArgumentException.class, () -> new Foo(""));
        assertThrows(IllegalArgumentException.class, () -> new Foo("ab"));
    }

    @Test
    public void testLengthEnforcerAccept() {
        assertNotNull(new Foo("a"));
        // U+1F31E, encodes to UTF-16 as "\uD83C\uDF1E", i.e. two code units
        assertNotNull(new Foo("🌞"));
    }
}
