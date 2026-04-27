/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.generator.impl.DefaultBindingGenerator;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class KeyGeneratorTest {
    @Test
    void compositeKeyClassTest() {
        final var genTypes = new DefaultBindingGenerator().generateTypes(
            YangParserTestUtils.parseYangResource("/list-composite-key.yang"));

        assertNotNull(genTypes);
        assertEquals(7, genTypes.size());

        int genTypesCount = 0;
        int keyArchetypeCount = 0;
        for (var type : genTypes) {
            if (type instanceof KeyArchetype archetype) {
                keyArchetypeCount++;

                switch (archetype.simpleName()) {
                    case "CompositeKeyListKey" -> {
                        final var properties = archetype.getProperties();
                        int propertyCount = 0;
                        for (var prop : properties) {
                            if (prop.getName().equals("key1") || prop.getName().equals("key2")) {
                                propertyCount++;
                            }
                        }

                        assertThat(new KeyGenerator(archetype).generate())
                            .contains("public CompositeKeyListKey(@NonNull Byte _key1, @NonNull String _key2)");

                        assertEquals(2, propertyCount);
                    }
                    case "InnerListKey" -> {
                        final var properties = archetype.getProperties();
                        assertEquals(1, properties.size());
                    }
                    default -> fail("Unexpected key " + archetype);
                }
            } else {
                genTypesCount++;
            }
        }

        assertEquals(5, genTypesCount);
        assertEquals(2, keyArchetypeCount);
    }
}
