/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal573Test {
    @Test
    void mdsal573Test() {
        final var generateTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYang("""
            module mdsal573 {
            namespace "mdsal573";
            prefix l;

            grouping g1 {
              container c1 {
                leaf l1 {
                  type string;
                }
              }
            }

            grouping g2 {
              uses g3;

              container c2 {
                leaf l2 {
                  type string;
                }
              }
            }

            grouping g3 {
              container c3 {
                leaf l3 {
                  type string;
                }
              }
            }

            uses g1;
            uses g2;
            }"""));
        assertNotNull(generateTypes);
        assertEquals(7, generateTypes.size());

        final var root = assertInstanceOf(DataRootArchetype.class, generateTypes.getFirst());
        assertEquals(JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal573.norev", "Mdsal573Data"),
            root.name());
        assertEquals(List.of(), root.getMethodDefinitions());
        assertThat(root.getImplements()).hasSize(3);
    }
}
