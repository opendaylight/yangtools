/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

@Deprecated
class IntegerRangeGeneratorTest {
    @Test
    void convertTest() {
        assertEquals(Long.valueOf(1).intValue(), new IntegerRangeGenerator().convert(1L));
    }
}
