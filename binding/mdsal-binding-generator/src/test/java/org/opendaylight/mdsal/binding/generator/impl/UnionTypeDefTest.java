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
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class UnionTypeDefTest {
    @Test
    public void unionTypeResolvingTest() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            UnionTypeDefTest.class, "/union-test-models/abstract-topology.yang", "/ietf-models/ietf-inet-types.yang"));

        assertNotNull("genTypes is null", genTypes);
        assertEquals(34, genTypes.size());

        // TODO: implement test
    }

    @Test
    public void unionTypedefLeafrefTest() {
        final List<GeneratedType> generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/bug8449.yang"));
        assertNotNull(generateTypes);
        assertEquals(6, generateTypes.size());

        final GeneratedType cont = generateTypes.stream()
            .filter(type -> type.getName().equals("Cont"))
            .findFirst()
            .orElseThrow();

        final List<GeneratedType> enclosedTypes = cont.getEnclosedTypes();
        assertEquals(1, enclosedTypes.size());

        final GeneratedType refType = enclosedTypes.get(0);
        final List<GeneratedProperty> properties = refType.getProperties();
        assertEquals(1, properties.size());

        final GeneratedProperty property = properties.get(0);
        assertEquals("stringRefValue", property.getName());
        assertEquals(Types.STRING, property.getReturnType());
    }
}
