/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ByteRangeGeneratorTest {
    @Test
    @Deprecated
    void convertTest() {
        assertTrue(new ByteRangeGenerator().convert(1L).equals(Long.valueOf(1).byteValue()));
    }
}
