/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug6135Test {
    @Test
    public void bug6135Test() {
        final var generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/bug6135.yang"));
        assertEquals(5, generateTypes.size());

        GeneratedType genInterface = null;
        for (var type : generateTypes) {
            if (type.getName().equals("TestLeafrefData")) {
                genInterface = type;
                break;
            }
        }
        assertNotNull(genInterface);
        final var enums = genInterface.getEnumerations();
        assertEquals(2, enums.size());
    }
}