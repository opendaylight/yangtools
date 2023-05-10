/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1127Test {
    private static final String YT1127_YANG = """
        module foo {
          namespace foo;
          prefix foo;
          grouping grp {
            leaf leaf1 {
              type leafref {
                path "../../foo:foo_cont/foo:name";
              }
            }
          }
          container cont {
            leaf leaf2 {
               type leafref {
                path "../../../foo:foo_cont/foo:name";
              }
            }
          }
        }""";
    private static EffectiveModelContext context;

    @BeforeAll
    static void beforeClass() {
        context = YangParserTestUtils.parseYang(YT1127_YANG);
    }

    @AfterAll
    static void afterClass() {
        context = null;
    }

    @Test
    void testGroupingLeafRef() {
        final var stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(QName.create("foo", "grp"));
        final var leaf1 = assertInstanceOf(LeafSchemaNode.class, stack.enterSchemaTree(QName.create("foo", "leaf1")));
        final var type = assertInstanceOf(LeafrefTypeDefinition.class, leaf1.getType());

        final var ex = assertThrows(IllegalArgumentException.class, () -> stack.resolveLeafref(type));
        assertThat(ex.getMessage(), startsWith("Illegal parent access in YangLocationPath"));
        final var cause = assertInstanceOf(IllegalStateException.class, ex.getCause());
        assertEquals("Unexpected current EmptyGroupingEffectiveStatement{argument=(foo)grp}", cause.getMessage());
    }

    @Test
    void testContainerLeafRef() {
        final var stack = SchemaInferenceStack.ofDataTreePath(context,
            QName.create("foo", "cont"), QName.create("foo", "leaf2"));

        final var leaf2 = assertInstanceOf(LeafSchemaNode.class, stack.currentStatement());
        final var type = assertInstanceOf(LeafrefTypeDefinition.class, leaf2.getType());

        final var ex = assertThrows(IllegalArgumentException.class, () -> stack.resolveLeafref(type));
        assertThat(ex.getMessage(), startsWith("Illegal parent access in YangLocationPath"));
        assertInstanceOf(NoSuchElementException.class, ex.getCause());
    }
}
