/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class AppendIterableTest {
    @Test
    void lastItemIsLast() {
        assertIterableEquals(List.of("one", "two", "three"), new AppendIterable<>(List.of("one", "two"), "three"));
    }

    @Test
    void toStringIsNice() {
        assertEquals("[one, , three]", new AppendIterable<>(List.of("one", ""), "three").toString());
    }
}
