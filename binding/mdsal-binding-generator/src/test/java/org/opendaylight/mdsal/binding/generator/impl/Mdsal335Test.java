/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal335Test {
    @Test
    public void mdsal335Test() {
        final List<GeneratedType> generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal335.yang"));
        assertNotNull(generateTypes);
        assertEquals(2, generateTypes.size());

        final GeneratedType gen = generateTypes.stream()
            .filter(type -> type.getFullyQualifiedName()
                .equals("org.opendaylight.yang.gen.v1.mdsal335.norev.Ipv4AddressNoZone"))
            .findFirst().orElseThrow();
        assertEquals(1, gen.getConstantDefinitions().size());
    }
}
