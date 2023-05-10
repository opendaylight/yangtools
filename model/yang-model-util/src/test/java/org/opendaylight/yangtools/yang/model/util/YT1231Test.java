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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1231Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");
    private static final QName BAZ = QName.create("foo", "baz");
    private static final QName XYZZY = QName.create("foo", "xyzzy");

    private static EffectiveModelContext CONTEXT;

    private final SchemaInferenceStack stack = SchemaInferenceStack.of(CONTEXT);

    @BeforeAll
    static void beforeClass() {
        CONTEXT = YangParserTestUtils.parseYang("""
            module foo {
              namespace foo;
              prefix foo;
              container foo {
                container foo;
                choice baz {
                  description desc;
                  case baz {
                    description desc;
                    choice bar {
                      description desc;
                      case bar {
                        status deprecated;
                        container bar;
                      }
                    }
                  }
                }
                choice bar {
                  reference ref;
                  case bar {
                    reference ref;
                    choice baz {
                      reference ref;
                      case baz {
                        reference ref;
                        container xyzzy;
                      }
                    }
                  }
                }
              }
            }""");
    }

    @Test
    void testEnterDataTree() {
        // Trivial
        assertInstanceOf(ContainerEffectiveStatement.class, stack.enterDataTree(FOO));
        assertSame(CONTEXT.getModuleStatement(FOO.getModule()), stack.currentModule());
        assertEquals(Absolute.of(FOO), stack.toSchemaNodeIdentifier());
        assertInstanceOf(ContainerEffectiveStatement.class, stack.enterDataTree(FOO));
        assertEquals(Absolute.of(FOO, FOO), stack.toSchemaNodeIdentifier());
        stack.exit();

        // Has to cross four layers of choice/case
        assertInstanceOf(ContainerEffectiveStatement.class, stack.enterDataTree(XYZZY));
        assertEquals(Absolute.of(FOO, BAR, BAR, BAZ, BAZ, XYZZY), stack.toSchemaNodeIdentifier());

        stack.exit();
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.enterSchemaTree(BAR));
        assertInstanceOf(CaseEffectiveStatement.class, stack.enterSchemaTree(BAR));
        assertEquals(Absolute.of(FOO, BAR, BAR), stack.toSchemaNodeIdentifier());
    }

    @Test
    void testEnterChoice() {
        // Simple
        assertInstanceOf(ContainerEffectiveStatement.class, stack.enterDataTree(FOO));
        assertEquals(Absolute.of(FOO), stack.toSchemaNodeIdentifier());
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.enterChoice(BAR));
        assertEquals(Absolute.of(FOO, BAR), stack.toSchemaNodeIdentifier());

        // Has to cross choice -> case -> choice
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.enterChoice(BAZ));
        assertEquals(Absolute.of(FOO, BAR, BAR, BAZ), stack.toSchemaNodeIdentifier());

        // Now the same with just case -> choice
        stack.exit();
        assertInstanceOf(CaseEffectiveStatement.class, stack.enterSchemaTree(BAR));
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.enterChoice(BAZ));
        assertEquals(Absolute.of(FOO, BAR, BAR, BAZ), stack.toSchemaNodeIdentifier());
    }

    @Test
    void testEnterChoiceToRootContainer() {
        assertEquals("Choice (foo)foo not present", assertEnterChoiceThrows(FOO));
    }

    @Test
    void testEnterChoiceToNestedContainer() {
        assertInstanceOf(ContainerEffectiveStatement.class, stack.enterDataTree(FOO));
        assertEquals(Absolute.of(FOO), stack.toSchemaNodeIdentifier());
        assertEquals("Choice (foo)foo not present in schema parent (foo)foo", assertEnterChoiceThrows(FOO));
    }

    @Test
    void testEnterChoiceNonExistent() {
        assertInstanceOf(ContainerEffectiveStatement.class, stack.enterDataTree(FOO));
        assertEquals(Absolute.of(FOO), stack.toSchemaNodeIdentifier());
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.enterSchemaTree(BAR));

        assertEquals("Choice (foo)foo not present in schema parent (foo)bar", assertEnterChoiceThrows(FOO));
        assertEquals("Choice (foo)bar not present in schema parent (foo)bar", assertEnterChoiceThrows(BAR));
    }

    private String assertEnterChoiceThrows(final QName nodeIdentifier) {
        return assertThrows(IllegalArgumentException.class, () -> stack.enterChoice(nodeIdentifier)).getMessage();
    }
}
