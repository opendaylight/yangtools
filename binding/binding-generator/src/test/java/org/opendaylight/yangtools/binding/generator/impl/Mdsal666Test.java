/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal666Test {
    private static final JavaTypeName FOO = JavaTypeName.create("org.opendaylight.yang.gen.v1.foo.norev", "Foo");
    private static final JavaTypeName FOO_GRP = JavaTypeName.create("org.opendaylight.yang.gen.v1.foo.norev", "Foo$G");
    private static final JavaTypeName BAZ_GRP = JavaTypeName.create("org.opendaylight.yang.gen.v1.foo.norev.bar",
        "Baz$G");

    @Test
    public void rpcPushesGrouping() {
        final var generatedNames = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal666.yang")).stream().map(GeneratedType::getIdentifier)
            .collect(Collectors.toUnmodifiableList());
        assertEquals(10, generatedNames.size());

        // 'rpc foo' ...
        assertTrue(generatedNames.stream().anyMatch(FOO::equals));
        // ... grouping is relocated for 'rpc foo' ...
        assertTrue(generatedNames.stream().anyMatch(FOO_GRP::equals));
        // .. and 'action baz'
        assertTrue(generatedNames.stream().anyMatch(BAZ_GRP::equals));
    }
}
