/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal554Test {
    @Test
    void builderTemplateGenerateListenerMethodsTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal554.yang"));
        assertEquals(4, genTypes.size());

        // status deprecated
        final var deprecated = (LegacyArchetype<?>) genTypes.get(1);
        assertEquals("DeprecatedNotification", deprecated.simpleName());
        assertEquals(List.of(), deprecated.annotations());

        // status obsolete
        final var obsolete = (LegacyArchetype<?>) genTypes.get(2);
        assertEquals("ObsoleteNotification", obsolete.simpleName());
        assertEquals(List.of(), obsolete.annotations());

        // status current
        final var current = assertInstanceOf(LegacyArchetype.class, genTypes.get(3));
        assertEquals("TestNotification", current.simpleName());
        assertEquals(List.of(), current.annotations());
    }
}
