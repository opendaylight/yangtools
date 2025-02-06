/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal829Test {
    private static final EffectiveModelContext MODEL_CONTEXT =
        YangParserTestUtils.parseYangResource("/mdsal829.yang", Set.of());

    @Test
    void testCompileTimeTypes() {
        assertEquals(1, DefaultBindingGenerator.generateFor(MODEL_CONTEXT).size());
    }

    @Test
    void testRunTimeTypes() {
        final var types = BindingRuntimeTypesFactory.createTypes(MODEL_CONTEXT);
        assertSame(MODEL_CONTEXT, types.modelContext());
        final var schema = types.findSchema(
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal829.norev", "Mdsal829Data")).orElseThrow();
        assertNotNull(schema);
    }
}
