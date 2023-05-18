/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1283Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");

    private static EffectiveModelContext context;

    private final SchemaInferenceStack stack = SchemaInferenceStack.of(context);

    @BeforeAll
    static void beforeClass() {
        context = YangParserTestUtils.parseYang("""
            module foo {
              namespace foo;
              prefix foo;
              container foo {
                choice foo {
                  case foo {
                    leaf foo {
                      type leafref {
                        path ../../bar;
                      }
                    }
                  }
                }
              }
              leaf bar {
                type string;
              }
            }""");
    }

    @Test
    void testResolveUnderCaseViaDataTree() {
        assertInstanceOf(ContainerEffectiveStatement.class, stack.enterDataTree(FOO));
        assertResolve(stack.enterDataTree(FOO));
    }

    @Test
    void testResolveUnderCaseViaSchemaTree() {
        assertInstanceOf(ContainerEffectiveStatement.class, stack.enterSchemaTree(FOO));
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.enterSchemaTree(FOO));
        assertInstanceOf(CaseEffectiveStatement.class, stack.enterSchemaTree(FOO));
        assertResolve(stack.enterSchemaTree(FOO));
    }

    private void assertResolve(final EffectiveStatement<?, ?> foo) {
        assertInstanceOf(LeafEffectiveStatement.class, foo);

        final TypeEffectiveStatement<?> type = foo.findFirstEffectiveSubstatement(TypeEffectiveStatement.class)
            .orElseThrow();
        final var bar = assertInstanceOf(LeafEffectiveStatement.class,
            stack.resolvePathExpression(
                type.findFirstEffectiveSubstatementArgument(PathEffectiveStatement.class).orElseThrow()));
        assertEquals(BAR, bar.argument());
    }
}
