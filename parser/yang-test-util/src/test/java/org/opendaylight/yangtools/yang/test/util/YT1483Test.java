/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.test.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;

public class YT1483Test {
    private static final String TEST_YANG = """
        module root-conflict { // results in RootConflictData
              namespace root-conflict;
              prefix rc;

              container root-conflict-data; // results in RootConflictData as well
            }""";
    private static final String INCORRECT_YANG1 = """
        /*a comment*/ module /*another comment*/ fail {
            namespace fail-namespace;
            prefix ff;
        }""";
    private static final String INCORRECT_YANG2 = """
        module /*a comment*/ fail {
            namespace fail-namespace;
            prefix ff;
        }""";
    private static final String INCORRECT_YANG3 = """
        /*comment*/ module fail {
            namespace fail-namespace;
            prefix ff;
        }""";

    @Test
    public void testParseYang() {
        final var fromLiteral = YangParserTestUtils.parseYang(TEST_YANG);
        final var fromResources = YangParserTestUtils.parseYangResourceDirectory("/");
        final var qname = QName.create("root-conflict", "root-conflict");
        assertTrue(fromResources.findModule(qname.getModule()).isPresent());
        assertTrue(fromLiteral.findModule(qname.getModule()).isPresent());
    }

    @Test
    public void testIncorrectYang() {
        assertThrows(IllegalArgumentException.class, () -> YangParserTestUtils.parseYang(INCORRECT_YANG1));
        assertThrows(IllegalArgumentException.class, () -> YangParserTestUtils.parseYang(INCORRECT_YANG2));
        assertThrows(IllegalArgumentException.class, () -> YangParserTestUtils.parseYang(INCORRECT_YANG3));
    }
}
