/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug6135Test {
    @Test
    public void bug6135Test() {
        final List<GeneratedType> generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/bug6135.yang"));
        assertEquals(5, generateTypes.size());

        GeneratedType genInterface = null;
        for (final GeneratedType type : generateTypes) {
            if (type.getName().equals("TestLeafrefData")) {
                genInterface = type;
                break;
            }
        }
        assertNotNull(genInterface);
        final List<Enumeration> enums = genInterface.getEnumerations();
        assertEquals(2, enums.size());
    }
}