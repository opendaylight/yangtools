/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal332Test {
    @Test
    public void mdsal332Test() {
        final var generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal332.yang"));
        assertNotNull(generateTypes);
        assertEquals(5, generateTypes.size());

        final var names = generateTypes.stream().map(GeneratedType::getIdentifier)
                .collect(ImmutableList.toImmutableList());
        final var uniqueNames = ImmutableSet.copyOf(names);
        assertEquals(ImmutableList.copyOf(uniqueNames), names);
    }
}
