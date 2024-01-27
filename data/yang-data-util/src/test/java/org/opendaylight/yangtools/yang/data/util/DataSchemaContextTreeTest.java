/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class DataSchemaContextTreeTest {
    private static final QNameModule MODULE = QNameModule.of("dataschemacontext");
    private static final QName FOO = QName.create(MODULE, "foo");
    private static final QName BAR = QName.create(MODULE, "bar");
    private static final QName BAZ = QName.create(MODULE, "baz");
    private static DataSchemaContextTree CONTEXT;

    @BeforeAll
    static void init() {
        CONTEXT = DataSchemaContextTree.from(YangParserTestUtils.parseYang("""
            module dataschemacontext {
              namespace "dataschemacontext";
              prefix dsc;

              container foo {
                choice bar {
                  leaf baz {
                    type string;
                  }
                }
              }
            }"""));
    }

    @AfterAll
    static void cleanup() {
        CONTEXT = null;
    }

    @Test
    void testCorrectInput() {
        assertTrue(CONTEXT.findChild(YangInstanceIdentifier.of(FOO)).isPresent());
        assertTrue(CONTEXT.findChild(YangInstanceIdentifier.of(FOO, BAR)).isPresent());
        assertTrue(CONTEXT.findChild(YangInstanceIdentifier.of(FOO, BAR, BAZ)).isPresent());
    }

    @Test
    void testSimpleBad() {
        assertEquals(Optional.empty(), CONTEXT.findChild(YangInstanceIdentifier.of(BAR)));
    }

    @Test
    void testNestedBad() {
        assertEquals(Optional.empty(), CONTEXT.findChild(YangInstanceIdentifier.of(BAR, BAZ)));
    }
}
